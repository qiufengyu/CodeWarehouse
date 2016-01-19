#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "tree.h"
#include "intercode.h"
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
	//insert read and write functions
	FuncList readfunc = (FuncList)malloc(sizeof(struct FuncList_));
	FuncList writefunc = (FuncList)malloc(sizeof(struct FuncList_));
	readfunc->name = (char*)malloc(4*8);
	writefunc->name = (char*)malloc(32);
	strcpy(readfunc->name,"read");
	strcpy(writefunc->name,"write");
	/*
	int lineno;
	TypePtr retType;
	FieldList paraList;
	FuncList nextHash; // open addressing
	* */
	readfunc->lineno = 0;
	writefunc->lineno = 0;
	TypePtr rt = (TypePtr)malloc(sizeof(struct Type_));
	rt->kind = 0;//basic
	rt->u.basic = 0;//int
	readfunc->retType = rt;
	writefunc->retType = rt;
	
	readfunc->paraList = NULL;
	readfunc->nextHash = NULL;
	add_functable(readfunc);
	
	writefunc->paraList=(FieldList)malloc(sizeof(struct FieldList_));
	writefunc->paraList->name = (char*)malloc(16*2);
	strcpy(writefunc->paraList->name, "write_param");
	writefunc->paraList->type = rt;
	writefunc->paraList->tail = NULL;
	writefunc->paraList->nextHash = NULL;
	writefunc->nextHash = NULL;
	add_functable(writefunc);
	init_intercode();
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
			else {
				Operand funcop = (Operand)malloc(sizeof(struct Operand_));
				funcop->kind = FUNCTION_OP;
				funcop->u.name = f->name;
				InterCode code = (InterCode)malloc(sizeof(struct InterCode_));
				code->kind = FUNCTION_IC;
				code->u.one.op = funcop;
				insert_code(code);
				//code for PARAM v1...
				FieldList paralist = f->paraList;
				while(paralist!=NULL) {
					Operand paraop = (Operand)malloc(sizeof(struct Operand_));
					paraop->kind = VARIABLE_OP;
					paraop->u.name = paralist->name;
					InterCode pcode = (InterCode)malloc(sizeof(struct InterCode_));
					pcode->kind = PARAM_IC;
					pcode->u.one.op = paraop;
					insert_code(pcode);
					paralist = paralist->tail;//next param
				}
			}
		}
		CompSt(child, type);
	}
}

//node intercode!
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
			printf("Cannot translate: Code contains float variables.\n");
			exit(-1);
		}
	}
	else { //struct!
		type = StructSpecifier(child);
	}
	return type;
}

