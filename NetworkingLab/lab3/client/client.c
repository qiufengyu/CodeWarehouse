#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <unistd.h>

#define MAXMSGLINE 512
#define SERV_PORT 3000
#define SERV_IP "192.168.242.134"

struct Message {
	unsigned char type;
	unsigned char opt;
	char username1[32];
	char username2[32];
	char content[256];
};

typedef struct Message MESSAGE;

extern void flush(); //clear the input line
extern void printline(); //print a line

extern int login(int sockfd);
extern int reg(int sockfd);
extern void logout(int sockfd);
extern int client_send(int sockfd, int t);
// extern int game_send(int sockfd, int t);

extern void* client_recv(void *sockfd);
// extern void* game_recv(void *sockfd);

//global
char clientname[32];
// char gamename[32];//游戏对手名
// pthread_mutex_t game_mutex_lock;
pthread_mutex_t display_lock;
// pthread_mutex_t status_lock;
// int game_lock = 1;


int main(int argc, char ** argv) {
	int sockfd;
	struct sockaddr_in servaddr;	
	if(argc != 1) {
		perror("Input error!\n");
		exit(1);
	}
	//Create socket for client
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
 
	memset(&servaddr,0,sizeof(servaddr));
	servaddr.sin_family=AF_INET;
	servaddr.sin_addr.s_addr=inet_addr(SERV_IP);
	servaddr.sin_port=htons(SERV_PORT);
	
	connect(sockfd, (struct sockaddr *) &servaddr, sizeof(servaddr));
	
	//thread handle not here, 会抢占log和register时的receive
	
	//do tasks
	int status = 0;//Initial
	char option = '0';
	while(1) {
		//pthread_mutex_lock(&status_lock);
		switch(status) {
			case 0: {//start
				system("clear");
				printf("Welcome to the NJUCS IM Game System!\n");
				printf("(l) login, (r) register, (#)exit\n");
				printf("----------------------------------------\n");
				scanf("%c", &option);
				flush();
				if(option == 'l') {
					status = 1; // login
					//printf("%d\n",status);
					break;
				}
				else if(option == 'r') {
					status = 2; //register
					break;
				}
				else if(option == '#') {
					status = 4; //exit
					break;
				}
				else {
					status = 0;
				}
			} break;
			case 1: {//login 
				int login_retval = login(sockfd);	
				if(login_retval == -1) {
					status = 4;
					break;
				}
				if( login_retval != 1) {
					status = 1;
					break;
				}
				else {
					status = 3;
					break;
				}							
			} break;
			case 2: {//register
				int reg_retval = reg(sockfd);	
				if(reg_retval == -1) {
					status = 4;
					break;
				}
				if(reg_retval != 1) {
					status = 2;
					break;
				}
				else {
					status = 3;
					break;
				}						
			} break;
			
			case 3: {//main function
				//system("clear");
				pthread_t thread;
				//pthread_attr_t attr;
				int rc;
				//thread handle when the user is in the message system!				
				//pthread_attr_init(&attr);
				//pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
				rc = pthread_create(&thread, NULL, client_recv, (void *)(intptr_t)sockfd);
				if(rc) {
					printf("ERROR; return code from pthread_create() is %d\n", rc);
					exit(-1);
				}
				//pthread_attr_destroy(&attr);
				printf("Functions: (o)send to one certain person, (a)send to all, (f)friend, (#)exit\n");
				printf("Other Functions: (h)hidden login, (r)level rank\n");
				char op;
				scanf("%c",&op);
				flush();
				if(op == '#') {
					status = 4;
					break;
				}
				if(op == 'o') {
					int send_retval = client_send(sockfd, 1);
					if(send_retval == -1) {
						perror("Socket error!\n");
						logout(sockfd);
						close(sockfd);
						exit(-1);
					}
				}
				else if(op == 'a') {
					int send_retval = client_send(sockfd, 0);
					if(send_retval == -1) {
						perror("Socket error!\n");
						logout(sockfd);
						close(sockfd);
						exit(-1);
					}
				}
				/*
				else if(op == 'g') {
					game_lock = 1;
					status = 6;
					break;
				}
				* delete the game*/
				else if(op == 'f') {
					int send_retval = client_send(sockfd, 2);
					if(send_retval == -1) {
						perror("Socket error!\n");
						logout(sockfd);
						close(sockfd);
						exit(-1);
					}
				}
				else if( op == 'h') {//隐身登录
					int send_retval = client_send(sockfd, 3);
					if(send_retval == -1) {
						perror("Socket error!\n");
						logout(sockfd);
						close(sockfd);
						exit(-1);
					}
				}
				else if(op == 'r') {
					int rank_retval = client_send(sockfd,4);
					if(rank_retval == -1) {
						perror("Socket error!\n");
						logout(sockfd);
						close(sockfd);
						exit(-1);
					}
				}
				//pthread_exit(NULL);?? 一开始怎么想的！！
				//status = 4;
			} break;
			
			case 4: {//exit
				printf("username: %s\n", clientname);
				logout(sockfd);
				close(sockfd);
				exit(0);
			} break;
			
			/* delete game related
			case 6: { //game entering
				int game_retval = game_send(sockfd,0);
				if (game_retval == -1) {
					perror("Game socket error!\n");
					logout(sockfd);
					close(sockfd);
					exit(-1);
				}
				else {
					status = 7;
					break;
				}
			} break;
			
			case 7: { //game interface!
				pthread_t thread_game;
				int rc;
				rc = pthread_create(&thread_game, NULL, game_recv, (void *)(intptr_t)sockfd);
				if(rc) {
					printf("ERROR; return code from pthread_create() is %d\n", rc);
					exit(-1);
				}
				pthread_mutex_lock(&game_mutex_lock);
				printf("Welcome to the game center!\n");
				printf("Functions: (s)send request, (l)game waiting list, (#)exit\n");
				pthread_mutex_unlock(&game_mutex_lock);
				//printf("Press (y) to agree while (n) to decline!\n");
				char op;
				scanf("%c",&op);
				flush();
				if( op == 'l') {
					game_send(sockfd,1);
				}
				if( op == 's') {
					game_send(sockfd,2);
					status = 9;
					break;
				}
				if( op == '#') {
					game_send(sockfd,8);
					status = 4;
					break;
				}
				if( op == 'y') {
					game_send(sockfd,3);
					status = 8;
					break;
				}
				if( op == 'n') {
					game_send(sockfd,4);
				}
			} break;
			
			case 8: {//游戏界面！
				//pthread_mutex_lock(&game_mutex_lock);
				printf("请输入出招(1: 石头、2:剪刀、3:布): ");				
				char c;
				scanf("%c", &c);
				flush();
				if(c == '1') {
					game_send(sockfd,5);
				}
				else if(c == '2') {
					game_send(sockfd,6);
				}
				else if(c == '3') {
					game_send(sockfd,7);
				}
				else {
					printf("Input error! You lose!\n");
				}
				sleep(6);
				//pthread_mutex_unlock(&game_mutex_lock);
			}break;	
			
			case 9:{
				sleep(3);
				printf("Game waiting...!\n");
				status = 8;
			} break;	
			*/
			default: break;
		}
		//pthread_mutex_unlock(&status_lock);
	}
	//pthread_mutex_destroy(&game_mutex_lock);
	pthread_mutex_destroy(&display_lock);
	return 0;
}

