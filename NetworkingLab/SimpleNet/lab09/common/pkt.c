// 文件名: common/pkt.c
// 创建日期: 2015年

#include "pkt.h"
#include <sys/socket.h>
#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define SEGSTART1 0
#define SEGSTART2 1
#define SEGRECV 2
#define SEGSTOP1 3


// son_sendpkt()由SIP进程调用, 其作用是要求SON进程将报文发送到重叠网络中. SON进程和SIP进程通过一个本地TCP连接互连.
// 在son_sendpkt()中, 报文及其下一跳的节点ID被封装进数据结构sendpkt_arg_t, 并通过TCP连接发送给SON进程. 
// 参数son_conn是SIP进程和SON进程之间的TCP连接套接字描述符.
// 当通过SIP进程和SON进程之间的TCP连接发送数据结构sendpkt_arg_t时, 使用'!&'和'!#'作为分隔符, 按照'!& sendpkt_arg_t结构 !#'的顺序发送.
// 如果发送成功, 返回1, 否则返回-1.
int son_sendpkt(int nextNodeID, sip_pkt_t* pkt, int son_conn)
{
	char start[2] = "!&";
	char end[2] = "!#";
	int s1 = send(son_conn, start, sizeof(start), 0);
	if(s1<0) {
		perror("Send start in son_send error!\n");
		return -1;
	}
	sendpkt_arg_t* pkt_arg= (sendpkt_arg_t*)malloc(sizeof(sendpkt_arg_t));
	pkt_arg->nextNodeID = nextNodeID;
	int length = sizeof(sip_hdr_t)+pkt->header.length;
	memcpy(&(pkt_arg->pkt), pkt, length);
	int s2 = send(son_conn, pkt_arg, length+4, 0);
	if(s2<0) {
		perror("Send seg error!\n");
		return -1;
	}
	//printf("send seq :%d ,ack :%d\n", segPtr->header.seq_num, segPtr->header.ack_num);
	int s3 = send(son_conn, end, sizeof(end), 0);
	if(s3<0) {
		perror("Send end error!\n");
		return -1;
	}
	return 0;
}

// son_recvpkt()函数由SIP进程调用, 其作用是接收来自SON进程的报文. 
// 参数son_conn是SIP进程和SON进程之间TCP连接的套接字描述符. 报文通过SIP进程和SON进程之间的TCP连接发送, 使用分隔符!&和!#. 
// 为了接收报文, 这个函数使用一个简单的有限状态机FSM
// PKTSTART1 -- 起点 
// PKTSTART2 -- 接收到'!', 期待'&' 
// PKTRECV -- 接收到'&', 开始接收数据
// PKTSTOP1 -- 接收到'!', 期待'#'以结束数据的接收 
// 如果成功接收报文, 返回1, 否则返回-1.
int son_recvpkt(sip_pkt_t* pkt, int son_conn)
{
	char recv_buf;
	int state = PKTSTART1;
	while(recv(son_conn, &recv_buf, 1, 0) >0) {
		switch(state) {
			case PKTSTART1: {
				if(recv_buf == '!')
					state = PKTSTART2;
			} break;
			case PKTSTART2: {
				if(recv_buf == '&') {
					int n = 0;
					//Attention the type!
					if((n = recv(son_conn, (char *)&pkt->header, sizeof(sip_hdr_t),0))>0) {
						unsigned int len = pkt->header.length;
						printf("length: %d\n", len);
						char *buf  = (char *)malloc(len+2);//leave for "!#"
						memset(buf, 0, len+2);
						if((n = recv(son_conn, buf, len+2, 0))>0) {
							if(buf[len] == '!' && buf[len+1] == '#') {
								memcpy(pkt->data, buf, len);
								return 1;
							}
						}
						else {
							perror("Receive error\n");
							return -1;
						}
					}
					else {
							perror("Receive error\n");
							return -1;
						}
				}
				else
					state = PKTSTART1; //rollback
			} break;
		}
	}
 	return -1;
}

