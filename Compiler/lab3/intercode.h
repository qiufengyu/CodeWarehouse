#ifndef __INTERCODE_H__
#define __INTERCODE_H__

#define MAX_LINE 1024

typedef struct Operand_* Operand;
typedef struct InterCode_* InterCode;

struct Operand_{
	enum { VARIABLE_OP, CONSTANT_OP_INT, CONSTANT_OP_FLOAT, VADDRESS_OP, TADDRESS_OP, LABEL_OP, TEMP_OP, FUNCTION_OP, RELOP_OP, ARG_OP, SIZE_OP, DEREF_OP, POINTER_OP } kind;
	union {
		int value; // store some int values
		float valuef; //store some float values
		int var_no; //with label, variable, temp no.
		char *name;
		Operand str;
	} u;
	Operand next; //Args
};

struct InterCode_ {
	enum { ASSIGN_IC, ADD_IC, SUB_IC, MUL_IC, DIV_IC, 
		LABEL_IC, FUNCTION_IC, GOTO_IC, RELOP_IC, RETURN_IC, DEC_IC, ARG_IC, CALL_IC, PARAM_IC,
		READ_IC, WRITE_IC,
		ADDR_IC } kind;
	union {
		struct { //label, function, goto, return, arg, param, read, write
			Operand op;
		} one;
		struct { //assign, call
			Operand left;
			Operand right;
		} two;
		struct { //add, sub, mul, div
			Operand result;
			Operand op1;
			Operand op2;
		} three;
		struct { //if relop goto
			Operand op1, op2;
			char *relop_sign;
			Operand label_true;
		} if_relop;
		struct { //dec
			Operand op;
			Operand size_op;
			int size;
		} dec_code;
	} u;
	InterCode prev, next;
};

void init_intercode();
int newlabel();
int newtemp();

//InterCode
void insert_code(InterCode ic);
void delete_code(InterCode ic);
void print_operand(FILE* fp, Operand op);
void print_code(FILE* fp);
Operand new_Operand_value(int kind, int value);
Operand new_Operand_string(int kind, char *str);
//Optimize
void checkassignsame();
//Global variables
InterCode head, tail;

#endif
