//文件名: server/stcp_server.c
//
//描述: 这个文件包含STCP服务器接口实现. 
//
//创建日期: 2015年

#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <stdio.h>
#include <sys/select.h>
#include <strings.h>
#include <unistd.h>
#include <time.h>
#include <pthread.h>
#include <assert.h>
#include "stcp_server.h"
#include "../topology/topology.h"
#include "../common/constants.h"


void usleep(unsigned long usec);
//声明tcbtable为全局变量
server_tcb_t* tcbtable[MAX_TRANSPORT_CONNECTIONS];
//声明到SIP进程的连接为全局变量
int sip_conn;
pthread_t mythread;

//若使用memcpy的话，可能会出现错位，在lab8中已经出现过，所以自己封装一个strncopy函数
void strncopy(char *dest, char *src, int n)
{
    int i;
    for(i = 0; i < n ; i ++)
    {
        dest[i] = src[i];
    }
}

/*********************************************************************/
//
//STCP API实现
//
/*********************************************************************/

// 这个函数初始化TCB表, 将所有条目标记为NULL. 它还针对TCP套接字描述符conn初始化一个STCP层的全局变量, 
// 该变量作为sip_sendseg和sip_recvseg的输入参数. 最后, 这个函数启动seghandler线程来处理进入的STCP段.
// 服务器只有一个seghandler.
void stcp_server_init(int conn) 
{
	int i = 0;
	for(; i<MAX_TRANSPORT_CONNECTIONS; i++) {
		tcbtable[i]=NULL;
	}
	sip_conn = conn;
	int rc = pthread_create(&mythread,NULL,seghandler,NULL);
	if(rc) {
		printf("ERROR: return code from pthread_create() is %d\n",rc);
		exit(-1);
	}
	return;
}

// 这个函数查找服务器TCB表以找到第一个NULL条目, 然后使用malloc()为该条目创建一个新的TCB条目.
// 该TCB中的所有字段都被初始化, 例如, TCB state被设置为CLOSED, 服务器端口被设置为函数调用参数server_port. 
// TCB表中条目的索引应作为服务器的新套接字ID被这个函数返回, 它用于标识服务器端的连接. 
// 如果TCB表中没有条目可用, 这个函数返回-1.
int stcp_server_sock(unsigned int server_port) 
{
	int i = 0;
    for(; i<MAX_TRANSPORT_CONNECTIONS; i++)
    {
        if(tcbtable[i] == NULL)
            break;
    }
    if(i == MAX_TRANSPORT_CONNECTIONS)
        return -1;
    tcbtable[i] = (server_tcb_t*)malloc(sizeof(server_tcb_t));
    memset((void *)tcbtable[i],0,sizeof(server_tcb_t));
    tcbtable[i]->state = CLOSED;
    tcbtable[i]->server_portNum = server_port;
    tcbtable[i]->recvBuf = (char *)malloc(RECEIVE_BUF_SIZE);
    tcbtable[i]->usedBufLen = 0;
    tcbtable[i]->bufMutex = (pthread_mutex_t *)malloc(sizeof(pthread_mutex_t));
    pthread_mutex_init(tcbtable[i]->bufMutex,NULL);
    return i;
}

// 这个函数使用sockfd获得TCB指针, 并将连接的state转换为LISTENING. 它然后启动定时器进入忙等待直到TCB状态转换为CONNECTED 
// (当收到SYN时, seghandler会进行状态的转换). 该函数在一个无穷循环中等待TCB的state转换为CONNECTED,  
// 当发生了转换时, 该函数返回1. 你可以使用不同的方法来实现这种阻塞等待.
int stcp_server_accept(int sockfd) 
{
	server_tcb_t* acc_tcb = tcbtable[sockfd];
	if(acc_tcb == NULL) {
		perror("Error get the tcb!\n");
		return -1;
	}
	acc_tcb->state = LISTENING;
	while(acc_tcb->state != CONNECTED) {
		//ACCEPT_POLLING_INTERVAL=0.1s, sleep不能满足！
		usleep(ACCEPT_POLLING_INTERVAL/1000);
	}
	return 1;
}

// 接收来自STCP客户端的数据. 这个函数每隔RECVBUF_POLLING_INTERVAL时间
// 就查询接收缓冲区, 直到等待的数据到达, 它然后存储数据并返回1. 如果这个函数失败, 则返回-1.
int stcp_server_recv(int sockfd, void* buf, unsigned int length) 
{
	server_tcb_t *current_tcb = tcbtable[sockfd];
    while(1)
    {
        pthread_mutex_lock(tcbtable[sockfd]->bufMutex);
        if(current_tcb->usedBufLen >= length)
        {
            strncopy(buf,current_tcb->recvBuf,length);
            strncopy(current_tcb->recvBuf, (current_tcb->recvBuf)+length,current_tcb->usedBufLen-length);
            current_tcb->usedBufLen -= length;
            pthread_mutex_unlock(tcbtable[sockfd]->bufMutex);
            return 1;
        }
        else
        {
            pthread_mutex_unlock(tcbtable[sockfd]->bufMutex);
            sleep(RECVBUF_POLLING_INTERVAL);
        }
    }
    return 0;
}

// 这个函数调用free()释放TCB条目. 它将该条目标记为NULL, 成功时(即位于正确的状态)返回1,
// 失败时(即位于错误的状态)返回-1.
int stcp_server_close(int sockfd) 
{
	server_tcb_t* close_tcb = tcbtable[sockfd];
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
	free(tcbtable[sockfd]);
	tcbtable[sockfd]=NULL;
	return 1;
}

