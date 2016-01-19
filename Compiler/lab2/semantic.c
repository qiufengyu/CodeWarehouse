#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "tree.h"
#include "semantic.h"

//全局符号表
FieldList fieldtable[MAX_TABLE_SIZE];
//函数表
FuncList functable[MAX_TABLE_SIZE];
//结构体表
StructList structtable[MAX_TABLE_SIZE];

unsigned int BKDR_hash(char *str)
{
	unsigned int seed = 13131; // 31 131 1313 13131 131313 etc..
	unsigned int hash = 0;
	while (*str)
	{
		hash = hash * seed + (*str++);
	}
	unsigned int t = (hash & 0x7FFFFFFF);
	return (t%MAX_TABLE_SIZE);
}

void initTables() {
	int i = 0;
	for(i=0; i<MAX_TABLE_SIZE; i++) {
		fieldtable[i]=NULL;
		functable[i]=NULL;
		structtable[i]=NULL;
	}
}

int add_fieldtable(FieldList f) {
	assert(f->name != NULL);
	unsigned int index = BKDR_hash(f->name);
	//printf("Index = %d\n", index);
	if(fieldtable[index]==NULL) { //insert directly
		fieldtable[index] = f;
		//printf("Add directly\n");
		return 0;
	}
	else {//2 cases:
		//redefined!
		if(strcmp(f->name, fieldtable[index]->name) == 0) {
			return 1;//Redefined error!
		}
		//conflicts!
		else {
			FieldList temp = fieldtable[index];
			while(temp->nextHash != NULL) {
				temp = temp->nextHash;
				if(strcmp(f->name, temp->name) == 0) {
					return 1;//Still Redefined
				}
			}
			temp->nextHash = f;
		}		
	}
	return 0;
}

int add_functable(FuncList f) {
	assert(f->name != NULL);
	unsigned int index = BKDR_hash(f->name);
	if(functable[index]==NULL) { //insert directly
		functable[index]=f;
		add_funcpara(f);
	}
	else {//2 cases:
		//redefined!
		if(strcmp(f->name, functable[index]->name) == 0) {
			return 1;//Redefined error!
		}
		//conflicts!
		else {
			FuncList temp = functable[index];
			while(temp->nextHash != NULL) {
				temp = temp->nextHash;
				if(strcmp(f->name, temp->name) == 0) {
					return 1; //Still Redefined
				}
			}
			temp->nextHash = f;
			add_funcpara(f);
		}		
	}
	return 0;
}

void add_funcpara(FuncList f) {
	FieldList fpara = f->paraList;
	int i = 0;
	//int t = 0;
	while(fpara != NULL) {
		//t++;
		//printf("%dpara: %s\n",t , f->name);
		i = add_fieldtable(fpara);
		if(i == 1) {
			printf("Error type 3 at Line %d: Redefined varaiable '%s'.\n", f->lineno, fpara->name);
		}
		fpara = fpara->tail;
	}
}

int add_structtable(StructList s) {
	assert(s->name != NULL);
	unsigned int index = BKDR_hash(s->name);
	if(structtable[index]==NULL) { //insert directly
		structtable[index]=s;
	}
	else {//2 cases
		//redefined!
		if(strcmp(s->name, structtable[index]->name) == 0) {
			return 1;//Redefined error!
		}
		//conflicts!
		else {
			StructList temp = structtable[index];
			while(temp->nextHash != NULL) {
				temp = temp->nextHash;
				if(strcmp(s->name, temp->name) == 0) {
					return 1; //Still Redefined
				}
			}
			temp->nextHash = s;
			//add_structcontent(s);
		}		
	}
	return 0;
}

void add_structcontent(StructList s, FieldList fcontent) {
	s->content = fcontent;
}

FieldList findSymbol(char *name){	
	unsigned int index = BKDR_hash(name);
	if(fieldtable[index] == NULL) { //not exists!
		return NULL;
	}
	FieldList temp = fieldtable[index];
	while(temp != NULL){
		if(strcmp(temp->name, name) == 0) {
			return temp;
		}
		temp = temp->nextHash;
	}
	//care the segment fault! when use != NULL
	return NULL;
}

FuncList findFunc(char *name) {
	unsigned int index = BKDR_hash(name);
	if(functable[index] == NULL ) {
		return NULL;
	}
	FuncList temp = functable[index];
	while(temp != NULL) {
		if(strcmp(temp->name, name) == 0) {
			return temp;
		}
		temp = temp->nextHash;
	}
	return NULL;	
}

