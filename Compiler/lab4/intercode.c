#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "intercode.h"

int label_no;
int temp_no;
int variable_no;

void init_intercode() {
	head = NULL;
	tail = NULL;
	label_no = 1;
	temp_no = 1;
	variable_no = 0;
}

int newlabel() {
	return label_no++;
}

int newtemp() {
	return temp_no++;
}


void insert_code(InterCode ic) {
	ic->prev = NULL;
	ic->next = NULL;
	if(head == NULL) {
		head = ic;
		tail = ic;
	}
	else {
		tail->next = ic;
		ic->prev = tail;
		tail = ic;
		tail->next = head;
		head->prev = tail;
	}
}

void delete_code(InterCode ic) {
	if(ic == NULL)
		return;
	ic->prev->next = ic->next;
	ic->next->prev = ic->prev;
	//free(ic);	
}

//print codes and operands
void print_code(FILE* fp) {
	assert(fp!=NULL);
	InterCode h = head;
	do {
		switch(h->kind) {
			case ASSIGN_IC: {
					print_operand(fp, h->u.two.left);
					fprintf(fp, " := ");
					print_operand(fp, h->u.two.right);					
			} break;
			case ADD_IC: {
				print_operand(fp, h->u.three.result);
				fprintf(fp, " := ");
				print_operand(fp, h->u.three.op1);
				fprintf(fp, " + ");
				print_operand(fp, h->u.three.op2);
			} break;
			case SUB_IC: {
				print_operand(fp, h->u.three.result);
				fprintf(fp, " := ");
				print_operand(fp, h->u.three.op1);
				fprintf(fp, " - ");
				print_operand(fp, h->u.three.op2);
			} break;
			case MUL_IC: {
				print_operand(fp, h->u.three.result);
				fprintf(fp, " := ");
				print_operand(fp, h->u.three.op1);
				fprintf(fp, " * ");
				print_operand(fp, h->u.three.op2);
			} break;
			case DIV_IC: {
				print_operand(fp, h->u.three.result);
				fprintf(fp, " := ");
				print_operand(fp, h->u.three.op1);
				fprintf(fp, " / ");
				print_operand(fp, h->u.three.op2);
			} break;
			case LABEL_IC: {
				fprintf(fp, "LABEL ");
				print_operand(fp, h->u.one.op);
				fprintf(fp, " : ");
			} break;
			case FUNCTION_IC: {
				fprintf(fp, "FUNCTION ");
				print_operand(fp, h->u.one.op);
				fprintf(fp, " : ");
			} break;
			case GOTO_IC: {
				fprintf(fp, "GOTO ");
				print_operand(fp, h->u.one.op);
			} break;
			case RELOP_IC: {
				fprintf(fp, "IF ");
				print_operand(fp, h->u.if_relop.op1);
				fprintf(fp, " %s ", h->u.if_relop.relop_sign);
				print_operand(fp, h->u.if_relop.op2);
				fprintf(fp, " GOTO ");
				print_operand(fp, h->u.if_relop.label_true);			
			} break;
			case RETURN_IC: {
				fprintf(fp, "RETURN ");
				print_operand(fp, h->u.one.op);
			} break;
			case DEC_IC: {
				fprintf(fp, "DEC ");
				print_operand(fp, h->u.dec_code.op);
				fprintf(fp, " %d", h->u.dec_code.size);
			} break;
			case ARG_IC: {
				fprintf(fp, "ARG ");
				print_operand(fp, h->u.one.op);
			} break;
			case CALL_IC: {
				print_operand(fp, h->u.two.left);
				fprintf(fp, " := CALL ");
				print_operand(fp, h->u.two.right);
			} break;
			case PARAM_IC: {
				fprintf(fp, "PARAM ");
				print_operand(fp, h->u.one.op);
			} break;
			case READ_IC: {
				fprintf(fp, "READ ");
				print_operand(fp, h->u.one.op);
			} break;
			case WRITE_IC: {
				fprintf(fp, "WRITE ");
				print_operand(fp, h->u.one.op);
			} break;
			//especially for array and some other address
			case ADDR_IC: {
				print_operand(fp, h->u.two.left);
				fprintf(fp, " := &");
				print_operand(fp, h->u.two.right);
			} break;
			default: {}
		}
		h = h->next;
		fprintf(fp,"\n");
	} while(h != head);
}

void print_operand(FILE* fp, Operand op) {
	assert(fp != NULL);
	//deal with function used without caring the return type!
	if(op == NULL) {
		fprintf(fp, "t0");
		return;
	}
	switch(op->kind) {
		case VARIABLE_OP: {
			fprintf(fp, "%s", op->u.name);
		} break;
		case CONSTANT_OP_INT: {
			fprintf(fp, "#%d", op->u.value);
		} break;
		case CONSTANT_OP_FLOAT: {
			fprintf(fp, "#%f", op->u.valuef);
		} break;
		case VADDRESS_OP: {
			fprintf(fp, "*%s", op->u.name);
		}break;
		case TADDRESS_OP: {
			fprintf(fp, "*t%d", op->u.str->u.var_no);
		} break;
		case LABEL_OP: {
			fprintf(fp, "label%d", op->u.var_no);
		} break;
		case TEMP_OP: {
			fprintf(fp, "t%d", op->u.var_no);
		} break;
		case FUNCTION_OP: {
			fprintf(fp, "%s", op->u.name);
		} break;
		case RELOP_OP: {
			fprintf(fp, "%s", op->u.name);
		} break;
		case SIZE_OP: {
			fprintf(fp, "%d", op->u.value);
		} break;
		default: {}
	}
}

