// 文件名: stcp_server.c
//
// 描述: 这个文件包含服务器STCP套接字接口定义. 你需要实现所有这些接口.
//
// 创建日期: 2015年
//

#include <stdlib.h>
#include <sys/socket.h>
#include <stdio.h>
#include <time.h>
#include <pthread.h>
#include "stcp_server.h"
#include "../common/constants.h"

//
//  用于服务器程序的STCP套接字API. 
//  ===================================
//
//  我们在下面提供了每个函数调用的原型定义和细节说明, 但这些只是指导性的, 你完全可以根据自己的想法来设计代码.
//
//  注意: 当实现这些函数时, 你需要考虑FSM中所有可能的状态, 这可以使用switch语句来实现. 
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//

// 这个函数初始化TCB表, 将所有条目标记为NULL. 它还针对重叠网络TCP套接字描述符conn初始化一个STCP层的全局变量, 
// 该变量作为sip_sendseg和sip_recvseg的输入参数. 最后, 这个函数启动seghandler线程来处理进入的STCP段.
// 服务器只有一个seghandler.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
void stcp_server_init(int conn)
{
	int i = 0;
	for(; i<MAX_TRANSPORT_CONNECTIONS; i++) {
		server_tcb_table[i]=NULL;
	}
	global_conn = conn;
	int rc = pthread_create(&mythread,NULL,seghandler,NULL);
	if(rc) {
		printf("ERROR: return code from pthread_create() is %d\n",rc);
		exit(-1);
	}
	return;
}

// 这个函数查找服务器TCB表以找到第一个NULL条目, 然后使用malloc()为该条目创建一个新的TCB条目.
// 该TCB中的所有字段都被初始化, 例如, TCB state被设置为CLOSED, 服务器端口被设置为函数调用参数server_port. 
// TCB表中条目的索引应作为服务器的新套接字ID被这个函数返回, 它用于标识服务器的连接. 
// 如果TCB表中没有条目可用, 这个函数返回-1.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_server_sock(unsigned int server_port)
{
	//find first NULL
	int first_null = 0;
	for(; first_null < MAX_TRANSPORT_CONNECTIONS; first_null++) {
		if(server_tcb_table[first_null] == NULL)
			break;
	}
	if (first_null == MAX_TRANSPORT_CONNECTIONS) {
		perror("No item available in tcb table!\n");
		return -1;
	}
	//initial
	server_tcb_t* new_tcb = (server_tcb_t*)malloc(sizeof(server_tcb_t));
	memset(&server_tcb_table[first_null],0,sizeof(server_tcb_t));
	new_tcb->state = CLOSED;
	new_tcb->server_portNum = server_port;
	new_tcb->expect_seqNum=0;
	new_tcb->recvBuf = (char *)malloc(RECEIVE_BUF_SIZE);
	memset(new_tcb->recvBuf, 0, RECEIVE_BUF_SIZE);
	new_tcb->usedBufLen = 0;
	new_tcb->bufMutex=(pthread_mutex_t *)malloc(sizeof(pthread_mutex_t));
	pthread_mutex_init(new_tcb->bufMutex, NULL);
	server_tcb_table[first_null]=new_tcb;
	return first_null;
}

// 这个函数使用sockfd获得TCB指针, 并将连接的state转换为LISTENING. 它然后启动定时器进入忙等待直到TCB状态转换为CONNECTED 
// (当收到SYN时, seghandler会进行状态的转换). 该函数在一个无穷循环中等待TCB的state转换为CONNECTED,  
// 当发生了转换时, 该函数返回1. 你可以使用不同的方法来实现这种阻塞等待.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_server_accept(int sockfd)
{
	server_tcb_t* acc_tcb = server_tcb_table[sockfd];
	if(acc_tcb == NULL) {
		perror("Error get the tcb!\n");
		return -1;
	}
	acc_tcb->state = LISTENING;
	while(acc_tcb->state != CONNECTED) {
		//ACCEPT_POLLING_INTERVAL=0.1s, sleep不能满足！
		usleep(ACCEPT_POLLING_INTERVAL/1000000);
	}
	return 1;
}

