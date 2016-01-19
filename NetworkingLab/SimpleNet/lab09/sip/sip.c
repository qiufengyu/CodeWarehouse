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
#include <arpa/inet.h>
#include <signal.h>
#include <netdb.h>
#include <assert.h>
#include <sys/utsname.h>
#include <pthread.h>
#include <unistd.h>

#include "../common/constants.h"
#include "../common/pkt.h"
#include "../topology/topology.h"
#include "sip.h"

/**************************************************************/
//声明全局变量
/**************************************************************/
int son_conn; 		//到重叠网络的连接

/**************************************************************/
//实现SIP的函数
/**************************************************************/

//SIP进程使用这个函数连接到本地SON进程的端口SON_PORT
//成功时返回连接描述符, 否则返回-1
int connectToSON() { 
	int sockfd;
	struct sockaddr_in serv_addr;
	char localIP[16]="127.0.0.1";
	memset(&serv_addr, 0, sizeof(struct sockaddr_in));
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr = inet_addr(localIP);
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

//这个线程每隔ROUTEUPDATE_INTERVAL时间就发送一条路由更新报文
//在本实验中, 这个线程只广播空的路由更新报文给所有邻居, 
//我们通过设置SIP报文首部中的dest_nodeID为BROADCAST_NODEID来发送广播
void* routeupdate_daemon(void* arg) {
	while(1) {
		sip_pkt_t *update_sip = (sip_pkt_t *)malloc(sizeof(sip_pkt_t));
		update_sip->header.src_nodeID = topology_getMyNodeID();
		update_sip->header.dest_nodeID = BROADCAST_NODEID;
		update_sip->header.type = ROUTE_UPDATE;
		update_sip->header.length = 0;
		pkt_routeupdate_t pkt_route;
		//here is 0!
		pkt_route.entryNum = 0;
		memset(pkt_route.entry, 0, sizeof(routeupdate_entry_t)*pkt_route.entryNum);
		update_sip->header.length = sizeof(int) + sizeof(routeupdate_entry_t)*pkt_route.entryNum;
		strncpy(update_sip->data, (char *)&pkt_route, update_sip->header.length);
		son_sendpkt(BROADCAST_NODEID, update_sip, son_conn);
		free(update_sip);
		sleep(ROUTEUPDATE_INTERVAL);
	}
	return 0;
}

//这个线程处理来自SON进程的进入报文
//它通过调用son_recvpkt()接收来自SON进程的报文
//在本实验中, 这个线程在接收到报文后, 只是显示报文已接收到的信息, 并不处理报文 
void* pkthandler(void* arg) {
	sip_pkt_t pkt;

	while(son_recvpkt(&pkt,son_conn)>0 && son_conn!= -1) {
		printf("Routing: received a packet from neighbor %d\n",pkt.header.src_nodeID);
	}
	close(son_conn);
	son_conn = -1;
	pthread_exit(NULL);
}

//这个函数终止SIP进程, 当SIP进程收到信号SIGINT时会调用这个函数. 
//它关闭所有连接, 释放所有动态分配的内存.
void sip_stop() {
	close(son_conn);
	son_conn = -1;
	printf("[SIP] stopped!\n");
	exit(1);
}

int main(int argc, char *argv[]) {
	printf("SIP layer is starting, please wait...\n");

	//初始化全局变量
	son_conn = -1;

	//注册用于终止进程的信号句柄
	signal(SIGINT, sip_stop);

	//连接到重叠网络层SON
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

	//永久睡眠
	while(1) {
		sleep(60);
	}
}


