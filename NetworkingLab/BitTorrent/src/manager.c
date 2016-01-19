#include "util.h"
#include "btdata.h"
#include <errno.h>

extern int errno;


int find_piece_len(int index,int j)
{
    int len;
    if(j != subpiecesNum[index] - 1)
    {
        len = SUB_PIECE_LEN;
    }
    else
    {
        if(index != piecesNum - 1)
        {
            printf("piece_len is %d and filelen is %d\n",g_torrentmeta->piece_len,g_filelen);
            len = g_torrentmeta->piece_len % SUB_PIECE_LEN;
            if(len == 0)
            {
                len = SUB_PIECE_LEN;
            }
        }
        else //最后一个分片？
        {
            int piece_len = g_filelen % g_torrentmeta->piece_len;
            if(piece_len == 0)
            {
                piece_len = g_torrentmeta->piece_len;
            }
            len = piece_len % SUB_PIECE_LEN;
            if(len == 0)
            {
                len = SUB_PIECE_LEN;
            }
        }
    }
    return len;
}

void sendBitField(int sockfd)
{
    //piecesInfo = parse_data_file(g_torrentmeta, &piecesNum);
    int bit_num = piecesNum / 8;
    if(piecesNum % 8 != 0)
        bit_num ++;
    unsigned char *buffer = (unsigned char*)malloc(sizeof(int) + (1 + bit_num) * sizeof(unsigned char));
    memset(buffer, 0, sizeof(int) + (1 + bit_num) * sizeof(unsigned char));
    unsigned char *temp_buffer = buffer;

    int len = 1 + bit_num;     //1位type值
    len = htonl(len);
    printf("len is %x in sendBitField\n",len);
    memcpy(buffer, (char*)&len, 4);
    buffer += sizeof(int);

    *buffer ++ = 5;

    int i = 0;
    char bit_8 = 0x80;
    for(; i < piecesNum; i ++)
    {
        if(piecesInfo[i] == 1)
        {
            (*buffer) = (*buffer) | bit_8;
        }
        bit_8 = bit_8 >> 1;
        if((i+1) % 8 == 0)
        {
            buffer++;
            bit_8 = 0x80;
        }
    }
    printf("temp buffer is %x %x %x %x\n",temp_buffer[0],temp_buffer[1],temp_buffer[2],temp_buffer[3]);


    printf("Send BitField pack\n");
    send(sockfd, temp_buffer, sizeof(int) + ntohl(len) * sizeof(unsigned char), 0);
    free(temp_buffer);
}

void *check_and_keepalive(void *p)
{

    int k = (int)p;
    while(1)
    {
        if(peers_pool[k].used == 1 && peers_pool[k].status >= 2)
        {
            pthread_mutex_lock(&peers_pool[k].alive_mutex);
            if(peers_pool[k].alive == 0)
            {
                pthread_mutex_lock(&peers_pool[k].sock_mutex);
                if(peers_pool[k].sockfd > 0)
                {
                    printf("check_and_keepalive close %d\n",peers_pool[k].sockfd);
                    close(peers_pool[k].sockfd);
                    peers_pool[k].sockfd = -1;
                    peers_pool[k].status = 0;
                }
                pthread_mutex_unlock(&peers_pool[k].sock_mutex);
                pthread_mutex_lock(&peers_pool[k].alive_mutex);
                break;
            }
            else
            {
                pthread_mutex_lock(&peers_pool[k].sock_mutex);
                if(peers_pool[k].sockfd > 0)
                {
                    int len = 0;
                    printf("Send keepalive pack to %s: %d\n", peers_pool[k].ip, peers_pool[k].port);
                    send(peers_pool[k].sockfd, (char *)&len, sizeof(int), 0);
                }
                else
                {
                    pthread_mutex_unlock(&peers_pool[k].sock_mutex);
                    pthread_mutex_lock(&peers_pool[k].alive_mutex);
                    break;
                }
                pthread_mutex_unlock(&peers_pool[k].sock_mutex);
            }
            peers_pool[k].alive = 0;
            pthread_mutex_unlock(&peers_pool[k].alive_mutex);
        }
        sleep(3000);
    }

}

