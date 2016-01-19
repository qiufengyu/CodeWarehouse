#ifndef __SEMANTIC_H__
#define __SEMANTIC_H__

#define MAX_TABLE_SIZE 13131

//Some definitions:
typedef struct Type_* TypePtr;
typedef struct FieldList_* FieldList;
typedef struct StructList_* StructList;
typedef struct FuncList_* FuncList;

struct Type_ {
		enum { BASIC, ARRAY, STRUCTURE, FUNCTION } kind;
		union {
			// 基本类型
			int basic;
			// 数组类型信息包括元素类型与数组大小构成
			struct { TypePtr elem; int size; } array;
			// 结构体类型信息是一个链表
			struct {
				char *structname; 
				FieldList content;
				int firstline;
			} structure;
			struct {
				char *functionname;
				FieldList paraList;
				TypePtr rettype;
				//int defflag;
				int firstlineno;
			} function;
		} u;
};

struct FieldList_ {
	char *name; //name
	//int lineno;
	TypePtr type; //type
	FieldList tail; //next field
	FieldList nextHash; //deal with conflicts!
};

struct StructList_ {
	char *name;//name
	int lineno;
	FieldList content; //inner components
	StructList nextHash; //deal with conflicts!
};

struct FuncList_ {
	char *name;
	int lineno;
	TypePtr retType;
	FieldList paraList;
	FuncList nextHash; // open addressing
};


//Functions:
//unsigned int hash_pjw(char *name);
unsigned int BKDR_hash(char *str);
//Initial the hashtables
void initTables();
//insert elements
int add_fieldtable(FieldList f);
int add_functable(FuncList f);
int add_structtable(StructList s);
void add_funcpara(FuncList f);
void add_structcontent(StructList s, FieldList fcontent);
FieldList findSymbol(char *name);
FuncList findFunc(char *name);
StructList findStruct(char *name);

//traverse the tree
void Program(TREENODE *root);
void ExtDefList(TREENODE *n);
void ExtDef(TREENODE *n);
TypePtr Specifier(TREENODE *n);
TypePtr StructSpecifier(TREENODE *n);
void ExtDecList(TREENODE *n, TypePtr type);
FuncList FunDec(TREENODE *n, TypePtr t);
FieldList DefList(TREENODE *n, int flag);
FieldList Def(TREENODE *n, int flag);
FieldList DecList(TREENODE *n, TypePtr type, int flag);
FieldList Dec(TREENODE *n, TypePtr type, int flag);
FieldList VarDec(TREENODE *n, TypePtr type, int flag);
FieldList VarList(TREENODE *n);
FieldList ParamDec(TREENODE *n);
void CompSt(TREENODE *n, TypePtr rettype);
void StmtList(TREENODE *n, TypePtr rettype);
void Stmt(TREENODE *n, TypePtr rettype);
TypePtr Exp(TREENODE* n);
int Args(TREENODE *n, FieldList f);

//some small functions
int typeMatch(TypePtr t1, TypePtr t2);
int contentEqual(FieldList f1, FieldList f2);
void printtype(TypePtr t);
void printparam(FieldList f);
#endif
