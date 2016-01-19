//文件名: son/son.c
//
//描述: 这个文件实现SON进程
//SON进程首先连接到所有邻居, 然后启动listen_to_neighbor线程, 每个该线程持续接收来自一个邻居的进入报文, 并将该报文转发给SIP进程. 
//然后SON进程等待来自SIP进程的连接. 在与SIP进程建立连接之后, SON进程持续接收来自SIP进程的sendpkt_arg_t结构, 并将接收到的报文发送到重叠网络中.  
//
//创建日期: 2015年

#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <pthread.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <signal.h>
#include <sys/utsname.h>
#include <assert.h>

#include "../common/constants.h"
#include "../common/pkt.h"
#include "son.h"
#include "../topology/topology.h"
#include "neighbortable.h"

//你应该在这个时间段内启动所有重叠网络节点上的SON进程
#define SON_START_DELAY 30

/**************************************************************/
//声明全局变量
/**************************************************************/
// listen socket should be global!
int listenfd;
//将邻居表声明为一个全局变量 
nbr_entry_t* nt; 
//将与SIP进程之间的TCP连接声明为一个全局变量
int sip_conn; 

/**************************************************************/
//实现重叠网络函数
/**************************************************************/

// 这个线程打开TCP端口CONNECTION_PORT, 等待节点ID比自己大的所有邻居的进入连接,
// 在所有进入连接都建立后, 这个线程终止.
void* waitNbrs(void* arg) {
	int connfd;
	int mynode= topology_getMyNodeID();
	int mynbrnum = topology_getNbrNum();
	int *nbrlist = topology_getNbrArray();
	int listenq=0;
	int i = 0;
	for(i=0; i<mynbrnum; i++) {
		if(nbrlist[i]>mynode)
			listenq++;
	}
	socklen_t clilen;
	struct sockaddr_in cli_addr, serv_addr;
	if((listenfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		 perror("Error create socket\n");
		 exit(-1);
	}
	const int one = 1;	//port可以立即重新使用
	setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &one, sizeof(one) );
	memset(&serv_addr,0,sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(CONNECTION_PORT);
	if(bind(listenfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr))<0) {
		perror("Error bind\n");
		exit(-1);
	}
	listen(listenfd, MAX_NODE_NUM);
	clilen = sizeof(cli_addr);
	for(i=0; i<listenq; i++) {
		connfd = accept(listenfd, (struct sockaddr *)&cli_addr, &clilen);
		int node = topology_getNodeIDfromip(&cli_addr.sin_addr);
		if(nt_addconn(nt, node, connfd) == -1) {
			printf("Error add!\n");
		}
		printf("Add Node: %d\n", node);
	}
	return 0;
}

// 这个函数连接到节点ID比自己小的所有邻居.
// 在所有外出连接都建立后, 返回1, 否则返回-1.
int connectNbrs() {
	int mynode= topology_getMyNodeID();
	int mynbrnum = topology_getNbrNum();
	int *nbrlist = topology_getNbrArray();
	int i=0;
	for(i=0; i<mynbrnum; i++) {
		if(nbrlist[i] < mynode) {
			int sockfd;
			struct sockaddr_in serv_addr;
			if((sockfd = socket(AF_INET, SOCK_STREAM, 0)) <0) {
				perror("Error create socket!\n");
				return -1;
			}
			memset(&serv_addr, 0, sizeof(serv_addr));
			serv_addr.sin_family = AF_INET;
            serv_addr.sin_addr.s_addr = topology_getfromIDtoIP(nbrlist[i]);
            serv_addr.sin_port = htons(CONNECTION_PORT);
            if(connect(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr))<0) {
				perror("Error connect\n");
				return -1;
			}
			nt_addconn(nt, nbrlist[i], sockfd);
		}
	}
	return 1;
}

//每个listen_to_neighbor线程持续接收来自一个邻居的报文. 它将接收到的报文转发给SIP进程.
//所有的listen_to_neighbor线程都是在到邻居的TCP连接全部建立之后启动的.
void* listen_to_neighbor(void* arg) {
	int i = *(int *)arg;
	int conn = nt[i].conn;
	sip_pkt_t* sip_pkt;
	while(1) {
		sip_pkt = (sip_pkt_t*)malloc(sizeof(sip_pkt_t));
		memset(sip_pkt, 0, sizeof(sip_pkt_t));
		if(recvpkt(sip_pkt, conn) > 0) {
				if(sip_pkt->header.type == SIP ) {
					printf("receive stcp packet from node %d\n", sip_pkt->header.src_nodeID);
				}
				forwardpktToSIP(sip_pkt, sip_conn);
		}
		else {
			//nt[i].conn = -1; wait for next connection
			close(conn);
			socklen_t clilen;
			struct sockaddr_in cli_addr;
			clilen = sizeof(cli_addr);
			conn = accept(listenfd, (struct sockaddr*)&cli_addr, &clilen);
			int node = topology_getNodeIDfromip(&cli_addr.sin_addr);
			nt_addconn(nt, node, conn);
		}
		free(sip_pkt);
	}
}

