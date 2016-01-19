//文件名: sip/sip.c
//
//描述: 这个文件实现SIP进程  
//
//创建日期: 2015年

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <strings.h>
#include <arpa/inet.h>
#include <signal.h>
#include <netdb.h>
#include <assert.h>
#include <sys/utsname.h>
#include <pthread.h>
#include <unistd.h>

#include "../common/constants.h"
#include "../common/pkt.h"
#include "../common/seg.h"
#include "../topology/topology.h"
#include "sip.h"
#include "nbrcosttable.h"
#include "dvtable.h"
#include "routingtable.h"

//SIP层等待这段时间让SIP路由协议建立路由路径. 
#define SIP_WAITTIME 60

/**************************************************************/
//声明全局变量
/**************************************************************/
int son_conn; 			//到重叠网络的连接
int stcp_conn;			//到STCP的连接
nbr_cost_entry_t* nct;			//邻居代价表
dv_t* dv;				//距离矢量表
pthread_mutex_t* dv_mutex;		//距离矢量表互斥量
routingtable_t* routingtable;		//路由表
pthread_mutex_t* routingtable_mutex;	//路由表互斥量
pthread_t monitorroute_thread;			//路由监测线程

//强制进行按字节拷贝memcpy会导致一系列段错误或字节乱序
void strncopy(char *dest, char *src, int n)
{
    int i;
    for(i = 0; i < n; i ++)
    {
        dest[i] = src[i];
    }
}
/**************************************************************/
//实现SIP的函数
/**************************************************************/

//SIP进程使用这个函数连接到本地SON进程的端口SON_PORT.
//成功时返回连接描述符, 否则返回-1.
int connectToSON() { 
	int sockfd;
	struct sockaddr_in serv_addr;
	memset(&serv_addr, 0, sizeof(struct sockaddr_in));
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr = topology_getfromIDtoIP(topology_getMyNodeID());
	serv_addr.sin_port=htons(SON_PORT);
	if((sockfd = socket(AF_INET, SOCK_STREAM, 0))<0) {
		perror("Error create socket\n");
		return -1;
	}
	if(connect(sockfd, (struct sockaddr*)&serv_addr, sizeof(struct sockaddr))<0) {
		perror("Error connecting\n");
		return -1;
	}
	printf("[SIP]connect ok!\n");
	return sockfd;
}

//这个线程每隔ROUTEUPDATE_INTERVAL时间发送路由更新报文.路由更新报文包含这个节点
//的距离矢量.广播是通过设置SIP报文头中的dest_nodeID为BROADCAST_NODEID,并通过son_sendpkt()发送报文来完成的.
void* routeupdate_daemon(void* arg) {
	sip_pkt_t *update_sip;
	while(1) {
		sleep(ROUTEUPDATE_INTERVAL);
		update_sip = (sip_pkt_t *)malloc(sizeof(sip_pkt_t));
		update_sip->header.src_nodeID = topology_getMyNodeID();
		update_sip->header.dest_nodeID = BROADCAST_NODEID;
		update_sip->header.type = ROUTE_UPDATE;
		pkt_routeupdate_t pkt_route;
		//here is not 0!
		pkt_route.entryNum = topology_getNodeNum();
		memset(pkt_route.entry, 0, sizeof(routeupdate_entry_t)*pkt_route.entryNum);
		int *node = topology_getNodeArray();
		int i;
		//互斥访问
		pthread_mutex_lock(dv_mutex);
		for(i = 0; i< pkt_route.entryNum; i++) {
			pkt_route.entry[i].nodeID = node[i];
			pkt_route.entry[i].cost = dvtable_getcost(dv, topology_getMyNodeID(), node[i]);
		}
		pthread_mutex_unlock(dv_mutex);
		update_sip->header.length = sizeof(int) + sizeof(routeupdate_entry_t)*pkt_route.entryNum;
		strncopy(update_sip->data, (char *)&pkt_route, update_sip->header.length);
		printf("[SIP] update daemon\n");
		//printf("!!son_conn = %d\n", son_conn);
		if(son_conn >0) {
			son_sendpkt(BROADCAST_NODEID, update_sip, son_conn);
		}
		free(update_sip);
	}
	return 0;
}

