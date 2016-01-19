#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <netinet/in.h>  //struct sockaddr_in
#include <sys/socket.h> //socket
#include <string.h>
#include <arpa/inet.h> //solve the problem of implicit definition of function -> inet_addr()

#define MAXLINE 4096
//get from wireshark
#define CLIENT_PORT 52881
#define SERV_PORT 6667
#define CLIENT_IP "192.168.242.133"
#define SERV_IP "114.212.191.43"

/* warnings!
#define min(x, y) ({                            \
         typeof(x) _min1 = (x);                  \
         typeof(y) _min2 = (y);                  \
         (void) (&_min1 == &_min2);              \
         _min1 < _min2 ? _min1 : _min2; })
// http://blog.sbw.so/Article/index/title/Linux%20%E6%BA%90%E7%A0%81%E4%B8%AD%E5%AF%B9%20min,max%20%E5%87%BD%E6%95%B0%E7%9A%84%E5%AE%8F%E5%AE%9E%E7%8E%B0.html

*/
extern void dayDisplay(char x);
extern void timeDisplay(char x);
extern int min(int a, int b);
int main(int argc, char *argv[]) {

	/*first send a tcp packet,
	code from the book page 158, appendix D.10*/
	int socket_fd;
	struct sockaddr_in servaddr;
	char sendline[MAXLINE], recvline[MAXLINE];
	//here i won't check the argcs for there is no need to input arguments
	if(argc != 1) {
		perror("Usage: ./course\n");
		exit(1);
	}

	//create a socket for the client
	socket_fd = socket(AF_INET, SOCK_STREAM, 0);
	if(socket_fd <0) {
		perror("Create socket error\n");
		exit(1);
	}

	//fill in the socket address for server
	memset(&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = inet_addr(SERV_IP);
	servaddr.sin_port=htons(SERV_PORT);

	//connect to the server
	connect(socket_fd, (struct sockaddr *) &servaddr, sizeof(servaddr));

	char option = '0';
	int status = 0;

	while(1) {
		memset(sendline,0,MAXLINE);
		memset(recvline,0,MAXLINE);
		switch(status) {
		case 0: {
		printf("Welcome to NJUCS Course Information Demo Program!\nPlease choose Type of Enquiry.\n1、Querying By Course Name\n2、Querying By Grade and DateTime\n(c)cls,(#)exit\n");
		//try to send a message
		char choose[128];
A:  	memset(choose,0,128);
		fgets(choose,128,stdin);
		int chooselen = strlen(choose);
		if (chooselen <= 2) {
			option = choose[0];		
			if(option == '1') {status = 1; option='0'; break;}
			else if(option == '2') {status = 2; option='0'; break;}
			else if(option == 'c' || option == '\n') {system("clear"); option='0'; break;}
			else if(option == '#') {status = 4; option='0'; break;}
			else { printf("Input error!\n"); status = 0; option='0'; goto A;}
		}
		else {
			printf("Input error!\n"); status = 0; option='0'; goto A;
		}
		//printf("status = %d\n\n",status);
		} break;
		case 1:  {
			printf("Please enter the Course Name to query (We return the courses that contain the string you input)\n(r)back,(c)cls,(#)exit\n");
			char input[128];
			memset(input,0,128);
			scanf("%s", input);
			//input ends with '\n', need to remove!
			if(input[strlen(input)-1] == '\n'){
				//printf("\n\nwei!\n\n");
				input[strlen(input)-1]='\0';
			}
			sendline[0]='1';
			if(strlen(input) > 1) {
				strcpy(&sendline[1],input);
				//printf("%s\n",sendline);
				//for sendline[0] is occupied with 1 or 2
				sendline[strlen(input)+1]='\0';
				//Each request send length is 33!
				//when define strlen(sendline), error! and no response!
				if(send(socket_fd,sendline,33,0) < 0)
					printf("Send failed!\n");
				//printf("Send success!\n");
				//We'll get two packet, the second has the data!
				if(recv(socket_fd,recvline,MAXLINE,0) == 0) {
					perror("The server terminated prematurely");
					exit(4);
				}

				//Get the message
				//printf("String from the server: \n");
				int i = 0;
				if(recvline[0] == 'A') {
					//printf("Query success!\n");
				}
				else {
					printf("Query Error1!\nSorry, Server does not have course information for course name %s!\n",input);
					break;//exit(1);
				}
				// The count contains in the very first package
				int count = (int)recvline[1];
				printf("======================================================\n");
				printf("find %d courses information for course name %s\n", count, input);

				//second package!
				memset(recvline,0,MAXLINE);
				int t = recv(socket_fd,recvline,MAXLINE,0) ;
				if(t == 0) {
					perror("The server terminated prematurely");
					exit(4);
				}
				//Get the message
				//printf("\nString from the server: \n");
				int j = 0;
				i = 0;
				//1: class
				//2: grade
				//3: room
				//4: datetime
				int next_p = 0;
				while (i<MAXLINE && next_p == 0) {
					//name
					printf("------------------------------------------------------\n");
					int name_start = i;
					if(recvline[name_start] == '\0')
						break;
					printf("name: %s",&recvline[name_start]);
					//printf("namelength: %d\n", (int)strlen(&recvline[i]));
					//when search for "二"，因为课程名太长，所以实际上跨越到了class域，需要额外处理，否则会出错
					i = name_start + min(32,strlen(&recvline[name_start]));
					//class
					int class_start = i;
					for(j = 0; j<100; j++) {
						if(recvline[i+j] != '\0') {
							class_start=i+j;
							break;
						}
					}
					if(j == 100) {
						next_p = 1;
						break;
					}
					printf("class: %s",&recvline[class_start]);
					//printf("classlength: %d\n", (int)strlen(&recvline[i]));
					i = class_start + strlen(&recvline[class_start]);
					//grade
					int grade_start = i;
					for(j = 0; j<100; j++) {
						if(recvline[i+j] != '\0') {
							grade_start=i+j;
							break;
						}
					}
					if(j == 100) {
						next_p = 2;
						break;
					}
					printf("grade: %c\n", recvline[grade_start]);
					int room_start = grade_start+1;
					//classroom
					if(recvline[room_start] =='\0') {
						next_p = 3;
						break;
					}
					printf("classroom: %s", &recvline[room_start]);
					i = room_start+strlen(&recvline[room_start]);
					//datetime
					int datetime_start = i;
					for(j = 0; j<100; j++) {
						if(recvline[i+j] != '\0') {
							datetime_start=i+j;
							break;
						}
					}
					if(j == 100) {
						next_p = 4;
						break;
					}
					//char day = recvline[i];
					dayDisplay(recvline[datetime_start]);
					if(recvline[datetime_start+2] == '9') {
						printf("%c-10\n",recvline[datetime_start+1]+1);
					}
					else {
						printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
					}
					i = datetime_start + 3;
				}


				while(next_p != 0) {
					//third or other next package!
					memset(recvline,0,MAXLINE);
					if(recv(socket_fd,recvline,MAXLINE,0) == 0) {
						perror("The server terminated prematurely");
						exit(4);
					}
					//now we've got the next packages
					//printf("next_p= %d\n\n\n", next_p);
					switch(next_p) {
						case 1: //class next，不会发生课程名太长的情况
							i = 0;
							next_p = 0;
							while (i<MAXLINE) {
								int class_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										class_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 1;
									break;
								}
								printf("class: %s",&recvline[class_start]);
								//printf("classlength: %d\n", (int)strlen(&recvline[i]));
								i = class_start + strlen(&recvline[class_start]);
								//grade
								int grade_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										grade_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 2;
									break;
								}
								printf("grade: %c\n", recvline[grade_start]);
								int room_start = grade_start+1;
								//classroom
								if(recvline[room_start] =='\0') {
									next_p = 3;
									break;
								}
								printf("classroom: %s", &recvline[room_start]);
								i = room_start+strlen(&recvline[room_start]);
								//datetime
								int datetime_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										datetime_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 4;
									break;
								}
								//char day = recvline[i];
								dayDisplay(recvline[datetime_start]);
								if(recvline[datetime_start+2] == '9') {
									printf("%c-10\n",recvline[datetime_start+1]+1);
								}
								else {
									printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
								}
								i = datetime_start + 3;
								//name
								printf("----------------------------------------------\n");
								int name_start = i;
								if(recvline[name_start] == '\0')
									break;
								printf("name: %s",&recvline[name_start]);
								//printf("namelength: %d\n", (int)strlen(&recvline[i]));
								i = name_start + strlen(&recvline[name_start]);
							}
							break;
							case 2: //grade next
							i = 0;
							next_p = 0;
							while(i < MAXLINE) {
									int grade_start = i;
									for(j = 0; j<100; j++) {
										if(recvline[i+j] != '\0') {
											grade_start=i+j;
											break;
										}
									}
									if(j == 100) {
										next_p = 2;
										break;
									}
									printf("grade: %c\n", recvline[grade_start]);
									int room_start = grade_start+1;
									//classroom
									if(recvline[room_start] =='\0') {
										next_p = 3;
										break;
									}
									printf("classroom: %s", &recvline[room_start]);
									i = room_start+strlen(&recvline[room_start]);
									//datetime
									int datetime_start = i;
									for(j = 0; j<100; j++) {
										if(recvline[i+j] != '\0') {
											datetime_start=i+j;
											break;
										}
									}
									if(j == 100) {
										next_p = 4;
										break;
									}
									//char day = recvline[i];
									dayDisplay(recvline[datetime_start]);
									if(recvline[datetime_start+2] == '9') {
										printf("%c-10\n",recvline[datetime_start+1]+1);
									}
									else {
										printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
									}
									i = datetime_start + 3;
									//name
									printf("----------------------------------------------\n");
									int name_start = i;
									if(recvline[name_start] == '\0')
										break;
									printf("name: %s",&recvline[name_start]);
									//printf("namelength: %d\n", (int)strlen(&recvline[i]));
									i = name_start + min(32,strlen(&recvline[name_start]));
									int class_start = i;
									for(j = 0; j<100; j++) {
										if(recvline[i+j] != '\0') {
											class_start=i+j;
											break;
										}
									}
									if(j == 100) {
										next_p = 1;
										break;
									}
									printf("class: %s",&recvline[class_start]);
									//printf("classlength: %d\n", (int)strlen(&recvline[i]));
									i = class_start + strlen(&recvline[class_start]);
								}
							break;

							case 3 : //classroom next
							i = 0;
							next_p = 0;
							while(i<MAXLINE) {
								int room_start = i;
								//classroom
								if(recvline[room_start] =='\0') {
									next_p = 3;
									break;
								}
								printf("classroom: %s", &recvline[room_start]);
								i = room_start+strlen(&recvline[room_start]);
								//datetime
								int datetime_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										datetime_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 4;
									break;
								}
								//char day = recvline[i];
								dayDisplay(recvline[datetime_start]);
								if(recvline[datetime_start+2] == '9') {
									printf("%c-10\n",recvline[datetime_start+1]+1);
								}
								else {
									printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
								}
								i = datetime_start + 3;
								//name
								printf("----------------------------------------------\n");
								int name_start = i;
								if(recvline[name_start] == '\0')
									break;
								printf("name: %s",&recvline[name_start]);
								//printf("namelength: %d\n", (int)strlen(&recvline[i]));
								i = name_start + min(32,strlen(&recvline[name_start]));
								//class
								int class_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										class_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 1;
									break;
								}
								printf("class: %s",&recvline[class_start]);
								//printf("classlength: %d\n", (int)strlen(&recvline[i]));
								i = class_start + strlen(&recvline[class_start]);
								//grade
								int grade_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										grade_start=i+j;
										break;
									}
								}
								if(j == 100) {
										next_p = 2;
										break;
									}
								printf("grade: %c\n", recvline[grade_start]);
								i = grade_start+1;
							}
							break;

							case 4: //datetime next
							i = 0;
							next_p = 0;
							while (i<MAXLINE) {
								int datetime_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										datetime_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 4;
									break;
								}
								//char day = recvline[i];
								dayDisplay(recvline[datetime_start]);
								if(recvline[datetime_start+2] == '9') {
									printf("%c-10\n",recvline[datetime_start+1]+1);
								}
								else {
									printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
								}
								i = datetime_start + 3;
								//name
								printf("----------------------------------------------\n");
								int name_start = i;
								if(recvline[name_start] == '\0')
									break;
								printf("name: %s",&recvline[name_start]);
								//printf("namelength: %d\n", (int)strlen(&recvline[i]));
								i = name_start + min(32,strlen(&recvline[name_start]));
								//class
								int class_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										class_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 1;
									break;
								}
								printf("class: %s",&recvline[class_start]);
								//printf("classlength: %d\n", (int)strlen(&recvline[i]));
								i = class_start + strlen(&recvline[class_start]);
								//grade
								int grade_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										grade_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 2;
									break;
								}
								printf("grade: %c\n", recvline[grade_start]);
								int room_start = grade_start+1;
								//classroom
								if(recvline[room_start] =='\0') {
									next_p = 3;
									break;
								}
								printf("classroom: %s", &recvline[room_start]);
								i = room_start+strlen(&recvline[room_start]);
							}
							break;

							default: break;
					}
				}
				memset(sendline,0,MAXLINE);
				memset(recvline,0,MAXLINE);
			}// when input length > 0
			if (strlen(input) == 1) {
				option = input[0];
			}
			if(option == 'r') {status = 0; option='0'; break;}
			//if you do not make sure the option, then it will be cleared everytime it search success!
			if(option == 'c') {system("clear"); option='0'; break;}
			if(option == '#') {status = 4; option='0'; break;}
		} break;//end for do the work query 1
		case 2: {
			printf("Grade (Grade:1,2,3,4)  Date (Mon:1,Tue:2,Wen:3,Thu:4,Fri:5) Time (ALLDAY:0,AM:1, PM:2)\nPlease enter the Grade Date Time (e.g. 1,1,0)\n(r)back,(c)cls,(#)exit\n");
			char input[32];
			scanf("%s", input);
			//input ends with '\n', need to remove!
			if(input[strlen(input)-1] == '\n'){
				//printf("\n\nwei!\n\n");
				input[strlen(input)-1]='\0';
			}
			sendline[0]='2';
			if(strlen(input) > 1) {
				//check input legal!
				if(input[1]!=',' || input[3] != ',') {
					printf("Input error!\n");
					break;
				}
				if(input[0] <= '0' || input[0] >='5') {
					printf("Input error0!\n");
					break;
				}
				if(input[2] <= '0' || input[2] >='6') {
					printf("Input error!\n");
					break;
				}
				if(input[4] < '0' || input[4] >='3') {
					printf("Input error!\n");
					break;
				}
				sendline[1]=input[0];
				sendline[2]=input[2];
				sendline[3]=input[4];
				//ignore input[5] and the later letters!
				sendline[4]='\0';
				//Each request send length is 33!
				//when define strlen(sendline), error! and no response!
				if(send(socket_fd,sendline,33,0) < 0)
					printf("Send failed!\n");
				//printf("Send success!\n");
				//We'll get two packet, the second has the data!
				if(recv(socket_fd,recvline,MAXLINE,0) == 0) {
					perror("The server terminated prematurely");
					exit(4);
				}

				//Get the message
				//printf("String from the server: \n");
				int i = 0;
				if(recvline[0] == '1') {
					printf("Query success!\n");
				}
				else if(recvline[0] == '2'){
					printf("Query Error1!\nSorry, Server does not have course for given grade: %c, ",input[0]);
					dayDisplay(input[2]);
					timeDisplay(input[4]);
					break;//exit(1);
				}
				// The count contains in the very first package
				int count = (int)recvline[1];
				printf("======================================================\n");
				printf("find %d courses information for given grade: %c, ", count, input[0]);
				dayDisplay(input[2]);
				timeDisplay(input[4]);

				//second package!
				memset(recvline,0,MAXLINE);
				int t = recv(socket_fd,recvline,MAXLINE,0) ;
				if(t == 0) {
					perror("The server terminated prematurely");
					exit(4);
				}

				//Show the result
				int j = 0;
				i = 0;
				//1: class
				//2: grade
				//3: room
				//4: datetime
				int next_p = 0;
				while (i<MAXLINE && next_p == 0) {
					//name
					printf("------------------------------------------------------\n");
					int name_start = i;
					if(recvline[name_start] == '\0')
						break;
					printf("name: %s",&recvline[name_start]);
					//printf("namelength: %d\n", (int)strlen(&recvline[i]));
					//when search for "二"，因为课程名太长，所以实际上跨越到了class域，需要额外处理，否则会出错
					i = name_start + min(32,strlen(&recvline[name_start]));
					//class
					int class_start = i;
					for(j = 0; j<100; j++) {
						if(recvline[i+j] != '\0') {
							class_start=i+j;
							break;
						}
					}
					if(j == 100) {
						next_p = 1;
						break;
					}
					printf("class: %s",&recvline[class_start]);
					//printf("classlength: %d\n", (int)strlen(&recvline[i]));
					i = class_start + strlen(&recvline[class_start]);
					//grade
					int grade_start = i;
					for(j = 0; j<100; j++) {
						if(recvline[i+j] != '\0') {
							grade_start=i+j;
							break;
						}
					}
					if(j == 100) {
						next_p = 2;
						break;
					}
					printf("grade: %c\n", recvline[grade_start]);
					int room_start = grade_start+1;
					//classroom
					if(recvline[room_start] =='\0') {
						next_p = 3;
						break;
					}
					printf("classroom: %s", &recvline[room_start]);
					i = room_start+strlen(&recvline[room_start]);
					//datetime
					int datetime_start = i;
					for(j = 0; j<100; j++) {
						if(recvline[i+j] != '\0') {
							datetime_start=i+j;
							break;
						}
					}
					if(j == 100) {
						next_p = 4;
						break;
					}
					//char day = recvline[i];
					dayDisplay(recvline[datetime_start]);
					//deal with the datetime is 9 but it should be 10 not the '9'+1=':'
					if(recvline[datetime_start+2] == '9') {
						printf("%c-10\n",recvline[datetime_start+1]+1);
					}
					else {
						printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
					}
					i = datetime_start + 3;
				}


				while(next_p != 0) {
					//third or other next package!
					memset(recvline,0,MAXLINE);
					if(recv(socket_fd,recvline,MAXLINE,0) == 0) {
						perror("The server terminated prematurely");
						exit(4);
					}
					//now we've got the next packages
					//printf("next_p= %d\n\n\n", next_p);
					switch(next_p) {
						case 1: //class next，不会发生课程名太长的情况
							i = 0;
							next_p = 0;
							while (i<MAXLINE) {
								int class_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										class_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 1;
									break;
								}
								printf("class: %s",&recvline[class_start]);
								//printf("classlength: %d\n", (int)strlen(&recvline[i]));
								i = class_start + strlen(&recvline[class_start]);
								//grade
								int grade_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										grade_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 2;
									break;
								}
								printf("grade: %c\n", recvline[grade_start]);
								int room_start = grade_start+1;
								//classroom
								if(recvline[room_start] =='\0') {
									next_p = 3;
									break;
								}
								printf("classroom: %s", &recvline[room_start]);
								i = room_start+strlen(&recvline[room_start]);
								//datetime
								int datetime_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										datetime_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 4;
									break;
								}
								//char day = recvline[i];
								dayDisplay(recvline[datetime_start]);
								if(recvline[datetime_start+2] == '9') {
									printf("%c-10\n",recvline[datetime_start+1]+1);
								}
								else {
									printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
								}
								i = datetime_start + 3;
								//name
								printf("----------------------------------------------\n");
								int name_start = i;
								if(recvline[name_start] == '\0')
									break;
								printf("name: %s",&recvline[name_start]);
								//printf("namelength: %d\n", (int)strlen(&recvline[i]));
								i = name_start + strlen(&recvline[name_start]);
							}
							break;
							case 2: //grade next
							i = 0;
							next_p = 0;
							while(i < MAXLINE) {
									int grade_start = i;
									for(j = 0; j<100; j++) {
										if(recvline[i+j] != '\0') {
											grade_start=i+j;
											break;
										}
									}
									if(j == 100) {
										next_p = 2;
										break;
									}
									printf("grade: %c\n", recvline[grade_start]);
									int room_start = grade_start+1;
									//classroom
									if(recvline[room_start] =='\0') {
										next_p = 3;
										break;
									}
									printf("classroom: %s", &recvline[room_start]);
									i = room_start+strlen(&recvline[room_start]);
									//datetime
									int datetime_start = i;
									for(j = 0; j<100; j++) {
										if(recvline[i+j] != '\0') {
											datetime_start=i+j;
											break;
										}
									}
									if(j == 100) {
										next_p = 4;
										break;
									}
									//char day = recvline[i];
									dayDisplay(recvline[datetime_start]);
									if(recvline[datetime_start+2] == '9') {
										printf("%c-10\n",recvline[datetime_start+1]+1);
									}
									else {
										printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
									}
									i = datetime_start + 3;
									//name
									printf("----------------------------------------------\n");
									int name_start = i;
									if(recvline[name_start] == '\0')
										break;
									printf("name: %s",&recvline[name_start]);
									//printf("namelength: %d\n", (int)strlen(&recvline[i]));
									i = name_start + min(32,strlen(&recvline[name_start]));
									int class_start = i;
									for(j = 0; j<100; j++) {
										if(recvline[i+j] != '\0') {
											class_start=i+j;
											break;
										}
									}
									if(j == 100) {
										next_p = 1;
										break;
									}
									printf("class: %s",&recvline[class_start]);
									//printf("classlength: %d\n", (int)strlen(&recvline[i]));
									i = class_start + strlen(&recvline[class_start]);
								}
							break;

							case 3 : //classroom next
							i = 0;
							next_p = 0;
							while(i<MAXLINE) {
								int room_start = i;
								//classroom
								if(recvline[room_start] =='\0') {
									next_p = 3;
									break;
								}
								printf("classroom: %s", &recvline[room_start]);
								i = room_start+strlen(&recvline[room_start]);
								//datetime
								int datetime_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										datetime_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 4;
									break;
								}
								//char day = recvline[i];
								dayDisplay(recvline[datetime_start]);
								if(recvline[datetime_start+2] == '9') {
									printf("%c-10\n",recvline[datetime_start+1]+1);
								}
								else {
									printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
								}
								i = datetime_start + 3;
								//name
								printf("----------------------------------------------\n");
								int name_start = i;
								if(recvline[name_start] == '\0')
									break;
								printf("name: %s",&recvline[name_start]);
								//printf("namelength: %d\n", (int)strlen(&recvline[i]));
								i = name_start + min(32,strlen(&recvline[name_start]));
								//class
								int class_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										class_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 1;
									break;
								}
								printf("class: %s",&recvline[class_start]);
								//printf("classlength: %d\n", (int)strlen(&recvline[i]));
								i = class_start + strlen(&recvline[class_start]);
								//grade
								int grade_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										grade_start=i+j;
										break;
									}
								}
								if(j == 100) {
										next_p = 2;
										break;
									}
								printf("grade: %c\n", recvline[grade_start]);
								i = grade_start+1;
							}
							break;

							case 4: //datetime next
							i = 0;
							next_p = 0;
							while (i<MAXLINE) {
								int datetime_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										datetime_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 4;
									break;
								}
								//char day = recvline[i];
								dayDisplay(recvline[datetime_start]);
								if(recvline[datetime_start+2] == '9') {
									printf("%c-10\n",recvline[datetime_start+1]+1);
								}
								else {
									printf("%c-%c\n",recvline[datetime_start+1]+1,recvline[datetime_start+2]+1);
								}
								i = datetime_start + 3;
								//name
								printf("----------------------------------------------\n");
								int name_start = i;
								if(recvline[name_start] == '\0')
									break;
								printf("name: %s",&recvline[name_start]);
								//printf("namelength: %d\n", (int)strlen(&recvline[i]));
								i = name_start + min(32,strlen(&recvline[name_start]));
								//class
								int class_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										class_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 1;
									break;
								}
								printf("class: %s",&recvline[class_start]);
								//printf("classlength: %d\n", (int)strlen(&recvline[i]));
								i = class_start + strlen(&recvline[class_start]);
								//grade
								int grade_start = i;
								for(j = 0; j<100; j++) {
									if(recvline[i+j] != '\0') {
										grade_start=i+j;
										break;
									}
								}
								if(j == 100) {
									next_p = 2;
									break;
								}
								printf("grade: %c\n", recvline[grade_start]);
								int room_start = grade_start+1;
								//classroom
								if(recvline[room_start] =='\0') {
									next_p = 3;
									break;
								}
								printf("classroom: %s", &recvline[room_start]);
								i = room_start+strlen(&recvline[room_start]);
							}
							break;

							default: break;
					}
				}
				memset(sendline,0,MAXLINE);
				memset(recvline,0,MAXLINE);
			}// when input length >=1
			if (strlen(input) == 1) {
				option = input[0];
			}
			if(option == 'r') {status = 0; option='0'; break;}
			if(option == 'c') {system("clear"); option='0'; break;}
			if(option == '#') {status = 4; option='0'; break;}
			} break;
		case 4: {
			close(socket_fd);
			exit(0);

			} break;
	default: break;
	}
}//end of while!
	close(socket_fd);
	return 0;
}

void dayDisplay(char x) {
	if(x == '1') printf("datetime: MON, ");
	if(x == '2') printf("datetime: TUE, ");
	if(x == '3') printf("datetime: WED, ");
	if(x == '4') printf("datetime: THU, ");
	if(x == '5') printf("datetime: FRI, ");
}

void timeDisplay(char x) {
	if(x == '0') printf("ALLDAY\n");
	if(x == '1') printf("AM\n");
	if(x == '2') printf("PM\n");
}

int min(int a, int b) {
	return (a<b)?a:b;
}