void* client_recv(void *sockfd) {
	int csockfd = (intptr_t) sockfd;
	//http://stackoverflow.com/questions/21323628/warning-cast-to-from-pointer-from-to-integer-of-different-size
	MESSAGE recv_msg;
	memset(&recv_msg, 0, sizeof(MESSAGE));
	char uname1[32];
	char uname2[32];
	char ctent[256];
	while(1) { 
		if(recv(csockfd, &recv_msg, sizeof(MESSAGE), 0)>0) {
			//pthread_mutex_unlock(&usersocket_lock);
			unsigned char tp = recv_msg.type;
			unsigned char option = recv_msg.opt;
			//printf("%d %d\n", (int)tp, (int)option);			
			memset(uname1,0,32);
			memset(uname2,0,32);
			memset(ctent,0,32);
			strncpy(uname1,recv_msg.username1,32);
			strncpy(uname2,recv_msg.username2,32);
			strncpy(ctent,recv_msg.content,256);
			pthread_mutex_lock(&display_lock);
			if(tp == 3) { //message to all
				if(option == 2) {//some one enter
					printf("User \"%s\" entering...\n", uname1);
				}
				else if(option ==3) {//some one leave
					if(uname1[0] != 0) {
						printf("User \"%s\" leaving...\n", uname1);
					}
				}
				else if(option == 1) {//some one say to all
					printf("User \"%s\":", uname1);
					int t = 0;
					for(t=0; t<256; t++) {
						printf("%c",ctent[t]);
					}
					printf("\n");
				}
				else 
					printf("What? option = %d\n", option);
			}
			else if(tp == 2) { // to one
				if(option == 1) {
					if(strcmp(uname2, clientname) == 0) {
						printf("\"%s\" said to %s: %s\n", uname1, uname2, ctent);
					}
				}
				else if(option == 2) {
					printf("User %s does not exsist!\n", uname2);
				}		
				else if(option == 3) {
					printf("User %s is offline!\n", uname2);
				}		
			}
			else if(tp == 5) { //show the online friends!
				int num_of_friends = (int) option;
				printline();
				printf("In all %d online friends: \n", num_of_friends);
				int k = 0;
				for(k = 0; k<256; k++) {
					if(ctent[k] == '$') {
						printf("\n");
					}
					else
						printf("%c", ctent[k]);
				}
				printline();
			}
			else if(tp == 7) {
				printline();
				printf("User Level Rank List\n");
				int k = 0;
				for(k = 0; k<256; k++) {
					if(ctent[k] == '$') {
						printf("\n");
					}
					else
						printf("%c", ctent[k]);
				}
				printline();
			}
			pthread_mutex_unlock(&display_lock);
		}
	}
	//pthread_mutex_unlock(&usersocket_lock);
}
	