// 接收来自STCP客户端的数据. 请回忆STCP使用的是单向传输, 数据从客户端发送到服务器端.
// 信号/控制信息(如SYN, SYNACK等)则是双向传递. 这个函数每隔RECVBUF_ROLLING_INTERVAL时间
// 就查询接收缓冲区, 直到等待的数据到达, 它然后存储数据并返回1. 如果这个函数失败, 则返回-1.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_server_recv(int sockfd, void* buf, unsigned int length)
{
	server_tcb_t *recv_tcb = server_tcb_table[sockfd];
	if(recv_tcb == NULL) return -1;
	if(length>RECEIVE_BUF_SIZE)
		return -1;
	while(1) {
		pthread_mutex_lock(recv_tcb->bufMutex);//互斥进入
		//printf("\n!!!!!!!!!!\n%d\n\n!!!!!!!!!\n", recv_tcb->usedBufLen);
		if(recv_tcb->usedBufLen >= length) {
			/*
			int i;	
			printf("\njj\n");
			for(i=0;i<length;i++)
			{
				printf("%c",recv_tcb->recvBuf[i]);
			}
			printf("\njj\n");
			* */
			memcpy((char *)buf, recv_tcb->recvBuf, length);
			//memcpy(recv_tcb->recvBuf, (recv_tcb->recvBuf)+length,recv_tcb->usedBufLen-length);
			recv_tcb->usedBufLen = recv_tcb->usedBufLen-length;
			//用memcpy会出现字节错位的问题！
			int st = 0;
			for(;st < recv_tcb->usedBufLen; st++)
			{
				recv_tcb->recvBuf[st] = recv_tcb->recvBuf[st+length];
			}
			pthread_mutex_unlock(recv_tcb->bufMutex);
			return 1;
		}
		pthread_mutex_unlock(recv_tcb->bufMutex);
		sleep(RECVBUF_POLLING_INTERVAL);
	}
}

// 这个函数调用free()释放TCB条目. 它将该条目标记为NULL, 成功时(即位于正确的状态)返回1,
// 失败时(即位于错误的状态)返回-1.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_server_close(int sockfd)
{
	server_tcb_t* close_tcb = server_tcb_table[sockfd];
	if(close_tcb == NULL) {
		perror("CLose error!\n");
		return -1;
	}
	int close_state = close_tcb->state;
	if(close_state == LISTENING || close_state == CONNECTED) {
		perror("CLose error! Still running!\n");
		return -1;
	} 
	//释放缓冲区
	free(close_tcb->recvBuf);
	//销毁互斥量
	pthread_mutex_destroy(close_tcb->bufMutex);
	free(close_tcb->bufMutex);
	free(server_tcb_table[sockfd]);
	server_tcb_table[sockfd]=NULL;
	return 1;
}

