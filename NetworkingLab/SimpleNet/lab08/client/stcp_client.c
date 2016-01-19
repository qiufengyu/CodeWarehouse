#include <stdlib.h>
#include <stdio.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <time.h>
#include <pthread.h>
#include <string.h>
#include "stcp_client.h"
#include <pthread.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
//
//  我们在下面提供了每个函数调用的原型定义和细节说明, 但这些只是指导性的, 你完全可以根据自己的想法来设计代码.
//
//  注意: 当实现这些函数时, 你需要考虑FSM中所有可能的状态, 这可以使用switch语句来实现.
//
//  目标: 你的任务就是设计并实现下面的函数原型.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



// 这个函数初始化TCB表, 将所有条目标记为NULL.  
// 它还针对重叠网络TCP套接字描述符conn初始化一个STCP层的全局变量, 该变量作为sip_sendseg和sip_recvseg的输入参数.
// 最后, 这个函数启动seghandler线程来处理进入的STCP段. 客户端只有一个seghandler.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
void stcp_client_init(int conn)
{
	int i;
	for( i = 0; i < MAX_TRANSPORT_CONNECTIONS; i++){
		client_tcb_table[i]=NULL;		
	}
	connection=conn;
	int rc =pthread_create(&mythread, NULL, seghandler, NULL);
	if(rc){
		printf("ERROR; return code from pthread_create() is %d\n", rc);
		exit(-1);
	}
	return;
}

// 这个函数查找客户端TCB表以找到第一个NULL条目, 然后使用malloc()为该条目创建一个新的TCB条目.
// 该TCB中的所有字段都被初始化. 例如, TCB state被设置为CLOSED，客户端端口被设置为函数调用参数client_port. 
// TCB表中条目的索引号应作为客户端的新套接字ID被这个函数返回, 它用于标识客户端的连接. 
// 如果TCB表中没有条目可用, 这个函数返回-1.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_client_sock(unsigned int client_port)
{
	//find first NULL
	int first_null = 0;
	for(;first_null < MAX_TRANSPORT_CONNECTIONS ; first_null++){
		if(client_tcb_table[first_null] == NULL)
			break;
	}
	if(first_null == MAX_TRANSPORT_CONNECTIONS)
		return -1;	

	//initial
	client_tcb_t* new_tcb = (client_tcb_t*)malloc(sizeof(client_tcb_t));
	new_tcb->server_nodeID = 0;
	new_tcb->server_portNum = 0;
	new_tcb->client_nodeID = 0;
	new_tcb->client_portNum=client_port;
	new_tcb->state=CLOSED;
	new_tcb->next_seqNum=0;
	//Initial mutex!
	new_tcb->bufMutex = (pthread_mutex_t*)malloc(sizeof(pthread_mutex_t));
	pthread_mutex_init(new_tcb->bufMutex,NULL);	
	new_tcb->sendBufHead = NULL;
	new_tcb->sendBufunSent = NULL;
	new_tcb->sendBufTail = NULL;
	new_tcb->unAck_segNum = 0;
	client_tcb_table[first_null] = new_tcb;
	//printf("\n----\n%d\n----\n", first_null);
	return first_null;
}

// 这个函数用于连接服务器. 它以套接字ID和服务器的端口号作为输入参数. 套接字ID用于找到TCB条目.  
// 这个函数设置TCB的服务器端口号,  然后使用sip_sendseg()发送一个SYN段给服务器.  
// 在发送了SYN段之后, 一个定时器被启动. 如果在SYNSEG_TIMEOUT时间之内没有收到SYNACK, SYN 段将被重传. 
// 如果收到了, 就返回1. 否则, 如果重传SYN的次数大于SYN_MAX_RETRY, 就将state转换到CLOSED, 并返回-1.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_client_connect(int sockfd, unsigned int server_port)
{
	client_tcb_t* cnn_tcb=client_tcb_table[sockfd];
	if(cnn_tcb == NULL){
		perror("Error get the tcb!\n");
		//return -1;
	}
	cnn_tcb->server_portNum=server_port;
	seg_t *seg=malloc(sizeof(seg_t));
	seg->header.src_port=cnn_tcb->client_portNum;
	seg->header.dest_port=cnn_tcb->server_portNum;
	seg->header.seq_num=0;
	seg->header.ack_num=0;
	seg->header.length = 0;
	seg->header.type=SYN;
	seg->header.rcv_win=0;
	seg->header.checksum = 0;
	memset(seg->data, 0, MAX_SEG_LEN);
	seg->header.checksum = checksum(seg);
	printf("send SYN to server_port:%d\n", server_port);
	
	int i=0;
	int retry=0;
	while(retry<SYN_MAX_RETRY)
	{
		cnn_tcb->state=SYNSENT;
		retry++;
		i=sip_sendseg(connection,seg);
		//clock函数进行秒级计时，用timeval结构体进行微秒的计时
		struct timeval start, end;
		gettimeofday(&start,0);
		if(i<0)
			continue; //send error!
		while(1)
		{
			gettimeofday(&end,0);
			double timeuse  = 1000000*(end.tv_sec - start.tv_sec) + end.tv_usec - start.tv_usec;
			if(timeuse>SYN_TIMEOUT/1000) {
				printf("SYN time out\n");
				break;	//time out
			}
			if(cnn_tcb->state==CONNECTED)	//connect 
			{
				printf("connection has established \n");
				return 1;
			}
		}
	}
	cnn_tcb->state=CLOSED;
	printf("error: connection has not established  \n");
	return -1;
}