/* 
void* game_recv(void *sockfd) {
	int gsockfd = (intptr_t) sockfd;
	MESSAGE recv_msg;
	memset(&recv_msg, 0, sizeof(MESSAGE));
	char uname1[32];
	char uname2[32];
	char ctent[256];
	while(1) { 
		if(recv(gsockfd, &recv_msg, sizeof(MESSAGE), 0)>0) {
			unsigned char tp = recv_msg.type;
			unsigned char option = recv_msg.opt;
			//printf("%d %d\n", (int)tp, (int)option);			
			memset(uname1,0,32);
			memset(uname2,0,32);
			memset(ctent,0,32);
			strncpy(uname1,recv_msg.username1,32);
			strncpy(uname2,recv_msg.username2,32);
			strncpy(ctent,recv_msg.content,256);
			pthread_mutex_lock(&game_mutex_lock);
			if(tp == 3) { //message to all
				if(option == 2) {//some one enter
					printf("User \"%s\" entering...\n", uname1);
				}
				else if(option ==3) {//some one leave
					if(uname1[0] != 0) {
						printf("User \"%s\" leaving...\n", uname1);
					}
				}
				else if(option == 1) {//some one say to all
					printf("User \"%s\": %s\n", uname1, ctent);
				}
				else 
					printf("What? option = %d\n", option);
			}
			if( tp == 6 && option == 2) {
				printline();
				printf("Waiting List\n");
				int k = 0;
				for(k = 0; k<256; k++) {
					if(ctent[k] == '$') {
						printf("\n");
					}
					else
						printf("%c", ctent[k]);
				}
				printline();
			}
			else if( tp == 6 && option == 5) {
				printf("Ehhhhhh, the user is not online or doesn't exists!\n");
			}
			else if( tp == 6 && option == 4) {
				printf("User %s invites you!\n", uname2);
				printf("(y) yes, (n) no\n");
				memset(gamename, 0, 32);
				strncpy(gamename, uname2, 32);
			}
			else if( tp == 6 && option == 8) {
				status = 8;				
				system("clear");
				//printf("请输入出招(1: 石头、2:剪刀、3:布): ");
			}
			else if( tp == 6 && option == 9) {
				printf("Sorry, you are declined....\n");
			}
			else if(tp == 6 && option == 20) {
					printf("Draw!\n");
			}
			else if(tp == 6 && option == 21) {
					printf("You Win!\n");
			}
			else if(tp == 6 && option == 22) {
					printf("You Lose!\n");
			}
			pthread_mutex_unlock(&game_mutex_lock);
		}
	}
}
* */

