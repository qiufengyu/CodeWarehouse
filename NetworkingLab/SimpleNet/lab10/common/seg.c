
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <linux/random.h>
#include "seg.h"

void strncopy_seg(char *dest, char *src, int n)
{
    int i;
    for(i = 0; i < n; i ++)
    {
        dest[i] = src[i];
    }
}

//STCP进程使用这个函数发送sendseg_arg_t结构(包含段及其目的节点ID)给SIP进程.
//参数sip_conn是在STCP进程和SIP进程之间连接的TCP描述符.
//如果sendseg_arg_t发送成功,就返回1,否则返回-1.
int sip_sendseg(int sip_conn, int dest_nodeID, seg_t* segPtr)
{
	segPtr->header.checksum = 0;
    segPtr->header.checksum = checksum(segPtr);
    sendseg_arg_t *sendseg = (sendseg_arg_t *)malloc(sizeof(sendseg_arg_t));
    sendseg->nodeID = dest_nodeID;
    strncopy_seg((char *)&(sendseg->seg), (char *)segPtr, sizeof(stcp_hdr_t) + segPtr->header.length);
    sendseg->seg.data[segPtr->header.length] = '!';
    sendseg->seg.data[segPtr->header.length + 1] = '#';
    send(sip_conn,"!&",2,0);
    send(sip_conn, sendseg, 4 + sizeof(int) + sizeof(stcp_hdr_t) + segPtr->header.length + 2, 0);
    free(sendseg);
    return 1;
}

//STCP进程使用这个函数来接收来自SIP进程的包含段及其源节点ID的sendseg_arg_t结构.
//参数sip_conn是STCP进程和SIP进程之间连接的TCP描述符.
//当接收到段时, 使用seglost()来判断该段是否应被丢弃并检查校验和.
//如果成功接收到sendseg_arg_t就返回1, 否则返回-1.
int sip_recvseg(int sip_conn, int* src_nodeID, seg_t* segPtr)
{
	char recv_buf;
    int n;
    int state = SEGSTART1;
    while((n = recv(sip_conn, &recv_buf, 1, 0))>0)
    {
        if(recv_buf == '!' && state == SEGSTART1)
        {
            state = SEGSTART2;
        }
        else if(recv_buf == '&' && state == SEGSTART2)
        {
            if((n=recv(sip_conn,(char *)src_nodeID,sizeof(int), 0))<=0)
            {
                printf("5.0 recv error\n");
                return -1;
            }
            if((n = recv(sip_conn,(char *)&segPtr->header,sizeof(stcp_hdr_t), 0)) > 0)
            {
                int len = segPtr->header.length;
                char *buf = (char *)malloc(len+2);
                memset(buf, 0, len+2);
                if((n = recv(sip_conn, buf, len+2, 0))>0)
                {
                    if(buf[len] == '!' && buf[len+1] == '#')
                    {
                        strncopy_seg(segPtr->data,buf,len);
                        if(seglost(segPtr) == 0)
                        {
                            if(checkchecksum(segPtr) == 1)
                            {
                                return 1;
                            }
                            else
                            {
                                printf("checksum error\n");
                                return 2;
                            }
                        }
                        else
                        {
                            printf("segment be chosen to lost\n");
                            return 3;
                        }
                    }
                    else
                    {
                        printf("1.0 error in recv \n");
                        return -1;
                    }
                }
                else
                {
                    printf("2.0 error in recv\n");
                    return -1;
                }
            }
            else
            {
                printf("3.0 error in recv \n");
                return -1;
            }
        }
        else
        {
            state = SEGSTART1;
        }
    }
    return -1;
}

