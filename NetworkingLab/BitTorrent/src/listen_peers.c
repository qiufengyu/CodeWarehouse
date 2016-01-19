#include "util.h"
#include "btdata.h"
#include <errno.h>

extern int errno;


struct param_list{
    char *ip;
    int sockfd;
};

void *peer_deal(void *i)
{
    struct param_list *my_param = (struct param_list *)i;
    int sockfd = my_param->sockfd;
    int close_bit = 1;
    printf("Child created for dealing with client request\n");
    char *buf = (char *)malloc(HANDSHAKE_LEN);
    memset(buf,0,HANDSHAKE_LEN);
    int n;
    while(n=recv(sockfd,buf,HANDSHAKE_LEN,0) > 0)
    {
        struct handshake_packet *my_packet = (struct handshake_packet *)buf;
        if(my_packet->len != strlen(BT_PROTOCOL))
        {
            printf("len is not match\n");
            break;
        }
        if(strncmp(my_packet->name,BT_PROTOCOL,strlen(BT_PROTOCOL)) != 0)
        {
            printf("BT_PROTOCOL is not match\n");
            break;
        }
        int i = 0,flag = 1;
        unsigned char *buf_info = my_packet->info_hash;
        for(; i<5; i++)
        {
            int j = 0;
            int part = reverse_byte_orderi(g_infohash[i]);
            unsigned char *p = (unsigned char *)&part;
            for(; j<4; j++)
            {
                if(*buf_info != p[j])
                {
                    printf("buf_info is %x and p[j] is %x\n",*buf_info,p[j]);
                    flag = 0;
                    goto END;
                }
                buf_info ++;
            }
        }
END:
        if(flag != 1)
        {
            printf("\033[33m buf_info is not match\n \033[m");
            break;
        }
        printf("waiting client get info\n");
        //sleep(10);    //这里是为了等客户端向tracker服务器发送更新请求报文
        //这里需要上锁
        pthread_mutex_lock(&g_mutex);
        for(i=0; i<MAXPEERS; i++)
        {
            //printf("peers_pool[i].id is %s and my_packet->peer_id is %s\n",peers_pool[i].id,my_packet->peer_id);
            if(peers_pool[i].used == 1 && strncmp(peers_pool[i].ip,my_param->ip,strlen(my_param->ip)) == 0)
            {
                printf("find you\n");
                break;
            }
        }
        pthread_mutex_unlock(&g_mutex);
        if(i == MAXPEERS || peers_pool[i].sockfd > 0)
        {
            printf("not you\n");
            break;
        }
        printf("i is %d\n",i);
        pthread_mutex_lock(&peers_pool[i].sock_mutex);
        if(peers_pool[i].status == 1)
        {
            pthread_mutex_unlock(&peers_pool[i].sock_mutex);
            printf("I already some shake hand to peer\n");
            break;
        }
        peers_pool[i].status = 2;
        peers_pool[i].sockfd = sockfd;
        memcpy(peers_pool[i].id,my_packet->peer_id,20);
        printf("peers_pool 0 status is %d in listen_peers\n",peers_pool[0].status);
        pthread_mutex_unlock(&peers_pool[i].sock_mutex);
        //这里开始向对方返回握手信息
        printf("send handshake packet return \n");
        memcpy(my_packet->peer_id,g_my_id,20);
        send(sockfd,buf,HANDSHAKE_LEN,0);
        close_bit = 0;
        pthread_t thread;
        int rc = pthread_create(&thread, NULL, recv_from_peer, (void *)i);
        if(rc)
        {
            printf("Error, return code from pthread_create() is %d\n", rc);
            exit(-1);
        }
        printf("shake hands succeed\n");
        //sleep(10);           //等一会
        sendBitField(sockfd);
        pthread_t thread_1;
        pthread_create(&thread_1, NULL, check_and_keepalive, (void*)i);
        break;
    }
    if(n<0)
    {
        printf("%s\n",strerror(errno));
    }
    if(close_bit)
        close(sockfd);
    free(buf);
}

void *listen_peers(void *p)
{
    int listenfd = make_listen_port(g_peerport);
    while(1)
    {
        struct sockaddr_in cliaddr;
        int clilen = sizeof(cliaddr);
        int sockfd = accept(listenfd, (struct sockaddr*)&cliaddr, &clilen);
        struct param_list my_param;
        char *ip = (char*)malloc(17*sizeof(char));
        memset(ip,0,17);
        strcpy(ip, inet_ntoa(cliaddr.sin_addr));
        my_param.ip = ip;
        my_param.sockfd = sockfd;
        pthread_t thread;
        printf("I listen a peer\n");
        pthread_create(&thread,NULL,peer_deal,(void *)&my_param);
        /*
        printf("listen_peer accept sockfd : %d\n",sockfd);
        char *ip = (char*)malloc(17*sizeof(char));
        memset(ip,0,17);
        strcpy(ip, inet_ntoa(cliaddr.sin_addr));
        int port = cliaddr.sin_port;
        printf("\033[31m""I have listen %s:%d\n""\033[m",ip,port);
        int i = 0;
        for(; i < MAXPEERS; i ++)
        {
            peer_t *ptr = &peers_pool[i];
            if(ptr->used == 1 && strcmp(ptr->ip, ip) == 0)
            {
                printf("\033[31m""111\n""\033[m");
                pthread_mutex_lock(&ptr->sock_mutex);
                if(ptr->sockfd < 0)
                {
                    ptr->sockfd = sockfd;
                    pthread_mutex_unlock(&ptr->sock_mutex);
                    break;
                }
                pthread_mutex_unlock(&ptr->sock_mutex);
            }
        }
        if(i != MAXPEERS )
        {
            pthread_t thread;
            int rc = pthread_create(&thread, NULL, recv_from_peer, (void *)i);
            if(rc)
            {
                printf("Error, return code from pthread_create() is %d\n", rc);
                exit(-1);
            }
        }
        else
        {
            printf("listen_peers close %d\n",sockfd);
            close(sockfd);
            sockfd = -1;
        }
        free(ip);
        */
    }
}
