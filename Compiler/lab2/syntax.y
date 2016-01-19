%{
	#include "lex.yy.c"
	#include "tree.h"
	#include "semantic.h"
	// #define YYDEBUG 1
	TREENODE* root;
%}

/* declared types */
%union {
	struct treeNode* node;
}

/* declared tokens */
%token <node> INT
%token <node> FLOAT
%token <node> ID
%token <node> TYPE
%token <node> STRUCT RETURN IF ELSE WHILE
%token <node> SEMI COMMA ASSIGNOP RELOP PLUS MINUS STAR DIV AND OR DOT NOT LP RP LB RB LC RC

%type <node> Program ExtDefList ExtDef Specifier ExtDecList FunDec CompSt VarDec
%type <node> StructSpecifier OptTag DefList Tag
%type <node> VarList ParamDec
%type <node> StmtList Stmt
%type <node> Def Dec DecList
%type <node> Exp Args

%right ASSIGNOP
%left OR
%left AND
%left RELOP
%left PLUS MINUS
%left STAR DIV
%right NOT
%left LP RP LB RB DOT

%nonassoc LOWER_THAN_ELSE
%nonassoc ELSE

%%
Program: ExtDefList {
	$$ = init_node("Program", Type, @$.first_line, "");
	if( $1 != NULL) {
		add_child($$, $1);
		root = $$;
	}
	else
		$$ = NULL;
}
;
ExtDefList: /* empty */ { $$ = NULL; }
| ExtDef ExtDefList {
	$$ = init_node("ExtDefList", Type, @$.first_line, ""); 	
	add_child($$, $1);
	if( $2 != NULL) /* by: 陶堃：否则导致段错误！ 只要遇到可能为空的产生式，都要先判断！*/
		add_sibling($1, $2);
}
;
ExtDef: Specifier ExtDecList SEMI {
	$$ = init_node("ExtDef", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Specifier SEMI {
	$$ = init_node("ExtDef", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
}
| Specifier FunDec CompSt {
	$$ = init_node("ExtDef", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| error SEMI {
	/* init_node(&$$, "ExtDef", Type, @$.first_line); */
	/* add_child($$, $2); */
	errors++;
}
;
ExtDecList: VarDec {
	$$ = init_node("ExtDecList", Type, @$.first_line, "");
	add_child($$, $1);
}
| VarDec COMMA ExtDecList {
	$$ = init_node("ExtDecList", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
;

Specifier: TYPE {
	$$ = init_node("Specifier", Type, @$.first_line, "");
	add_child($$, $1);
}
| StructSpecifier {
	$$ = init_node("Specifier", Type, @$.first_line, "");
	add_child($$, $1);
}
;
StructSpecifier: STRUCT OptTag LC DefList RC {
	$$ = init_node("StructSpecifier", Type, @$.first_line, "");
	add_child($$, $1);
	if(($2 != NULL) && ($4 != NULL)){ /* by 陶堃! empty maybe*/
		add_sibling($1, $2);
		add_sibling($2, $3);
		add_sibling($3, $4);
		add_sibling($4, $5);
	}
	else if(($2 != NULL) && ($4 == NULL)){
		add_sibling($1, $2);
		add_sibling($2, $3);
		add_sibling($3, $5);
	}
	else if(($2 == NULL) && ($4 != NULL)){
		add_sibling($1, $3);
		add_sibling($3, $4);
		add_sibling($4, $5);
	}
	else{
		add_sibling($1, $3);
		add_sibling($3, $5);
	}
}
| STRUCT Tag {
	$$ = init_node("StructSpecifier", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
}
| STRUCT OptTag LC error RC {
	/* $$ = init_node("StructSpecifier", Type, @$.first_line, ""); */
	/* add_child($$, $1); */
	/* add_sibling($1, $2); */
	/* add_sibling($2, $3); */
	/* add_sibling($3, $5); */
	errors++;
}
;
OptTag: /* empty */ { $$ = NULL;}
| ID {
	$$ = init_node("OptTag", Type, @$.first_line, "");
	add_child($$, $1);
}
;
Tag: ID {
	$$ = init_node("Tag", Type, @$.first_line, "");
	add_child($$, $1);
}
;

VarDec: ID {
	$$ = init_node("VarDec", Type, @$.first_line, "");
	add_child($$, $1);
}
| VarDec LB INT RB {
	$$ = init_node("VarDec", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
	add_sibling($3, $4);
}
| VarDec LB error RB {
	/* $$ = init_node("VarDec", Type, @$.first_line, ""); */
	/* add_child($$, $1); */
	/* add_sibling($1, $2); */
	/* add_sibling($2, $4); */
	errors++;
}
;
FunDec: ID LP VarList RP {
	$$ = init_node("FunDec", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
	add_sibling($3, $4);
}
| ID LP RP {
	$$ = init_node("FunDec", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| error RP {
	/* $$ = init_node("FunDec", Type, @$.first_line, ""); */
	/* add_child($$, $1); */
	/* add_sibling($1, $2); */
	/* add_sibling($2, $4); */
	errors++;
}
;
VarList: ParamDec COMMA VarList {
	$$ = init_node("VarList", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| ParamDec {
	$$ = init_node("VarList", Type, @$.first_line, "");
	add_child($$, $1);
}
;
ParamDec: Specifier VarDec {
	$$ = init_node("ParamDec", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
}
| COMMA error {
	errors++;
}
| error COMMA {
	errors++;
}
| error RP {
	errors++;
}
;

CompSt: LC DefList StmtList RC {
	$$ = init_node("CompSt", Type, @$.first_line, "");
	add_child($$, $1);
	if($3 != NULL && $2 != NULL) {
		add_sibling($1, $2);
		add_sibling($2, $3);
		add_sibling($3, $4);
	}
	else if($2 == NULL && $3 != NULL) {
		add_sibling($1,$3);
		add_sibling($3, $4);
	}
	else if($2 != NULL && $3 == NULL) {
		add_sibling($1, $2);
		add_sibling($2, $4);
	}
	else { // all NULL
		add_sibling($1, $4);
	}
}
| error RC { /* with LP then conflicts! */
	/* $$ = init_node("CompSt", Type, @$.first_line, ""); */
	/* add_child($$, $2); */
	errors++;
}
;
StmtList: /* empty */ { $$ = NULL;}
| Stmt StmtList {
	$$ = init_node("StmtList", Type, @$.first_line, "");
	add_child($$, $1);
	if( $2 != NULL)
		add_sibling($1, $2);
}
;
Stmt: Exp SEMI {
	$$ = init_node("Stmt", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
}
| CompSt {
	$$ = init_node("Stmt", Type, @$.first_line, "");
	add_child($$, $1);
}
| RETURN Exp SEMI {
	$$ = init_node("Stmt", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);	
}
| IF LP Exp RP Stmt %prec LOWER_THAN_ELSE {
	$$ = init_node("Stmt", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
	add_sibling($3, $4);
	add_sibling($4, $5);
}
| IF LP Exp RP Stmt ELSE Stmt {
	$$ = init_node("Stmt", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
	add_sibling($3, $4);
	add_sibling($4, $5);
	if($6 != NULL) {
		add_sibling($5, $6);
		add_sibling($6, $7);
	}
}
| WHILE LP Exp RP Stmt {
	$$ = init_node("Stmt", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
	add_sibling($3, $4);
	add_sibling($4, $5);
}
| error SEMI {
	/* $$ = init_node("Stmt", Type, @$.first_line, ""); */
	/* add_child($$, $2); */
	errors++;
}
| error RP {
	/* $$ = init_node("Stmt", Type, @$.first_line, ""); */
	/* add_child($$, $2); */
	errors++;
}
| Exp error {
	errors++;
}
;

DefList: /* empty */ { $$ = NULL;}
| Def DefList {
	$$ = init_node("DefList", Type, @$.first_line, "");
	add_child($$, $1);
	if($2 != NULL) {
		add_sibling($1, $2);
	}
}
/* | error SEMI { */
/*	 errors++; */
/* } */
;
Def: Specifier DecList SEMI {
	$$ = init_node("Def", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Specifier DecList error {
	errors++;
}
/* | error SEMI { conflict*/
	/* $$ = init_node("Stmt", Type, @$.first_line, ""); */
	/* add_child($$, $2); */
	/* errors++; */
/* } */
;
DecList: Dec {
	$$ = init_node("DecList", Type, @$.first_line, "");
	add_child($$, $1);
}
| Dec COMMA DecList {
	$$ = init_node("DecList", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Dec COMMA error {
	errors++;
}
;
Dec: VarDec {
	$$ = init_node("Dec", Type, @$.first_line, "");
	add_child($$, $1);
}
| VarDec ASSIGNOP Exp {
	$$ = init_node("Dec", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| VarDec ASSIGNOP error {
	errors++;
}
;

Exp: Exp ASSIGNOP Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp AND Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp OR Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp RELOP Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp PLUS Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp MINUS Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp STAR Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp DIV Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| LP Exp RP {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| MINUS Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
}
| NOT Exp {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
}
| ID LP Args RP {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
	add_sibling($3, $4);
}
| ID LP RP {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp LB Exp RB {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
	add_sibling($3, $4);
}
| Exp DOT ID {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| ID {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
}
| INT {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
}
| FLOAT {
	$$ = init_node("Exp", Type, @$.first_line, "");
	add_child($$, $1);
}
| error RB {
	/* $$ = init_node("Exp", Type, @$.first_line, ""); */
	/* dd_child($$, $2); */
	errors++;
}
| ASSIGNOP error {
	errors++;
}
| error ASSIGNOP {
	errors++;
}
;
Args: Exp COMMA Args {
	$$ = init_node("Args", Type, @$.first_line, "");
	add_child($$, $1);
	add_sibling($1, $2);
	add_sibling($2, $3);
}
| Exp {
	$$ = init_node("Args", Type, @$.first_line, "");
	add_child($$, $1);
}
;

%%
yyerror(char* msg) {
	/* printf("Error type B at line %d.%d: %s\n", yylloc.first_line,yylloc.first_column+yyleng-1, msg); */
}

int main(int argc, char** argv)
{
	if (argc <=1 ) return 1;
	FILE* f = fopen(argv[1], "r");
	if(!f)
	{
		perror(argv[1]);
		return 1;
	}
	yyrestart(f);
	/* yydebug = 1; */
	yyparse();
	/* if(errors == 0) { */
		//print_tree(root,0);
		Program(root);	
	/* } */
	return 0;
}