Operand new_Operand_value(int kind, int value) {
	Operand op = (Operand)malloc(sizeof(struct Operand_));
	op->kind = kind;
	op->u.value = value;
	return op;
}
Operand new_Operand_string(int kind, char *str) {
	if(str == NULL)
		return NULL;
	Operand op = (Operand)malloc(sizeof(struct Operand_));
	op->kind = kind;
	op->u.name = (char *)malloc(8*4);
	strcpy(op->u.name, str);
	return op;
}

/*
InterCode new_code(int kind, Operand result, Operand op1, Operand op2) {
	InterCode new_ic = (InterCode)malloc(sizeof(struct InterCode_));
	new_ic->kind = kind;
	if(kind == LABEL_IC || kind == FUNCTION_IC || kind == GOTO_IC || kind == RETURN_IC || kind == ARG_IC || kind == PARAM_IC || kind == READ_IC || kind == WRITE_IC) {
		new_ic->u.one.op = result;
	}
	else if(kind == CALL_IC || kind == ASSIGN_IC) {
		new_ic->u.two.left = result;
		new_ic->u.two.right = op1;
	}
	else if(kind == ADD_IC || kind == SUB_IC || kind == MUL_IC || kind == DIV_IC) {
		new_ic->u.three.result = result;
		new_ic->u.three.op1 = op1;
		new_ic->u.three.op2 = op2;
	}
	else if(kind == RELOP_IC) {
		new_ic->u.if_relop.op1 = result;
		new_ic->u.if_relop.relop_sign = op1;
		new_ic->u.if_relop.op2 = op2;
	}
	else if(kind == DEC_IC) {
		new_ic->u.dec_code.op = result;
		new_ic->u.dec_code.size = op1;
	}
	else {
		printf("KIND = %d\n", kind);
		return NULL;
	}
	return new_ic;
}

InterCodes insert_label_code(int label_no) {
	Operand op = new_Operand_value(LABEL_OP, label_no);
	InterCode ic = new_code(LABEL_IC, op, NULL, NULL);
	return insert_code(ic);
}

InterCodes insert_function_code(char *fname) {
	Operand op = new_Operand_string(FUNCTION_OP, fname);
	InterCode ic = new_code(FUNCTION_IC, op, NULL, NULL);
	return insert_code(ic);
}

InterCodes insert_goto_code(int goto_no) {
	Operand op = new_Operand_value(LABEL_OP, goto_no);
	InterCode ic = new_code(GOTO_IC, op, NULL, NULL);
	return insert_code(ic);
}

InterCodes insert_return_code(int temp_no) {
	Operand op = new_Operand_value(TEMP_OP, temp_no);
	InterCode ic = new_code(RETURN_IC, op, NULL, NULL);
	return insert_code(ic);
}

InterCodes insert_arg_code(int temp_no) {
	Operand op = new_Operand_value(TEMP_OP, temp_no);
	InterCode ic = new_code(ARG_IC, op, NULL, NULL);
	return insert_code(ic);
}

InterCodes insert_param_code(Operand pa) {
	InterCode ic = new_code(PARAM_IC, pa, NULL, NULL);
	return insert_code(ic);
}

InterCodes insert_read_code(int temp_no) {
	Operand op = new_Operand_value(TEMP_OP, temp_no);
	InterCode ic = new_code(READ_IC, op, NULL, NULL);
	return insert_code(ic);
}

InterCodes insert_write_code(int temp_no) {
	Operand op = new_Operand_value(TEMP_OP, temp_no);
	InterCode ic = new_code(WRITE_IC, op, NULL, NULL);
	return insert_code(ic);
}

InterCodes insert_call_code(int temp_no, char *fname) {
	Operand lop = new_Operand_value(TEMP_OP, temp_no);
	Operand rop = new_Operand_string(FUNCTION_OP, fname);
	InterCode ic = new_code(CALL_IC, lop, rop, NULL);
	return insert_code(ic);
}

InterCodes insert_assign_code(Operand lop, Operand rop) {
	InterCode ic = new_code(ASSIGN_IC, lop, rop, NULL);
	return insert_code(ic);
}

//0+a; 1-s; 2*m; 3/d.
InterCodes insert_asmd_code(Operand result, Operand op1, Operand op2, int asmd) {
	InterCode ic = new_code(asmd, result, op1, op2);
	return insert_code(ic);
}

InterCodes insert_ifrelop_code(Operand op1, int relop_sign, Operand op2, int t_label) {
	Operand op = new_Operand_value(RELOP_OP, relop_sign);
	InterCode ic = new_code(RELOP_IC, op1, op, op2);
	return insert_code(ic);
}

InterCodes insert_dec_code(int var_no, int size) {
	Operand op = new_Operand_value(VARIABLE_OP, var_no);
	Operand sizeop = new_Operand_value(SIZE_OP, size);
	InterCode ic = new_code(DEC_IC, op, sizeop, NULL);
}

InterCodes translate_Exp(TREENODE* Exp, int place) {
	if(strcmp(Exp->ttname, "INT") == 0) {
		Operand left = new_Operand_value(TEMP_OP, place);
		Operand right = new_Operand_value(CONSTANT_OP, Exp->val_int);
		return insert_assign_code(left, right);
	}
	else if(strcmp(Exp->ttname, "ID") == 0 && Exp->sibling == NULL) {
		int id_no = locate_v(Exp->val);
		Operand right = NULL;
		FieldList tempf = findSymbol(Exp->val);
		if(tempf->type->kind == 1) { //array
			right = new_Operand_value(ADDRESS_OP, id_no);
		}
		else
			right = new_Operand_value(VARIABLE, id_no);
		Operand left = new_Operand_value(TEMP_OP, place);
		return insert_assign_code(left, right);
	}
	else if(strcmp(Exp->sibling->ttname, "ASSIGNOP") == 0) {
		
	}
}

*/


