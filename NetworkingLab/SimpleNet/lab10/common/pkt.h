#ifndef PKT_H
#define PKT_H

#include "constants.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>

//±¨ÎÄÀàÐÍ¶¨Òå, ÓÃÓÚ±¨ÎÄÊ×²¿ÖÐµÄtype×Ö¶Î
#define	ROUTE_UPDATE 1
#define SIP 2	
#define ROUTE_UPDATE_1 3
#define ROUTE_UPDATE_2 4
//recv×´Ì¬»ú×´Ì¬
#define PKTSTART1	0
#define PKTSTART2	1

//SIP±¨ÎÄ¸ñÊ½¶¨Òå
typedef struct sipheader {
  int src_nodeID;		          //Ô´½ÚµãID
  int dest_nodeID;		          //Ä¿±ê½ÚµãID
  unsigned short int length;	  //±¨ÎÄÖÐÊý¾ÝµÄ³¤¶È
  unsigned short int type;	      //±¨ÎÄÀàÐÍ 
} sip_hdr_t;

typedef struct packet {
  sip_hdr_t header;
  char data[MAX_PKT_LEN];
} sip_pkt_t;

//Â·ÓÉ¸üÐÂ±¨ÎÄ¶¨Òå
//¶ÔÓÚÂ·ÓÉ¸üÐÂ±¨ÎÄÀ´Ëµ, Â·ÓÉ¸üÐÂÐÅÏ¢´æ´¢ÔÚ±¨ÎÄµÄdata×Ö¶ÎÖÐ

//Ò»ÌõÂ·ÓÉ¸üÐÂÌõÄ¿
typedef struct routeupdate_entry {
        unsigned int nodeID;	//Ä¿±ê½ÚµãID
        unsigned int cost;	    //´ÓÔ´½Úµã(±¨ÎÄÊ×²¿ÖÐµÄsrc_nodeID)µ½Ä¿±ê½ÚµãµÄÁ´Â·´ú¼Û
} routeupdate_entry_t;

//Â·ÓÉ¸üÐÂ±¨ÎÄ¸ñÊ½
typedef struct pktrt{
        unsigned int entryNum;	//Õâ¸öÂ·ÓÉ¸üÐÂ±¨ÎÄÖÐ°üº¬µÄÌõÄ¿Êý
        routeupdate_entry_t entry[MAX_NODE_NUM];
} pkt_routeupdate_t;

//用于修改路由信息
typedef struct pktrc {
	unsigned int nodeID;
	int haveReceivedNodeNum;
	int haveReceivedNode[MAX_NODE_NUM];
} route_change_t;
// Êý¾Ý½á¹¹sendpkt_arg_tÓÃÔÚº¯Êýson_sendpkt()ÖÐ. 
// son_sendpkt()ÓÉSIP½ø³Ìµ÷ÓÃ, Æä×÷ÓÃÊÇÒªÇóSON½ø³Ì½«±¨ÎÄ·¢ËÍµ½ÖØµþÍøÂçÖÐ. 
// 
// SON½ø³ÌºÍSIP½ø³ÌÍ¨¹ýÒ»¸ö±¾µØTCPÁ¬½Ó»¥Á¬, ÔÚson_sendpkt()ÖÐ, SIP½ø³ÌÍ¨¹ý¸ÃTCPÁ¬½Ó½«Õâ¸öÊý¾Ý½á¹¹·¢ËÍ¸øSON½ø³Ì. 
// SON½ø³ÌÍ¨¹ýµ÷ÓÃgetpktToSend()½ÓÊÕÕâ¸öÊý¾Ý½á¹¹. È»ºóSON½ø³Ìµ÷ÓÃsendpkt()½«±¨ÎÄ·¢ËÍ¸øÏÂÒ»Ìø.
typedef struct sendpktargument {
  char flag_head[4];
  int nextNodeID;        //ÏÂÒ»ÌøµÄ½ÚµãID
  sip_pkt_t pkt;         //Òª·¢ËÍµÄ±¨ÎÄ
} sendpkt_arg_t;