// 发送数据给STCP服务器. 这个函数使用套接字ID找到TCB表中的条目. 
// 然后它使用提供的数据创建segBuf, 将它附加到发送缓冲区链表中. 
// 如果发送缓冲区在插入数据之前为空, 一个名为sendbuf_timer的线程就会启动. 
// 每隔SENDBUF_ROLLING_INTERVAL时间查询发送缓冲区以检查是否有超时事件发生.
// 这个函数在成功时返回1，否则返回-1. 
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_client_send(int sockfd, void* data, unsigned int length)
{
	if(length > RECEIVE_BUF_SIZE)
		return -1;
	while(length > MAX_SEG_LEN)//最大长度的限制
	{
		stcp_send(sockfd, data, MAX_SEG_LEN);
		data += MAX_SEG_LEN;
		length -= MAX_SEG_LEN;
	}
	stcp_send(sockfd, data, length);
}

void stcp_send(int sockfd, void* data, unsigned int length) {
 	client_tcb_t* tcb=client_tcb_table[sockfd];
	if(tcb == NULL){
		perror("Error get the tcb!\n");
		return;
	}
	segBuf_t *segPtr = (segBuf_t*)malloc(sizeof(segBuf_t));
	//初始化头部
	segPtr->seg.header.src_port = tcb->client_portNum;
	segPtr->seg.header.dest_port = tcb->server_portNum;
	segPtr->seg.header.seq_num = tcb->next_seqNum;
	segPtr->seg.header.ack_num = 0;
	segPtr->seg.header.length = length;
	segPtr->seg.header.type = DATA;
	segPtr->seg.header.rcv_win = 0;
	memcpy(segPtr->seg.data, data, length);
	//printf("send string: %s, length: %d\n", (char *)data, length);
	segPtr->next = NULL;
	segPtr->seg.header.checksum = 0;
	segPtr->seg.header.checksum = checksum(&segPtr->seg);
	tcb->next_seqNum = (tcb->next_seqNum+length);
	
	pthread_mutex_lock(tcb->bufMutex);//互斥访问缓冲区！
	
	if(tcb->sendBufHead == NULL)
	{
		tcb->sendBufHead = segPtr;
		tcb->sendBufTail = segPtr;
		sip_sendseg(connection, &(segPtr->seg));
		tcb->unAck_segNum++;
		segPtr->sentTime = (unsigned)clock();
		pthread_t timerPthread;
		int rc = pthread_create(&timerPthread, NULL, sendBuf_timer, (void*)tcb);
		if(rc)
		{
			printf("ERROR; return code from pthread_create() is %d\n", rc);
		}
	}
	else
	{
		tcb->sendBufTail->next = segPtr;
		tcb->sendBufTail = segPtr;
		if(tcb->sendBufunSent==NULL) //所有的段都已经发送！
			tcb->sendBufunSent=segPtr;
		while(tcb->unAck_segNum < GBN_WINDOW && tcb->sendBufunSent != NULL) //根据滑动窗口协议，发送未发送的段
		{
			printf("send UNSENT DATA to server port: %d, seq: %d\n", tcb->sendBufunSent->seg.header.dest_port, tcb->sendBufunSent->seg.header.seq_num);
			sip_sendseg(connection, &(tcb->sendBufunSent->seg));
			tcb->sendBufunSent->sentTime = clock();
			tcb->sendBufunSent = tcb->sendBufunSent->next;
			tcb->unAck_segNum++;
		}
	}
	pthread_mutex_unlock(tcb->bufMutex);
	return;
}



