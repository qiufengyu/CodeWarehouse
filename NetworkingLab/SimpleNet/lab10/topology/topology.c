//文件名: topology/topology.c
//
//描述: 这个文件实现一些用于解析拓扑文件的辅助函数 
//
//创建日期: 2015年

#include "topology.h"

int gethostname(char *hostname,size_t size);

//define struct topo to store info in topology.dat
typedef struct topo {
	char host1[32];
	char host2[32];
	int id1;
	int id2;
	unsigned int cost;
	struct topo* next;
} TOPO;
TOPO *head_topo=NULL;

typedef struct hostname{
	char host[32];
	unsigned int ip;
	struct hostname* next;
} HOSTNAME;
HOSTNAME *head_host=NULL;



//read dat file

void read_topo() {
	FILE *fp;
	fp=fopen("../topology/topology.dat","r");
	assert(fp!=NULL);	
	char buf[32];
	int n;
	head_topo=NULL;
	while((n = fscanf(fp, "%s", buf))>0) {
		TOPO* newTopo=(TOPO*)malloc(sizeof(TOPO));
		memset(newTopo, 0, sizeof(TOPO));
		newTopo->next=NULL;
		strcpy(newTopo->host1, buf);
		newTopo->id1 = topology_getNodeIDfromname(buf);
		memset(buf, 0, 32);
		if((n = fscanf(fp, "%s", buf))>0) {
			strcpy(newTopo->host2, buf);
			newTopo->id2 = topology_getNodeIDfromname(buf);
		}
		else {
			perror("Error read file!\n");
			exit(-1);
		}
		int cost;
		if((n=fscanf(fp, "%d", &cost))>0) {
			newTopo->cost=cost;
		}
		else {
			perror("Error read file!\n");
			exit(-1);
		}
		//read finished! then add node
		if(head_topo==NULL) {
			head_topo=newTopo;
			newTopo->next=NULL;
		}
		else {
			newTopo->next=head_topo;
			head_topo=newTopo;
		}
	}
	fclose(fp);
	return;
}

void read_host() {
	FILE *fp;
	fp=fopen("../topology/hostname.dat","r");
	assert(fp!=NULL);
	char buf[32];
	int n;
	head_host=NULL;
	while((n=fscanf(fp, "%s", buf))>0) {
		HOSTNAME *newHostname = (HOSTNAME*)malloc(sizeof(HOSTNAME));
		memset(newHostname, 0, sizeof(HOSTNAME));
		newHostname->next=NULL;
		strcpy(newHostname->host, buf);
		if((n=fscanf(fp, "%s", buf))>0) {
			newHostname->ip = inet_addr(buf);
		}
		if(head_host==NULL) {
			newHostname->next=NULL;
			head_host=newHostname;
		}
		else {
			newHostname->next = head_host;
			head_host=newHostname;
		}
	}
	fclose(fp);
	return;
}
//这个函数返回指定主机的节点ID.
//节点ID是节点IP地址最后8位表示的整数.
//例如, 一个节点的IP地址为202.119.32.12, 它的节点ID就是12.
//如果不能获取节点ID, 返回-1.
int topology_getNodeIDfromname(char* hostname) 
{
	read_host();
	HOSTNAME *myname = head_host;
	while(myname!=NULL) {
		if(strcmp(hostname, myname->host) == 0) {
			unsigned int myip = myname->ip;
			int node = myip>>24;
			return node;
		}
		myname = myname->next;
	}
	return -1;
}

//这个函数返回指定的IP地址的节点ID.
//如果不能获取节点ID, 返回-1.
int topology_getNodeIDfromip(struct in_addr* addr)
{
	int node = (addr->s_addr>>24);
	return node;
}

//这个函数返回本机的节点ID
//如果不能获取本机的节点ID, 返回-1.
int topology_getMyNodeID()
{
	if(head_host == NULL)
		read_host();
	char name[32];
	gethostname(name, sizeof(name));
	return topology_getNodeIDfromname(name);
}

