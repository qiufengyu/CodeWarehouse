#include "util.h"
#include "btdata.h"
#include <errno.h>

#include <sys/types.h>
#include <sys/socket.h>

#define BUFSIZE 1500

extern int errno;

/* readn - read exactly n bytes */
int readn( int fd, char *bp, size_t len)
{
    int cnt;
    int rc;

    cnt = len;
    while ( cnt > 0 )
    {
        rc = recv( fd, bp, cnt, 0 );
        if ( rc < 0 )				/* read error? */
        {
            if ( errno == EINTR )	/* interrupted? */
                continue;			/* restart the read */
            return -1;				/* return error */
        }
        if ( rc == 0 )				/* EOF? */
            return len - cnt;		/* return short count */
        bp += rc;
        cnt -= rc;
    }
    return len;
}

void sendshkhdmsg(int sockfd)
{
    printf("\033[34m""I will send shkhdmsg to somebody\n""\033[m");
    unsigned char *shkhdmsg1;
    unsigned char *current;
    int msglen = 0;
    shkhdmsg1 = (unsigned char* )malloc(HANDSHAKE_LEN);
    current = shkhdmsg1;
    int pstrlen = strlen(BT_PROTOCOL);
    memcpy(current, (unsigned char*)&pstrlen, sizeof(int));
    current += sizeof(int);
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

    msglen = current - shkhdmsg1;
    send(sockfd, shkhdmsg1, msglen, 0);
    free(shkhdmsg1);
    shkhdmsg1 = NULL;
    current = NULL;
}