void sendRequest(int k)
{
    peer_t* my_peer = &peers_pool[k];
    int i, requestPiece = -1;
    pthread_mutex_lock(&least_prefer_mutex);
    if(least_prefer == 0)                   //非最小优先策略
    {
        for(i = 0; i < piecesNum; i ++)
        {
            pthread_mutex_lock(&my_peer->piecesInfo_mutex);
            if(piecesInfo[i] == 0 && my_peer->piecesInfo[i] == 1)
            {
                requestPiece = i;
                pthread_mutex_unlock(&my_peer->piecesInfo_mutex);
                break;
            }
            pthread_mutex_unlock(&my_peer->piecesInfo_mutex);
        }
    }
    else                                       //最小优先策略
    {
		printf("Least first!\n");
        int *have_piece_num = (int *)malloc(sizeof(int)*piecesNum);
        for(i=0;i<piecesNum;i++)
        {
            have_piece_num[i] = 0;
        }
        for(i = 0; i < MAXPEERS; i ++)
        {
            pthread_mutex_lock(&peers_pool[i].piecesInfo_mutex);
            if(peers_pool[i].used == 1 && peers_pool[i].status >= 2)
            {
                int j = 0;
                for(; j < piecesNum; j ++)
                {
                    if(peers_pool[i].piecesInfo[j] == 1)
                    {
                        have_piece_num[j] ++;
                    }
                }
            }
            pthread_mutex_unlock(&peers_pool[i].piecesInfo_mutex);
        }
        int min = MAXPEERS + 1;
        for(i = 0; i < piecesNum; i ++)
        {
            pthread_mutex_lock(&my_peer->piecesInfo_mutex);
            if(piecesInfo[i] == 0 && my_peer->piecesInfo[i] == 1)
            {
                if(have_piece_num[i] < min)
                {
                    min = have_piece_num[i];
                    requestPiece = i;
                }
            }
            pthread_mutex_unlock(&my_peer->piecesInfo_mutex);
        }
    }
    pthread_mutex_unlock(&least_prefer_mutex);
    printf("requestPiece is %d\n", requestPiece);
    if(requestPiece >= 0)
    {
        my_peer->isRequest = 1;
        pthread_mutex_lock(&g_mutex);
        piecesInfo[requestPiece] = 1;
        pthread_mutex_unlock(&g_mutex);
        int j;
        printf("subpiecesNum is %d\n", subpiecesNum[requestPiece]);
        for(j = 0; j < subpiecesNum[requestPiece]; j ++)
        {
            unsigned char *buffer = (char*)malloc(sizeof(int)*4 + sizeof(unsigned char));
            memset(buffer, 0, sizeof(int)*4 + sizeof(unsigned char));
            unsigned char *temp_buffer = buffer;

            int len = 13;
            len = htonl(len);
            memcpy(buffer, (char*)&len, sizeof(int));
            buffer += sizeof(int);
            len = ntohl(len);

            *buffer ++ = (unsigned char)6;

            int index = htonl(requestPiece);
            memcpy(buffer, (char*)&index, sizeof(int));
            buffer += sizeof(int);
            int begin = j * SUB_PIECE_LEN;
            begin = htonl(begin);
            memcpy(buffer, (char*)&begin, sizeof(int));
            buffer += sizeof(int);
            int len1 = find_piece_len(requestPiece,j);
            len1 = htonl(len1);
            memcpy(buffer, (char*)&len1, sizeof(int));
            printf("Send Request pack to %s:%d\n", peers_pool[k].ip, peers_pool[k].port);
            printf("index is %d, begin is %d, len is %d\n",ntohl(index), ntohl(begin), ntohl(len1));
            printf("send to %d in sendRequest\n",my_peer->sockfd);
            int n = send(my_peer->sockfd, temp_buffer, sizeof(int)*4 + sizeof(char), 0);
            //printf("n is %d\n", n);
            free(temp_buffer);
        }
    }
    // printf("now will return from sendRequest\n");
}


void sendRequestForEnd(int sockfd,int index)
{
    piecesInfo[index] = 1;
    int j;
    for(j=0; j<subpiecesNum[index]; j++)
    {
        unsigned char *buffer = (unsigned char *)malloc(sizeof(int)*4+sizeof(unsigned char));
        memset(buffer,0,sizeof(int)*4+sizeof(unsigned char));
        unsigned char *temp_buffer = buffer;
        int len = 13;
        len = htonl(len);
        memcpy(buffer,(char *)&len,sizeof(int));
        buffer += sizeof(int);
        len = ntohl(len);
        *buffer ++ = (unsigned char)6;
        index = htonl(index);
        memcpy(buffer, (char*)&index, sizeof(int));
        index = ntohl(index);
        buffer += sizeof(int);
        int begin = j * SUB_PIECE_LEN;
        begin = htonl(begin);
        memcpy(buffer, (char*)&begin, sizeof(int));
        buffer += sizeof(int);
        int len1 = find_piece_len(index,j);
        len1 = htonl(len1);
        memcpy(buffer, (char*)&len1, sizeof(int));
        printf("index: %d, begin: %d, len: %d\n",ntohl(index), ntohl(begin), ntohl(len1));
        printf("send to %d in sendRequest\n",sockfd);
        int n = send(sockfd, temp_buffer, sizeof(int)*4 + sizeof(char), 0);
        //printf("n is %d\n", n);
        free(temp_buffer);
    }
}