// 这个函数用于断开到服务器的连接. 它以套接字ID作为输入参数. 套接字ID用于找到TCB表中的条目.  
// 这个函数发送FIN段给服务器. 在发送FIN之后, state将转换到FINWAIT, 并启动一个定时器.
// 如果在最终超时之前state转换到CLOSED, 则表明FINACK已被成功接收. 否则, 如果在经过FIN_MAX_RETRY次尝试之后,
// state仍然为FINWAIT, state将转换到CLOSED, 并返回-1.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_client_disconnect(int sockfd)
{
	client_tcb_t *dscnn_tcb=client_tcb_table[sockfd];
	if(dscnn_tcb==NULL) return -1;
	int flag = 1;
	while(flag) {
		pthread_mutex_lock(dscnn_tcb->bufMutex);
		if(dscnn_tcb->sendBufHead == NULL) {
			flag = 0;
		}
		pthread_mutex_unlock(dscnn_tcb->bufMutex);
		sleep(CLOSEWAIT_TIMEOUT);
	}
	seg_t *seg=malloc(sizeof(seg_t));
	seg->header.src_port=dscnn_tcb->client_portNum;
	seg->header.dest_port=dscnn_tcb->server_portNum;
	seg->header.seq_num=dscnn_tcb->next_seqNum;
	seg->header.ack_num=0;
	seg->header.length = 0;
	seg->header.type=FIN;
	seg->header.rcv_win=0;
	seg->header.checksum = 0;
	memset(seg->data, 0, MAX_SEG_LEN);
	seg->header.checksum=checksum(seg);
	int i=0;
	int retry=0;
	dscnn_tcb->state=FINWAIT;
	while(retry<FIN_MAX_RETRY)
	{
		retry++;
		i=sip_sendseg(connection,seg);
		struct timeval start, end;
		gettimeofday(&start,0);
		if(i<0)  continue;
		while(1)
		{
			gettimeofday(&end,0);
			double timeuse  = 1000000*(end.tv_sec - start.tv_sec) + end.tv_usec - start.tv_usec;
			if(timeuse>FIN_TIMEOUT/1000)
			{
				printf("FIN time out\n");
				break;	//time out
			}
			if(dscnn_tcb->state==CLOSED) //closed
			{
				printf("disconnection has done\n");
				return 1;
			}
		}
	}
	perror("error: disconnection\n");
	dscnn_tcb->state=CLOSED;
	return -1;
}

// 这个函数调用free()释放TCB条目. 它将该条目标记为NULL, 成功时(即位于正确的状态)返回1,
// 失败时(即位于错误的状态)返回-1.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
int stcp_client_close(int sockfd)
{
	client_tcb_t* close_tcb=client_tcb_table[sockfd];
	if(close_tcb == NULL) return -1;
	int close_state = close_tcb->state;
	if(close_state!=CLOSED){
		printf("close error!\n");
		return -1;
	}
	pthread_mutex_destroy(close_tcb->bufMutex);
	free(close_tcb->bufMutex);
	free(client_tcb_table[sockfd]);
	client_tcb_table[sockfd]=NULL;
	return 1;
}