StructList findStruct(char *name) {
	unsigned int index = BKDR_hash(name);
	if(structtable[index] == NULL ) {
		return NULL;
	}
	StructList temp = structtable[index];
	while(temp != NULL) {
		if(strcmp(temp->name, name) == 0) {
			return temp;
		}
		temp = temp->nextHash;
	}
	return NULL;	
}

//Functions deal with grammar and semantics
void Program(TREENODE *root) {
	//printf("Program%d\n", root->lineno);
	initTables();
	ExtDefList(root->child);
}

void ExtDefList(TREENODE *n) {
	if(n == NULL)
		return;
	//printf("ExtDefList%d\n", n->lineno);
	TREENODE *child = n->child;
	if(child != NULL) {
		//printf("111ddd\n");
		ExtDef(child);
		//printf("ddd\n");
		ExtDefList(child->sibling);
		//printf("ddfdfsd\n");
	}
}

void ExtDef(TREENODE *n) {
	if(n == NULL)
		return;
	//printf("ExtDef%d\n", n->lineno);
	TREENODE *child = n->child;
	TypePtr type;
	type = Specifier(child);
	child = child->sibling;
	if(strcmp(child->ttname, "SEMI") == 0) {
		return;
	}
	else if(strcmp(child->ttname, "ExtDecList") == 0) {
		ExtDecList(child, type);
	}
	else {
		FuncList f = FunDec(child, type);
		child = child->sibling;
		int i = 0;
		if(f != NULL) {
			i = add_functable(f);
			if(i == 1) {
				printf("Error type 4 at Line %d: Redefined function '%s'\n",f->lineno, f->name);
			}
		}
		CompSt(child, type);
	}
}

TypePtr Specifier(TREENODE *n) {
	if(n == NULL)
		return NULL;
	//printf("Specifier%d\n", n->lineno);
	TREENODE *child = n->child;
	if(child == NULL)
		return NULL;
	TypePtr type;
	type = (TypePtr) malloc(sizeof(struct Type_));
	if(strcmp(child->ttname, "TYPE") == 0) {
		type->kind = 0;//basic
		if(strcmp(child->val, "int") == 0) {
			type->u.basic = 0;
		}
		else if(strcmp(child->val, "float") == 0) {
			type->u.basic = 1;
		}
	}
	else { //struct!
		type = StructSpecifier(child);
	}
	return type;
}

TypePtr StructSpecifier(TREENODE *n) {
	if(n == NULL)
		return NULL;
	//printf("StructSpecifier%d\n", n->lineno);
	TREENODE *child = n->child;
	TypePtr type = malloc(sizeof(struct Type_));
	type->kind = 2; //struct
	type->u.structure.content = NULL;
	FieldList f = malloc(sizeof(struct FieldList_));
	f->type = type;
	//child = STRUCT
	TREENODE *sib = child->sibling;
	while(sib != NULL) {
		if(strcmp(sib->ttname, "OptTag") == 0) {
			//OptTag -> empty
			TREENODE *temp = sib->child;
			if(temp == NULL) {
				type->u.structure.structname = NULL;
			}
			else {
				type->u.structure.structname = (char *)malloc(32);
				//not temp->ttname(OptTag!!!!), but val
				strncpy(type->u.structure.structname, temp->val, 32);
				f->name = (char *)malloc(32);
				strncpy(f->name, temp->val, 32);
			}
		}
		else if(strcmp(sib->ttname, "Tag") == 0) {
			//when use
			FieldList ftemp = findSymbol(sib->child->val);
			if(ftemp == NULL || ftemp->type->kind != 2) {
				printf("Error type 17 at Line %d: Undefined structure %s'.\n", sib->lineno, sib->child->val);
				return NULL;
			}
			return ftemp->type;
		}
		else if(strcmp(sib->ttname, "DefList") == 0) {
			//add content variables to fieldtable, structure flag and it is inh
			type->u.structure.content = DefList(sib,2);
			if(f->type->u.structure.structname == NULL)
				return type; //anonymous
			int i = add_fieldtable(f);
			if( i == 1) {
				printf("Error type 16 at Line %d: Duplicated structure name '%s'.\n", sib->lineno, f->name);
				return NULL;
			}
		}
		sib = sib->sibling;
	}
	return type;	
}

void ExtDecList(TREENODE *n, TypePtr type) {
	if(n == NULL)
		return;
	//printf("ExtDecList%d\n", n->lineno);
	TREENODE *child = n->child;
	VarDec(child, type, 3);
	child = child->sibling;
	if(child != NULL) {
		child = child->sibling;
		ExtDecList(child, type);
	}
}

