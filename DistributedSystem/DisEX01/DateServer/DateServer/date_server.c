/*
 * This is sample code generated by rpcgen.
 * These are only templates and you can use them
 * as a guideline for developing your own functions.
 */
#define _CRT_SECURE_NO_WARNINGS
#include "date.h"

long t1, t2, t3, t4;
long d; // delay
long t; //offset
unsigned int names[MAXCLIENT];
int names_size = 0;

long timeget()
{
	static long thetime;
	thetime = time((long*)0);
	return thetime;
}


unsigned int BKDRHash(char *str)
{
	unsigned int seed = 131; // 31 131 1313 13131 131313 etc..
	unsigned int hash = 0;

	while (*str)
	{
		hash = hash * seed + (*str++);
	}

	return (hash & 0x7FFFFFFF);
}


void init_client_info() {
	FILE * fp = fopen("auth", "r+");
	if (fp == NULL) {
		printf("Authentic users loading error!\n");
		exit(-1);
	}
	char buf[32];
	size_t len = 0;
	unsigned int read;
	int num_of_clients = 0;
	while (!feof(fp)) {
		fscanf(fp, "%s", buf);
		names[num_of_clients] = BKDRHash(buf);
		num_of_clients++;
	}
	names_size = num_of_clients;
	fclose(fp);
}



sntp *raw_date_1_svc(sntp *argp, struct svc_req *rqstp)
{
	static sntp result;

	// read authentic files
	init_client_info();
	
	int flag = 0;
	for (int i = 0; i<names_size; i++) {
		printf("names = %d and keyid = %d\n", names[i], argp->keyid);
		if (names[i] == argp->keyid) {
			flag = 1;
			break;
		}
	}

	if (flag != 1) {
		result.type = -1;
		return &result;
	}
	

	printf("TYPE = %d\n", argp->type);

	// 1. origin time
	if (argp->type == 1) {
		// 2. receive time
		t2 = timeget();
		t1 = argp->org;
		printf("t1 = %ld\n", t1);
		printf("t2 = %ld\n", t2);
		// 3. transmit time
		result.type = 3;
		result.xmt = timeget();
		t3 = result.xmt;
		printf("t3 = %ld\n", t3);
		return &result;
	}

	// 4. destination time
	if (argp->type == 4) {
		t4 = argp->dst;
		printf("t4 = %ld\n", t4);
		d = (t4 - t1) - (t3 - t2);
		t = ((t2 - t1) + (t3 - t4)) / 2;
		printf("delay = %ld\n", d);
		printf("offset = %ld\n", t);
		// 5. send raw date
		result.type = 5;
		result.ref = timeget() + d / 2;
		printf("---------- End of Raw Date Call ----------\n");
		return &result;
	}

	return &result;
}

char **
str_date_1_svc(sntp *argp, struct svc_req *rqstp)
{
	static char * result;
	
	init_client_info();

	int flag = 0;
	for (int i = 0; i<names_size; i++) {
		printf("names = %d and keyid = %d\n", names[i], argp->keyid);
		if (names[i] == argp->keyid) {
			flag = 1;
			break;
		}
	}

	if (flag != 1) {
		char invalidString[32] = "Invalid Request!";
		result = invalidString;
		return &result;
	}
	

	printf("TYPE = %d\n", argp->type);

	if (argp->type == 6) {
		char *ctime();
		time_t p = argp->ref;
		time(&p);
		result = ctime(&p);
		if (result == (char *)NULL) {
			printf("Error!");
		}
		// printf("result = %s\n", result);
	}

	printf("---------- End of String Date Call ----------\n");
	return &result;
}