//no intercode
TypePtr StructSpecifier(TREENODE *n) {
	if(n == NULL)
		return NULL;
	else {
		printf("Cannot translate: Code contains variables or parameters of structure type.\n");
		exit(-1);
	}
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
	FieldList ftemp = VarDec(child, type, 3);
	//if array? then generate dec code
	if(ftemp != NULL) {
		if(ftemp->type->kind == 1) {
			//array: deccode
			Operand decop = (Operand)malloc(sizeof(struct Operand_));
			decop->kind = TEMP_OP;
			decop->u.var_no = newtemp();
			Operand sop = (Operand)malloc(sizeof(struct Operand_));
			sop->kind = CONSTANT_OP_INT;
			sop->u.value = ElemSize(ftemp->type);			
			InterCode deccode = (InterCode)malloc(sizeof(struct InterCode_));
			deccode->kind = DEC_IC;
			deccode->u.dec_code.op = decop;
			deccode->u.dec_code.size_op = sop;
			deccode->u.dec_code.size = sop->u.value;
			//printf("HERE sop->u.value=%d\n", sop->u.value);
			insert_code(deccode);
			//generate address &
			Operand vaddr = (Operand)malloc(sizeof(struct Operand_));
			vaddr->kind = VARIABLE_OP;
			vaddr->u.name = ftemp->name;
			
			InterCode addrcode = (InterCode)malloc(sizeof(struct InterCode_));
			addrcode->kind = ADDR_IC;
			addrcode->u.two.left = vaddr;
			addrcode->u.two.right = decop;
			insert_code(addrcode);
		}
	}
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

//no intercode
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
	FieldList f = Dec(child, type, flag);
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
	FieldList f = VarDec(child, type, flag);
	// not structure
	if(f != NULL && f->type->kind == 1 && flag != 2) {
		//array: deccode
		Operand decop = (Operand)malloc(sizeof(struct Operand_));
		decop->kind = TEMP_OP;
		decop->u.var_no = newtemp();
		Operand sop = (Operand)malloc(sizeof(struct Operand_));
		sop->kind = CONSTANT_OP_INT;
		sop->u.value = ElemSize(f->type);			
		InterCode deccode = (InterCode)malloc(sizeof(struct InterCode_));
		deccode->kind = DEC_IC;
		deccode->u.dec_code.op = decop;
		deccode->u.dec_code.size_op = sop;
		deccode->u.dec_code.size = sop->u.value;
		//printf("534HERE sop->u.value=%d\n", sop->u.value);
		insert_code(deccode);
		//generate address &
		Operand vaddr = (Operand)malloc(sizeof(struct Operand_));
		vaddr->kind = VARIABLE_OP;
		vaddr->u.name = f->name;
		
		InterCode addrcode = (InterCode)malloc(sizeof(struct InterCode_));
		addrcode->kind = ADDR_IC;
		addrcode->u.two.left = vaddr;
		addrcode->u.two.right = decop;
		insert_code(addrcode);
	}
	if(f == NULL) 
		return NULL;
	child = child->sibling;//ASSIGNOP
	if(child == NULL) {
		return f;
	}
	else {//ASSIGNOP
		Operand place = (Operand)malloc(sizeof(struct Operand_));
		place->kind = VARIABLE_OP;
		child = child->sibling;
		assert(child != NULL);
		//return f;
		place->u.name = f->name;
		TypePtr t = Exp(child, place);
		if(t == NULL || type == NULL) {
			return f;
		}
		if(flag == 2) { //structure
			printf("Error type 15 at line %d: can not initial field '%s' in structure.\n",child->lineno, f->name);
		}
		else if(!typeMatch(t, type)) {
			printf("Error type 5 at Line %d: Type mismatched for assignment.\n", child->lineno);
		}
		Operand leftop = (Operand)malloc(sizeof(struct Operand_));
		leftop->kind=VARIABLE_OP;
		leftop->u.name = f->name;
		InterCode assigncode = (InterCode)malloc(sizeof(struct InterCode_));
		assigncode->kind = ASSIGN_IC;
		assigncode->u.two.left = leftop;
		assigncode->u.two.right = place;
		insert_code(assigncode);	
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
		if(flag == 2 && type->kind == 2) {
			//structure deccode
			Operand decop = (Operand)malloc(sizeof(struct Operand_));
			decop->kind = TEMP_OP;
			decop->u.var_no = newtemp();
			Operand sop = (Operand)malloc(sizeof(struct Operand_));
			sop->kind = CONSTANT_OP_INT;
			sop->u.value = ElemSize(type);		
			InterCode deccode = (InterCode)malloc(sizeof(struct InterCode_));
			deccode->kind = DEC_IC;
			deccode->u.dec_code.op = decop;
			deccode->u.dec_code.size_op = sop;
			deccode->u.dec_code.size = sop->u.value;
			insert_code(deccode);
			//generate address &
			Operand vaddr = (Operand)malloc(sizeof(struct Operand_));
			vaddr->kind = VARIABLE_OP;
			vaddr->u.name = f->name;
			
			InterCode addrcode = (InterCode)malloc(sizeof(struct InterCode_));
			addrcode->kind = ADDR_IC;
			addrcode->u.two.left = vaddr;
			addrcode->u.two.right = decop;
			insert_code(addrcode);
		}		
		return f;	
	}
	else { //array type
		f = VarDec(child, type, flag);
		if(f != NULL) {
			child = child->sibling->sibling;//come to int
			TypePtr temp = (TypePtr)malloc(sizeof(struct Type_));
			temp->kind = 1;
			temp->u.array.size = child->val_int;
			//printf("size = %d\n", temp->u.array.size);
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
	if(child != NULL) {
		if(strcmp(child->ttname, "DefList") ==0 ) {
			DefList(child, 0);
			child=child->sibling;
			StmtList(child, rettype);
		}
		else if(strcmp(child->ttname, "StmtList") ==0 ) {
			StmtList(child, rettype);
		}
	}
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
	if(n == NULL) {
		return;
	}
	//printf("Stmt%d\n", n->lineno);
	TREENODE *child = n->child;
	if(child != NULL) {
		//printf("STMT: child->name: %s\n", child->ttname);
		if(strcmp(child->ttname, "Exp") == 0) {
			//printf("tail == %d\n", (int)tail);
			Exp(child, NULL);
			//printf("tail == %d\n", (int)tail);
			//return;
		}
		else if(strcmp(child->ttname, "CompSt") == 0) {
			CompSt(child, rettype);
			//return;
		}
		else if(strcmp(child->ttname, "RETURN") == 0) {
			//printf("RETURN STMT\n");
			child=child->sibling;//exp
			Operand tempop = (Operand)malloc(sizeof(struct Operand_));
			tempop->kind = TEMP_OP;
			tempop->u.var_no = newtemp();
			TypePtr t = Exp(child, tempop);
			//printtype(t);
			//printtype(rettype);
			//if(rettype == NULL && t == NULL)
				//return;
			//else if(rettype == NULL || t == NULL) {
				//printf("Error type 8 at Line %d: Type mismatched for return.\n", child->lineno);
				//return;
			//}
			/*
			if(!typeMatch(rettype, t)) {
				printf("Error type 8 at Line %d: Type mismatched for return.\n", child->lineno);
				//return;
			}
			* */
			//insert return code
			InterCode retcode = (InterCode)malloc(sizeof(struct InterCode_));
			retcode->kind = RETURN_IC;
			retcode->u.one.op = tempop;
			insert_code(retcode);
			return;
		}
		else if(strcmp(child->ttname, "IF") == 0) {
			child = child->sibling->sibling;
			Operand label1 = (Operand)malloc(sizeof(struct Operand_));
			label1->kind = LABEL_OP;
			label1->u.var_no=newlabel();
			Operand label2 = (Operand)malloc(sizeof(struct Operand_));
			label2->kind = LABEL_OP;
			label2->u.var_no=newlabel();
			TypePtr t = Exp_Cond(child, label1, label2);
			if(t == NULL || t->kind != 0 || t->u.basic != 0) {
				//printf("Error type x at Line %d: Conditional statement wrong type.\n", child->lineno);
				//return;
			}
			
			InterCode code1 = (InterCode) malloc(sizeof(struct InterCode_));
			code1->kind = LABEL_IC;
			code1->u.one.op = label1;
			insert_code(code1);
			child = child->sibling->sibling;//Stmt
			//insert code2 for next stmt following if
			//printf("tail == %d\n", (int)tail);
			Stmt(child, rettype);
			//printf("%s\n", child->child->child->ttname);
			//printf("tail == %d\n", (int)tail);
			//insert label
			InterCode label2code = (InterCode)malloc(sizeof(struct InterCode_));
			label2code->kind = LABEL_IC;
			label2code->u.one.op = label2;
			//for if else!!!!
			if(child->sibling != NULL) {
				//with else
				Operand label3 = (Operand)malloc(sizeof(struct Operand_));
				label3->kind = LABEL_OP;
				label3->u.var_no = newlabel();
				InterCode codegoto = (InterCode)malloc(sizeof(struct InterCode_));
				codegoto->kind = GOTO_IC;//add goto label3
				codegoto->u.one.op = label3;
				insert_code(codegoto);
				insert_code(label2code);
				//else Stmt
				child = child->sibling->sibling;
				//insert code3
				Stmt(child, rettype);
				//insert label3code
				InterCode label3code = (InterCode)malloc(sizeof(struct InterCode_));
				label3code->kind = LABEL_IC;
				label3code->u.one.op = label3;
				insert_code(label3code);
				
			}
			else {//without else!!!
				insert_code(label2code);
			}
		}
		else if(strcmp(child->ttname, "WHILE") == 0) {
			//new operand labels
			Operand label1 = (Operand)malloc(sizeof(struct Operand_));
			label1->kind = LABEL_OP;
			label1->u.var_no = newlabel();
			Operand label2 = (Operand)malloc(sizeof(struct Operand_));
			label2->kind = LABEL_OP;
			label2->u.var_no = newlabel();
			Operand label3 = (Operand)malloc(sizeof(struct Operand_));
			label3->kind = LABEL_OP;
			label3->u.var_no = newlabel();
			//insert code
			InterCode label1code = (InterCode)malloc(sizeof(struct InterCode_));
			label1code->kind = LABEL_IC;
			label1code->u.one.op=label1;
			insert_code(label1code);
			child = child->sibling->sibling;//Exp
			//insert code1
			TypePtr temp = Exp_Cond(child, label2, label3);
			InterCode label2code = (InterCode)malloc(sizeof(struct InterCode_));
			label2code->kind = LABEL_IC;
			label2code->u.one.op=label2;
			insert_code(label2code);
			child= child->sibling->sibling;
			//code2			
			//printf("while content!\n");
			//if(child != NULL) {
				//printf("--name: %s\n\n", child->ttname);
			//}			
			Stmt(child, rettype);
			//insert goto label1
			InterCode gotocode = (InterCode)malloc(sizeof(struct InterCode_));
			gotocode->kind = GOTO_IC;
			gotocode->u.one.op=label1;
			insert_code(gotocode);
			//insert label3code
			InterCode label3code = (InterCode)malloc(sizeof(struct InterCode_));
			label3code->kind = LABEL_IC;
			label3code->u.one.op=label3;
			insert_code(label3code);			
		}
		else if(strcmp(child->ttname, "Stmt") == 0) {
			Stmt(child, rettype);
		}
	}
	return;
}

TypePtr Exp(TREENODE *n, Operand place) {
	if(n == NULL)
		return NULL;
	//printf("Exp%d\n", n->lineno);
	TREENODE *child = n->child;
	if(child == NULL)
		return NULL;
	if(strcmp(child->ttname, "Exp") == 0) {
		TREENODE *sib = child->sibling;
		//printf("--name: %s\n", sib->ttname);
		if(sib == NULL)
			return NULL;
		if(strcmp(sib->ttname, "ASSIGNOP") == 0) {
			TypePtr ltype = NULL;
			TypePtr rtype = NULL;
			TREENODE *childchild = child->child;
			//we can't just make ltype = Exp(child), for array and structure! then check type match
			Operand leftOperand = (Operand)malloc(sizeof(struct Operand_));
			leftOperand->kind = TEMP_OP;
			leftOperand->u.var_no = newtemp();
			if(strcmp(childchild->ttname, "ID" )== 0 && childchild->sibling == NULL) {
				ltype = Exp(child, leftOperand);
			}
			else if(strcmp(childchild->ttname, "Exp") == 0 && strcmp(childchild->sibling->ttname, "LB") == 0  && childchild->sibling != NULL) { //array
				ltype = Exp(child, leftOperand);
			}
			else if(strcmp(childchild->ttname, "Exp") == 0 && strcmp(childchild->sibling->ttname, "DOT") == 0 && childchild->sibling != NULL) { //structure
				ltype = Exp(child, leftOperand);
			}
			else {
				printf("Error type 6 at Line %d: The left-hand side of an assignment must be a variable.\n", child->lineno);
				return NULL;
			}
			sib = sib->sibling;
			Operand rightOperand = (Operand)malloc(sizeof(struct Operand_));
			rightOperand->kind = TEMP_OP;
			rightOperand->u.var_no = newtemp();
			rtype = Exp(sib, rightOperand);
			if(ltype != NULL && rtype != NULL) {
				if(!typeMatch(ltype, rtype)) {
					printf("Error type 5 at Line %d: Type mismatched for assignment.\n", child->lineno);
					return NULL;
				}
				else {
					InterCode code1 = (InterCode)malloc(sizeof(struct InterCode_));
					code1->kind=ASSIGN_IC;
					code1->u.two.left = leftOperand;
					code1->u.two.right = rightOperand;
					insert_code(code1);
					InterCode code2 = (InterCode)malloc(sizeof(struct InterCode_));
					code2->kind = ASSIGN_IC;
					code2->u.two.left=place;
					code2->u.two.right=rightOperand;
					if(place != NULL) {
						insert_code(code2);
					}
					return ltype;//or rtype
				}
			}
			else 
				return NULL;
		} //end of assignop
		else if(strcmp(sib->ttname, "PLUS") == 0 || strcmp(sib->ttname, "MINUS") == 0 || strcmp(sib->ttname, "STAR") == 0 || strcmp(sib->ttname, "DIV") == 0 || strcmp(sib->ttname, "RELOP") == 0 ) {
			//new temp 1
			//printf("STAR\n");
			Operand op1 = (Operand)malloc(sizeof(struct Operand_));
			op1->kind = TEMP_OP;
			op1->u.var_no = newtemp();
			TypePtr t1 = Exp(child, op1);
			//new temp 2
			Operand op2 = (Operand)malloc(sizeof(struct Operand_));
			op2->kind = TEMP_OP;
			op2->u.var_no = newtemp();
			TypePtr t2 = Exp(sib->sibling, op2);
			if(t1 == NULL || t2 == NULL)
				return NULL;
			else if(t1->kind == 0 && t2->kind == 0 && t1->u.basic == t2->u.basic) {
				if(place == NULL)
					return t1;
				InterCode code = (InterCode)malloc(sizeof(struct InterCode_));
				code->u.three.result=place;
				code->u.three.op1 = op1;
				code->u.three.op2 = op2;
				if(strcmp(sib->ttname, "PLUS") == 0)
					code->kind=ADD_IC;
				else if(strcmp(sib->ttname, "MINUS") == 0)
					code->kind = SUB_IC;
				else if(strcmp(sib->ttname, "STAR") == 0)
					code->kind = MUL_IC;
				else if(strcmp(sib->ttname, "DIV") == 0)
					code->kind = DIV_IC;
				insert_code(code);
				return t1;
			}
			else {
				printf("Error type 7 at Line %d: Type mismatched for operands.\n", child->lineno);
				return NULL;
			}
		} //end of + - * /
		else if(strcmp(sib->ttname, "AND") == 0 || strcmp(sib->ttname, "OR") == 0 || strcmp(sib->ttname, "RELOP") == 0) {
			Operand label_t = (Operand) malloc(sizeof(struct Operand_));
			label_t->kind = LABEL_OP;
			label_t->u.var_no = newlabel();
			Operand label_f = (Operand)malloc(sizeof(struct Operand_));
			label_f->kind = LABEL_OP;
			label_f->u.var_no = newlabel();
			if(place != NULL) {
				InterCode code = (InterCode)malloc(sizeof(struct InterCode_));
				code->kind=ASSIGN_IC;
				code->u.two.left=place;
				Operand temp_right = (Operand)malloc(sizeof(struct Operand_));
				temp_right->kind=CONSTANT_OP_INT;
				temp_right->u.value = 0;
				code->u.two.right = temp_right;
				insert_code(code);
			}
			TypePtr tp = Exp_Cond(n, label_t, label_f);
			
			InterCode label_t_code = (InterCode) malloc(sizeof(struct InterCode_));
			InterCode label_f_code = (InterCode) malloc(sizeof(struct InterCode_));
			label_t_code->kind = LABEL_IC;
			label_f_code->kind = LABEL_IC;
			label_t_code->u.one.op = label_t;
			insert_code(label_t_code);
			
			Operand temp2_right = (Operand)malloc(sizeof(struct Operand_));
			temp2_right->kind = CONSTANT_OP_INT;
			temp2_right->u.value = 1;
			InterCode code_i = (InterCode) malloc(sizeof(struct Operand_));
			code_i->kind = ASSIGN_IC;
			code_i->u.two.left = place;
			code_i->u.two.right = temp2_right;
			if(place != NULL) {
				insert_code(code_i);
			}			
			label_f_code->u.one.op = label_f;
			insert_code(label_f_code);
			return tp;			
		}//end of and, or, relop
		else if(strcmp(sib->ttname, "LB") == 0) {
			//array
			Operand array_op = (Operand)malloc(sizeof(struct Operand_));
			array_op->kind = TEMP_OP;
			array_op->u.var_no=newtemp();
			TypePtr t = Exp(child, array_op);
			if(t == NULL) {
				return NULL;
			}
			//printtype(t);
			if(t->kind != 1) {
				printf("Error type 10 at Line %d: it is not an array\n", child->lineno);
				return NULL;
			}
			Operand inner = (Operand)malloc(sizeof(struct Operand_));
			inner->kind=TEMP_OP;
			inner->u.var_no = newtemp();
			sib = sib->sibling;
			TypePtr t2 = Exp(sib, inner);
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
			
			Operand offset_op = (Operand)malloc(sizeof(struct Operand_));
			offset_op->kind = TEMP_OP;
			offset_op->u.var_no=newtemp();
			Operand array_size = (Operand)malloc(sizeof(struct Operand_));
			array_size->kind = CONSTANT_OP_INT;
			//only here use u.value
			array_size->u.value = ElemSize(t->u.array.elem);
			//delete one instruction
			int offset_constant = sib->child->val_int*array_size->u.value;
			Operand offset_c = (Operand)malloc(sizeof(struct Operand_));
			offset_c->kind = CONSTANT_OP_INT;
			offset_c->u.value = offset_constant;
			//to be done: add offset constant value!
			/*
			InterCode array_addr = (InterCode)malloc(sizeof(struct InterCode_));
			array_addr->kind = MUL_IC;
			array_addr->u.three.result = offset_op;
			array_addr->u.three.op1 = inner;
			array_addr->u.three.op2 = array_size;
			insert_code(array_addr);
			* */
			
			InterCode array_addr2 = (InterCode)malloc(sizeof(struct InterCode_));
			array_addr2->kind = ADD_IC;
			TypePtr subtype = t->u.array.elem;
			Operand base_addr = (Operand)malloc(sizeof(struct Operand_));
			base_addr->kind = TEMP_OP;
			base_addr->u.var_no=newtemp();
			//can i use base_addr directly?
			//TODO -
			if(subtype->kind == 0) {
				place->kind = TADDRESS_OP;
				place->u.str = base_addr;
				array_addr2->u.three.result = base_addr;
			}
			else
				array_addr2->u.three.result = place;
			array_addr2->u.three.op1 = array_op;
			array_addr2->u.three.op2 = offset_c;
			insert_code(array_addr2);
						
			return t->u.array.elem;
		}
		else if(strcmp(sib->ttname, "DOT") == 0) {
			//struct inner content!
			Operand in_op = (Operand)malloc(sizeof(struct Operand_));
			in_op->kind = TEMP_OP;
			in_op->u.var_no = newtemp();
			TypePtr t1 = Exp(child, in_op);
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
			int offsetsize = 0;
			while(f != NULL) {
				if(strcmp(f->name, sib->val) == 0) {
					//calculate offset
					if(offsetsize == 0) {
						if(in_op->kind == VARIABLE_OP || in_op->kind == VADDRESS_OP) {
							if(f->type->kind == 0) {
								place->kind = VADDRESS_OP;
								place->u.str = in_op;
							}
							else {
								place->kind = VARIABLE_OP;
								place->u.var_no = in_op->u.var_no;
							}
						}
						else if(in_op->kind == TEMP_OP || in_op->kind == TADDRESS_OP) {
							if(f->type->kind == 0) {
								place->kind = TADDRESS_OP;
								place->u.str = in_op;
							}
							else {
								place->kind = TEMP_OP;
								place->u.var_no = in_op->u.var_no;
							}
						}
					}
					else {
						Operand temp = (Operand)malloc(sizeof(struct Operand_));
						temp->kind = TEMP_OP;
						temp->u.var_no = newtemp();
						Operand temp2 = (Operand)malloc(sizeof(struct Operand_));
						temp2->kind = CONSTANT_OP_INT;
						temp2->u.value = offsetsize;
						InterCode code1 = (InterCode)malloc(sizeof(struct InterCode_));
						code1->kind  = ADD_IC;
						if(f->type->kind == 0) {
							place->kind=TADDRESS_OP;
							place->u.str = temp;
							code1->u.two.left = temp;
						}
						else {
							code1->u.two.left = place;
						}
						code1->u.two.left = in_op;
						code1->u.two.right = temp2;
						insert_code(code1);
					}
					return f->type;
				}
				offsetsize = offsetsize+ElemSize(f->type);
				f = f->tail;
			}
			if(f == NULL) {
				printf("Error type 14 at Line %d: Non-existent field '%s'.\n", child->lineno, sib->val);
				return NULL;
			}
		} //end of dot of struct
	} //end of Exp
	else if(strcmp(child->ttname, "LP") == 0) { //deal with sibling
		//printf("LP\n");
		child = child->sibling;
		return Exp(child, place);
	}
	else if(strcmp(child->ttname, "MINUS") == 0) {
		//t? =  #0 - #constant!
		child = child->sibling;
		Operand op = (Operand)malloc(sizeof(struct Operand_));
		op->kind = TEMP_OP;
		op->u.var_no = newtemp();
		TypePtr t = Exp(child, op);
		if(t == NULL) {
			return NULL;
		}
		else {
			if(t->kind!=0) {
				printf("Error type 7 at Line %d: Type mismatched for operands '-'.\n", child->lineno);
				return NULL;
			}
		}
		
		Operand op1 = (Operand)malloc(sizeof(struct Operand_));
		op1->kind = CONSTANT_OP_INT;
		op1->u.value = 0;
		InterCode code = (InterCode)malloc(sizeof(struct InterCode_));
		code->kind = SUB_IC;
		code->u.three.result = place;
		code->u.three.op1 = op1;
		code->u.three.op2 = op;
		/*
		Operand op1 = (Operand)malloc(sizeof(struct Operand_));
		op1->kind = CONSTANT_OP_INT;
		op1->u.value = -op->u.value;
		InterCode code_c = (InterCode)malloc(sizeof(struct InterCode_));
		code_c->kind = ASSIGN_IC;
		code_c->u.two.left = place;
		code_c->u.two.right = op1;
		* */
		//segment fault!
		if(place!=NULL) {
			insert_code(code);
		}
		return t;
	}
	else if(strcmp(child->ttname, "NOT") == 0) {
		TREENODE *sib1 = sib1->sibling;
		Operand op0 = (Operand)malloc(sizeof(struct Operand_));
		op0->kind = TEMP_OP;
		op0->u.var_no = newtemp();
		TypePtr t = Exp(sib1, op0);
		if(t == NULL) {
			return NULL;
		}
		if(!(t->kind == 0 && t->u.basic == 0)) {
			printf("Error type 7 at Line %d: Type mismatched for operands '!'.\n", sib1->lineno);
			return NULL;
		}
		Operand label1 = (Operand)malloc(sizeof(struct Operand_));
		label1->kind  = LABEL_OP;
		label1->u.var_no=newlabel();
		Operand label2 = (Operand)malloc(sizeof(struct Operand_));
		label2->kind  = LABEL_OP;
		label2->u.var_no=newlabel();
		InterCode code = (InterCode)malloc(sizeof(struct InterCode_));
		code->kind = ASSIGN_IC;
		code->u.two.left = place;
		
		Operand temp0 = (Operand)malloc(sizeof(struct Operand_));
		temp0->kind= CONSTANT_OP_INT;
		temp0->u.value = 0;
		code->u.two.right = temp0;
		if(place != NULL) {
			insert_code(code);
		} 
		//will insert code1
		TypePtr t2 = Exp_Cond(n, label1, label2);
		
		InterCode l1_code = (InterCode)malloc(sizeof(struct InterCode_));
		l1_code->kind = LABEL_IC;
		l1_code->u.one.op = label1;
		insert_code(l1_code);
		
		Operand temp1 = (Operand)malloc(sizeof(struct Operand_));
		temp0->kind= CONSTANT_OP_INT;
		temp0->u.value = 1;
		InterCode code2 = (InterCode) malloc(sizeof(struct InterCode_));
		code2->kind = ASSIGN_IC;
		code2->u.two.left = place;
		code2->u.two.right = temp1;
		if(place != NULL) 
			insert_code(code2);
		
		InterCode l2_code = (InterCode)malloc(sizeof(struct InterCode_));
		l2_code->kind = LABEL_IC;
		l2_code->u.one.op = label2;
		insert_code(l2_code);
		
		return t2;
	}
	else if(strcmp(child->ttname, "ID") == 0) {
		TREENODE *sib = child->sibling;
		if(sib == NULL) { //
			FieldList f = findSymbol(child->val);
			if(f == NULL) {
				printf("Error type 1 at Line %d: Undefined variable '%s'.\n", child->lineno, child->val);
				return NULL;
			}
			place->kind = VARIABLE_OP;
			place->u.name = child->val;
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
			if(strcmp(sib->ttname, "RP") == 0) { //without args!
				if(pList != NULL) {
					printf("Error type 9 at Line %d: Function '%s' is not applicable for arguments.\n", sib->lineno, fu->name);
				}
				//read function
				if(strcmp(fu->name, "read") == 0) {
					InterCode readcode = (InterCode)malloc(sizeof(struct InterCode_));
					readcode->kind = READ_IC;
					readcode->u.one.op = place;
					if(place != NULL)
						insert_code(readcode);
				}
				else { //other functions
					Operand funcop = (Operand)malloc(sizeof(struct Operand_));
					funcop->kind = FUNCTION_OP;
					funcop->u.name = fu->name;
					InterCode callfunccode = (InterCode)malloc(sizeof(struct InterCode_));
					callfunccode->kind = CALL_IC;
					callfunccode->u.two.left = place;
					callfunccode->u.two.right = funcop;
					//if(place != NULL)
					//printf("11\n");
						insert_code(callfunccode);
				}
			}
			else {
				Operand arglist = (Operand)malloc(sizeof(struct Operand_));
				arglist->next = NULL;
				if(Args(sib, pList, arglist) == 0) {
					printf("Error type 9 at Line %d: Function '%s' is not applicable for arguments.\n", sib->lineno, fu->name);
				}
				else {
					if(strcmp(fu->name, "write") == 0) {
						InterCode writecode = (InterCode)malloc(sizeof(struct InterCode_));
						writecode->kind = WRITE_IC;
						writecode->u.one.op=arglist->next;
						insert_code(writecode);
					}
					else {
						arglist = arglist->next;
						while(arglist != NULL) {
							InterCode argcode = (InterCode) malloc(sizeof(struct InterCode_));
							argcode->kind = ARG_IC;
							argcode->u.one.op = arglist;
							insert_code(argcode);
							arglist = arglist->next;
						}
						Operand funcop = (Operand)malloc(sizeof(struct Operand_));
						funcop->kind = FUNCTION_OP;
						funcop->u.name = fu->name;
						InterCode callfunccode = (InterCode) malloc(sizeof(struct InterCode_));
						callfunccode->kind = CALL_IC;
						//printf("12\n");
						callfunccode->u.two.left = place;
						callfunccode->u.two.right = funcop;
						insert_code(callfunccode);
					}
				}
			}
			return fu->retType;
		}
	}
	else if(strcmp(child->ttname, "INT") == 0) {
		TypePtr temp = malloc(sizeof(struct Type_));
		temp->kind = 0;
		temp->u.basic = 0;
		//segment fault!
		if(place != NULL) {
			place->kind = CONSTANT_OP_INT;
			place->u.value = child->val_int;
		}
		return temp;
	}
	else if(strcmp(child->ttname, "FLOAT") == 0) {
		TypePtr temp = malloc(sizeof(struct Type_));
		temp->kind = 0;
		temp->u.basic = 1;
		if(place!= NULL) {
			place->kind = CONSTANT_OP_FLOAT;
			place->u.valuef = child->val_float;
		}
		return temp;
	}
	return NULL;
}

TypePtr Exp_Cond(TREENODE* n,Operand label_true, Operand label_false) {
	TREENODE *child = n->child;
	if(strcmp(child->ttname, "Exp") == 0) {
		TREENODE *sib = child->sibling;
		if(strcmp(sib->ttname, "RELOP") == 0) {
			Operand op1 = (Operand)malloc(sizeof(struct Operand_));
			op1->kind = TEMP_OP;
			op1->u.var_no=newtemp();
			Operand op2 = (Operand)malloc(sizeof(struct Operand_));
			op2->kind = TEMP_OP;
			op2->u.var_no=newtemp();
			TREENODE *sibsib = sib->sibling;
			TypePtr t1 = Exp(child, op1);//code for op1
			TypePtr t2 = Exp(sibsib, op2);//code for op2
			if(t1 == NULL || t2 == NULL)
				return NULL;
			else if(t1->kind == 0 && t2->kind == 0 && t1->u.basic == t2->u.basic) {
				InterCode code3 = (InterCode)malloc(sizeof(struct InterCode_));
				code3->kind = RELOP_IC;
				code3->u.if_relop.op1 = op1;
				code3->u.if_relop.op2 = op2;
				code3->u.if_relop.label_true = label_true;
				code3->u.if_relop.relop_sign = sib->val;
				insert_code(code3);
				//goto false
				InterCode code4 = (InterCode)malloc(sizeof(struct InterCode_));
				code4->kind = GOTO_IC;
				code4->u.one.op = label_false;
				insert_code(code4);
				return t1;
			}
			else 
				return NULL;
		}
		else if(strcmp(sib->ttname, "AND") == 0) {
			Operand label1 = (Operand)malloc(sizeof(struct Operand_));
			label1->kind = LABEL_OP;
			label1->u.var_no = newlabel();
			//code for child
			TypePtr t1 = Exp_Cond(child, label1, label_false);
			
			InterCode label1_code = (InterCode)malloc(sizeof(struct InterCode_));
			label1_code->kind = LABEL_IC;
			label1_code->u.one.op = label1;
			insert_code(label1_code);
			
			TREENODE* sibsib = sib->sibling;
			//code for sibsib
			TypePtr t2 = Exp_Cond(sibsib, label_true, label_false);
			if(t1 == NULL || t2 == NULL) {
				return NULL;
			}
			else if(t1->kind==0 && t2->kind == 0 && t1->u.basic == t2->u.basic) {
				return t1;
			}
			else
				return NULL;
		}
		else if(strcmp(sib->ttname, "OR") == 0) {
			Operand label1 = (Operand)malloc(sizeof(struct Operand_));
			label1->kind = LABEL_OP;
			label1->u.var_no = newlabel();
			//code for child
			TypePtr t1 = Exp_Cond(child, label_true, label1);
			
			InterCode label1_code = (InterCode)malloc(sizeof(struct InterCode_));
			label1_code->kind = LABEL_IC;
			label1_code->u.one.op = label1;
			insert_code(label1_code);
			
			TREENODE* sibsib = sib->sibling;
			//code for sibsib
			TypePtr t2 = Exp_Cond(sibsib, label_true, label_false);
			if(t1 == NULL || t2 == NULL) {
				return NULL;
			}
			else if(t1->kind==0 && t2->kind == 0 && t1->u.basic == t2->u.basic) {
				return t1;
			}
			else
				return NULL;
		}
	}//end of Exp, then NOT
	else if(strcmp(child->ttname, "NOT") == 0) {
		TREENODE* sib = child->sibling;
		//reverse!
		TypePtr t = Exp_Cond(sib, label_false, label_true);
		if(t == NULL)
			return NULL;
		if(t->kind == 0)
			return t;
	}
	//add codes for other cases?
	/* 
	Operand op_1 = (Operand)malloc(sizeof(struct Operand_)); 
	op_1->kind = TEMP_OP;
	op_1->u.var_no=newtemp();
	TypePtr tp = Exp(n, op_1);
	InterCode code_1 = (InterCode)malloc(sizeof(Operand_));
	code_1->kind = RELOP_IC;
	code_1->u.
	* */
}

int Args(TREENODE *n, FieldList f, Operand list) {
	if(n == NULL)
		return 1;
	//printf("Args%d\n", n->lineno);
	if(n == NULL) {
		if(f == NULL)
			return 1;
		else
			return 0;
	}
	Operand op = (Operand)malloc(sizeof(struct Operand_));
	op->kind = TEMP_OP;
	op->u.var_no = newtemp();
	TREENODE *child = n->child;
	//code for PARAM: 
	TypePtr t = Exp(child, op);
	op->next = list->next;
	list->next = op;
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
	return Args(child->sibling->sibling, f->tail, list);
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

int ElemSize(TypePtr tp) {
	if(tp->kind == 0) {
		if(tp->u.basic == 0) {
			return 4;
		}
		else if(tp->u.basic == 1) {
			return 8;
		}
	}
	else if(tp->kind == 1) {//array
		if(tp->u.array.elem->kind == 1) {
			printf("Cannot translate: Code contains variables of multi-dimensional array type or parameters of array type.\n");
			exit(-1);
		}
		return tp->u.array.size*ElemSize(tp->u.array.elem);
	}
	return 0;
}