//这个函数解析保存在文件topology.dat中的拓扑信息.
//返回邻居数.
int topology_getNbrNum()
{
	read_topo();
	char name[32];
	gethostname(name, sizeof(name));
	TOPO *mytopo = head_topo;
	int num = 0;
	while(mytopo!=NULL) {
		if(strcmp(mytopo->host1, name)==0 || strcmp(mytopo->host2, name) == 0) {
			num++;
		}
		mytopo = mytopo->next;
	}
	return num;
}

//这个函数解析保存在文件topology.dat中的拓扑信息.
//返回重叠网络中的总节点数.
int topology_getNodeNum()
{
	if(head_topo == NULL)
		read_topo();
	int sum_node[MAX_NODE_NUM];
	int i = 0;
	for(i=0; i<MAX_NODE_NUM; i++)
		sum_node[i]=-1;
	int count=0;
	TOPO *mytopo = head_topo;
	while(mytopo!=NULL) {
		for(i=0; i<MAX_NODE_NUM; i++) {
			if(sum_node[i] == mytopo->id1)
				break;
		}
		if(MAX_NODE_NUM == i) {
			sum_node[count]=mytopo->id1;
			count++;
		}
		for(i=0; i<MAX_NODE_NUM; i++) {
			if(sum_node[i] == mytopo->id2)
				break;
		}
		if(MAX_NODE_NUM == i) {
			sum_node[count]=mytopo->id2;
			count++;
		}
		mytopo=mytopo->next;		
	}
	return count;
}

//这个函数解析保存在文件topology.dat中的拓扑信息.
//返回一个动态分配的数组, 它包含重叠网络中所有节点的ID. 
int* topology_getNodeArray()
{
	if(head_topo == NULL)
		read_topo();
	int num = topology_getNodeNum();
	int *sum_node= (int *)malloc(sizeof(int) * num);
	memset((char *)sum_node, 0, sizeof(int) * num);
	int count=0;
	TOPO *mytopo= head_topo;
	while(mytopo!=NULL) {
		int i=0;
		for(i=0; i<num; i++) {
			if(sum_node[i] == mytopo->id1)
				break;
		}
		if(num == i) {
			sum_node[count]=mytopo->id1;
			count++;
		}
		for(i=0; i<num; i++) {
			if(sum_node[i] == mytopo->id2)
				break;
		}
		if(num == i) {
			sum_node[count]=mytopo->id2;
			count++;
		}
		mytopo=mytopo->next;		
	}
	return sum_node;
}

//这个函数解析保存在文件topology.dat中的拓扑信息.
//返回一个动态分配的数组, 它包含所有邻居的节点ID.  
int* topology_getNbrArray()
{
	if(head_topo == NULL)
		read_topo();
	int *nbr_node = (int *)malloc(sizeof(int)* topology_getNbrNum());
	char name[32];
	gethostname(name, 32);
	TOPO *mytopo = head_topo;
	int count=0;
	while(mytopo!=NULL) {
		if(strcmp(mytopo->host1, name) == 0) {
			nbr_node[count] = topology_getNodeIDfromname(mytopo->host2);
			count++;
		}
		else if(strcmp(mytopo->host2, name) == 0) {
			nbr_node[count] = topology_getNodeIDfromname(mytopo->host1);
			count++;
		}
		mytopo=mytopo->next;
	}
	return nbr_node;
}

//这个函数解析保存在文件topology.dat中的拓扑信息.
//返回指定两个节点之间的直接链路代价. 
//如果指定两个节点之间没有直接链路, 返回INFINITE_COST.
unsigned int topology_getCost(int fromNodeID, int toNodeID)
{
	read_topo();
	TOPO *mytopo= head_topo;
	while(mytopo!=NULL) {
		if(mytopo->id1 == fromNodeID && mytopo->id2== toNodeID) {
			return mytopo->cost;
		}
		else if(mytopo->id2 == fromNodeID && mytopo->id1== toNodeID) {
			return mytopo->cost;
		}
		mytopo=mytopo->next;
	}
	return INFINITE_COST;
}



unsigned int topology_getfromIDtoIP(int nodeID) {
	read_host();
	HOSTNAME *myhostname = head_host;
	while(myhostname!=NULL) {
		unsigned int ip = myhostname->ip;
		int node = ip>>24;
		if(node == nodeID) {
			return ip;
		}
		myhostname = myhostname->next;
	}
	return 0;
}