// 这是由stcp_server_init()启动的线程. 它处理所有来自客户端的进入数据. seghandler被设计为一个调用sip_recvseg()的无穷循环, 
// 如果sip_recvseg()失败, 则说明重叠网络连接已关闭, 线程将终止. 根据STCP段到达时连接所处的状态, 可以采取不同的动作.
// 请查看服务端FSM以了解更多细节.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
void* seghandler(void* arg)
{
	server_tcb_t *recv_tcb = NULL;
	seg_t *seg = (seg_t *)malloc(sizeof(seg_t));
	seg_t *seg_ack = (seg_t *)malloc(sizeof(seg_t));
	while(1) {
		recv_tcb = NULL;
		memset(seg, 0, sizeof(seg_t));
		memset(seg_ack, 0, sizeof(seg_t));
		int n = sip_recvseg(global_conn, seg);
		if(n == 2) {
			//printf("Seg lost!\n");
			printf("---------------------------------\n");
			continue;
		}
		else if(n == 0) {
			//judge checksum first!
			if(checkchecksum(seg) == -1) {
				printf("Error checksum!\n");
				printf("---------------------------------\n");
				continue;
			}
			//printf("Receive a seg!\n");
			int i = 0;
			int port = seg->header.dest_port;
			for(i=0; i<MAX_TRANSPORT_CONNECTIONS; i++) {
				if(server_tcb_table[i]->server_portNum == port)
					break;
			}
			recv_tcb = server_tcb_table[i];
			if(recv_tcb == NULL || i == MAX_TRANSPORT_CONNECTIONS) {
				perror("Server tcb table error!\n");
				exit(-1);
			}
			switch(recv_tcb->state) {
				case CLOSED: {
					printf("State: CLOSED\n");
					printf("---------------------------------\n");
				} break;
				case LISTENING: {
					printf("State: LISTENING\n");
					printf("---------------------------------\n");
					if(seg->header.type == SYN) { 
						recv_tcb->client_portNum = seg->header.src_port;
						recv_tcb->expect_seqNum = seg->header.seq_num;
						sendAck(seg, seg_ack, recv_tcb, SYNACK);
						recv_tcb->state = CONNECTED;
					}
				} break;
				case CONNECTED: {
					//printf("State: CONNECTED\n");
					//printf("---------------------------------\n");
					if(seg->header.type == SYN) {
						printf("State: CONNECTED -> SYN\n");
						recv_tcb->client_portNum = seg->header.src_port;
						recv_tcb->expect_seqNum = seg->header.seq_num;
						sendAck(seg, seg_ack, recv_tcb, SYNACK);
						recv_tcb->state = CONNECTED;
					}
					else if(seg->header.type == FIN ) {
						printf("State: CONNECTED -> FIN\n");
						if(recv_tcb->expect_seqNum == seg->header.seq_num) {
							sendAck(seg, seg_ack, recv_tcb, FINACK);
							recv_tcb->state = CLOSEWAIT;
						}
						else {
							printf("FIN Seq error!\n");
						}
					}
					else if(seg->header.type == DATA) {
						printf("State: CONNECTED -> DATA\n");
						//judge seq num
						printf("from port: %d to port %d, seq = %d, expected: %d\n", seg->header.src_port, seg->header.dest_port, seg->header.seq_num, recv_tcb->expect_seqNum);
						if(recv_tcb->expect_seqNum == seg->header.seq_num) {
							pthread_mutex_lock(recv_tcb->bufMutex);
							printf("data length: %d\n", seg->header.length);
							memcpy(recv_tcb->recvBuf+recv_tcb->usedBufLen, seg->data, seg->header.length);
							/*
							int i= 0;
							printf("\nin data recv-----------\n");
							for(i = 0; i<seg->header.length; i++) {
								printf("%c", (char *)(recv_tcb->recvBuf)[i]);
							}
							printf("\n-----------\n");
							*/
							/*
							printf("\n-------------\n usedbuflen: %d\n---------------\n",recv_tcb->usedBufLen);
							* */
							recv_tcb->usedBufLen += seg->header.length;
							/*
							printf("\n-------------\n usedbuflen: %d\n----------------\n",recv_tcb->usedBufLen);
							* */
							recv_tcb->expect_seqNum += seg->header.length;
							//recv_tcb->expect_seqNum %= MAX_SEG_LEN;
							//printf("\n+++++++++---------\nrecv_tcb->expect_seqNum = %d\n\n", recv_tcb->expect_seqNum);
							pthread_mutex_unlock(recv_tcb->bufMutex);
							sendAck(seg, seg_ack, recv_tcb, DATAACK);
						}
						else {
							printf("DATA Seq error!\n");
							seg_ack->header.type = DATAACK;
							seg_ack->header.src_port = seg->header.dest_port;
							seg_ack->header.dest_port = seg->header.src_port;
							seg_ack->header.seq_num = 0;
							seg_ack->header.ack_num = recv_tcb->expect_seqNum;
							seg_ack->header.length = 0;
							seg_ack->header.rcv_win=0;
							seg_ack->header.checksum = 0;
							seg_ack->header.checksum = checksum(seg_ack);
							sip_sendseg(global_conn, seg_ack);
						}
					}
				} break;
				case CLOSEWAIT: {
					printf("State: CLOSEWAIT\n");
					printf("---------------------------------\n");
					if(seg->header.type == FIN) {
						sendAck(seg, seg_ack, recv_tcb, FINACK);
						recv_tcb->state = CLOSEWAIT;
					}
				} break;
				default: break;
			}
		}
		else if(n == 1) {
			continue;
		}
	}
	return 0;
}

void sendAck(seg_t *recv, seg_t* send, server_tcb_t* recv_tcb,int ackType) {
	printf("Sending Ack to port: %d, type = %d\n", recv_tcb->client_portNum, ackType);
	memset(send, 0, sizeof(seg_t));
	send->header.type = ackType;
	send->header.dest_port = recv_tcb->client_portNum;
	send->header.src_port = recv_tcb->server_portNum;
	send->header.ack_num = recv_tcb->expect_seqNum;
	send->header.checksum = checksum(send);
	sip_sendseg(global_conn, send);
}