//type = retType
FuncList FunDec(TREENODE *n, TypePtr t) {
	if(n == NULL)
		return NULL;
	//printf("FunDec%d\n", n->lineno);
	TREENODE *child = n->child;
	FuncList f = malloc(sizeof(struct FuncList_));
	f->name = (char *)malloc(32);
	//child == ID
	strncpy(f->name, child->val, 32);
	f->lineno = child->lineno; //record first line
	f->retType = t;
	f->nextHash = NULL;
	f->paraList = NULL;
	TREENODE *next = child->sibling->sibling;//VarList
	if(strcmp(next->ttname, "VarList") == 0) {
		f->paraList = VarList(next);
	}
	else { //function without para!
		f->paraList = NULL;
	}
	return f;
}

FieldList VarList(TREENODE *n) {
	if(n == NULL)
		return NULL;
	//printf("VarList%d\n", n->lineno);
	TREENODE *child = n->child;//child: ParamDec
	FieldList f; //for return
	f = ParamDec(child);
	child = child->sibling;
	if(child != NULL) {//have comma and other params
		child = child->sibling;
		FieldList temp = f;
		if(temp == NULL)
			f = VarList(child);
		else { //add to f->tail
			while(temp->tail!=NULL)
				temp = temp->tail;
			temp->tail = VarList(child);
		}
	}
	return f;
}

FieldList DefList(TREENODE *n, int flag) {
	if(n == NULL)
		return NULL;
	TREENODE *child = n->child;
	if(child == NULL)
		return NULL;
	//printf("DefList%d\n", n->lineno);
	FieldList f;
	f = Def(child, flag);
	child = child->sibling;
	FieldList temp = f;
	if(f != NULL) {
		while(temp->tail != NULL)
			temp = temp->tail;
		temp->tail = DefList(child, flag);
	}
	else f = DefList(child, flag);
	return f;
}

FieldList Def(TREENODE *n, int flag) {
	//printf("Def%d\n", n->lineno);
	TREENODE* child = n->child;
	FieldList f;
	TypePtr t = Specifier(child);
	child=child->sibling;
	f = DecList(child, t, flag);
	return f;
}

FieldList DecList(TREENODE *n, TypePtr type, int flag) {
	if(n == NULL)
		return NULL;
	//printf("DecList%d\n", n->lineno);
	TREENODE *child = n->child;
	if(child == NULL)
		return NULL;
	FieldList f;
	f = Dec(child, type, flag);
	child = child->sibling;
	if(child != NULL) {
		child = child->sibling;//DecList
		FieldList temp = f;
		if(temp != NULL) {
			while(temp->tail != NULL) {
				temp = temp->tail;
			}
			temp->tail = DecList(child, type, flag);
		}
		else 
			f = DecList(child, type, flag);
	}
	return f;
}

FieldList Dec(TREENODE *n, TypePtr type, int flag) {
	//flag inh to here
	if(n == NULL)
		return NULL;
	//printf("Dec%d\n", n->lineno);
	TREENODE *child = n->child;
	if(child == NULL)
		return NULL;
	FieldList f;
	f = VarDec(child, type, flag);
	child = child->sibling;//ASSIGNOP
	if(child == NULL) {
		return f;
	}
	else {//ASSIGNOP
		child = child->sibling;
		TypePtr t = Exp(child);
		if(t == NULL || type == NULL) {
			return f;
		}
		if(flag == 2) { //structure
			printf("Error type 15 at line %d: can not initial field '%s' in structure.\n",child->lineno, f->name);
		}
		else if(!typeMatch(t, type)) {
			printf("Error type 5 at Line %d: Type mismatched for assignment.\n", child->lineno);
		}		
	}
	return f;
}