//SIP进程使用这个函数接收来自STCP进程的包含段及其目的节点ID的sendseg_arg_t结构.
//参数stcp_conn是在STCP进程和SIP进程之间连接的TCP描述符.
//如果成功接收到sendseg_arg_t就返回1, 否则返回-1.
int getsegToSend(int stcp_conn, int* dest_nodeID, seg_t* segPtr)
{
	char recv_buf;
    int n;
    int state = SEGSTART1;
    while((n = recv(stcp_conn, &recv_buf, 1, 0))>0)
    {
        if(recv_buf == '!' && state == SEGSTART1)
        {
            state = SEGSTART2;
        }
        else if(recv_buf == '&' && state == SEGSTART2)
        {
            if((n=recv(stcp_conn,(char *)dest_nodeID,sizeof(int), 0))<=0)
            {
                printf("5.0 recv error\n");
                return -1;
            }
            if((n = recv(stcp_conn,(char *)&segPtr->header,sizeof(stcp_hdr_t), 0)) > 0)
            {
                int len = segPtr->header.length;
                char *buf = (char *)malloc(len+2);
                memset(buf,0,len+2);
                if((n = recv(stcp_conn,buf,len+2, 0))>0)
                {
                    if(buf[len] == '!' && buf[len+1] == '#')
                    {
                        strncopy_seg(segPtr->data,buf,len);
                        return 1;
                    }
                    else
                    {
                        printf("1.0 error in recv \n");
                        return 2;
                    }
                }
                else
                {
                    printf("2.0 error in recv\n");
                    return 3;
                }
            }
            else
            {
                printf("3.0 error in recv \n");
                return 4;
            }
        }
        else
        {
            state = SEGSTART1;
        }
    }
    return -1;
}

//SIP进程使用这个函数发送包含段及其源节点ID的sendseg_arg_t结构给STCP进程.
//参数stcp_conn是STCP进程和SIP进程之间连接的TCP描述符.
//如果sendseg_arg_t被成功发送就返回1, 否则返回-1.
int forwardsegToSTCP(int stcp_conn, int src_nodeID, seg_t* segPtr)
{
	char start[2] = "!&";
	int s1 = send(stcp_conn, start, sizeof(start), 0);
	if(s1<0) {
		perror("Send start error!\n");
		return -1;
	}
	sendseg_arg_t *sendseg = (sendseg_arg_t *)malloc(sizeof(sendseg_arg_t));
    sendseg->nodeID = src_nodeID;
    strncopy_seg((char *)&(sendseg->seg), (char *)segPtr, sizeof(stcp_hdr_t) + segPtr->header.length);
    sendseg->seg.data[segPtr->header.length] = '!';
    sendseg->seg.data[segPtr->header.length + 1] = '#';
    send(stcp_conn, sendseg, 4 + sizeof(int) + sizeof(stcp_hdr_t) + segPtr->header.length + 2, 0);
    free(sendseg);
    return 1;
}

// 一个段有PKT_LOST_RATE/2的可能性丢失, 或PKT_LOST_RATE/2的可能性有着错误的校验和.
// 如果数据包丢失了, 就返回1, 否则返回0. 
// 即使段没有丢失, 它也有PKT_LOST_RATE/2的可能性有着错误的校验和.
// 我们在段中反转一个随机比特来创建错误的校验和.
int seglost(seg_t* segPtr)
{
	int random = rand()%100;
	if(random<PKT_LOSS_RATE*100) {
		//50%可能性丢失段
		if(rand()%2==0) {
			printf("seg lost!!!\n");
			return 1;
		}
		//50%可能性是错误的校验和
		else {
			//获取数据长度
			int len = sizeof(stcp_hdr_t)+segPtr->header.length;
			//获取要反转的随机位
			int errorbit = rand()%(len*8);
			//反转该比特
			char* temp = (char*)segPtr;
			temp = temp + errorbit/8;
			*temp = *temp^(1<<(errorbit%8));
			return 0;
		}
	}
	return 0;
}

//这个函数计算指定段的校验和.
//校验和覆盖段首部和段数据. 你应该首先将段首部中的校验和字段清零, 
//如果数据长度为奇数, 添加一个全零的字节来计算校验和.
//校验和计算使用1的补码.
unsigned short checksum(seg_t* segment)
{
	int length = sizeof(stcp_hdr_t)+segment->header.length;
	unsigned long sum = 0;
	unsigned short *p = (unsigned short *)segment;
	//累加
	while(length>1) {
		sum += *p++;
		length -= 2;
	}
	//长度为奇数
	if(length == 1) {
		sum += *(unsigned char*)p;
	}
	//低16位+高16位，直到高位为0
	while(sum >> 16) {
		sum = (sum >>16) + (sum & 0xFFFF);
	}
	//if overflow
	sum += (sum>>16);
	return (unsigned short)(~sum);
}

//这个函数检查段中的校验和, 正确时返回1, 错误时返回-1
int checkchecksum(seg_t* segment)
{
	//校验和填入并不会影响校验和计算，如此处理即可
	if(checksum(segment) == 0) {
		return 1;
	}
	else
		return -1;
}