// son_sendpkt()ÓÉSIP½ø³Ìµ÷ÓÃ, Æä×÷ÓÃÊÇÒªÇóSON½ø³Ì½«±¨ÎÄ·¢ËÍµ½ÖØµþÍøÂçÖÐ. SON½ø³ÌºÍSIP½ø³ÌÍ¨¹ýÒ»¸ö±¾µØTCPÁ¬½Ó»¥Á¬.
// ÔÚson_sendpkt()ÖÐ, ±¨ÎÄ¼°ÆäÏÂÒ»ÌøµÄ½ÚµãID±»·â×°½øÊý¾Ý½á¹¹sendpkt_arg_t, ²¢Í¨¹ýTCPÁ¬½Ó·¢ËÍ¸øSON½ø³Ì. 
// ²ÎÊýson_connÊÇSIP½ø³ÌºÍSON½ø³ÌÖ®¼äµÄTCPÁ¬½ÓÌ×½Ó×ÖÃèÊö·û.
// µ±Í¨¹ýSIP½ø³ÌºÍSON½ø³ÌÖ®¼äµÄTCPÁ¬½Ó·¢ËÍÊý¾Ý½á¹¹sendpkt_arg_tÊ±, Ê¹ÓÃ'!&'ºÍ'!#'×÷Îª·Ö¸ô·û, °´ÕÕ'!& sendpkt_arg_t½á¹¹ !#'µÄË³Ðò·¢ËÍ.
// Èç¹û·¢ËÍ³É¹¦, ·µ»Ø1, ·ñÔò·µ»Ø-1.
int son_sendpkt(int nextNodeID, sip_pkt_t* pkt, int son_conn);

// son_recvpkt()º¯ÊýÓÉSIP½ø³Ìµ÷ÓÃ, Æä×÷ÓÃÊÇ½ÓÊÕÀ´×ÔSON½ø³ÌµÄ±¨ÎÄ. 
// ²ÎÊýson_connÊÇSIP½ø³ÌºÍSON½ø³ÌÖ®¼äTCPÁ¬½ÓµÄÌ×½Ó×ÖÃèÊö·û. ±¨ÎÄÍ¨¹ýSIP½ø³ÌºÍSON½ø³ÌÖ®¼äµÄTCPÁ¬½Ó·¢ËÍ, Ê¹ÓÃ·Ö¸ô·û!&ºÍ!#. 
// ÎªÁË½ÓÊÕ±¨ÎÄ, Õâ¸öº¯ÊýÊ¹ÓÃÒ»¸ö¼òµ¥µÄÓÐÏÞ×´Ì¬»úFSM
// PKTSTART1 -- Æðµã 
// PKTSTART2 -- ½ÓÊÕµ½'!', ÆÚ´ý'&' 
// PKTRECV -- ½ÓÊÕµ½'&', ¿ªÊ¼½ÓÊÕÊý¾Ý
// PKTSTOP1 -- ½ÓÊÕµ½'!', ÆÚ´ý'#'ÒÔ½áÊøÊý¾ÝµÄ½ÓÊÕ 
// Èç¹û³É¹¦½ÓÊÕ±¨ÎÄ, ·µ»Ø1, ·ñÔò·µ»Ø-1.
int son_recvpkt(sip_pkt_t* pkt, int son_conn);

// Õâ¸öº¯ÊýÓÉSON½ø³Ìµ÷ÓÃ, Æä×÷ÓÃÊÇ½ÓÊÕÊý¾Ý½á¹¹sendpkt_arg_t.
// ±¨ÎÄºÍÏÂÒ»ÌøµÄ½ÚµãID±»·â×°½øsendpkt_arg_t½á¹¹.
// ²ÎÊýsip_connÊÇÔÚSIP½ø³ÌºÍSON½ø³ÌÖ®¼äµÄTCPÁ¬½ÓµÄÌ×½Ó×ÖÃèÊö·û. 
// sendpkt_arg_t½á¹¹Í¨¹ýSIP½ø³ÌºÍSON½ø³ÌÖ®¼äµÄTCPÁ¬½Ó·¢ËÍ, Ê¹ÓÃ·Ö¸ô·û!&ºÍ!#. 
// ÎªÁË½ÓÊÕ±¨ÎÄ, Õâ¸öº¯ÊýÊ¹ÓÃÒ»¸ö¼òµ¥µÄÓÐÏÞ×´Ì¬»úFSM
// PKTSTART1 -- Æðµã 
// PKTSTART2 -- ½ÓÊÕµ½'!', ÆÚ´ý'&' 
// PKTRECV -- ½ÓÊÕµ½'&', ¿ªÊ¼½ÓÊÕÊý¾Ý
// PKTSTOP1 -- ½ÓÊÕµ½'!', ÆÚ´ý'#'ÒÔ½áÊøÊý¾ÝµÄ½ÓÊÕ
// Èç¹û³É¹¦½ÓÊÕsendpkt_arg_t½á¹¹, ·µ»Ø1, ·ñÔò·µ»Ø-1.
int getpktToSend(sip_pkt_t* pkt, int* nextNode,int sip_conn);

