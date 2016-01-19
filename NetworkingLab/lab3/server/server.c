#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>

#define MAXMSGLINE 512
#define SERV_PORT 3000
#define LISTENQ 8
#define NUMOFTHREADS 1024

struct Message {
	unsigned char type;
	unsigned char opt;
	char username1[32];
	char username2[32];
	char content[256];
};
typedef struct Message MESSAGE;

struct User {
	char username[32];
	char password[32];
	unsigned int state;//1: online, 0: offline, 2 hidden online
	int sockfd;//record the socket!
	int lifetime;// in the game it means live, here means level
};
typedef struct User USER;

struct UserRound {
	char u1[32];
	char u2[32];
	char z1;
	char z2;
	int flag;
};
typedef struct UserRound USERROUND;

struct UserRequest {
	char invitor[32];
	char invited[32];
	int invite_state;
	//-1: invalid
	//0: means invited not reply
	//1: means agree
	//2: decline
};
typedef struct UserRequest UR;
//A global user table
USER userlist[32];
USER userlist2[32];
int num_of_users = 0;
UR urlist[32];
int num_of_urlist=0;
USERROUND uroundlist[32];
//locks
pthread_mutex_t userlist_lock;
pthread_mutex_t userinfo_lock;

extern int index_of_user(char *name);

extern int login_deal(char *username, char *password, int connfd);
extern int reg_deal(char *username, char *password, int connfd);
extern void initial_userinfo();
extern void update_level(char *uname);
extern void add_userinfo(char *uname, char *password);
extern void swap(USER arr[], int i, int j);
extern int partition(USER arr[], int low, int up);
extern void quickSort(USER arr[], int low, int up);
extern void *server_thread(void *sockfd);

int main(int argc, char ** argv) {
	//initial
	initial_userinfo();
	int x = 0;
	for(; x<num_of_users; x++) {
		printf("User: %s password: %s, level: %d\n", userlist[x].username, userlist[x].password, userlist[x].lifetime);
	}
	for(x=0; x<32;x++) {	
		urlist[x].invite_state = -1;//no occupied!
		uroundlist[x].flag = -1;//not occupied!
	}		
	//posix thread in Appendix E.3
	pthread_t thread[NUMOFTHREADS];
	//pthread_attr_t attr;
	int rc;
	long t;
	//void *status;
	/* Initialand set thread detached attribute */
	//pthread_attr_init(&attr);
	//pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
	int listenfd, connfd;
	//pid_t childpid;
	socklen_t clilen;
	
	struct sockaddr_in cliaddr, servaddr;	
	//为服务器创建一个套接字
	listenfd = socket(AF_INET, SOCK_STREAM, 0);
	//填充套接字地址
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servaddr.sin_port = htons(SERV_PORT);
	
	const int on = 1;
	setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on));
    
	//为套接字分配一个本地套接字地址
	bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr));
	//将服务器套接字转换为被动套接字，接受客户端请求
	listen(listenfd, LISTENQ);
	
	printf("%s\n","Server running... Waiting for connections!");
	for(t=0;t<NUMOFTHREADS;t++) {
		clilen=sizeof(cliaddr);
		connfd = accept(listenfd, (struct sockaddr *) &cliaddr, &clilen);
		printf("Received request!\n");
		rc = pthread_create(&thread[t], NULL, server_thread, (void *) (intptr_t)connfd);
		if(rc) {
			printf("ERROR; return code from pthread_create() is %d\n", rc);
			exit(-1);
		}
	}
	/* pthread_attr_destroy(&attr);
	for(t=0;t<NUMOFTHREADS;t++) {
		rc = pthread_join(thread[t], &status);
		if(rc) {
		printf("ERROR; return code from pthread_create() is %d\n", rc);
		exit(-1);
		}
	}
	printf("Main program completed. Exiting.\n");
	* */
	//pthread_exit(NULL);		
	return 0;			

}