FieldList VarDec(TREENODE *n, TypePtr type, int flag) {
	if(n == NULL || n->child == NULL)
		return NULL;
	//printf("VarDec%d\n", n->lineno);
	TREENODE *child = n->child;
	FieldList f;
	if(strcmp(child->ttname, "ID") == 0) {
		f = (FieldList)malloc(sizeof(struct FieldList_));
		f->name = (char *)malloc(32);
		strncpy(f->name, child->val, 32);
		f->type = type;
		f->tail = NULL;
		f->nextHash = NULL;
		if(flag == 1) //Function patalist, not to add again!
			return f;
		int i = add_fieldtable(f);
		if(i == 1) {
			if(flag == 2) {
				printf("Error type 15 at Line %d: Redefined variable '%s' in structure.\n", child->lineno, f->name);
			}
			else {
				printf("Error type 3 at Line %d: Redefined variable '%s'.\n", child->lineno, f->name);
			}
			return NULL;
		}
		return f;	
	}
	else { //array type
		f = VarDec(child, type, flag);
		if(f != NULL) {
			child = child->sibling->sibling;//come to int
			TypePtr temp = (TypePtr)malloc(sizeof(struct Type_));
			temp->kind = 1;
			temp->u.array.size = atoi(child->val);
			temp->u.array.elem = type;
			if(f->type == NULL) return NULL;
			if(f->type->kind != 1) { //change f->type
				f->type = temp;
				return f;
			}
			TypePtr temp2 = f->type;
			while(temp2->u.array.elem->kind == 1) {
				//the end of the array
				temp2 = temp2->u.array.elem;
			}
			//the innest type of the elements of the array
			temp2->u.array.elem = temp;
			return f;
		}
		return NULL;
	}
}

FieldList ParamDec(TREENODE *n) {
	if(n == NULL)
		return NULL;
	//printf("ParamDec%d\n", n->lineno);
	TREENODE *child = n->child;
	FieldList f;
	TypePtr t = Specifier(child);
	f = VarDec(child->sibling, t, 1); //function para!
	return f;
}

void CompSt(TREENODE *n, TypePtr rettype) {
	if(n == NULL)
		return;
	//printf("CompSt%d\n", n->lineno);
	TREENODE *child=n->child;
	if(child == NULL)
		return;
	child=child->sibling; //come to DefList
	DefList(child, 0);
	child=child->sibling;
	StmtList(child, rettype);
}

void StmtList(TREENODE *n, TypePtr rettype) {
	if(n == NULL)
		return;
	//printf("StmtList%d\n", n->lineno);
	TREENODE *child = n->child;
	if(child != NULL) {
		Stmt(child, rettype);
		child = child->sibling;
		StmtList(child, rettype);
	}
}

void Stmt(TREENODE *n, TypePtr rettype) {
	if(n == NULL)
		return;
	//printf("Stmt%d\n", n->lineno);
	TREENODE *child = n->child;
	while(child != NULL) {
		//printf("STMT: child->name: %s\n", child->ttname);
		if(strcmp(child->ttname, "Exp") == 0) {
			Exp(child);
		}
		else if(strcmp(child->ttname, "CompSt") == 0) {
			CompSt(child, rettype);
		}
		else if(strcmp(child->ttname, "RETURN") == 0) {
			//printf("RETURN STMT\n");
			child=child->sibling;//exp
			TypePtr t = Exp(child);
			//printtype(t);
			//printtype(rettype);
			if(rettype == NULL && t == NULL)
				return;
			else if(rettype == NULL || t == NULL) {
				printf("Error type 8 at Line %d: Type mismatched for return.\n", child->lineno);
				return;
			}
			if(!typeMatch(rettype, t)) {
				printf("Error type 8 at Line %d: Type mismatched for return.\n", child->lineno);
				return;
			}
		}
		else if(strcmp(child->ttname, "LP") == 0) {
			child = child->sibling;
			TypePtr t = Exp(child);
			if(t == NULL || t->kind != 0 || t->u.basic != 0) {
				//printf("Error type x at Line %d: Conditional statement wrong type.\n", child->lineno);
				return;
			}
		}
		else if(strcmp(child->ttname, "Stmt") == 0) {
			Stmt(child, rettype);
		}
		child = child->sibling;
	}
}