// 这是由stcp_server_init()启动的线程. 它处理所有来自客户端的进入数据. seghandler被设计为一个调用sip_recvseg()的无穷循环, 
// 如果sip_recvseg()失败, 则说明到SIP进程的连接已关闭, 线程将终止. 根据STCP段到达时连接所处的状态, 可以采取不同的动作.
// 请查看服务端FSM以了解更多细节.
void* seghandler(void* arg) 
{
	printf("Server running....\n");
	while(1)
    {
        seg_t *my_seg_t = (seg_t* )malloc(sizeof(seg_t));
        memset(my_seg_t,0,sizeof(seg_t));
        int client_nodeID;
        int result = sip_recvseg(sip_conn, &client_nodeID, my_seg_t);
        //printf("~~!!!!result = %d\n", result);
        if(result == 1)
        {
            int i = 0;
            int port = my_seg_t->header.dest_port;
            for(; i<MAX_TRANSPORT_CONNECTIONS; i++)
            {
                if(tcbtable[i]->server_portNum == port)
                {
                    break;
                }
            }
            if(i == MAX_TRANSPORT_CONNECTIONS)
            {
                perror("seghandler error!");
                exit(3);
            }
            switch(tcbtable[i]->state)
            {
            case LISTENING:
                if(my_seg_t->header.type == SYN)
                {
                    printf("recv SYN from port :%d\n",my_seg_t->header.src_port);
                    tcbtable[i]->state = CONNECTED;
                    my_seg_t->header.type = SYNACK;
                    int src_port = my_seg_t->header.src_port;
                    my_seg_t->header.src_port = my_seg_t->header.dest_port;
                    my_seg_t->header.dest_port = src_port;
                    printf("send SYNACK to port:  %d\n",my_seg_t->header.dest_port);
                    sip_sendseg(sip_conn,client_nodeID,my_seg_t);
                }
                break;
            case CONNECTED:
                if(my_seg_t->header.type == FIN)
                {
                    printf("recv FIN from port: %d\n",my_seg_t->header.src_port);
                    tcbtable[i]->state = CLOSEWAIT;
                    my_seg_t->header.type = FINACK;
                    int src_port = my_seg_t->header.src_port;
                    my_seg_t->header.src_port = my_seg_t->header.dest_port;
                    my_seg_t->header.dest_port = src_port;
                    printf("send FINACK to port: %d\n",my_seg_t->header.dest_port);
                    sip_sendseg(sip_conn,client_nodeID,my_seg_t);
                }
                else if(my_seg_t->header.type == SYN)
                {
                    printf("recv SYN from port: %d\n",my_seg_t->header.src_port);
                    tcbtable[i]->state = CONNECTED;
                    my_seg_t->header.type = SYNACK;
                    int src_port = my_seg_t->header.src_port;
                    my_seg_t->header.src_port = my_seg_t->header.dest_port;
                    my_seg_t->header.dest_port = src_port;
                    printf("send SYNACK to port: %d\n",my_seg_t->header.dest_port);
                    sip_sendseg(sip_conn,client_nodeID,my_seg_t);
                }
                else if(my_seg_t->header.type == DATA)
                {
                    printf("recv DATA from port:%d, seq: %d\n",my_seg_t->header.src_port,my_seg_t->header.seq_num);
                    if(my_seg_t->header.seq_num == tcbtable[i]->expect_seqNum)
                    {
                        int data_len = my_seg_t->header.length;
                        if(data_len <= (RECEIVE_BUF_SIZE-tcbtable[i]->usedBufLen))
                        {
                            pthread_mutex_lock(tcbtable[i]->bufMutex);
                            strncopy(tcbtable[i]->recvBuf+tcbtable[i]->usedBufLen, my_seg_t->data,data_len);
                            tcbtable[i]->expect_seqNum += data_len;
                            tcbtable[i]->usedBufLen += data_len;
                            pthread_mutex_unlock(tcbtable[i]->bufMutex);
                        }
                    }
                    my_seg_t->header.type = DATAACK;
                    unsigned int src_port = my_seg_t->header.src_port;
                    my_seg_t->header.src_port = my_seg_t->header.dest_port;
                    my_seg_t->header.dest_port = src_port;
                    my_seg_t->header.ack_num = tcbtable[i]->expect_seqNum;
                    my_seg_t->header.length = 0;
                    printf("send DATAACK to port :%d,ack is %d\n",my_seg_t->header.dest_port, my_seg_t->header.ack_num);
                    sip_sendseg(sip_conn,client_nodeID,my_seg_t);
                }
                break;
            case CLOSEWAIT:
                if(my_seg_t->header.type == FIN)
                {
                    printf("recv FIN from port: %d\n",my_seg_t->header.src_port);
                    tcbtable[i]->state = CLOSEWAIT;
                    my_seg_t->header.type = FINACK;
                    int src_port = my_seg_t->header.src_port;
                    my_seg_t->header.src_port = my_seg_t->header.dest_port;
                    my_seg_t->header.dest_port = src_port;
                    printf("send FINACK to port :%d\n",my_seg_t->header.dest_port);
                    sip_sendseg(sip_conn,client_nodeID,my_seg_t);
                }
                break;
            case CLOSED:
                break;
            }
        }
        else if(result < 0)
        {
            break;
        }
        free(my_seg_t);
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
	sip_sendseg(sip_conn, recv_tcb->client_nodeID, send);
}