//如果这也是一个线程的话，会造成不可预料的竞争！
int client_send(int sockfd, int t) {
	MESSAGE send_msg;
	char input_user[32];
	char input_content[256];
	memset(input_user, 0, 32);
	memset(input_content, 0, 256);
	memset(&send_msg, 0, sizeof(MESSAGE));
	send_msg.opt = 0;
	strncpy(send_msg.username1, clientname, 32);
	pthread_mutex_lock(&display_lock);
	if(t == 1) {
		printf("Please input the target username: ");
		scanf("%s",input_user);
		flush();
		strncpy(send_msg.username2,input_user, 32);
		send_msg.type = 2;
		printf("Please input the message: ");
		gets(input_content);
		//flush();
		strncpy(send_msg.content, input_content, 256);
	}
	else if(t == 0) {//send to all
		send_msg.type = 3;
		printf("Please input the message: ");
		gets(input_content);
		//fgets(input_content, 256, stdin);
		//scanf("%s", input_content);
		//flush();
		strncpy(send_msg.content, input_content, 256);
	}	
	else if(t == 2) {//online friends!
		memset(&send_msg, 0, sizeof(MESSAGE));
		send_msg.type = 5;
		send_msg.opt = 0;
		strncpy(send_msg.username1, clientname, 32);
	}
	else if( t==3) {//隐身登录
		memset(&send_msg, 0, sizeof(MESSAGE));
		send_msg.type = 0;
		send_msg.opt = 5;
		strncpy(send_msg.username1, clientname, 32);
	}
	else if(t ==4) {//等級排名
		memset(&send_msg, 0, sizeof(MESSAGE));
		send_msg.type = 7;
		send_msg.opt = 0;
		strncpy(send_msg.username1, clientname, 32);
	}
	pthread_mutex_unlock(&display_lock);
	//pthread_mutex_lock(&usersocket_lock);
	if(send(sockfd, &send_msg, sizeof(MESSAGE), 0)>0) {
		//pthread_mutex_unlock(&usersocket_lock);
		//printf("Send success!\n");
		return 0;
	}
	else {
		//pthread_mutex_unlock(&usersocket_lock);
		return -1;
	}
}

/* 
int game_send(int sockfd, int t) {
	int gsockfd = sockfd;
	MESSAGE game_send;
	memset(&game_send, 0, sizeof(MESSAGE));
	if(t == 0 ) {//完成进入游戏平台的请求
		game_send.type = 6;
		game_send.opt = 0;
		strncpy(game_send.username1,clientname,32);
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);
	}
	else if(t == 1) {//发送游戏在线列表
		game_send.type = 6;
		game_send.opt = 1;
		strncpy(game_send.username1,clientname,32);
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);
	}
	else if(t == 2) { //向好友发送请求
		game_send.type = 6;
		game_send.opt = 3;
		strncpy(game_send.username1,clientname,32);
		//send(gsockfd, &game_send, sizeof(MESSAGE), 0);
		pthread_mutex_lock(&game_mutex_lock);
		char invite_name[32];
		printf("Please input the username you want to invite: ");
		memset(invite_name, 0, 32);
		scanf("%s", invite_name);
		flush();
		strncpy(game_send.username2, invite_name, 32);
		strncpy(gamename, invite_name, 32);
		pthread_mutex_unlock(&game_mutex_lock);
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);
	}
	else if( t == 3) { //发送同意游戏请求
		game_send.type = 6;
		game_send.opt = 6;
		strncpy(game_send.username1, clientname, 32);
		strncpy(game_send.username2, gamename,32);
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);
	}
	else if( t == 4) {//发送拒绝游戏请求
		game_send.type = 6;
		game_send.opt = 7;
		strncpy(game_send.username1, clientname,32);
		strncpy(game_send.username2, gamename,32);
		memset(gamename, 0, 32);
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);
	}
	else if(t == 5) {//发送游戏的出招：石头
		char c='1';
		game_send.type = 6;
		game_send.opt = 10;
		strncpy(game_send.username1, clientname, 32);
		strncpy(game_send.username2, gamename, 32);
		game_send.content[0]=c;
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);

	}
	else if(t == 6) {//发送游戏的出招：剪刀
		char c='2';
		game_send.type = 6;
		game_send.opt = 11;
		strncpy(game_send.username1, clientname, 32);
		strncpy(game_send.username2, gamename, 32);
		game_send.content[0]=c;
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);
	}
	else if(t == 7) {//发送游戏的出招：布
		char c = '3';
		game_send.type = 6;
		game_send.opt = 12;
		strncpy(game_send.username1, clientname, 32);
		strncpy(game_send.username2, gamename, 32);
		game_send.content[0]=c;
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);
	}
	else if( t == 8) { //发送离开游戏消息
		game_send.type = 6;
		game_send.opt = 13;
		strncpy(game_send.username1, clientname, 32);
		send(gsockfd, &game_send, sizeof(MESSAGE), 0);
	}
	return 0;
}
* */

 void flush() {
	char c;
    while((c = getchar()) != '\n' && c != EOF);
}