// forwardpktToSIP()º¯ÊýÊÇÔÚSON½ø³Ì½ÓÊÕµ½À´×ÔÖØµþÍøÂçÖÐÆäÁÚ¾ÓµÄ±¨ÎÄºó±»µ÷ÓÃµÄ. 
// SON½ø³Ìµ÷ÓÃÕâ¸öº¯Êý½«±¨ÎÄ×ª·¢¸øSIP½ø³Ì. 
// ²ÎÊýsip_connÊÇSIP½ø³ÌºÍSON½ø³ÌÖ®¼äµÄTCPÁ¬½ÓµÄÌ×½Ó×ÖÃèÊö·û. 
// ±¨ÎÄÍ¨¹ýSIP½ø³ÌºÍSON½ø³ÌÖ®¼äµÄTCPÁ¬½Ó·¢ËÍ, Ê¹ÓÃ·Ö¸ô·û!&ºÍ!#, °´ÕÕ'!& ±¨ÎÄ !#'µÄË³Ðò·¢ËÍ. 
// Èç¹û±¨ÎÄ·¢ËÍ³É¹¦, ·µ»Ø1, ·ñÔò·µ»Ø-1.
int forwardpktToSIP(sip_pkt_t* pkt, int sip_conn);

// sendpkt()º¯ÊýÓÉSON½ø³Ìµ÷ÓÃ, Æä×÷ÓÃÊÇ½«½ÓÊÕ×ÔSIP½ø³ÌµÄ±¨ÎÄ·¢ËÍ¸øÏÂÒ»Ìø.
// ²ÎÊýconnÊÇµ½ÏÂÒ»Ìø½ÚµãµÄTCPÁ¬½ÓµÄÌ×½Ó×ÖÃèÊö·û.
// ±¨ÎÄÍ¨¹ýSON½ø³ÌºÍÆäÁÚ¾Ó½ÚµãÖ®¼äµÄTCPÁ¬½Ó·¢ËÍ, Ê¹ÓÃ·Ö¸ô·û!&ºÍ!#, °´ÕÕ'!& ±¨ÎÄ !#'µÄË³Ðò·¢ËÍ. 
// Èç¹û±¨ÎÄ·¢ËÍ³É¹¦, ·µ»Ø1, ·ñÔò·µ»Ø-1.
int sendpkt(sip_pkt_t* pkt, int conn);

// recvpkt()º¯ÊýÓÉSON½ø³Ìµ÷ÓÃ, Æä×÷ÓÃÊÇ½ÓÊÕÀ´×ÔÖØµþÍøÂçÖÐÆäÁÚ¾ÓµÄ±¨ÎÄ.
// ²ÎÊýconnÊÇµ½ÆäÁÚ¾ÓµÄTCPÁ¬½ÓµÄÌ×½Ó×ÖÃèÊö·û.
// ±¨ÎÄÍ¨¹ýSON½ø³ÌºÍÆäÁÚ¾ÓÖ®¼äµÄTCPÁ¬½Ó·¢ËÍ, Ê¹ÓÃ·Ö¸ô·û!&ºÍ!#. 
// ÎªÁË½ÓÊÕ±¨ÎÄ, Õâ¸öº¯ÊýÊ¹ÓÃÒ»¸ö¼òµ¥µÄÓÐÏÞ×´Ì¬»úFSM
// PKTSTART1 -- Æðµã 
// PKTSTART2 -- ½ÓÊÕµ½'!', ÆÚ´ý'&' 
// PKTRECV -- ½ÓÊÕµ½'&', ¿ªÊ¼½ÓÊÕÊý¾Ý
// PKTSTOP1 -- ½ÓÊÕµ½'!', ÆÚ´ý'#'ÒÔ½áÊøÊý¾ÝµÄ½ÓÊÕ 
// Èç¹û³É¹¦½ÓÊÕ±¨ÎÄ, ·µ»Ø1, ·ñÔò·µ»Ø-1.
int recvpkt(sip_pkt_t* pkt, int conn);

#endif