// 这是由stcp_client_init()启动的线程. 它处理所有来自服务器的进入段. 
// seghandler被设计为一个调用sip_recvseg()的无穷循环. 如果sip_recvseg()失败, 则说明重叠网络连接已关闭,
// 线程将终止. 根据STCP段到达时连接所处的状态, 可以采取不同的动作. 请查看客户端FSM以了解更多细节.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
void *seghandler(void* arg)
{
	int rslt;
	seg_t* seg=(seg_t *)malloc(sizeof(seg_t));
	while(1)
	{
		memset(seg, 0, sizeof(seg_t));
		rslt=sip_recvseg(connection, seg);
		if(rslt == 2) {
			printf("Receive seg lost!\n");
			continue;
		}
		if(checkchecksum(seg)==-1) {
			printf("Receive seg Checksum error\n");
			continue;
		}
		if(rslt==0) {
			int i = 0;
			for(i=0;i<MAX_TRANSPORT_CONNECTIONS;i++)
			{
				if(client_tcb_table[i]!=NULL){
					if(client_tcb_table[i]->client_portNum == seg->header.dest_port){
						//printf("======= i = %d ========\n", i);
						break;
					}
				}
			}
			if(i == MAX_TRANSPORT_CONNECTIONS) {
				printf("TCB ERROR!\n");
			}
			client_tcb_t* recv_tcb = client_tcb_table[i];
			switch(client_tcb_table[i]-> state)	{
				case CLOSED: {
					printf("State: CLOSED\n");
					printf("---------------------------------\n");
				} break;
									
				case SYNSENT: {
					printf("State: SYNSENT\n");
					printf("---------------------------------\n");
					if(seg->header.type==SYNACK){
						printf("recv SYNACK from server_port: %d\n", seg->header.src_port);
						client_tcb_table[i]->state=CONNECTED;
					}
				} break;
				case CONNECTED: 
					if(seg->header.type == DATAACK)
					{	
						printf("State: CONNECTED -> DATAACK\n");
						pthread_mutex_lock(client_tcb_table[i]->bufMutex);
						segBuf_t *phead = recv_tcb->sendBufHead;
						int ack = seg->header.ack_num;
						printf("ACK: %d\n", ack);
						// <=ack 的都被确认为收到，不需要再发送
						if(recv_tcb->sendBufHead!= NULL) {
							int n = ack - recv_tcb->sendBufHead->seg.header.seq_num;
							while(n > 0) {
								segBuf_t *phead = recv_tcb->sendBufHead;
								recv_tcb->sendBufHead = phead->next;
								n -= phead->seg.header.length;
								free(phead);
								recv_tcb->unAck_segNum--;
								if(recv_tcb->sendBufunSent != NULL) {
									printf("send data to server, seq: %d\n", recv_tcb->sendBufunSent->seg.header.seq_num);
									sip_sendseg(connection, &(recv_tcb->sendBufunSent->seg));
									recv_tcb->sendBufunSent->sentTime = (unsigned)clock();
									recv_tcb->sendBufunSent= recv_tcb->sendBufunSent->next;
									recv_tcb->unAck_segNum++;
								}
							}
						}
						pthread_mutex_unlock(client_tcb_table[i]->bufMutex);
					}
                break;
					
				case FINWAIT: {
					if(seg->header.type == FINACK)
					{
						printf("recv FINACK from server_port: %d\n", seg->header.src_port);
						client_tcb_table[i]->state = CLOSED;
					}
				} break;
            	default: break;
			}
		}
		
		else if(rslt < 0){
			printf("Recv Error!\n");
			break;
		}
   }
   return 0;
}

// 这个线程持续轮询发送缓冲区以触发超时事件. 如果发送缓冲区非空, 它应一直运行.
// 如果(当前时间 - 第一个已发送但未被确认段的发送时间) > DATA_TIMEOUT, 就发生一次超时事件.
// 当超时事件发生时, 重新发送所有已发送但未被确认段. 当发送缓冲区为空时, 这个线程将终止.
//
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//
void* sendBuf_timer(void* clienttcb)
{
	client_tcb_t *tcb = (client_tcb_t *)clienttcb;
	if(tcb == NULL) {
		return;
	}
	//int tu = 0;
    while(1)
    {	
		//printf("\n------check time out segs at %d!!!------\n", ++tu);
		pthread_mutex_lock(tcb->bufMutex);
        if(tcb->sendBufHead == NULL) {
			pthread_mutex_unlock(tcb->bufMutex);
			continue;
		}
        if(tcb->sendBufunSent == tcb->sendBufHead) {
			pthread_mutex_unlock(tcb->bufMutex);
			continue;
		}
		unsigned int now  = (unsigned int)clock();
        if(now - tcb->sendBufHead->sentTime > DATA_TIMEOUT / 1000000) {
			printf("Time out and resend\n");
			segBuf_t* bufHead = tcb->sendBufHead;
			while(bufHead != tcb->sendBufunSent) {
				 printf("Resend data to server port:%d, seq:%d\n", bufHead->seg.header.dest_port, bufHead->seg.header.seq_num);
				sip_sendseg(connection, &(bufHead->seg));
				bufHead->sentTime = (unsigned int) clock();
				bufHead = bufHead->next;
			}
		}
		pthread_mutex_unlock(tcb->bufMutex);
        usleep(SENDBUF_POLLING_INTERVAL/1000);
	}
}