void *server_thread(void *sockfd) {
	int connfd = (intptr_t) sockfd;
	MESSAGE send_msg;
	MESSAGE recv_msg;
	unsigned char tp;
	unsigned char option;
	char uname[32];
	char pword[32];
	char uname2[32];
	char ctent[256];
	//keep doing!
	while(1) {
		memset(&send_msg, 0, sizeof(MESSAGE));
		memset(&recv_msg, 0, sizeof(MESSAGE));
		memset(uname,0,32);
		memset(uname2,0,32);
		memset(pword,0,32);
		memset(ctent,0,32);
		if(recv(connfd, &recv_msg, sizeof(MESSAGE), 0) > 0) {
			printf("RECV a package!\n");
			tp = recv_msg.type;
			option = recv_msg.opt;
			printf("%d\t%d\n",tp, option);
			if(tp == 0 && option == 0) {//login
				strncpy(uname, recv_msg.username1, 32);
				strncpy(pword, recv_msg.content, 32);
				//pthread_mutex_lock(&userlist_lock);
				int l_retval = login_deal(uname, pword, connfd);
				//pthread_mutex_unlock(&userlist_lock);
				send_msg.type = 0;
				send_msg.opt = l_retval;
				//send to the login in user
				send(connfd, &send_msg, sizeof(MESSAGE), 0);
				if(l_retval == 1) { //login ok!
					printf("%s loggin!\n", uname);
					int current_index = index_of_user(uname);
					//send to other people
					int i = 0;
					memset(&send_msg, 0, sizeof(MESSAGE));
					//someone login to all!
					send_msg.type = 3; 
					send_msg.opt = 2;
					strncpy(send_msg.username1, uname, 32);
					for(; i<num_of_users; i++) {
						if(i != current_index && userlist[i].state != 0) {
							send(userlist[i].sockfd, &send_msg, sizeof(MESSAGE), 0);
						}
					}
				}
			}
			else if(tp == 0 && option == 5) {//hidden login
				strncpy(uname, recv_msg.username1, 32);
				printf("%s hidden!\n", uname);
				int current_index = index_of_user(uname);
				userlist[current_index].state = 2;
				//send fake leave message to all
				memset(&send_msg, 0, sizeof(MESSAGE));				
				send_msg.type = 3;
				send_msg.opt = 3;
				strncpy(send_msg.username1, uname, 32);
				int i = 0;
				for(i=0; i<num_of_users; i++) {
					if(i != current_index && userlist[i].state != 0) {
						send(userlist[i].sockfd, &send_msg, sizeof(MESSAGE), 0);
					}
				}
			}
			else if(tp == 1 && option == 0) { //register
				strncpy(uname, recv_msg.username1, 32);
				strncpy(pword, recv_msg.content, 32);
				//pthread_mutex_lock(&userlist_lock);
				int r_retval = reg_deal(uname, pword, connfd);
				//pthread_mutex_unlock(&userlist_lock);
				send_msg.type = 1;
				send_msg.opt= r_retval;
				//sent to the registrator
				send(connfd, &send_msg, sizeof(MESSAGE), 0);
				if(r_retval == 1) {
					printf("%s registered!\n", uname);
					//send to others
					int current_index = index_of_user(uname);
					int i = 0;
					memset(&send_msg, 0, sizeof(MESSAGE));
					//someone login message to all!
					send_msg.type = 3; 
					send_msg.opt = 2;
					strncpy(send_msg.username1, uname, 32);
					for(; i<num_of_users; i++) {
						if(i != current_index && userlist[i].state == 1) {
							send(userlist[i].sockfd, &send_msg, sizeof(MESSAGE), 0);
						}
					}
				}
			}
			else if(tp == 2 && option == 0) { // send to a certain user
				strncpy(uname, recv_msg.username1, 32);
				strncpy(uname2, recv_msg.username2, 32);
				strncpy(ctent, recv_msg.content, 256);
				printf("%s to %s: %s\n", uname, uname2, ctent);
				int index1 = index_of_user(uname);
				int index2 = index_of_user(uname2);
				if(index2 != -1) {
					send_msg.type = 2;
					if( userlist[index2].state != 0) {//隐身可送达！
						send_msg.opt = 1;
						strncpy(send_msg.username1, recv_msg.username1, 32);
						strncpy(send_msg.username2, recv_msg.username2, 32);
						strncpy(send_msg.content, recv_msg.content, 256);
						send(userlist[index1].sockfd, &send_msg, sizeof(MESSAGE), 0);
						send(userlist[index2].sockfd, &send_msg, sizeof(MESSAGE), 0);
					}
					else {//User is offline
						send_msg.opt = 3;
						strncpy(send_msg.username2, recv_msg.username2, 32);
						send(userlist[index1].sockfd, &send_msg, sizeof(MESSAGE), 0);
					}				
				}
				else {//no such user
					printf("User %s not found!\n", uname2);
					send_msg.type = 2;
					send_msg.opt = 2;
					//strncpy(send_msg.username1, recv_msg.username1, 32);
					strncpy(send_msg.username2, recv_msg.username2, 32);
					send(userlist[index1].sockfd, &send_msg, sizeof(MESSAGE), 0);
				}				
			}
			else if(tp == 3 && option == 0) { //send to all
				strncpy(uname, recv_msg.username1, 32);
				strncpy(ctent, recv_msg.content, 256);
				printf("%s to all: %s\n", uname, ctent);
				//int current_index = index_of_user(uname);
				send_msg.type = 3;//一开始忘记写了，呵呵
				send_msg.opt = 1;
				strncpy(send_msg.username1, uname, 32);
				strncpy(send_msg.content, ctent, 256);
				int i = 0;
				for(i=0; i<num_of_users; i++) {
					if( userlist[i].state != 0 && strcmp(uname, userlist[i].username)!= 0) {
						send(userlist[i].sockfd, &send_msg, sizeof(MESSAGE), 0);
					}
				}
			}
			else if(tp == 4 && option == 0) { //send leave to all
				strncpy(uname, recv_msg.username1, 32);
				int i = 0;
				int current_index = index_of_user(uname);
				close(userlist[current_index].sockfd);	
				if(userlist[current_index].state == 1) { //online!
					memset(&send_msg, 0, sizeof(MESSAGE));				
					send_msg.type = 3;
					send_msg.opt = 3;
					strncpy(send_msg.username1, uname, 32);
					printf("%s log out\n", uname);
					for(i=0; i<num_of_users; i++) {
						if(i != current_index && userlist[i].state != 0) {
							send(userlist[i].sockfd, &send_msg, sizeof(MESSAGE), 0);
						}
					}
				}
				userlist[current_index].state = 0;
				//exit here!
				//否则会不停发送下线消息～
				pthread_exit((void *) sockfd);
			}	
			else if(tp == 5 && option == 0) { //online friends
				int j = 0;
				int count = 0;
				char ret_content[256];
				memset(ret_content, 0, 256);
				strncpy(uname, recv_msg.username1, 32);
				for(j = 0; j<num_of_users; j++) { //considering the hidden person!
					if(userlist[j].state == 1 && strcmp(uname, 
					userlist[j].username) != 0) {
						count++;
						strcat(ret_content, userlist[j].username);
						strcat(ret_content, "$");
					}
				}
				memset(&send_msg, 0, sizeof(MESSAGE));				
				send_msg.type = 5;
				send_msg.opt = count; //only here opt means the number of friends!	
				strncpy(send_msg.content, ret_content, 256);
				send(connfd, &send_msg, sizeof(MESSAGE), 0);		
			}		
			else if(tp == 6) {			
				if(option == 0) {
					strncpy(uname, recv_msg.username1, 32);
					//printf("%s", uname);
					send_msg.type = 6;
					send_msg.opt = 100;
					strncpy(send_msg.username1, uname, 32);
					strncpy(send_msg.content, "ok", 256);
					//send(connfd, &send_msg, sizeof(MESSAGE), 0);//ignore this package on client!
					int index = index_of_user(uname);
					userlist[index].state = 2;//state = 2 means waiting for game
				}
				if(option == 1) { //server receive a request!
					strncpy(uname, recv_msg.username1, 32);
					printf("Receive a game request from %s\n", uname);
					memset(&send_msg, 0, sizeof(MESSAGE));	
					send_msg.type = 6;
					send_msg.opt = 2;
					strncpy(send_msg.username1, uname, 32);	
					char ret_content[256];
					memset(ret_content, 0, 256);
					int j;
					for(j = 0; j<num_of_users; j++) {
						if(userlist[j].state == 2 && strcmp(uname, 
						userlist[j].username) != 0) {
							strcat(ret_content, userlist[j].username);
							strcat(ret_content, "$");
						}
					}
					strncpy(send_msg.content, ret_content, 256);
					send(connfd, &send_msg, sizeof(MESSAGE), 0);							
				}
				if(option == 3) {
					strncpy(uname, recv_msg.username1, 32);
					printf("Receive a game request from %s\n", uname);
					strncpy(uname2, recv_msg.username2, 32);
					int u1index = index_of_user(uname);
					int u2index = index_of_user(uname2);
					memset(&send_msg, 0, sizeof(MESSAGE));	
					send_msg.type = 6;
					int j = 0;
					for(j=0; j<num_of_users; j++) {
						if(strcmp(userlist[j].username, uname2) == 0) 
							break;
					}
					if (j >= num_of_users) {//send invite error!
						send_msg.opt = 5;
						send(userlist[u1index].sockfd, &send_msg, sizeof(MESSAGE), 0);
					}
					else {
						send_msg.opt = 4;
						strncpy(send_msg.username2, uname, 32);
						send(userlist[u2index].sockfd, &send_msg, sizeof(MESSAGE), 0);
						strncpy(urlist[num_of_urlist].invitor, uname, 32);
						strncpy(urlist[num_of_urlist].invited, uname2, 32);
						urlist[num_of_urlist].invite_state = 0;
					}
				}
				if(option == 6) { //服务器接收同意
					strncpy(uname, recv_msg.username1, 32);
					strncpy(uname2, recv_msg.username2, 32);
					int y = 0;
					for(; y<num_of_urlist; y++) {
						if( (urlist[y].invite_state == 0) && (strcmp(urlist[y].invited, uname) == 0) && (strcmp(urlist[y].invitor, uname2) == 0)) {
							urlist[y].invite_state= 1;
							break;
						}
					}
					send_msg.type = 6;
					send_msg.opt = 8;
					strncpy(send_msg.username1,urlist[y].invitor,32);
					strncpy(send_msg.username2,urlist[y].invited,32);
					int index1 = index_of_user(urlist[y].invitor);
					int index2 = index_of_user(urlist[y].invited);
					send(userlist[index1].sockfd, &send_msg, sizeof(MESSAGE), 0);
					send(userlist[index2].sockfd, &send_msg, sizeof(MESSAGE), 0);	
					int idx = 0;
					for(; idx<32; idx++) {
						if(uroundlist[idx].flag == -1)
							break;
					}
					uroundlist[idx].flag = 0;//双方都没有出招！
					strncpy(uroundlist[idx].u1, urlist[y].invitor,32);
					strncpy(uroundlist[idx].u2, urlist[y].invited,32);		
				}
				if(option == 7) { //服务器接收拒绝
					strncpy(uname, recv_msg.username1, 32);
					strncpy(uname2, recv_msg.username2, 32);
					int y = 0;
					for(; y<num_of_urlist; y++) {
						if( (urlist[y].invite_state == 0) && (strcmp(urlist[y].invited, uname) == 0) && (strcmp(urlist[y].invitor, uname2) == 0)) {
							urlist[y].invite_state = 2;
							break;
						}
					}
					send_msg.type = 6;
					send_msg.opt = 9;
					strncpy(send_msg.username1,urlist[y].invitor,32);
					int index1 = index_of_user(urlist[y].invitor);
					send(userlist[index1].sockfd, &send_msg, sizeof(MESSAGE), 0);	
				}
				if(option >= 10 && option <= 12) {
					MESSAGE s1,s2;
					memset(&s1,0,sizeof(MESSAGE));
					memset(&s2,0,sizeof(MESSAGE));
					s1.type = 6;
					s2.type = 6;
					strncpy(uname, recv_msg.username1, 32);
					strncpy(uname2, recv_msg.username2, 32);
					char z = recv_msg.content[0];
					printf("User: %s and %s, content: %c\n", uname, uname2, z);
					int idx = 0;
					for(; idx<32; idx++) {
						if(((strcmp(uname, uroundlist[idx].u1) == 0) && strcmp(uname2, uroundlist[idx].u2) ==0) || ((strcmp(uname2, uroundlist[idx].u1) == 0) && (strcmp(uname, uroundlist[idx].u2) ==0))) {
							break;
						}
					}
					if(uroundlist[idx].flag == 0) {
						if(strcmp(uname, uroundlist[idx].u1) == 0) {
							uroundlist[idx].z1 = z;
						}
						else
							uroundlist[idx].z2 = z;
						uroundlist[idx].flag = 1; //此时有一方已经出招啦！
					}
					else if(uroundlist[idx].flag == 1) { //一方已经出招！
						if(strcmp(uname, uroundlist[idx].u1) == 0) {
							uroundlist[idx].z1 = z;
						}							
						else
							uroundlist[idx].z2 = z;
						char zz1 = uroundlist[idx].z1;
						char zz2 = uroundlist[idx].z2;
						int id1 = index_of_user(uroundlist[idx].u1);
						int id2 = index_of_user(uroundlist[idx].u2);
						if(zz1 == zz2) { //平局，opt = 20
							s1.opt=20;
							s2.opt=20;
							send(userlist[id1].sockfd,&s1,sizeof(MESSAGE),0);
							send(userlist[id2].sockfd,&s2,sizeof(MESSAGE),0);
						}
						else if(zz1 == '1') { //1是石头
							if(zz2 == '2') {
								s1.opt = 21;
								s2.opt = 22;
								userlist[id1].lifetime++;
								userlist[id2].lifetime--;
								send(userlist[id1].sockfd,&s1,sizeof(MESSAGE),0);
								send(userlist[id2].sockfd,&s2,sizeof(MESSAGE),0);
							}
							else if(zz2 == '3') {
								s1.opt = 22;
								s2.opt = 21;
								userlist[id2].lifetime++;
								userlist[id1].lifetime--;
								send(userlist[id1].sockfd,&s1,sizeof(MESSAGE),0);
								send(userlist[id2].sockfd,&s2,sizeof(MESSAGE),0);
							}
						}
						else if(zz1 == '2') {//1是剪刀
							if(zz2 == '1') {
								s1.opt = 22;
								s2.opt = 21;
								userlist[id2].lifetime++;
								userlist[id1].lifetime--;
								send(userlist[id1].sockfd,&s1,sizeof(MESSAGE),0);
								send(userlist[id2].sockfd,&s2,sizeof(MESSAGE),0);
							}
							else if(zz2 == '3') {
								s1.opt = 21;
								s2.opt = 22;
								userlist[id1].lifetime++;
								userlist[id2].lifetime--;
								send(userlist[id1].sockfd,&s1,sizeof(MESSAGE),0);
								send(userlist[id2].sockfd,&s2,sizeof(MESSAGE),0);
							}
						}
						else if(zz1 == '3') {//1是布
							if(zz2 == '1') {
								s1.opt = 21;//win
								s2.opt = 22;
								userlist[id1].lifetime++;
								userlist[id2].lifetime--;
								send(userlist[id1].sockfd,&s1,sizeof(MESSAGE),0);
								send(userlist[id2].sockfd,&s2,sizeof(MESSAGE),0);
							}
							else if(zz2 == '2') {
								s1.opt = 22;
								s2.opt = 21;
								userlist[id2].lifetime++;
								userlist[id1].lifetime--;
								send(userlist[id1].sockfd,&s1,sizeof(MESSAGE),0);
								send(userlist[id2].sockfd,&s2,sizeof(MESSAGE),0);
							}
						}
						uroundlist[idx].flag = 0;
					}
					printf("pair %d, user1, user2: %s and %s, z1 = %c, z2=%c, status = %d\n", idx, uroundlist[idx].u1, uroundlist[idx].u2, uroundlist[idx].z1, uroundlist[idx].z2, uroundlist[idx].flag);
					
			
				}
				if(option == 13) {
					strncpy(uname, recv_msg.username1, 32);
					int uindex = 0;
					for(; uindex<num_of_users; uindex++) {
						if(strcmp(uname, userlist[uindex].username) == 0)
							break;
					}
					userlist[uindex].state = 0;//back to online!
				}
			}
			else if(tp == 7 && option == 0) { //等級排名
				int x = 0;
				int j = 0;
				char ret_content[256];
				memset(ret_content, 0, 256);
				for(x = 0; x<num_of_users; x++) {
					memcpy(&userlist2[x], &userlist[x], sizeof(USER));
				}
				quickSort(userlist2, 0, num_of_users-1);
				for(j = 0; j<num_of_users; j++) { //considering the hidden person!
					strcat(ret_content, userlist2[j].username);
					strcat(ret_content, "\t");
					char lev[8];
					sprintf(lev,"%d",userlist2[j].lifetime);
					strcat(ret_content, lev);					
					strcat(ret_content, "$");
				}
				memset(&send_msg, 0, sizeof(MESSAGE));				
				send_msg.type = 7;
				send_msg.opt = 1; 
				strncpy(send_msg.content, ret_content, 256);
				send(connfd, &send_msg, sizeof(MESSAGE), 0);	
			}
		}
		else {//recv = 0! the client exit for some unknown reason, maybe Ctrl+C
			//inform all online client the information
			int i = 0;
			//strncpy(uname, recv_msg.username1, 32); <-当然是空白，recv都是0！
			int j = 0;
			for (j = 0; j<num_of_users; j++) {
				if(userlist[j].sockfd == connfd)
					break;
			}
			int current_index = j;
			if(userlist[current_index].state == 1) { //只有在线用户下线才有提示，若为隐身，则不会有提示！
				close(userlist[current_index].sockfd);	
				memset(&send_msg, 0, sizeof(MESSAGE));		
				send_msg.type = 3;
				send_msg.opt = 3;
				strncpy(send_msg.username1, userlist[current_index].username, 32);
				printf("%s exit! (Ctrl+C)\n", send_msg.username1);
				for(i=0; i<num_of_users; i++) {
					if(i != current_index && userlist[i].state == 1) {
						send(userlist[i].sockfd, &send_msg, sizeof(MESSAGE), 0);
					}
				}
			}
			userlist[current_index].state = 0;
			//exit here!
			pthread_exit((void *) sockfd);
		}
		//pthread_exit((void *) sockfd);
		// 若这里退出，则只进行一次接收了！
	}
}