//这个函数用于对路由表信息进行更新的操作，是一个子函数供调用
void update_routingtable() {
	int srcNode = topology_getMyNodeID();
    int nodeNum = topology_getNodeNum();
    int *nodeArray = topology_getNodeArray();
    int i, j;
    for(i = 0; i < nodeNum; i++){
        if(nodeArray[i] != srcNode){
            int dstNode = nodeArray[i];
            int minCost, nextNode;
            if(dvtable_getOnlineFlag(dv, srcNode, dstNode) == 0){
                minCost = INFINITE_COST;
                nextNode = -1;
            }
            else{
                minCost = topology_getCost(srcNode, dstNode);
                if(minCost == INFINITE_COST){
                    nextNode = -1;
                }
                else{
                    nextNode = dstNode;
                }
                for(j = 0; j < nodeNum; j ++){
                    int midNode = nodeArray[j];
                    if(midNode != srcNode){
                        int newCost = dvtable_getcost(dv, srcNode, midNode) + dvtable_getcost(dv, midNode, dstNode);
                        if(newCost < minCost){
                            minCost = newCost;
                            nextNode = midNode;
                        }
                    }
                }
            }
            dvtable_setcost(dv, srcNode, dstNode, minCost);
            routingtable_setnextnode(routingtable, dstNode, nextNode);
        }
    }
}

