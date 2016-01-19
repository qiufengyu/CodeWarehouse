#include <netdb.h>
#include <sys/types.h>
#include <netinet/in.h> 
#include <arpa/inet.h>
#include <netinet/in.h>
#include <signal.h>
#include <time.h>
#include "util.h"
#include "btdata.h"
#include "bencode.h"
#include <pthread.h>

//#define MAXLINE 4096 
// pthread数据
// 全局变量初始化
void init()
{
    g_done = 0;
    g_tracker_response = NULL;
    g_uploaded = 0;
    g_downloaded = 0;
    pthread_mutex_init(&g_mutex,NULL);
    pthread_mutex_init(&least_prefer_mutex, NULL);
    int i;
    for(i=0; i<MAXPEERS; i++)
    {
        peers_pool[i].used = 0;
        memset(peers_pool[i].id,0,21);
        peers_pool[i].port = 0;
        peers_pool[i].ip = NULL;
        peers_pool[i].status = 0;
        peers_pool[i].alive = 1;
        peers_pool[i].sockfd = -1;
        peers_pool[i].choking = 1;
        peers_pool[i].interested = 0;
        peers_pool[i].choked = 1;
        peers_pool[i].have_interest = 0;
        peers_pool[i].isRequest = 0;
        memset(peers_pool[i].name,0,20);
        pthread_mutex_init(&(peers_pool[i].sock_mutex),NULL);
        pthread_mutex_init(&(peers_pool[i].alive_mutex),NULL);
        pthread_mutex_init(&(peers_pool[i].request_mutex),NULL);
        pthread_mutex_init(&(peers_pool[i].piecesInfo_mutex),NULL);
    }
    recvPieceNum = 0;
    endGame = 0;
    pthread_mutex_init(&recvPieceNum_mutex,NULL);
    pthread_mutex_init(&recvPieceNum_mutex,NULL);
    endPieceIndex = -1;
    endPeer = -1;
}
void init_subpiece()
{
    subpiecesNum = (int *)malloc(sizeof(int) * piecesNum);
    isSubpiecesReceived = (int **)malloc(sizeof(int *) * piecesNum);
    printf("piece_len is %d\n", g_torrentmeta->piece_len);
    int i;
    for(i= 0; i < piecesNum; i ++)
    {
        int temp;
        if(i != piecesNum -1)
        {
            temp = g_torrentmeta->piece_len / SUB_PIECE_LEN;
            if(g_torrentmeta->piece_len % SUB_PIECE_LEN != 0)
            {
                temp ++;
            }
        }
        else
        {
            int piece_len = g_filelen % g_torrentmeta->piece_len;
            if(piece_len == 0)
            {
                piece_len = g_torrentmeta->piece_len;
            }
            temp = piece_len / SUB_PIECE_LEN;
            if(piece_len % SUB_PIECE_LEN != 0)
            {
                temp ++;
            }
        }
        subpiecesNum[i] = temp;
        isSubpiecesReceived[i] = (int *)malloc(sizeof(int) * temp);
        int j = 0;
        for(; j < temp; j ++)
        {
            isSubpiecesReceived[i][j] = piecesInfo[i];
        }
    } 
}

void update_g_left(int *pieces_info)
{
   g_left = g_torrentmeta->length;
   /*int i;
   for(i=0;i<g_torrentmeta->num_pieces;i++)
   {
       if(pieces_info[i])
           g_left -= g_torrentmeta->piece_len;
   }*/
   assert(g_left >= 0);
}

int alloc_peer()
{
    int i;
    /*for(i=0; i<MAXPEERS; i++)
    {
        if(peers_pool[i].used == 1 && strncmp(peer_id,peers_pool[i].id,20) == 0)
            return -1;
    }*/
    for(i=0; i<MAXPEERS; i++)
    {
        if(peers_pool[i].used == 0)
        {
            peers_pool[i].used = 1;
            return i;
        }
    }
    return -1;
}

