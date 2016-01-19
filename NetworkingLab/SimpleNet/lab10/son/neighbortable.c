//文件名: son/neighbortable.c
//
//描述: 这个文件实现用于邻居表的API
//
//创建日期: 2015年

#include "neighbortable.h"

//这个函数首先动态创建一个邻居表. 然后解析文件topology/topology.dat, 填充所有条目中的nodeID和nodeIP字段, 将conn字段初始化为-1.
//返回创建的邻居表.
nbr_entry_t* nt_create()
{
	int numofnbr = topology_getNbrNum();
	nbr_entry_t *nbr_table = (nbr_entry_t *)malloc(sizeof(nbr_entry_t)*numofnbr);
	int *nbrarray = topology_getNbrArray();
	int i = 0;
	for(i =0; i<numofnbr; i++) {
		nbr_table[i].nodeID = nbrarray[i];
		nbr_table[i].nodeIP = topology_getfromIDtoIP(nbrarray[i]);
		nbr_table[i].conn = -1;
	}
	return nbr_table;
}

//这个函数删除一个邻居表. 它关闭所有连接, 释放所有动态分配的内存.
void nt_destroy(nbr_entry_t* nt)
{
	int numofnbr = topology_getNbrNum();
	int i = 0;
	for(i=0; i<numofnbr; i++) {
		close(nt[i].conn);
		nt[i].conn=-1;
	}
	free(nt);
	return;
}

//这个函数为邻居表中指定的邻居节点条目分配一个TCP连接. 如果分配成功, 返回1, 否则返回-1.
int nt_addconn(nbr_entry_t* nt, int nodeID, int conn)
{
	int numofnbr = topology_getNbrNum();
	int i = 0;
	for(i=0; i<numofnbr; i++) {
		if(nt[i].nodeID == nodeID) {
			nt[i].conn = conn;
			return 1;
		}
	}
	return -1;
}