void printline() {
	printf("----------------------------------------\n");
}

int login(int sockfd) {
	MESSAGE send_msg, recv_msg;
	char username[32];
	char passwd[32];
	memset(&send_msg,0,sizeof(MESSAGE));
	memset(&recv_msg,0,sizeof(MESSAGE));
	//deal with the functions for exit
	printf("If you want to exit, input (#)\n");
	printline();	
	printf("Please input your username: ");
	scanf("%s", username);
	flush();
	if(strcmp("#",username) == 0) 
		return -1;
	printf("Please input your password: ");
	scanf("%s", passwd);
	flush();
	if(strcmp("#",passwd) == 0) 
		return -1;
	//fill in the send message
	send_msg.type = 0;
	send_msg.opt = 0;
	strncpy(send_msg.username1,username,32);
	strncpy(send_msg.content,passwd,32);
	//pthread_mutex_lock(&usersocket_lock);
	send(sockfd, &send_msg,sizeof(MESSAGE),0);	
	//pthread_mutex_unlock(&usersocket_lock);
	//pthread_mutex_lock(&usersocket_lock);
	if(recv(sockfd, &recv_msg,sizeof(MESSAGE),0) >0) {
		//pthread_mutex_unlock(&usersocket_lock);
		if(recv_msg.type == 0) {
			if(recv_msg.opt == 1) {
				printf("Login ok!\n");
				strncpy(clientname, username, 32);
				return 1;
			}
			else if(recv_msg.opt == 2) {
				printf("The username doesn't exists!\n");
				return 2;
			}
			else if(recv_msg.opt == 3) {
				printf("The username has logged in!\n");
				return 3;
			}
			else if(recv_msg.opt == 4) {
				printf("Error password!\n");
				return 4;
			}
		}
	}
	//pthread_mutex_unlock(&usersocket_lock);
	return 0;
}

int reg(int sockfd) {
	MESSAGE send_msg, recv_msg;
	char uname[32];
	char pword[32];
	memset(&send_msg,0,sizeof(MESSAGE));
	memset(&recv_msg,0,sizeof(MESSAGE));
	//deal with the functions for exit
	printf("If you want to exit, input (#)\n");
	printline();	
	printf("Please input your username: ");
	scanf("%s", uname);
	flush();
	if(strcmp("#",uname) == 0) 
		return -1;
	printf("Please input your password: ");
	scanf("%s", pword);
	flush();
	if(strcmp("#",pword) == 0) 
		return -1;
	
	send_msg.type = 1;
	send_msg.opt = 0;
	strncpy(send_msg.username1,uname,32);
	strncpy(send_msg.content,pword,32);
	//pthread_mutex_lock(&usersocket_lock);
	send(sockfd, &send_msg,sizeof(MESSAGE),0);	
	//pthread_mutex_unlock(&usersocket_lock);
	//pthread_mutex_lock(&usersocket_lock);
	if(recv(sockfd, &recv_msg,sizeof(MESSAGE),0) >0) {
		//pthread_mutex_unlock(&usersocket_lock);
		if(recv_msg.type == 1) {
			if(recv_msg.opt == 1) {
				printf("Registration OK!\n");
				strncpy(clientname, uname, 32);
				return 1;
			}
			else if(recv_msg.opt == 2) {
				printf("The username existed!\n");
				return 2;
			}
			else {
				printf("Error Reserved!\n");	
				return 3;		
			}
		}
	}
	//pthread_mutex_unlock(&usersocket_lock);
	return 0;
}

void logout(int sockfd) {
	MESSAGE logout_msg;
	memset(&logout_msg, 0, sizeof(MESSAGE));
	logout_msg.type = 4;
	logout_msg.opt = 0;
	strncpy(logout_msg.username1,clientname,32);
	//pthread_mutex_lock(&usersocket_lock);
	send(sockfd, &logout_msg,sizeof(MESSAGE),0);
	//pthread_mutex_unlock(&usersocket_lock);
}