// 这个函数由SON进程调用, 其作用是接收数据结构sendpkt_arg_t.
// 报文和下一跳的节点ID被封装进sendpkt_arg_t结构.
// 参数sip_conn是在SIP进程和SON进程之间的TCP连接的套接字描述符. 
// sendpkt_arg_t结构通过SIP进程和SON进程之间的TCP连接发送, 使用分隔符!&和!#. 
// 为了接收报文, 这个函数使用一个简单的有限状态机FSM
// PKTSTART1 -- 起点 
// PKTSTART2 -- 接收到'!', 期待'&' 
// PKTRECV -- 接收到'&', 开始接收数据
// PKTSTOP1 -- 接收到'!', 期待'#'以结束数据的接收
// 如果成功接收sendpkt_arg_t结构, 返回1, 否则返回-1.
int getpktToSend(sip_pkt_t* pkt, int* nextNode,int sip_conn)
{
	char recv_buf;
	int state = PKTSTART1;
	while(recv(sip_conn, &recv_buf, 1, 0) >0) {
		switch(state) {
			case PKTSTART1: {
				if(recv_buf == '!')
					state = PKTSTART2;
			} break;
			case PKTSTART2: {
				if(recv_buf == '&') {
					int n = 0;
					//Attention the type!
					if((n = recv(sip_conn, (char *)nextNode, sizeof(int),0)) >0) {
						printf("nextNode: %d\n", *nextNode);
					}
					if((n = recv(sip_conn, (char *)&pkt->header, sizeof(sip_hdr_t),0))>0) {
						unsigned int len = pkt->header.length;
						printf("length: %d\n", len);
						char *buf  = (char *)malloc(len+2);//leave for "!#"
						memset(buf, 0, len+2);
						if((n = recv(sip_conn, buf, len+2, 0))>0) {
							if(buf[len] == '!' && buf[len+1] == '#') {
								memcpy(pkt->data, buf, len);
								return 1;
							}
						}
						else {
							perror("Receive error1\n");
							return -1;
						}
					}
					else {
							perror("Receive error2\n");
							return -1;
						}
				}
				else
					state = PKTSTART1; //rollback
			} break;
		}
	}
 	return -1;
}

// forwardpktToSIP()函数是在SON进程接收到来自重叠网络中其邻居的报文后被调用的. 
// SON进程调用这个函数将报文转发给SIP进程. 
// 参数sip_conn是SIP进程和SON进程之间的TCP连接的套接字描述符. 
// 报文通过SIP进程和SON进程之间的TCP连接发送, 使用分隔符!&和!#, 按照'!& 报文 !#'的顺序发送. 
// 如果报文发送成功, 返回1, 否则返回-1.
int forwardpktToSIP(sip_pkt_t* pkt, int sip_conn)
{
	char start[2]="!&";
	char end[2] = "!#";
	int s1 = send(sip_conn, start, sizeof(start), 0);
	if(s1<0) {
		perror("Send start in forward error!\n");
		return -1;
	}
	int length = pkt->header.length+sizeof(sip_hdr_t);
	int s2 = send(sip_conn, pkt, length, 0);
	if(s2<0) {
		perror("Send pkt error!\n");
		return -1;
	}
	int s3=send(sip_conn, end, sizeof(end), 0);
	if(s3<0) {
		perror("Send end error!\n");
		return -1;
	}
	printf("send to SIP, length: %d\n", length);
	return 1;
}

// sendpkt()函数由SON进程调用, 其作用是将接收自SIP进程的报文发送给下一跳.
// 参数conn是到下一跳节点的TCP连接的套接字描述符.
// 报文通过SON进程和其邻居节点之间的TCP连接发送, 使用分隔符!&和!#, 按照'!& 报文 !#'的顺序发送. 
// 如果报文发送成功, 返回1, 否则返回-1.
int sendpkt(sip_pkt_t* pkt, int conn)
{
 	char start[2]="!&";
	char end[2] = "!#";
	int s1 = send(conn, start, sizeof(start), 0);
	if(s1<0) {
		perror("Send start in sendpkt error!\n");
		return -1;
	}
	int length = pkt->header.length+sizeof(sip_hdr_t);
	int s2 = send(conn, pkt, length, 0);
	if(s2<0) {
		perror("Send pkt error!\n");
		return -1;
	}
	int s3=send(conn, end, sizeof(end), 0);
	if(s3<0) {
		perror("Send end error!\n");
		return -1;
	}
	printf("sendpkt, length: %d\n", length);
	return 1;
}

// recvpkt()函数由SON进程调用, 其作用是接收来自重叠网络中其邻居的报文.
// 参数conn是到其邻居的TCP连接的套接字描述符.
// 报文通过SON进程和其邻居之间的TCP连接发送, 使用分隔符!&和!#. 
// 为了接收报文, 这个函数使用一个简单的有限状态机FSM
// PKTSTART1 -- 起点 
// PKTSTART2 -- 接收到'!', 期待'&' 
// PKTRECV -- 接收到'&', 开始接收数据
// PKTSTOP1 -- 接收到'!', 期待'#'以结束数据的接收 
// 如果成功接收报文, 返回1, 否则返回-1.
int recvpkt(sip_pkt_t* pkt, int conn)
{
	return son_recvpkt(pkt,conn);
}