int login_deal(char *username, char *password, int connfd) {
	int i = 0;
	for(;i<num_of_users; i++) {
		if( strcmp(username, userlist[i].username) == 0 ) {
			if(strcmp(password, userlist[i].password) == 0) {
				if(userlist[i].state == 0) {
					userlist[i].sockfd = connfd;
					userlist[i].state = 1;
					userlist[i].lifetime++;
					update_level(username);
					return 1;//success!
				}
				else
					return 3; //repeated login!
			}
			else
				return 4; //error password!
		}
	}
	return 2;// no such user!
}

int reg_deal(char *username, char *password, int connfd) {
	int i = 0;
	for(; i<num_of_users; i++) {
		if( strcmp(username, userlist[i].username) == 0 ) {
			return 2;
		}
	}
	strncpy(userlist[i].username,username,32);
	strncpy(userlist[i].password,password,32);
	userlist[i].state = 1;
	userlist[i].sockfd = connfd;
	userlist[i].lifetime = 1;
	add_userinfo(username,password);
	num_of_users++;//need protect in thread!
	return 1;
}

int index_of_user(char *name) {
	int ret = 0;
	for(; ret<num_of_users; ret++) {
		if(strcmp(name, userlist[ret].username) == 0) {
			return ret;
		}
	}
	return -1;
}