TypePtr Exp(TREENODE *n) {
	if(n == NULL)
		return NULL;
	//printf("Exp%d\n", n->lineno);
	TREENODE *child = n->child;
	if(child == NULL)
		return NULL;
	if(strcmp(child->ttname, "Exp") == 0) {
		TREENODE *sib = child->sibling;
		if(sib == NULL)
			return NULL;
		if(strcmp(sib->ttname, "ASSIGNOP") == 0) {
			TypePtr ltype = NULL;
			TypePtr rtype = NULL;
			TREENODE *childchild = child->child;
			//we can't just make ltype = Exp(child), for array and structure! then check type match
			if(strcmp(childchild->ttname, "ID" )== 0 && childchild->sibling == NULL) {
				ltype = Exp(child);
			}
			else if(strcmp(childchild->ttname, "Exp") == 0 && strcmp(childchild->sibling->ttname, "LB") == 0  && childchild->sibling != NULL) { //array
				ltype = Exp(child);
			}
			else if(strcmp(childchild->ttname, "Exp") == 0 && strcmp(childchild->sibling->ttname, "DOT") == 0 && childchild->sibling != NULL) { //structure
				ltype = Exp(child);
			}
			else {
				printf("Error type 6 at Line %d: The left-hand side of an assignment must be a variable.\n", child->lineno);
				return NULL;
			}
			sib = sib->sibling;
			rtype = Exp(sib);
			if(ltype != NULL && rtype != NULL) {
				if(!typeMatch(ltype, rtype)) {
					printf("Error type 5 at Line %d: Type mismatched for assignment.\n", child->lineno);
					return NULL;
				}
				else
					return ltype;//or rtype
			}
			else 
				return NULL;
		} //end of assignop
		else if(strcmp(sib->ttname, "PLUS") == 0 || strcmp(sib->ttname, "MINUS") == 0 || strcmp(sib->ttname, "STAR") == 0 || strcmp(sib->ttname, "DIV") == 0 || strcmp(sib->ttname, "RELOP") == 0 ) {
			TypePtr t1 = Exp(child);
			TypePtr t2 = Exp(sib->sibling);
			if(t1 == NULL || t2 == NULL)
				return NULL;
			else if(t1->kind == 0 && t2->kind == 0 && t1->u.basic == t2->u.basic) {
				return t1;
			}
			else {
				printf("Error type 7 at Line %d: Type mismatched for operands.\n", child->lineno);
				return NULL;
			}
		} //end of + - * / relop
		else if(strcmp(sib->ttname, "LB") == 0) {
			TypePtr t = Exp(child);
			if(t == NULL) {
				return NULL;
			}
			//printtype(t);
			if(t->kind != 1) {
				printf("Error type 10 at Line %d: it is not an array\n", child->lineno);
				return NULL;
			}
			sib = sib->sibling;
			TypePtr t2 = Exp(sib);
			if(t == NULL && t2 == NULL) {
				printf("Error type 10 at Line %d: it is not an array\n", sib->lineno);
				return NULL;
			}
			if(t2 == NULL) {
				return NULL;
			}
			if(!(t2->kind == 0 && t2->u.basic == 0)) {
				printf("Error type 12 at Line %d: between '[' and  ']' is not an integer.\n", child->lineno);
				return NULL;
			}
			return t->u.array.elem;
		}
		else if(strcmp(sib->ttname, "DOT") == 0) {
			//struct inner content!
			TypePtr t1 = Exp(child);
			if(t1 == NULL) 
				return NULL;
			if(t1->kind != 2) {
				printf("Error type 13 at Line %d: Illegal use of '.'.\n", sib->lineno);
				return NULL;
			}
			//check the field!
			FieldList f = t1->u.structure.content;
			sib = sib->sibling;
			TypePtr tf = NULL;
			while(f != NULL) {
				if(strcmp(f->name, sib->val) == 0) {
					tf = f->type;
					/*if(!(typeMatch(tf, t1))) {
						printf("Error type 4 at Line %d: Type mismatched for assignment.\n", child->lineno);
					}
					* */
					return f->type;
				}
				f = f->tail;
			}
			if(f == NULL) {
				printf("Error type 14 at Line %d: Non-existent field '%s'.\n", child->lineno, sib->val);
				return NULL;
			}
		} //end of dot of struct
	} //end of Exp
	else if(strcmp(child->ttname, "LP") == 0) { //deal with sibling
		child = child->sibling;
		return Exp(child);
	}
	else if(strcmp(child->ttname, "MINUS") == 0) {
		child = child->sibling;
		TypePtr t = Exp(child);
		if(t == NULL) {
			return NULL;
		}
		else {
			if(t->kind!=0) {
				printf("Error type 7 at Line %d: Type mismatched for operands '-'.\n", child->lineno);
				return NULL;
			}
		}
		return t;
	}
	else if(strcmp(child->ttname, "NOT") == 0) {
		TREENODE *sib1 = sib1->sibling;
		TypePtr t = Exp(sib1);
		if(t == NULL) {
			return NULL;
		}
		if(!(t->kind == 0 && t->u.basic == 0)) {
			printf("Error type 7 at Line %d: Type mismatched for operands '!'.\n", sib1->lineno);
			return NULL;
		}
		return t;
	}
	else if(strcmp(child->ttname, "ID") == 0) {
		TREENODE *sib = child->sibling;
		if(sib == NULL) { //
			FieldList f = findSymbol(child->val);
			if(f == NULL) {
				printf("Error type 1 at Line %d: Undefined variable '%s'.\n", child->lineno, child->val);
				return NULL;
			}
			return f->type;
		}
		else { //function
			//printf("function!!!\n\n");
			FieldList f = findSymbol(child->val);
			FuncList fu = findFunc(child->val);
			//printf("fu name: %s\n", fu->name);
			if(fu == NULL) {
				if(f == NULL) {
					printf("Error type 2 at Line %d: Undefined function '%s'.\n", child->lineno, child->val);
					return NULL;
				}
				else {
					printf("Error type 11 at Line %d: '%s' is not a function.\n", child->lineno, child->val);
					return NULL;
				}
			}
			//printf("fu != NULL\n");
			FieldList pList = fu->paraList;
			sib = sib->sibling;
			if(strcmp(sib->ttname, "RP") == 0) {
				printparam(pList);
				if(pList != NULL) {
					printf("Error type 9 at Line %d: Function '%s' is not applicable for arguments.\n", sib->lineno, fu->name);
				}
			}
			else {
				if(Args(sib, pList) == 0) {
					printf("Error type 9 at Line %d: Function '%s' is not applicable for arguments.\n", sib->lineno, fu->name);
				}
			}
			return fu->retType;
		}
	}
	else if(strcmp(child->ttname, "INT") == 0) {
		TypePtr temp = malloc(sizeof(struct Type_));
		temp->kind = 0;
		temp->u.basic = 0;
		return temp;
	}
	else if(strcmp(child->ttname, "FLOAT") == 0) {
		TypePtr temp = malloc(sizeof(struct Type_));
		temp->kind = 0;
		temp->u.basic = 1;
		return temp;
	}
	return NULL;
}

