#include "tree.h"

/* functions from oct/hex to dec */
int atoi_oct(char* string, int leng) {
	int ret_val=0;
	int i = 0;
	for(i=1; i<leng; i++) {
		ret_val = ret_val*8+(int)(string[i]-'0');
	}
	return ret_val;	
}

int atoi_hex(char* string, int leng) {
	int ret_val=0;
	int i = 0;
	for(i=2; i<leng; i++) {
		char c = string[i];
		if( c >= '0' && c<= '9')
			ret_val = ret_val*16+(int)(c-'0');
		else if( c>= 'a' && c<='f')
			ret_val = ret_val*16+(int)(c-'a'+10);
		else if( c>= 'A' && c<='F')
			ret_val = ret_val*16+(int)(c-'A'+10);
	}
	return ret_val;	
}

TREENODE* init_node(char* name, enum tokenType tttype, int lineno, char* tttext) {
	TREENODE* temp = malloc(sizeof(TREENODE));
	strncpy(temp->ttname, name, 32);
	temp->lineno = lineno;
	temp->type = tttype;
	if(strcmp(name, "INT") == 0 ) {//int value
		if(tttext[0]=='0' && (tttext[1] == 'x' || tttext[1] == 'X')) {// hex
			temp->val_int = atoi_hex(tttext, strlen(tttext));
		}
		else if(tttext[0] == '0' && (strlen(tttext) != 1)) {//oct
			temp->val_int = atoi_oct(tttext, strlen(tttext));
		}
		else {
			temp->val_int = atoi(tttext);
		}
	}
	else if(strcmp(name, "FLOAT") == 0 ) {// float value
		temp->val_float = atof(tttext);
	}
	else if(strcmp(name, "TYPE") == 0 ) {
		strncpy(temp->val, tttext, 32);
	}
	else {
		strncpy(temp->val, tttext, 32);
	}
	temp->child = NULL;
	temp->sibling = NULL;
	return temp;
}

void add_child(TREENODE* pa, TREENODE *cld) {
	pa->child = cld;
}
void add_sibling(TREENODE* bro, TREENODE* sis) {
	bro->sibling = sis;
}
void print_tree(TREENODE* root, int depth) { //使用前序遍历、DFS输出
	TREENODE* temp = root;
	if( temp!= NULL) {
		if(temp->type == Type) {
			int i = 0;
			for(; i<depth; i++) {
				printf("  ");
			}
			printf("%s (%d)\n", temp->ttname, temp->lineno);			
		}
		else if(temp->type == Token) {
			int j = 0;
			for(; j<depth; j++) {
				printf("  ");
			}
			if(strcmp(temp->ttname, "INT") == 0) {
				printf("INT: %d\n", temp->val_int);
			}
			else if(strcmp(temp->ttname, "FLOAT") == 0) {
				printf("FLOAT: %f\n", temp->val_float);
			}
			else if(strcmp(temp->ttname, "ID") == 0){
				printf("ID: %s\n", temp->val);
			}
			else 
				printf("%s\n", temp->ttname);
		}
		else {
			int j = 0;
			for(; j<depth; j++) {
				printf("  ");
			}
			printf("Error Token!\n");
		}
		temp = temp->child;
		while(temp != NULL) {
			print_tree(temp, depth+1);
			temp = temp->sibling;
		}
	}
	else
		return;
}