//这个线程处理来自SON进程的进入报文. 它通过调用son_recvpkt()接收来自SON进程的报文.
//如果报文是SIP报文,并且目的节点就是本节点,就转发报文给STCP进程. 如果目的节点不是本节点,
//就根据路由表转发报文给下一跳.如果报文是路由更新报文,就更新距离矢量表和路由表.
void* pkthandler(void* arg) {
	sip_pkt_t pkt;
	while(son_recvpkt(&pkt,son_conn)>0) {
		printf("[pkthandler] Routing info from neighbor %d\n",pkt.header.src_nodeID);
        if(pkt.header.type == SIP && pkt.header.src_nodeID != -1) {
            if(pkt.header.dest_nodeID == topology_getMyNodeID()){
                printf("Receive a STCP packet to myself\n");
                forwardsegToSTCP(stcp_conn, pkt.header.src_nodeID, (seg_t*)pkt.data);
            }
            else{
				pthread_mutex_lock(routingtable_mutex);
                int next = routingtable_getnextnode(routingtable, pkt.header.dest_nodeID);
				pthread_mutex_unlock(routingtable_mutex);
                printf("Forward to neighbor:%d\n", next);
                if(next == -1){
                    perror("can not find routing info"); 
                }
                else{
                    son_sendpkt(next, &pkt, son_conn);
                }
            }
        }
        else if(pkt.header.type == ROUTE_UPDATE){
            int myNode = topology_getMyNodeID();
            int srcNode = pkt.header.src_nodeID;
            pkt_routeupdate_t *rupkt = (pkt_routeupdate_t*)pkt.data;
            pthread_mutex_lock(dv_mutex);
            pthread_mutex_lock(routingtable_mutex);
			int onlineflag = dvtable_getOnlineFlag(dv, myNode, srcNode);
            if(onlineflag == 1){
                int i = 0;
                for(; i < rupkt->entryNum; i++){
                    int dstNode = rupkt->entry[i].nodeID;
                    int cost = rupkt->entry[i].cost;
                    if(dvtable_getOnlineFlag(dv, myNode, dstNode) == 1){
                        dvtable_setcost(dv, srcNode, dstNode, cost);
                    }
                }
                update_routingtable();
            }
            else{
                dvtable_setOnlineFlag(dv, myNode, srcNode, 1);
                int i = 0;
                for(; i < rupkt->entryNum; i++) {
                    int dstNode = rupkt->entry[i].nodeID;
                    int cost = rupkt->entry[i].cost;
                    if(dvtable_getOnlineFlag(dv, myNode, dstNode) == 1) {
                        dvtable_setcost(dv, srcNode, dstNode, cost);
                    }
                }
                dvtable_setcost(dv, myNode, srcNode, topology_getCost(myNode, srcNode));
                routingtable_setnextnode(routingtable, srcNode, srcNode);
                update_routingtable();
                int nbrnum = topology_getNbrNum();
                int j = 0;
                for(; j < nbrnum; j++) {
                    if(nct[j].nodeID != srcNode && dvtable_getOnlineFlag(dv, myNode, nct[j].nodeID) == 1){
                        sip_pkt_t *sippkt = (sip_pkt_t*)malloc(sizeof(sip_pkt_t));
                        sippkt->header.src_nodeID = myNode;
                        sippkt->header.dest_nodeID = nct[j].nodeID;
                        sippkt->header.type = ROUTE_UPDATE_2;
                        route_change_t rct_pkt;
                        rct_pkt.nodeID = srcNode;
                        rct_pkt.haveReceivedNodeNum = 0;
                        rct_pkt.haveReceivedNode[rct_pkt.haveReceivedNodeNum] = myNode;
                        rct_pkt.haveReceivedNodeNum ++;
                        sippkt->header.length = sizeof(int) * (2 + rct_pkt.haveReceivedNodeNum);
                        strncopy(sippkt->data, (char *)&rct_pkt, sippkt->header.length);
                        int nextNode = routingtable_getnextnode(routingtable, nct[j].nodeID);
                        if(nextNode == -1){
                            perror("can not find routing info"); 
                        }
                        else{
                            son_sendpkt(nextNode, sippkt, son_conn);
                        }
                    }
                }
            }
            int p = 0;
            for(; p < topology_getNbrNum(); p++){
                if(nct[p].nodeID == srcNode){
                    break;
                }
            }
            pthread_mutex_lock(&nct[p].flag_mutex);
            nct[p].flag = 1;
            pthread_mutex_unlock(&nct[p].flag_mutex);
            pthread_mutex_unlock(dv_mutex);
            pthread_mutex_unlock(routingtable_mutex);
        }
        else if(pkt.header.type == ROUTE_UPDATE_1){
            route_change_t rct_pkt;
            strncopy((char *)&rct_pkt, pkt.data, pkt.header.length);
            int offlineNode = rct_pkt.nodeID;
            int myNode = topology_getMyNodeID();
            int *nodeArray = topology_getNodeArray();
            int nbrnum = topology_getNbrNum();
            pthread_mutex_lock(dv_mutex);
            pthread_mutex_lock(routingtable_mutex);
            dvtable_setOnlineFlag(dv, myNode, offlineNode, 0);
            int j = 0;
            for(; j < topology_getNodeNum(); j++){
                dvtable_setcost(dv, offlineNode, nodeArray[j], INFINITE_COST);
                dvtable_setcost(dv, nodeArray[j], offlineNode, INFINITE_COST);
            }
            routingtable_setnextnode(routingtable, offlineNode, -1);
            update_routingtable();
            pthread_mutex_unlock(dv_mutex);
            pthread_mutex_unlock(routingtable_mutex);
            for(j = 0; j < nbrnum; j++){
                if(dvtable_getOnlineFlag(dv, myNode, nct[j].nodeID) == 1){
                    int flag = 1, k = 0;;
                    for(; k < rct_pkt.haveReceivedNodeNum; k++){
                        if(rct_pkt.haveReceivedNode[k] == nct[j].nodeID){
                            flag = 0;
                            break;
                        }
                    }
                    if(flag == 1){
                        sip_pkt_t *sippkt = (sip_pkt_t*)malloc(sizeof(sip_pkt_t));
                        sippkt->header.src_nodeID = topology_getMyNodeID();
                        sippkt->header.dest_nodeID = nct[j].nodeID;
                        sippkt->header.type = ROUTE_UPDATE_1;
                        rct_pkt.haveReceivedNode[rct_pkt.haveReceivedNodeNum] = myNode;
                        rct_pkt.haveReceivedNodeNum++;
                        sippkt->header.length = sizeof(int) * (2 + rct_pkt.haveReceivedNodeNum);
                        strncopy(sippkt->data, (char *)&rct_pkt, sippkt->header.length);
                        int nextNode = routingtable_getnextnode(routingtable, nct[j].nodeID);
                        if(nextNode == -1){
                            perror("can not find routing info"); 
                        }
                        else{
                            son_sendpkt(nextNode, sippkt, son_conn);
                        }
                    }
                }
            }
        }
        else if(pkt.header.type == ROUTE_UPDATE_2){
            route_change_t rct_pkt;
            memcpy((char *)&rct_pkt, pkt.data, pkt.header.length);
            int onlineNode = rct_pkt.nodeID;
            int myNode = topology_getMyNodeID();
            int nbrnum = topology_getNbrNum();
            pthread_mutex_lock(dv_mutex);
            dvtable_setOnlineFlag(dv, myNode, onlineNode, 1);
            pthread_mutex_unlock(dv_mutex);
            int j;
            for(j = 0; j < nbrnum; j ++){
                if(nct[j].nodeID != onlineNode && dvtable_getOnlineFlag(dv, myNode, nct[j].nodeID) == 1){
                    int flag = 1, k = 0;;
                    for(; k < rct_pkt.haveReceivedNodeNum; k ++){
                        if(rct_pkt.haveReceivedNode[k] == nct[j].nodeID){
                            flag = 0;
                            break;
                        }
                    }
                    if(flag == 1){
                        sip_pkt_t *sippkt = (sip_pkt_t*)malloc(sizeof(sip_pkt_t));
                        sippkt->header.src_nodeID = topology_getMyNodeID();
                        sippkt->header.dest_nodeID = nct[j].nodeID;
                        sippkt->header.type = ROUTE_UPDATE_2;
                        rct_pkt.haveReceivedNode[rct_pkt.haveReceivedNodeNum] = myNode;
                        rct_pkt.haveReceivedNodeNum ++;
                        sippkt->header.length = sizeof(int) * (2 + rct_pkt.haveReceivedNodeNum);
                        strncopy(sippkt->data, (char *)&rct_pkt, sippkt->header.length);
                        int nextNode = routingtable_getnextnode(routingtable, nct[j].nodeID);
                        if(nextNode == -1){
                            perror("can not find routing info"); 
                        }
                        else{
                            son_sendpkt(nextNode, sippkt, son_conn);
                        }
                    }
                }
            }
        }
    }
    printf("sip conn close \n");
	close(son_conn);
	son_conn = -1;
	pthread_exit(NULL);
}