void initial_userinfo() {
	FILE *fp;
	fp = fopen("userinfo", "r"); 
	if(fp == NULL) {
		printf("File Loading error!\n");
		exit(0);
	}
	//strtok: http://blog.chinaunix.net/uid-26488891-id-3134863.html
	char buf[256];
	char *info = NULL;
	//char delim = " ";
	while(fgets(buf,256,fp) != NULL) {
		//printf("%s\n", buf);
		info = strtok(buf, " ");
		//printf("%s", info);
		strncpy(userlist[num_of_users].username, info, 32);
		info = strtok(NULL, " ");
		strncpy(userlist[num_of_users].password, info, 32);
		info = strtok(NULL, " ");
		char level[8];
		strncpy(level, info, 8);
		userlist[num_of_users].lifetime = atoi(level);
		num_of_users++;
		memset(buf,0,256);
	}
	fclose(fp);
}

void update_level(char *uname) {
	pthread_mutex_lock(&userinfo_lock);
	FILE *fp;
	fp = fopen("userinfo", "r");
	FILE *out=fopen("back","w"); 
	if(fp == NULL || out == NULL) {
		printf("File Loading error!\n");
		exit(0);
	}
	char buf[256];
	char buf_temp[256];
	char buf_back[256];
	memset(buf_back,0, 256);
	char *info = NULL;
	//char delim = " ";
	while(fgets(buf,256,fp) != NULL) {
		printf("%s\n", buf);
		strncpy(buf_temp, buf, 256);
		info = strtok(buf, " ");
		//printf("%s", info);
		if(strcmp(info, uname) == 0) {//level + 1
			strcat(buf_back, info);
			strcat(buf_back," ");
			info = strtok(NULL, " ");
			strcat(buf_back, info);
			strcat(buf_back," ");
			info = strtok(NULL, " ");
			int new_lifetime = atoi(info)+1;
			char lifetime_new[8];
			sprintf(lifetime_new, "%d", new_lifetime);
			strcat(buf_back, lifetime_new);
			strcat(buf_back, "\n");
			fputs(buf_back,out);
		}
		else 
			fputs(buf_temp,out);
		memset(buf,0,256);
		memset(buf_temp,0,256);
	}
	fclose(fp);
	fclose(out);
	// http://bbs.csdn.net/topics/310052128
	unlink("userinfo"); // 删除源文件
    rename("back","userinfo"); // 改名
    pthread_mutex_unlock(&userinfo_lock);
}