//这个函数打开TCP端口SON_PORT, 等待来自本地SIP进程的进入连接. 
//在本地SIP进程连接之后, 这个函数持续接收来自SIP进程的sendpkt_arg_t结构, 并将报文发送到重叠网络中的下一跳. 
//如果下一跳的节点ID为BROADCAST_NODEID, 报文应发送到所有邻居节点.
void waitSIP() {
	int sockfd;
	struct sockaddr_in cli_addr, serv_addr;
	socklen_t clilen;
	if((sockfd = socket(AF_INET, SOCK_STREAM, 0))<0) {
		perror("Error create socket\n");
		exit(-1);
	}
	const int one = 1;	//port可以立即重新使用
	setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &one, sizeof(one));
	
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons(SON_PORT);
	if(bind(sockfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr))<0) {
		perror("Error bind\n");
		exit(-1);
	}
	listen(sockfd, MAX_NODE_NUM);
	clilen = sizeof(cli_addr);
	while(1) {
		sip_conn = accept(sockfd, (struct sockaddr*)&cli_addr, &clilen);
		sip_pkt_t* sip_pkt;
		int next;
		while(1) {
			sip_pkt = (sip_pkt_t*)malloc(sizeof(sip_pkt_t));
			memset(sip_pkt, 0, sizeof(sip_pkt_t));
			if(getpktToSend(sip_pkt, &next, sip_conn)>0) {
				if(next == BROADCAST_NODEID) {
					int nbrnum = topology_getNbrNum();
					int i = 0;
					for(i=0; i<nbrnum; i++) {
						//if(nt[i].conn > 0) {
							sendpkt(sip_pkt, nt[i].conn);
						//}
						//else {
							//printf("sendpkt conn<0, id: %d\n", nt[i].nodeID);
						//}
					}
				}
				else {
					if(sip_pkt->header.type == SIP) {
						printf("Receive stcp packet!\n");
					}
					int nbrnum = topology_getNbrNum();
					int i = 0;
					for(i=0; i<nbrnum; i++) {
						if(next == nt[i].nodeID) {
							printf("send message to node %d\n", next);
							sendpkt(sip_pkt, nt[i].conn);
							break;
						}
					}
				}
			}
			else {
				free(sip_pkt);
				close(sip_conn);
				break;
			}
			free(sip_pkt);
		}
	}
	return;
}

//这个函数停止重叠网络, 当接收到信号SIGINT时, 该函数被调用.
//它关闭所有的连接, 释放所有动态分配的内存.
void son_stop() {
	close(sip_conn);
	sip_conn = -1;
	nt_destroy(nt);
	printf("[SON] stop!\n");
	exit(1);
}

int main() {
	//启动重叠网络初始化工作
	printf("Overlay network: Node %d initializing...\n",topology_getMyNodeID());	

	//创建一个邻居表
	nt = nt_create();
	//将sip_conn初始化为-1, 即还未与SIP进程连接.
	sip_conn = -1;
	
	//注册一个信号句柄, 用于终止进程
	signal(SIGINT, son_stop);

	//打印所有邻居
	int nbrNum = topology_getNbrNum();
	int i;
	for(i=0;i<nbrNum;i++) {
		printf("Overlay network: neighbor %d:%d\n",i+1,nt[i].nodeID);
	}

	//启动waitNbrs线程, 等待节点ID比自己大的所有邻居的进入连接
	pthread_t waitNbrs_thread;
	pthread_create(&waitNbrs_thread,NULL,waitNbrs,(void*)0);

	//等待其他节点启动
	sleep(SON_START_DELAY);
	
	//连接到节点ID比自己小的所有邻居
	connectNbrs();

	//等待waitNbrs线程返回
	pthread_join(waitNbrs_thread,NULL);	

	//此时, 所有与邻居之间的连接都建立好了
	
	//创建线程监听所有邻居
	for(i=0;i<nbrNum;i++) {
		int* idx = (int*)malloc(sizeof(int));
		*idx = i;
		pthread_t nbr_listen_thread;
		pthread_create(&nbr_listen_thread,NULL,listen_to_neighbor,(void*)idx);
	}
	printf("Overlay network: node initialized...\n");
	printf("Overlay network: waiting for connection from SIP process...\n");

	//等待来自SIP进程的连接
	waitSIP();
}