void *recv_from_peer(void *p)
{
    int k =  (int)p;
    peer_t *my_peer = &peers_pool[k];
    if(peers_pool[k].used == 0)
    {
        perror("监听的peer不是我感兴趣的\n");
        exit(-1);
    }
    int sockfd = my_peer->sockfd;
    char *ip = my_peer->ip;
    int port = my_peer->port;
    int n;

    unsigned char *buffer;
    unsigned char *piecebuffer;
    piecebuffer = (unsigned char*)malloc(g_torrentmeta->piece_len);
    memset(piecebuffer, 0, g_torrentmeta->piece_len);

    while(1)
    {
        buffer = (unsigned char*)malloc(BUFSIZE);
        memset(buffer, 0, BUFSIZE);
        printf("\033[34m now I waiting recv(sockfd is %d) \033[m\n",sockfd);
        n = readn(sockfd, buffer, 4);
        if(n <= 0)
        {
            printf("recv length error\n");
            break;
        }
        int len = *(int*)buffer;
        len = ntohl(len);
        assert(len >= 0 && len <= SUB_PIECE_LEN+9);
        /*
        if(len == 19 && strcmp(buffer, BT_PROTOCOL) == 0){
            //握手报文
            memset(buffer, 0, BUFSIZE);
            n = recv(sockfd, buffer, 8, 0);
            if(n<=0)
                break;
            memset(buffer, 0, BUFSIZE);
            n = recv(sockfd, buffer, 20, 0);
            if(n<=0)
                break;
            int i = 0, flag = 1;
            unsigned char *buffer_temp = buffer;
            for(; i < 5; i ++){
                int j = 0;
                int part = reverse_byte_orderi(g_infohash[i]);
                unsigned char *p = (unsigned char*)&part;
                for(; j < 4; j ++){
                    if(*buffer != p[j]){
                        flag = 0;
                        break;
                    }
                    buffer ++;
                }
            }
            buffer = buffer_temp;
            if(flag == 1){
                memset(buffer, 0, BUFSIZE);
                n = recv(sockfd, buffer, 20, 0);
                if(n<=0)
                    break;
                strncpy(my_peer->id, buffer, 20);
                if(my_peer->status != 2){
                    if(my_peer->status == 0){
                        sendshkhdmsg(my_peer->sockfd);
                    }
                    my_peer->status = 2;
                }
                printf("shake hands succeed\n");
                sendBitField(my_peer->sockfd);
                pthread_t thread;
                pthread_create(&thread, NULL, check_and_keepalive, (void*)k);
            }
            else
            {
                perror("torrent file dismatched\n");
                exit(-1);
            }
        }*/
        if(len == 0)
        {
            //keepalive
            printf("Now I recv keepalive pack from %s:%d\n", my_peer->ip, my_peer->port);
            my_peer->alive = 1;
        }
        else
        {
            free(buffer);
            buffer = (unsigned char*)malloc(len);
            memset(buffer, 0, len);
            printf("len is %d before recv buf\n",len);
            //n = recv(sockfd, buffer, len, 0);
            n = readn(sockfd,buffer,len);
            if(n<=0)
            {
                printf("recv buffer error\n");
                break;
            }
            assert(n == len);

            unsigned char id = buffer[0];
            printf("id is %d\n",id);
            switch(id)
            {
            case 0:
            {
                //choke
                break;
            }
            case 1:
            {
                //unchoke
                my_peer->choked = 0;
                //pthread_mutex_lock(&my_peer->request_mutex);
                if(my_peer->isRequest == 0)
                {
                    //send request
                    pthread_mutex_lock(&my_peer->sock_mutex);
                    sendRequest(k);
                    pthread_mutex_unlock(&my_peer->sock_mutex);
                }
                //pthread_mutex_unlock(&my_peer->request_mutex);
                break;
            }
            case 2:
            {
                //interested
                my_peer->interested = 1;
                pthread_mutex_lock(&my_peer->sock_mutex);
                sendUnchoked(my_peer->sockfd);
                pthread_mutex_unlock(&my_peer->sock_mutex);
                break;
            }
            case 3:
            {
                //not interested
                break;
            }
            case 4:
            {
                //have
                int index = *(int*)&buffer[1];
                index = ntohl(index);
                pthread_mutex_lock(&my_peer->piecesInfo_mutex);
                my_peer->piecesInfo[index] = 1;
                pthread_mutex_unlock(&my_peer->piecesInfo_mutex);
                if(piecesInfo[index] == 0)
                {
                    if(my_peer->have_interest == 0)
                    {
                        //send interested
                        pthread_mutex_lock(&my_peer->sock_mutex);
                        sendInterested(sockfd);
                        pthread_mutex_unlock(&my_peer->sock_mutex);
                        my_peer->have_interest = 1;
                    }
                    if(my_peer->choked == 0)
                    {
                        //pthread_mutex_lock(&my_peer->request_mutex);
                        if(my_peer->isRequest == 0)
                        {
                            //send request
                            my_peer->isRequest = 1;
                            pthread_mutex_lock(&my_peer->sock_mutex);
                            sendRequestForEnd(my_peer->sockfd, index);
                            pthread_mutex_unlock(&my_peer->sock_mutex);
                        }
                        //pthread_mutex_unlock(&my_peer->request_mutex);
                    }
                }
                break;
            }
            case 5:
            {
                //bitfield
                assert(len > 0);
                printf("Now I recv bitfield pack from %s:%d\n", my_peer->ip, my_peer->port);
                char *bit_8 = buffer+1;
                int *bit_array = (int *)malloc(sizeof(int)*8*(len-1));
                memset(bit_array,0,sizeof(int)*8*(len-1));
                int i;
                for(i=0; i<(len-1)*8; i++)
                {
                    if((*bit_8 & 0x80) != 0)
                    {
                        printf("hit ");
                        bit_array[i] = 1;
                    }
                    else
                        bit_array[i] = 0;
                    *bit_8 = *bit_8 << 1;
                    if((i+1) % 8 == 0)
                        bit_8++;
                }
                assert((len-1)*8 >= piecesNum);
                printf("piecesNum is %d\n",piecesNum);
                pthread_mutex_lock(&my_peer->piecesInfo_mutex);
                for(i=0; i<piecesNum; i++)
                    my_peer->piecesInfo[i] = bit_array[i];
                pthread_mutex_unlock(&my_peer->piecesInfo_mutex);
                free(bit_array);
                /*
                {
                    my_peer->piecesInfo[i - 1] = buffer[i];
                }
                */

                printf("%s:%d has pieces:", my_peer->ip, my_peer->port);
                for(i = 0; i < piecesNum; i ++)
                {
                    printf("%d ", my_peer->piecesInfo[i]);
                }
                printf("\n");
                int f = 0, flag = 0;
                pthread_mutex_lock(&my_peer->piecesInfo_mutex);
                for(; f < piecesNum; f ++)
                {
                    if(piecesInfo[f] == 0 && my_peer->piecesInfo[f] == 1)
                    {
                        flag = 1;
                    }
                }
                pthread_mutex_unlock(&my_peer->piecesInfo_mutex);
                if(flag == 1)
                {
                    if(my_peer->have_interest == 0)
                    {
                        //send interested
                        pthread_mutex_lock(&my_peer->sock_mutex);
                        sendInterested(sockfd);
                        pthread_mutex_unlock(&my_peer->sock_mutex);
                        my_peer->have_interest = 1;
                    }
                }
                break;
            }
            case 6:
            {
                //request
                if(my_peer->interested == 1)
                {
                    int index = *(int*)&buffer[1];
                    index = ntohl(index);
                    int begin = *(int*)&buffer[5];
                    begin = ntohl(begin);
                    int blocklen = *(int*)&buffer[9];
                    blocklen = ntohl(blocklen);
                    printf("\033[33m request packet 's index is %d,begin is %d,blocklen is %d,sockfd is %d\n \033[m",index,begin,blocklen,sockfd);
                    pthread_mutex_lock(&my_peer->sock_mutex);
                    sendPiece(my_peer->sockfd, index, begin, blocklen);
                    pthread_mutex_unlock(&my_peer->sock_mutex);
                }
                break;
            }
            case 7:
            {
                //piece
                printf("now I recv piece packet:");
                int index = *(int*)&buffer[1];
                index = ntohl(index);
                int begin = *(int*)&buffer[5];
                begin = ntohl(begin);
                printf("index is %x,and begin is %x and sockfd is %d\n",index,begin,sockfd);
                int blocklen = len - sizeof(char) - sizeof(int)*2;
                memcpy(piecebuffer+begin, buffer+9, blocklen);
                pthread_mutex_lock(&g_mutex);
                g_downloaded += blocklen;
                pthread_mutex_unlock(&g_mutex);
                int subPieceNo = begin / SUB_PIECE_LEN;
                assert(piecesInfo[index] == 1);
                isSubpiecesReceived[index][subPieceNo] = 1;
                int flag = 1;
                int m = 0;
                for(; m < subpiecesNum[index]; m ++)
                {
                    if(isSubpiecesReceived[index][m] == 0)
                    {
                        flag = 0;
                    }
                }
                if(flag == 1)
                {
                    int piecelen;
                    if(index != piecesNum - 1)
                    {
                        piecelen = g_torrentmeta->piece_len;
                    }
                    else
                    {
                        piecelen = g_filelen % g_torrentmeta->piece_len;
                        if(piecelen == 0)
                        {
                            piecelen = g_torrentmeta->piece_len;
                        }
                    }
                    printf("I ready to write file\n");
                    if(buffer2file(index, piecelen,piecebuffer) == 0)
                    {
                        pthread_mutex_lock(&least_prefer_mutex);
                        if(least_prefer == 0)
                        {
                            least_prefer = 1;
                        }
                        pthread_mutex_unlock(&least_prefer_mutex);
                        pthread_mutex_lock(&my_peer->request_mutex);
                        my_peer->isRequest = 0;
                        pthread_mutex_unlock(&my_peer->request_mutex);
                        //sendHave to all peers
                        int q = 0;
                        for(; q < MAXPEERS; q ++)
                        {
                            if(peers_pool[q].used == 1 && peers_pool[q].status >= 2 && peers_pool[q].sockfd > 0)
                            {
                                pthread_mutex_lock(&peers_pool[q].piecesInfo_mutex);
                                if(peers_pool[q].piecesInfo[index] == 0)
                                {
                                    pthread_mutex_unlock(&peers_pool[q].piecesInfo_mutex);
                                    pthread_mutex_lock(&peers_pool[q].sock_mutex);
                                    sendHave(peers_pool[q].sockfd, index);
                                    pthread_mutex_unlock(&peers_pool[q].sock_mutex);
                                }
                                else
                                    pthread_mutex_unlock(&peers_pool[q].piecesInfo_mutex);
                            }
                        }
                        //printf("22\n");
                    }
                    else
                    {
                        piecesInfo[index] = 0;
                        int j = 0;
                        for(; j < subpiecesNum[index]; j ++)
                        {
                            isSubpiecesReceived[index][j] = 0;
                        }
                    }
                    //printf("33\n");
                    int f = 0, flag1 = 0;
                    pthread_mutex_lock(&my_peer->piecesInfo_mutex);
                    for(; f < piecesNum; f ++)
                    {
                        if(piecesInfo[f] == 0 && my_peer->piecesInfo[f] == 1)
                        {
                            flag1 = 1;
                        }
                    }
                    pthread_mutex_unlock(&my_peer->piecesInfo_mutex);
                    if(flag1 == 1)
                    {
                        //sendInterested
                        if(my_peer->have_interest == 0)
                        {
                            pthread_mutex_lock(&my_peer->sock_mutex);
                            sendInterested(my_peer->sockfd);
                            pthread_mutex_unlock(&my_peer->sock_mutex);
                            my_peer->have_interest = 1;
                        }
                    }
                    if(my_peer->choked == 0)
                    {
                        //sendRequest
                        if(my_peer->isRequest == 0)
                        {
                            pthread_mutex_lock(&my_peer->sock_mutex);
                            sendRequest(k);
                            pthread_mutex_unlock(&my_peer->sock_mutex);
                        }
                    }
                    free(piecebuffer);
                    piecebuffer = (unsigned char*)malloc(g_torrentmeta->piece_len);
                    memset(piecebuffer, 0, g_torrentmeta->piece_len);
                }
                break;
            }
            case 8:
            {
                //cancel
                break;
            }
            }
        }
        free(buffer);
    }
    printf("recv n is %d\n", n);
    if(n < 0)
    {
        printf("errno is %d:%s\n", errno, strerror(errno));
    }
    free(buffer);
    free(piecebuffer);
    printf("connect broke\n");
    printf("sockfd is %d\n",sockfd);
    pthread_mutex_lock(&my_peer->sock_mutex);
    if(my_peer->sockfd > 0)
    {
        close(sockfd);
        destroy_peer(k);
    }
    pthread_mutex_unlock(&my_peer->sock_mutex);
}
