
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "../common/constants.h"
#include "../topology/topology.h"
#include "dvtable.h"

//这个函数动态创建距离矢量表.
//距离矢量表包含n+1个条目, 其中n是这个节点的邻居数,剩下1个是这个节点本身.
//距离矢量表中的每个条目是一个dv_t结构,它包含一个源节点ID和一个有N个dv_entry_t结构的数组, 其中N是重叠网络中节点总数.
//每个dv_entry_t包含一个目的节点地址和从该源节点到该目的节点的链路代价.
//距离矢量表也在这个函数中初始化.从这个节点到其邻居的链路代价使用提取自topology.dat文件中的直接链路代价初始化.
//其他链路代价被初始化为INFINITE_COST.
//该函数返回动态创建的距离矢量表.
dv_t* dvtable_create()
{
	int nbrnum = topology_getNbrNum();
	int* nbrlist = topology_getNbrArray();
	dv_t *mydv = (dv_t*)malloc(sizeof(dv_t)*(nbrnum+1));
	int i = 0;
	for(i=0; i<nbrnum; i++) {
		mydv[i].nodeID = nbrlist[i];
		int nodenum = topology_getNodeNum();
		int* nodelist = topology_getNodeArray();
		mydv[i].dvEntry = (dv_entry_t*)malloc(sizeof(dv_entry_t)*nodenum);
		int j = 0;
		for(j = 0; j<nodenum; j++) {
			mydv[i].dvEntry[j].nodeID = nodelist[j];
			mydv[i].dvEntry[j].cost = INFINITE_COST;
			mydv[i].dvEntry[j].onlineflag = 1;
		}
		free(nodelist);
	}
	//initial this node
	mydv[nbrnum].nodeID = topology_getMyNodeID();
	int nodenum=topology_getNodeNum();
	int *nodelist = topology_getNodeArray();
	mydv[nbrnum].dvEntry = (dv_entry_t *)malloc(sizeof(dv_entry_t)*nodenum);
	int j = 0;
	for(j=0; j<nodenum; j++) {
		mydv[nbrnum].dvEntry[j].nodeID = nodelist[j];
		if(nodelist[j] == mydv[nbrnum].nodeID) { //to oneself
			mydv[nbrnum].dvEntry[j].cost = 0;
		}
		else {
			mydv[nbrnum].dvEntry[j].cost = topology_getCost(mydv[nbrnum].nodeID,nodelist[j]);
			mydv[nbrnum].dvEntry[j].onlineflag = 1;
			
		}
	}
	free(nodelist);
	free(nbrlist);
	return mydv;
}

//这个函数删除距离矢量表.
//它释放所有为距离矢量表动态分配的内存.
void dvtable_destroy(dv_t* dvtable)
{
	int nbrnum = topology_getNbrNum();
	int i = 0;
	for(i=0; i<nbrnum+1; i++) {
		free(dvtable[i].dvEntry);
	}
	free(dvtable);
	return;
}

//这个函数设置距离矢量表中2个节点之间的链路代价.
//如果这2个节点在表中发现了,并且链路代价也被成功设置了,就返回1,否则返回-1.
int dvtable_setcost(dv_t* dvtable,int fromNodeID,int toNodeID, unsigned int cost)
{
	int nbrnum = topology_getNbrNum()+1;
	int nodenum = topology_getNodeNum();
	int i = 0;
	for(i=0; i<nbrnum; i++) {
		if(dvtable[i].nodeID == fromNodeID) {
			dv_entry_t *list_toNode = dvtable[i].dvEntry;
			int j = 0;
			for(j=0; j<nodenum; j++) {
				if(list_toNode[j].nodeID == toNodeID) {
					list_toNode[j].cost = cost;
					return 1;
				}
			}
		}
	}
	return -1;
}

//这个函数返回距离矢量表中2个节点之间的链路代价.
//如果这2个节点在表中发现了,就返回链路代价,否则返回INFINITE_COST.
unsigned int dvtable_getcost(dv_t* dvtable, int fromNodeID, int toNodeID)
{
	int nbrnum = topology_getNbrNum()+1;
	int nodenum = topology_getNodeNum();
	int i = 0;
	for(i=0; i<nbrnum; i++) {
		if(dvtable[i].nodeID == fromNodeID) {
			dv_entry_t *list_toNode = dvtable[i].dvEntry;
			int j = 0;
			for(j=0; j<nodenum; j++) {
				if(list_toNode[j].nodeID == toNodeID) {
					return list_toNode[j].cost;
				}
			}
		}
	}
	return INFINITE_COST;
}

//这个函数打印距离矢量表的内容.
void dvtable_print(dv_t* dvtable)
{
	int nbrnum = topology_getNbrNum()+1;
	int nodenum = topology_getNodeNum();
	int i = 0;
	for(i=0; i<nbrnum; i++) {
		dv_entry_t *list_toNode = dvtable[i].dvEntry;
		int j = 0;
		for(j=0; j<nodenum; j++) {
			printf("%d -> %d, cost: %d\n", dvtable[i].nodeID, list_toNode[j].nodeID, list_toNode[j].cost);
		}
	}
	return;
}


int dvtable_getOnlineFlag(dv_t* dvtable, int srcNodeID, int destNodeID) {
	int nbrnum = topology_getNbrNum()+1;
	int nodenum = topology_getNodeNum();
	int i = 0;
	for(i=0; i<nbrnum; i++) {
		if(dvtable[i].nodeID == srcNodeID) {
			dv_entry_t *list_toNode = dvtable[i].dvEntry;
			int j = 0;
			for(j=0; j<nodenum; j++) {
				if(list_toNode[j].nodeID == destNodeID) {
					return list_toNode[j].onlineflag;
				}
			}
		}
	}
	return -1;
}

int dvtable_setOnlineFlag(dv_t* dvtable, int srcNodeID, int destNodeID, int flag) {
	int nbrnum = topology_getNbrNum()+1;
	int nodenum = topology_getNodeNum();
	int i = 0;
	for(i=0; i<nbrnum; i++) {
		if(dvtable[i].nodeID == srcNodeID) {
			dv_entry_t *list_toNode = dvtable[i].dvEntry;
			int j = 0;
			for(j=0; j<nodenum; j++) {
				if(list_toNode[j].nodeID == destNodeID) {
					list_toNode[j].onlineflag = flag;
					return 1;
				}
			}
		}
	}
	return -1;
}
