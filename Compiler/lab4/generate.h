#ifndef __GENERATE_H__
#define __GENERATE_H__
#include "intercode.h"

typedef struct VarDescript_ VarDescript;
struct VarDescript_{
	char *name;
	int reg; //标志寄存器
    int varname;
    int offset;
    VarDescript* next;
};

typedef struct RegDescript_ RegDescript;
struct RegDescript_{
    char name[4];
    int usedno;
    char *var;
};

void init_reg();
void init_read_write(FILE* fp);
int get_vreg();
void add_var(VarDescript * v);
void del_varlist();
void init_regname(); //对某些寄存器0-18需要进行重置
int print_reg(int index, FILE *fp);
int varcount_alloc(InterCode ic);
// 中间代码->目标语言
void intercodeDeal(InterCode ic, FILE* fp);
extern Operand assOpCode(Operand o, FILE *fp);
void reset_reg(); //在函数调用前，重置某些寄存器
void objectCode(FILE *fp);
//全局变量：寄存器描述符和变量描述符
RegDescript reg[32];
VarDescript* varhead;

#endif