//用于实时监控路由信息的更新的线程函数
void* monitor_route_update(void* arg) {
	int myNode = topology_getMyNodeID();
	int *nodelist = topology_getNodeArray();
	while(1) {
		sleep(ROUTEUPDATE_INTERVAL * 2);
		int nodenum = topology_getNodeNum();
		int nbrnum = topology_getNbrNum();
		int i=0;
		for(i=0; i<nbrnum; i++) {
			pthread_mutex_lock(&nct[i].flag_mutex);
            if(nct[i].flag == 0){
				pthread_mutex_lock(dv_mutex);
                pthread_mutex_lock(routingtable_mutex);
				int offlineNode = nct[i].nodeID;
				dvtable_setOnlineFlag(dv, myNode, offlineNode, 0);
				int j = 0;
				for(j=0; j<nodenum; j++) {
					dvtable_setcost(dv, offlineNode, nodelist[j], INFINITE_COST);
					dvtable_setcost(dv, nodelist[j], offlineNode, INFINITE_COST);
				}
				routingtable_setnextnode(routingtable, offlineNode, -1);
				update_routingtable();
				pthread_mutex_unlock(dv_mutex);
                pthread_mutex_unlock(routingtable_mutex);
				for(j=0; j<nbrnum; j++) {
					if(dvtable_getOnlineFlag(dv, myNode, nct[i].nodeID) == 1) {
						sip_pkt_t *sip_pkt = (sip_pkt_t*)malloc(sizeof(sip_pkt_t));
                        sip_pkt->header.src_nodeID = topology_getMyNodeID();
                        sip_pkt->header.dest_nodeID = nct[j].nodeID;
                        sip_pkt->header.type = ROUTE_UPDATE_1;
                        route_change_t rct_pkt;
                        rct_pkt.nodeID = offlineNode;
                        rct_pkt.haveReceivedNodeNum = 0;
                        rct_pkt.haveReceivedNode[rct_pkt.haveReceivedNodeNum] = myNode;
                        rct_pkt.haveReceivedNodeNum++;
                        sip_pkt->header.length = sizeof(int) * (2 + rct_pkt.haveReceivedNodeNum);
                        strncopy(sip_pkt->data, (char *)&rct_pkt, sip_pkt->header.length);
                        int nextNode = routingtable_getnextnode(routingtable, nct[j].nodeID);
                        if(nextNode == -1){
                            printf("Error find routing info\n"); 
                        }
                        else{
                            son_sendpkt(nextNode, sip_pkt, son_conn);
							printf("[SIP] monitor\n");
                        }
					}
				}
				nct[i].flag = 2;				
			}
			else if(nct[i].flag == 1) {
				nct[i].flag = 0;
			}
			pthread_mutex_unlock(&nct[i].flag_mutex);
		}
	}
}

