#include "util.h"
#include "btdata.h"
#include <pthread.h>
void *connect_to_peer(void *p)
{
    printf("I will to connect to you\n");
    sleep(15);
    int k = (int)p;
    peer_t *mypeer = &peers_pool[k];
    printf("k is %d and status is %d and peers_pool[0] status is %d\n",k,mypeer->status,peers_pool[0].status);
    pthread_mutex_lock(&mypeer->sock_mutex);
    optimization(prepareForOptimaze(),0,99);
    
    if(mypeer->status == 2)           //这是为了确定是响应握手报文，还是主动连接别人
    {
        pthread_mutex_unlock(&mypeer->sock_mutex);
        return NULL;
    }
    mypeer->status = 1;
    pthread_mutex_unlock(&mypeer->sock_mutex);

    printf("\033[32m""I connect to somebody\n""\033[m");
    mypeer->sockfd = connect_to_host(mypeer->ip, mypeer->port);
    unsigned char *shkhdmsg;
    unsigned char *current;
    int msglen = 0;

    shkhdmsg = (char*)malloc((HANDSHAKE_LEN +1) * sizeof(char));
    current = shkhdmsg;

    /* 为了统一格式 */
    char pstrlen = strlen(BT_PROTOCOL);
    memcpy(current, (char*)&pstrlen, sizeof(char));
    current += sizeof(char);
    strncpy(current, BT_PROTOCOL, pstrlen);
    current += pstrlen;

    memset(current, 0, 8);
    current += 8;

    int i = 0;
    for(; i < 5; i ++)
    {
        int j = 0;
        int part = reverse_byte_orderi(g_infohash[i]);
        unsigned char *p = (unsigned char*)&part;
        for(; j < 4; j ++)
        {
            *current++ = p[j];
        }
    }

    for(i = 0; i < 20; i ++)
    {
        *current = g_my_id[i];
        current ++;
    }

    msglen = current - shkhdmsg;
    send(mypeer->sockfd, shkhdmsg, msglen, 0);
	optimization(prepareForOptimaze(),0,99);
    int n = recv(mypeer->sockfd,shkhdmsg,msglen,0);
    printf("I recv info from my peer\n");
    if(n <= 0)
    {
        printf("n is %d\n",n);
        goto END;
    }
    struct handshake_packet *my_packet = (struct handshake_packet *)shkhdmsg;
    if(my_packet->len != strlen(BT_PROTOCOL))
        goto END;
    if(strncmp(my_packet->name,BT_PROTOCOL,20) != 0)
        goto END;
    mypeer->status = 2;
    memcpy(mypeer->id,my_packet->peer_id,20);
    pthread_t thread;
      optimization(prepareForOptimaze(),0,99);
    int rc = pthread_create(&thread, NULL, recv_from_peer, (void *)k);
    if(rc)
    {
        printf("ERROR, return code from pthread_create() is %d\n", rc);
        exit(-1);
    }
    printf("\033[33m shake hands succeed\n \033[m");
    sendBitField(mypeer->sockfd);
    pthread_t thread_1;
    pthread_create(&thread_1, NULL, check_and_keepalive, (void*)k);
END:
    free(shkhdmsg);
    if(mypeer->status != 2)
        destroy_peer(k);
}
