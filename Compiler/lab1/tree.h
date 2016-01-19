#ifndef __TREE_H__
# define __TREE_H__

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

enum tokenType {Token, Type};
typedef enum tokenType TOKENTYPE;

struct treeNode {
	char ttname[32];
	union { //value
	int val_int;
	float val_float;
	char val[32];//type name or ID!
	};
	int lineno;
	enum tokenType type; //type: like int, id or Program Exp
	struct treeNode* child;
	struct treeNode* sibling;
};
typedef struct treeNode TREENODE;

TREENODE* init_node(char* name, enum tokenType tttype, int lineno, char* tttext);
void add_child(TREENODE* pa, TREENODE *cld);
void add_sibling(TREENODE* bro, TREENODE* sis);
void print_tree(TREENODE* root, int depth);
int atoi_oct(char* string, int leng);
int atoi_hex(char* string, int leng);
#endif
