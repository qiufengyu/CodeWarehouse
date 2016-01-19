
#include <pthread.h>
#include "bencode.h"

#ifndef BTDATA_H
#define BTDATA_H

/**************************************
 * 一些常量定义
**************************************/

#define HANDSHAKE_LEN 68  // peer握手消息的长度, 以字节为单位
#define BT_PROTOCOL "BitTorrent protocol"
#define BT_PROTOCOL_LEN 19
#define INFOHASH_LEN 20
#define PEER_ID_LEN 20
#define MAXPEERS 100
#define KEEP_ALIVE_INTERVAL 3
#define SUB_PIECE_LEN 65536

#define BT_STARTED 0
#define BT_STOPPED 1
#define BT_COMPLETED 2

/**************************************
 * 数据结构
**************************************/
// Tracker HTTP响应的数据部分
typedef struct _tracker_response {
  int size;       // B编码字符串的字节数
  char* data;     // B编码的字符串
} tracker_response;

typedef struct _sub_file{
    int length;
    char *path;
    struct _sub_file* next;
}sub_file;

// 元信息文件中包含的数据
typedef struct _torrentmetadata {
   
    int info_hash[5]; // torrent的info_hash值(info键对应值的SHA1哈希值)
    char* announce; // tracker的URL
    int length;     // 文件长度, 以字节为单位
    char* name;     // 文件名
    int piece_len;  // 每一个分片的字节数
    int num_pieces; // 分片数量(为方便起见)
    char* pieces;   // 针对所有分片的20字节长的SHA1哈希值连接而成的字符串
    char single_or_muti;
    sub_file* head_sub_file;    //只有当single_or_muti为1的时候这个指针才有效
    int count;
} torrentmetadata_t;

typedef struct _file_array{
    int length;
    char *name;
}file_array;

// 包含在announce url中的数据(例如, 主机名和端口号)
typedef struct _announce_url_t {
  char* hostname;
  int port;
} announce_url_t;

// 由tracker返回的响应中peers键的内容
typedef struct _peerdata {
  char id[21]; // 20用于null终止符
  int port;
  char* ip; // Null终止
} peerdata;

// 包含在tracker响应中的数据
typedef struct _tracker_data {
  int interval;
  int numpeers;
  peerdata* peers;
} tracker_data;

typedef struct _tracker_request {
  int info_hash[5];
  char peer_id[20];
  int port;
  int uploaded;
  int downloaded;
  int left;
  char ip[16]; // 自己的IP地址, 格式为XXX.XXX.XXX.XXX, 最后以'\0'结尾
} tracker_request;

// 针对到一个peer的已建立连接, 维护相关数据
typedef struct _peer_t {
  int used;
  char id[21]; // 20用于null终止符
  int port;
  char* ip; // Null终止
  int status;
  int alive;
  int sockfd;
  int choking;        // 作为上传者, 阻塞远端peer
  int interested;     // 远端peer对我们的分片有兴趣
  int choked;         // 作为下载者, 我们被远端peer阻塞
  int have_interest;  // 作为下载者, 对远端peer的分片有兴趣
  char name[20];
  int *piecesInfo;
  int isRequest;
  int isSendCancel;
  pthread_mutex_t sock_mutex;
  pthread_mutex_t alive_mutex;
  pthread_mutex_t request_mutex;
  pthread_mutex_t piecesInfo_mutex;
} peer_t;


struct handshake_packet
{
    char len;
    char name[BT_PROTOCOL_LEN];
    char reserve[8];
    char info_hash[20];
    char peer_id[20];
};


struct bit_flag
{
    char b1:1;
    char b2:1;
    char b3:1;
    char b4:1;
    char b5:1;
    char b6:1;
    char b7:1;
    char b8:1;
};

typedef struct bit_flag bit_flag;

/**************************************
 * 全局变量 
**************************************/
char g_my_ip[128]; // 格式为XXX.XXX.XXX.XXX, null终止
int g_peerport; // peer监听的端口号
int g_infohash[5]; // 要共享或要下载的文件的SHA1哈希值, 每个客户端同时只能处理一个文件
char g_my_id[20];

int g_done; // 表明程序是否应该终止

torrentmetadata_t* g_torrentmeta;
char* g_filedata;      // 文件的实际数据
int g_filelen;
int g_num_pieces;

pthread_mutex_t g_mutex;

char g_tracker_ip[16]; // tracker的IP地址, 格式为XXX.XXX.XXX.XXX(null终止)
int g_tracker_port;
tracker_data *g_tracker_response;

// 这些变量用在函数make_tracker_request中, 它们需要在客户端执行过程中不断更新.
int g_uploaded;
int g_downloaded;
int g_left;

peer_t peers_pool[MAXPEERS];

int piecesNum;
int *piecesInfo;
int **isSubpiecesReceived;
int *subpiecesNum;
file_array *my_file_array;
int least_prefer;
pthread_mutex_t least_prefer_mutex;

//用于end game
int recvPieceNum;
int endGame;
pthread_mutex_t recvPieceNum_mutex;
pthread_mutex_t endGame_mutex;
int endPieceIndex;
int endPeer;

#endif