void init_peer(peerdata *my_peer,int pos)
{
    printf("my_peer id is %s\n",my_peer->id);
    memcpy(peers_pool[pos].id,my_peer->id,21);
    peers_pool[pos].port = my_peer->port;
    peers_pool[pos].ip = (char *)malloc((strlen(my_peer->ip)+1)*sizeof(char));
    memcpy(peers_pool[pos].ip,my_peer->ip,strlen(my_peer->ip));
    peers_pool[pos].ip[strlen(my_peer->ip)] = '\0';
    peers_pool[pos].piecesInfo = (int*)malloc(piecesNum * sizeof(int));
    memset(peers_pool[pos].piecesInfo,0,piecesNum*sizeof(int));
    peers_pool[pos].isSendCancel = 0; 
}
void destroy_peer(int pos)
{
    peers_pool[pos].port = 0;
    //free(peers_pool[pos].ip);
    peers_pool[pos].used = 0;
    peers_pool[pos].sockfd = -1;
    peers_pool[pos].status = 0;
    peers_pool[pos].id[0] = '\0';
}
int find_in_poor(peerdata *my_peer)
{
    int i;
    for(i=0; i<MAXPEERS; i++)
    {
        if(peers_pool[i].used == 1 && strcmp(peers_pool[i].ip,my_peer->ip) == 0 && peers_pool[i].port == my_peer->port)
            return -1;
    }
    return 1;
}
void tracker_free(tracker_data *t)
{
    peerdata *p = t->peers;
    int i;
    for(i=0; i<t->numpeers; i++)
    {
        free(p[i].ip);
    }
    free(p);
    free(t);
}