//这个函数终止SIP进程, 当SIP进程收到信号SIGINT时会调用这个函数. 
//它关闭所有连接, 释放所有动态分配的内存.
void sip_stop() {
	close(son_conn);
	son_conn = -1;
	printf("[SIP] stopped!\n");
	exit(1);
}

//这个函数打开端口SIP_PORT并等待来自本地STCP进程的TCP连接.
//在连接建立后, 这个函数从STCP进程处持续接收包含段及其目的节点ID的sendseg_arg_t. 
//接收的段被封装进数据报(一个段在一个数据报中), 然后使用son_sendpkt发送该报文到下一跳. 下一跳节点ID提取自路由表.
//当本地STCP进程断开连接时, 这个函数等待下一个STCP进程的连接.
void waitSTCP() {
	int listenfd;
    socklen_t clilen;
    struct sockaddr_in cliaddr, servaddr;
    if((listenfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    {
        perror("Error create socket\n");
        exit(-1);
    }
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(SIP_PORT);
    bind(listenfd, (struct sockaddr*)&servaddr, sizeof(servaddr));
    listen(listenfd, 10);
    clilen = sizeof(cliaddr);
    while(1){
        stcp_conn = accept(listenfd, (struct sockaddr*)&cliaddr, &clilen);
        printf("STCP connection\n");
        seg_t *segpkt;
        int *dest;
        while(1) {
            segpkt = (seg_t*)malloc(sizeof(seg_t));
            dest = (int *)malloc(sizeof(int));
            if(getsegToSend(stcp_conn, dest, segpkt) > 0) {
                printf("received STCP info\n");
                sip_pkt_t *sippkt = (sip_pkt_t*)malloc(sizeof(sip_pkt_t));
                sippkt->header.src_nodeID = topology_getMyNodeID();
                sippkt->header.dest_nodeID = *dest;
                sippkt->header.type = SIP;
                sippkt->header.length = sizeof(stcp_hdr_t) + segpkt->header.length;
                strncopy(sippkt->data, (char *)segpkt, sizeof(stcp_hdr_t) + segpkt->header.length);
				pthread_mutex_lock(routingtable_mutex);
                int next = routingtable_getnextnode(routingtable, *dest);
				pthread_mutex_unlock(routingtable_mutex);
                if(next == -1){
                    perror("Error find routing info"); 
                }
                else{
                    printf("send message to son\n");
                    son_sendpkt(next, sippkt, son_conn);
                }
                free(sippkt);
            }
            else{
                printf("STCP connection break down\n");
                free(segpkt);
                free(dest);
                close(stcp_conn);
                break;
            }
            free(segpkt);
            free(dest);
        }
    }
}

int main(int argc, char *argv[]) {
	printf("SIP layer is starting, pls wait...\n");

	//初始化全局变量
	nct = nbrcosttable_create();
	dv = dvtable_create();
	dv_mutex = (pthread_mutex_t*)malloc(sizeof(pthread_mutex_t));
	pthread_mutex_init(dv_mutex,NULL);
	routingtable = routingtable_create();
	routingtable_mutex = (pthread_mutex_t*)malloc(sizeof(pthread_mutex_t));
	pthread_mutex_init(routingtable_mutex,NULL);
	son_conn = -1;
	stcp_conn = -1;

	nbrcosttable_print(nct);
	dvtable_print(dv);
	routingtable_print(routingtable);

	//注册用于终止进程的信号句柄
	signal(SIGINT, sip_stop);

	//连接到本地SON进程 
	son_conn = connectToSON();
	if(son_conn<0) {
		printf("can't connect to SON process\n");
		exit(1);		
	}
	
	//启动线程处理来自SON进程的进入报文 
	pthread_t pkt_handler_thread; 
	pthread_create(&pkt_handler_thread,NULL,pkthandler,(void*)0);

	//启动路由更新线程 
	pthread_t routeupdate_thread;
	pthread_create(&routeupdate_thread,NULL,routeupdate_daemon,(void*)0);	

	printf("SIP layer is started...\n");
	printf("waiting for routes to be established\n");
	sleep(SIP_WAITTIME);
	routingtable_print(routingtable);

	pthread_create(&monitorroute_thread,NULL,monitor_route_update,(void*)0);	
	
	//等待来自STCP进程的连接
	printf("waiting for connection from STCP process\n");
	waitSTCP(); 

}