void sendCancel(int sockfd,int index)
{
    int j;
    for(j=0; j<subpiecesNum[index]; j++)
    {
        unsigned char *buffer = (unsigned char *)malloc(sizeof(int)*4+sizeof(unsigned char));
        memset(buffer,0,sizeof(int)*4+sizeof(unsigned char));
        unsigned char *temp_buffer = buffer;
        int len = 13;
        len = htonl(len);
        memcpy(buffer,(char *)&len,sizeof(int));
        buffer += sizeof(int);
        len = ntohl(len);
        *buffer ++ = (unsigned char)8;
        index = htonl(index);
        memcpy(buffer, (char*)&index, sizeof(int));
        index = ntohl(index);
        buffer += sizeof(int);
        int begin = j * SUB_PIECE_LEN;
        begin = htonl(begin);
        memcpy(buffer, (char*)&begin, sizeof(int));
        buffer += sizeof(int);
        int len1 = find_piece_len(index,j);
        len1 = htonl(len1);
        memcpy(buffer, (char*)&len1, sizeof(int));
        int n = send(sockfd, temp_buffer, sizeof(int)*4 + sizeof(char), 0);
        printf("n is %d\n", n);
        free(temp_buffer);
    }
}

void sendPiece(int sockfd, int index, int begin, int len)
{
    unsigned char *send_buff = (unsigned char *)malloc(sizeof(int)*3+sizeof(unsigned char)*(1+len));
    memset(send_buff,0,sizeof(int)*3+sizeof(unsigned char)*(1+len));
    unsigned char* temp_buff = send_buff;

    int send_len = sizeof(int) * 2 + sizeof(char) * (1 + len);
    int send_len_n = htonl(send_len);
    printf("piece pack_len is %d,",send_len);
    memcpy(send_buff, (char*)&send_len_n, 4);
    assert(*(int *)send_buff == send_len_n);
    send_buff += 4;
    *send_buff ++ = 7;

    int index_n = htonl(index);
    printf("index is %d,",index);
    memcpy(send_buff, (char*)&index_n, 4);
    send_buff += 4;

    int begin_n = htonl(begin);
    printf("begin is %d\n",begin);
    memcpy(send_buff, (char*)&begin_n, 4);
    send_buff += 4;

    file2buffer(index, begin, len, send_buff);
    printf("Send piece pack\n");
    int n = send(sockfd, temp_buff, sizeof(int) * 3 + sizeof(unsigned char) * (1 + len), 0);
    //assert(sizeof(int)*3 + sizeof(unsigned char)*(1+len) == n);
    g_uploaded += len;
    free(temp_buff);
}

void sendHave(int sockfd, int index)
{
    unsigned char *send_buff = (unsigned char *)malloc(sizeof(int)*2 + 1);
    memset(send_buff,0,sizeof(int)*2+1);
    unsigned char* temp_buff = send_buff;

    int send_len = sizeof(int) + sizeof(char);
    int send_len_n = htonl(send_len);
    memcpy(send_buff, (char*)&send_len_n, 4);
    send_buff += 4;
    *send_buff ++ = 4;

    int index_n = htonl(index);
    strncpy(send_buff, (char*)&index_n, 4);
    printf("Send have pack\n");
    send(sockfd, temp_buff, sizeof(int) * 2 + sizeof(unsigned char), 0);
    free(temp_buff);
}
void sendInterested(int sockfd)
{
    unsigned char *send_buff = (unsigned char *)malloc(sizeof(int)+1);
    memset(send_buff,0,sizeof(int)+1);
    unsigned char *temp_buffer = send_buff;

    int send_len = 1;
    send_len = htonl(send_len);
    memcpy(send_buff,(char *)&send_len,4);
    send_buff +=4;
    *send_buff ++ = 2;
    printf("Send interested pack\n");
    send(sockfd,temp_buffer,sizeof(int)+sizeof(unsigned char),0);
    free(temp_buffer);
}
void sendUnchoked(int sockfd)
{
    unsigned char *send_buff = (unsigned char *)malloc(sizeof(int)+1);
    memset(send_buff,0,sizeof(int)+1);
    unsigned char *temp_buffer = send_buff;

    int send_len = 1;
    send_len = htonl(send_len);
    memcpy(send_buff,(char *)&send_len,4);
    send_buff +=4;
    *send_buff ++ = 1;
    printf("Send unchoked pack\n");
    send(sockfd,temp_buffer,sizeof(int)+sizeof(unsigned char),0);
    free(temp_buffer);
}