int main(int argc, char **argv)
{
    int sockfd = -1;
    char rcvline[MAXLINE];
    char tmp[MAXLINE];
    FILE* f;
    int rc;
    int i;
// 注意: 你可以根据自己的需要修改这个程序的使用方法
    if(argc < 3)
    {
        printf("Usage: SimpleTorrent <torrent file> <ip of this machine (XXX.XXX.XXX.XXX form)> [isseed]\n");
        printf("\t isseed is optional, 1 indicates this is a seed and won't contact other clients\n");
        printf("\t 0 indicates a downloading client and will connect to other clients and receive connections\n");
        exit(-1);
    }

    init();
    srand(time(NULL));

    int val[5];
    for(i=0; i<5; i++)
    {
        val[i] = rand();
    }
    g_peerport = rand() % (65535 - 1024) + 1025;   //分配监听peer的端口号
    //printf("g_peerport is %d\n",g_peerport);
    //g_peerport = 0x4554;
    memcpy(g_my_id,(char*)val,20);       //把五个随机int值拷贝到g_my_id中
    strncpy(g_my_ip,argv[2],strlen(argv[2]));
    g_my_ip[strlen(argv[2])] = '\0';

    g_torrentmeta = parsetorrentfile(argv[1]);
    memcpy(g_infohash,g_torrentmeta->info_hash,20);
    piecesInfo = parse_data_file(g_torrentmeta,&piecesNum); 
    printf("000\n");
    update_g_left(piecesInfo);

    //init least_prefer flag
    pthread_mutex_lock(&least_prefer_mutex);
    least_prefer = 0;
    int m = 0;
    for(; m < piecesNum; m ++){
        if(piecesInfo[m] == 1){
            least_prefer = 1;
        }
    }
    pthread_mutex_unlock(&least_prefer_mutex);

    g_filelen = g_torrentmeta->length;
    g_num_pieces = g_torrentmeta->num_pieces;
    //g_filedata = (char*)malloc(g_filelen*sizeof(char));
    printf("111\n");
    announce_url_t* announce_info;
    announce_info = parse_announce_url(g_torrentmeta->announce);

    init_subpiece();
    // 提取tracker url中的IP地址
    //printf("HOSTNAME: %s\n",announce_info->hostname);
    struct hostent *record;
    //int i_j = strcmp(announce_info->hostname,"114.212.190.188");
    //printf("HOSTNAME is %x\n",announce_info->hostname[15]);
    record = gethostbyname(announce_info->hostname);
    //record = gethostbyname("114.212.190.188");
    if (record==NULL)
    {
        printf("gethostbyname failed");
        exit(1);
    }
    struct in_addr* address;
    address =(struct in_addr * )record->h_addr_list[0];
    //printf("Tracker IP Address: %s\n", inet_ntoa(* address));
    strcpy(g_tracker_ip,inet_ntoa(*address));
    g_tracker_port = announce_info->port;

    free(announce_info);
    announce_info = NULL;

    // 初始化
    // 设置信号句柄
    signal(SIGINT,client_shutdown);

    // 设置监听peer的线程
    pthread_t temp_thread;
    pthread_create(&temp_thread,NULL,listen_peers,NULL);

    // 定期联系Tracker:
    int firsttime = 1;
    int mlen;
    char* MESG;
    MESG = make_tracker_request(BT_STARTED,&mlen);
    while(!g_done)
    {
        //printf("next send packet\n");
        if(sockfd <= 0)
        {
            //创建套接字发送报文给Tracker
            printf("Creating socket to tracker...\n");
            sockfd = connect_to_host(g_tracker_ip, g_tracker_port);
        }

        printf("Sending request to tracker...\n");

        if(!firsttime)
        {
            free(MESG);
            // -1 指定不发送event参数
            MESG = make_tracker_request(-1,&mlen);
            /*printf("MESG: ");
            for(i=0; i<mlen; i++)
                printf("%c",MESG[i]);
            printf("\n");*/
        }
        send(sockfd, MESG, mlen, 0);
        firsttime = 0;

        memset(rcvline,0x0,MAXLINE);
        memset(tmp,0x0,MAXLINE);

        // 读取并处理来自Tracker的响应
        tracker_response* tr;
        tr = preprocess_tracker_response(sockfd);

        // 关闭套接字, 以便再次使用
        shutdown(sockfd,SHUT_RDWR);
        close(sockfd);
        sockfd = 0;

        printf("Decoding response...\n");
        char* tmp2 = (char*)malloc(tr->size*sizeof(char)+1);
        memset(tmp2,0,tr->size*sizeof(char)+1);
        memcpy(tmp2,tr->data,tr->size*sizeof(char));

        printf("Parsing tracker data\n");
        g_tracker_response = get_tracker_data(tmp2,tr->size);
        /*
        if(tmp)
        {
            free(tmp2);
            tmp2 = NULL;
        }
        */
        free(tmp2);
        tmp2 = NULL;
        //printf("Num Peers: %d\n",g_tracker_response->numpeers);
        for(i=0; i<g_tracker_response->numpeers; i++)
        {
            //printf("Peer id: %d\n",g_tracker_response->peers[i].id);
            //printf("Peer id: ");
            int idl;
            /*for(idl=0; idl<20; idl++)
                printf("%02X ",(unsigned char)g_tracker_response->peers[i].id[idl]);
            printf("\n");
            printf("Peer ip: %s\n",g_tracker_response->peers[i].ip);
            printf("Peer port: %d\n",g_tracker_response->peers[i].port);*/
            //为每个新增的peer创建线程
            int flag = find_in_poor(&(g_tracker_response->peers[i]));
            if(flag < 0)
                continue;
            //printf("11\n");
            //char *hehe = (char *)malloc(60);
            //printf("num is %d\n",num);
            printf("g_my_ip is %s\n",g_my_ip);
            if(strcmp(g_tracker_response->peers[i].ip,g_my_ip) != 0)
            {
                printf("peers ip is %s\n",g_tracker_response->peers[i].ip);
                pthread_mutex_lock(&g_mutex);
                int num = alloc_peer();
                if(num >= 0)
                {
                    init_peer(&(g_tracker_response->peers[i]),num);
                    printf("init_peer \n");
                    pthread_mutex_lock(&peers_pool[num].sock_mutex);
                    if(peers_pool[num].status == 0 && peers_pool[num].sockfd < 0)
                    {
                        pthread_mutex_unlock(&peers_pool[num].sock_mutex);
                        pthread_t temp_thread1;
                        printf("I will create thread to connect_to_peer\n");
                        peers_pool[num].status = 3;
                        pthread_create(&temp_thread1,NULL,connect_to_peer,(void *)num);
                    }
                    else
                        pthread_mutex_unlock(&peers_pool[num].sock_mutex);
                }
                pthread_mutex_unlock(&g_mutex);
            }
            //printf("pthread error_no is %d\n",error_no);
        }
        //printf("I will sleep %d\n",g_tracker_response->interval);
        // 必须等待td->interval秒, 然后再发出下一个GET请求
        //int ret_sleep = sleep(g_tracker_response->interval);
        tracker_free(g_tracker_response);
        int ret_sleep = sleep(10);
        if(ret_sleep != 0)
            break;
    }

    // 睡眠以等待其他线程关闭它们的套接字, 只有在用户按下ctrl-c时才会到达这里
    sleep(2);
    printf("main exit\n");
    exit(0);
}

