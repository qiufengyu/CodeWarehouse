#pragma comment(lib,"pwrpc32.lib")
#pragma comment(lib,"ws2_32.lib")
/*
 * This is sample code generated by rpcgen.
 * These are only templates and you can use them
 * as a guideline for developing your own functions.
 */

#include "date.h"

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

void date_prog_1(char *host, char *name)
{
	CLIENT *clnt;
	sntp  *result_xmt;
	sntp  *result_date;
	sntp  date_org_arg;
	sntp  date_dst_arg;
	sntp  date_str_arg;
	char * *result_str;


	clnt = clnt_create(host, DATE_PROG, DATE_VERS, "tcp");
	if (clnt == NULL) {
		clnt_pcreateerror(host);
		exit(1);
	}

	// 1. send origin time
	date_org_arg.type = 1;
	date_org_arg.org = timeget();
	date_org_arg.keyid = BKDRHash(name);
	// 2. receive time is record on server
	// 3. transmit time is also record on server
	// server send the transmit time
	result_xmt = raw_date_1(&date_org_arg, clnt);

	if (result_xmt == (sntp *)NULL) {
		clnt_perror(clnt, "call failed\n");
		exit(1);
	}
	if (result_xmt->type < 0) {
		clnt_perror(clnt, "Request Rejected!\n");
		exit(2);
	}
	if (result_xmt->type != 3) {
		clnt_perror(clnt, "protocal error, type should be 3!\n");
		exit(1);
	}

	// 4. send destination time
	date_dst_arg.type = 4;
	date_dst_arg.dst = timeget();
	date_dst_arg.keyid = BKDRHash(name);
	// 5. get raw date time
	result_date = raw_date_1(&date_dst_arg, clnt);

	if (result_date == (sntp *)NULL) {
		clnt_perror(clnt, "call failed\n");
		exit(1);
	}
	if (result_date->type < 0) {
		clnt_perror(clnt, "Request Rejected!\n");
		exit(2);
	}
	if (result_date->type != 5) {
		clnt_perror(clnt, "protocal error, type should be 5!\n");
		exit(1);
	}

	// 6. send long to string request
	date_str_arg.type = 6;
	date_str_arg.ref = result_date->ref;
	date_str_arg.keyid = BKDRHash(name);

	printf("time on %s is %ld\n", host, date_str_arg.ref);

	// 7. have the server convert to a date strin g
	result_str = str_date_1(&date_str_arg, clnt);
	if (result_str == (char **)NULL) {
		clnt_perror(clnt, "call failed\n");
	}
	printf("date is %s\n", *result_str);

	clnt_destroy(clnt);

}


int main(int argc, char *argv[])
{
	char *host;
	char *name;

	if (argc < 3) {
		printf("usage: %s server_host client_name\n", argv[0]);
		exit(1);
	}
	host = argv[1];
	name = argv[2];
	date_prog_1(host, name);
	exit(0);
}