void checkassign() {
	InterCode cur = head;
	do {
		//assign the same! like a = a
		if(cur->kind == ASSIGN_IC) {
			if(cur->u.two.left->kind == cur->u.two.right->kind) {
				if(cur->u.two.left->kind == CONSTANT_OP_INT || cur->u.two.left->kind == CONSTANT_OP_FLOAT) {
					if(cur->u.two.left->u.value == cur->u.two.right->u.value) {
						delete_code(cur);
					}
				}
				else if(cur->u.two.left->kind == VARIABLE_OP || cur->u.two.left->kind == VADDRESS_OP) {
					if(strcmp(cur->u.two.left->u.name, cur->u.two.right->u.name) == 0) {
						delete_code(cur);
					}
				}
				else if(cur->u.two.left->kind == TEMP_OP || cur->u.two.left->kind == TADDRESS_OP) {
					if(cur->u.two.left->u.var_no == cur->u.two.right->u.var_no) {
						delete_code(cur);
					}
				}
			}				
		}
		else if(cur->kind == ADD_IC) {
			if(cur->u.three.op1->kind == CONSTANT_OP_INT && cur->u.three.op2->kind == CONSTANT_OP_INT) {
				InterCode ictemp = (InterCode)malloc(sizeof(struct InterCode_));
				memcpy(ictemp, cur, sizeof(struct InterCode_));
				Operand newop = malloc(sizeof(struct Operand_));
				newop->kind = CONSTANT_OP_INT;
				newop->u.value = cur->u.three.op1->u.value + cur->u.three.op2->u.value;
				cur->kind = ASSIGN_IC;
				cur->u.two.left = ictemp->u.three.result;
				cur->u.two.right = newop;
			}
		}
		else if	(cur->kind == SUB_IC) {
			if(cur->u.three.op1->kind == CONSTANT_OP_INT && cur->u.three.op2->kind == CONSTANT_OP_INT) {
				InterCode ictemp = (InterCode)malloc(sizeof(struct InterCode_));
				memcpy(ictemp, cur, sizeof(struct InterCode_));
				Operand newop = malloc(sizeof(struct Operand_));
				newop->kind = CONSTANT_OP_INT;
				newop->u.value = cur->u.three.op1->u.value - cur->u.three.op2->u.value;
				cur->kind = ASSIGN_IC;
				cur->u.two.left = ictemp->u.three.result;
				cur->u.two.right = newop;
			}
		}
		else if	(cur->kind == MUL_IC) {
			if(cur->u.three.op1->kind == CONSTANT_OP_INT && cur->u.three.op2->kind == CONSTANT_OP_INT) {
				InterCode ictemp = (InterCode)malloc(sizeof(struct InterCode_));
				memcpy(ictemp, cur, sizeof(struct InterCode_));
				Operand newop = malloc(sizeof(struct Operand_));
				newop->kind = CONSTANT_OP_INT;
				newop->u.value = cur->u.three.op1->u.value * cur->u.three.op2->u.value;
				cur->kind = ASSIGN_IC;
				cur->u.two.left = ictemp->u.three.result;
				cur->u.two.right = newop;
			}
		}
		else if	(cur->kind == DIV_IC) {
			if(cur->u.three.op1->kind == CONSTANT_OP_INT && cur->u.three.op2->kind == CONSTANT_OP_INT) {
				InterCode ictemp = (InterCode)malloc(sizeof(struct InterCode_));
				memcpy(ictemp, cur, sizeof(struct InterCode_));
				Operand newop = malloc(sizeof(struct Operand_));
				newop->kind = CONSTANT_OP_INT;
				newop->u.value = cur->u.three.op1->u.value / cur->u.three.op2->u.value;
				cur->kind = ASSIGN_IC;
				cur->u.two.left = ictemp->u.three.result;
				cur->u.two.right = newop;
			}
		}
		cur = cur->next;
	} while(cur!=head);
}