void add_userinfo(char *uname, char *password) {
	//printf("%s %s %d\n", uname, password, 1);
	pthread_mutex_lock(&userinfo_lock);
	FILE *fp;
	fp = fopen("userinfo", "r");
	FILE *out=fopen("back","w"); 
	if(fp == NULL || out == NULL) {
		printf("File Loading error!\n");
		exit(0);
	}
	char buf[256];
	char buf_temp[256];
	while(fgets(buf,256,fp) != NULL) {
		//printf("%s\n", buf);
		strncpy(buf_temp, buf, 256);
		fputs(buf_temp,out);
		memset(buf,0,256);
		memset(buf_temp,0,256);
	}
	fprintf(out, "%s %s 1\n", uname, password);
	fclose(fp);
	fclose(out);
	// http://bbs.csdn.net/topics/310052128
	unlink("userinfo"); // 删除源文件
    rename("back","userinfo"); // 改名

	pthread_mutex_unlock(&userinfo_lock);
}

//http://blog.csdn.net/kenden23/article/details/14643823
int partition(USER arr[], int low, int up)
{
	int pivot_lifetime = arr[up].lifetime;
	int i = low-1;
	int j = low;
	for (; j < up; j++)
	{
		if(arr[j].lifetime >= pivot_lifetime)
		{
			i++;
			swap(arr, i, j);
		}
	}
	swap(arr,i+1,up);
	return i+1;
}