int Args(TREENODE *n, FieldList f) {
	if(n == NULL)
		return 1;
	//printf("Args%d\n", n->lineno);
	if(n == NULL) {
		if(f == NULL)
			return 1;
		else
			return 0;
	}
	TREENODE *child = n->child;
	TypePtr t = Exp(child);
	if(typeMatch(t, f->type) == 0) {
		return 0;//Error!
	}
	if(child->sibling == NULL) {
		if(f->tail == NULL)
			return 1;
		else
			return 0;
	}
	if(f->tail == NULL) {
		if(child->sibling == NULL)
			return 1;
		else
			return 0;
	}
	return Args(child->sibling->sibling, f->tail);
}

int typeMatch(TypePtr t1, TypePtr t2) {
	if(t1 == NULL && t2 == NULL) {
		return 0;
	}
	else if(t1 == NULL || t2 == NULL) {
		return 1;
	}
	if(t1->kind == 0 && t2->kind == 0) { //basic
		if(t1->u.basic == t2->u.basic) {
			return 1;
		}
	}
	else if(t1->kind == 1 && t2->kind == 1) { //array
		return typeMatch(t1->u.array.elem, t2->u.array.elem);
	}
	else if(t1->kind == 2 && t2->kind == 2) { //struct
		if(t1->u.structure.structname == NULL && t2->u.structure.structname == NULL) {
			return 1;
		}
		else if(t1->u.structure.structname == NULL || t2->u.structure.structname == NULL) {
			return contentEqual(t1->u.structure.content, t2->u.structure.content);
		}
		else if(strcmp(t1->u.structure.structname, t2->u.structure.structname) == 0)
			return 1;
	} 
	return 0;
}

int contentEqual(FieldList f1, FieldList f2) {
	if(f1 == NULL && f2 == NULL)
		return 1;
	if(f1 == NULL || f2 == NULL) 
		return 0;
	if(typeMatch(f1->type, f2->type)) {
		return contentEqual(f1->tail, f2->tail);
	}
	return 0;
}

void printtype(TypePtr t){
	if(t == NULL)
		printf("NULL\n");
	else if((t->kind==0)&&t->u.basic==0)
		printf(" int ");
	else if((t->kind==0)&&t->u.basic==1)
		printf(" float ");
	else if(t->kind==2)
		printf("struct %s ", t->u.structure.structname);
	else if(t->kind==1){
		printtype(t->u.array.elem);
		printf("[]");
	}
}

void printparam(FieldList f)
{
	while(f!=NULL)
	{
		printtype(f->type);
		f=f->tail;
	}
}