void swap(USER arr[], int i, int j) {
	USER temp;
	memcpy(&temp, &arr[i], sizeof(USER));
	memcpy(&arr[i], &arr[j], sizeof(USER));
	memcpy(&arr[j], &temp, sizeof(USER));
	/*
	strncpy(temp.username, arr[i].username, 32);
	strncpy(temp.password, arr[i].password, 32);
	temp.sockfd = arr[i].sockfd;
	temp.state = arr[i].state;
	temp.lifetime = arr[i].lifetime;
	memset(&arr[i], 0, sizeof(USER));
	strncpy(arr[i].username, arr[j].username, 32);
	strncpy(arr[i].password, arr[j].password, 32);
	arr[i].sockfd = arr[j].sockfd;
	arr[i].state = arr[j].state;
	arr[i].lifetime=arr[j].lifetime;
	memset(&arr[j], 0, sizeof(USER));
	strncpy(arr[j].username, temp.username, 32);
	strncpy(arr[j].password, temp.password, 32);
	arr[j].sockfd = temp.sockfd;
	arr[j].state = temp.state;
	arr[j].lifetime = temp.lifetime;	
	* */
}

//C++'s array range should be [low, up], the same as [low, up+1)
void quickSort(USER arr[], int low, int up)
{
	if(low < up)
	{
		int mid = partition(arr, low, up);
		//Watch out! The mid position is on the place, so we don't need to consider it again.
		//That's why below is mid-1, not mid! Otherwise it will occur overflow error!!!
		quickSort(arr, low, mid-1);
		quickSort(arr, mid+1, up);
	}
}
