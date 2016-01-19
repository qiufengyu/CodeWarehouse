#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include "generate.h"

#define AVAILABLE_REG 18

int current_offset;
VarDescript* funlist;
int args_count;

void init_reg() {
	int i = 0;
	current_offset = 0;
	args_count = 0;
	funlist = NULL;
    for(i = 0; i < 32; i++) {
        reg[i].var = NULL;
        reg[i].usedno = 0;
		if(i>=0 && i<=9) { //t0-t9
			sprintf(reg[i].name, "$t%d", i);
		}
		else if(i>=10 && i<=18) {
			sprintf(reg[i].name, "$s%d", i-10);
		}
		else if(i == 19) {
			strncpy(reg[i].name, "$fp", 3);
		} 
		else if(i == 20) {
			strncpy(reg[i].name, "$sp", 3);
		}
		else if(i == 21) {
			strncpy(reg[i].name, "$ra", 3);
		}
		else if(i == 22) {
			strncpy(reg[i].name, "$v0", 3);
		}
		else if(i == 23) {
			strncpy(reg[i].name, "$v1", 3);
		}
		else if(i >= 24 && i <= 27) {
			sprintf(reg[i].name, "$a%d", i-24);
		}
	}
}

void init_read_write(FILE* fp) {
	fprintf(fp, ".data\n");
    fprintf(fp, "_prompt: .asciiz \"Enter an integer:\"\n");
	fprintf(fp, "_ret: .asciiz \"\\n\"\n");
    fprintf(fp, ".globl main\n");
    fprintf(fp, ".text\n");
    fprintf(fp, "\nread:\n");
    fprintf(fp, "subu $sp, $sp, 4\n");
    fprintf(fp, "sw $ra, 0($sp)\n");
	fprintf(fp, "subu $sp, $sp, 4\n");
	fprintf(fp, "sw $fp, 0($sp)\n");
	fprintf(fp, "addi $fp, $sp, 8\n");	
	fprintf(fp, "li $v0, 4\nla $a0, _prompt\n");
	fprintf(fp, "syscall\nli $v0, 5\nsyscall\n");
	fprintf(fp, "subu $sp, $fp, 8\n");
	fprintf(fp, "lw $fp, 0($sp)\n");
	fprintf(fp, "addi $sp, $sp, 4\n");
	fprintf(fp, "lw $ra, 0($sp)\n");
	fprintf(fp, "addi $sp, $sp, 4\n");
	fprintf(fp, "jr $ra\n");
	
    fprintf(fp, "\nwrite:\n");
    fprintf(fp, "subu $sp, $sp, 4\n"); 
	fprintf(fp, "sw $ra, 0($sp)\n");
	fprintf(fp, "subu $sp, $sp, 4\n");
	fprintf(fp, "sw $fp, 0($sp)\n");
	fprintf(fp, "addi $fp, $sp, 8\n");
    fprintf(fp, "li $v0, 1\nsyscall\nli $v0,4\n");
    fprintf(fp, "la $a0, _ret\nsyscall\nmove $v0, $0\n");
    fprintf(fp, "subu $sp, $fp, 8\n");
	fprintf(fp, "lw $fp, 0($sp)\n");
	fprintf(fp, "addi $sp, $sp, 4\n");
	fprintf(fp, "lw $ra, 0($sp)\n");
	fprintf(fp, "addi $sp, $sp, 4\n");
	fprintf(fp, "jr $ra\n");
}

//变量的寄存器分配
int get_vreg(Operand op, FILE *fp) {
	char *name = malloc(32);
	memset(name, 0, 32);
	//PARAM!!
	VarDescript* fhead = funlist;
	
	if(op->kind == TEMP_OP) {
		name[0] = 't';
		sprintf(name+1, "%d", op->u.var_no);
	}
	else if(op->kind == VARIABLE_OP) {
		strcpy(name, op->u.name);
	}
	
	while (fhead != NULL) {
		if(strcmp(fhead->name, name) == 0) {
			break;
		}
		fhead  = fhead->next;
	}
	
	int flag = 0;
	if (fhead == NULL) { //第一次
		fhead = malloc(sizeof(struct VarDescript_));
		fhead->name = name;
		fhead->reg = -1;
		current_offset += 4; //记录栈的位置
		fhead->offset = current_offset;
		flag = 1;
		fhead->next == NULL;
		add_var(fhead);
	}
	if(fhead->reg == -1) { //未分配寄存器，则分配一个
		int r = find_reg(fp); //find a free register
		fhead->reg = r;
		reg[r].var = fhead->name;
		if(fhead->offset >= 0 && !flag) {
			char * buf = malloc(32);
			memset(buf, 0, 32);
			sprintf(buf, "%d", fhead->offset);
			fprintf(fp, "subu $v1, $fp, %s\n", buf);
			fprintf(fp, "lw %s, 0($v1)\n", reg[fhead->reg].name);
		}
	}
	reg[fhead->reg].usedno = 0;
	return fhead->reg;
}

void add_var(VarDescript * v) {
	if(v == NULL) 
		return;
	v->next = NULL;
	if(funlist == NULL) {
		funlist = v;
	}
	else {
		VarDescript * h = funlist;
		while(1) {
			if(h->next == NULL) {
				break;
			}
			h = h->next;
		}
		h->next = v;
	}
}

void del_varlist() {
	VarDescript * h = funlist;
	while( h!= NULL) {
		funlist = funlist->next;
		free(h);
		h = funlist;
	}
}

int find_reg(FILE* fp) {
	int i = 0;
	for(; i< AVAILABLE_REG; i++) {
		if(reg[i].var != NULL) {
			reg[i].usedno++; //老1
		}
	}
	for(i=0; i< AVAILABLE_REG; i++) {
		if(reg[i].var == NULL) {
			return i;
		}
	}
	//都被使用，则需要将一个存储到内存中，进行置换
	int index = 0;
	int usedno_max = 0;
	for(i=0; i< AVAILABLE_REG; i++) {
		if(reg[i].usedno > usedno_max) {
			usedno_max = reg[i].usedno;
			index = i;
		}
	}
	
	VarDescript *h = funlist;
	while(h != NULL) {
		if(strcmp(reg[index].var, h->name) == 0) {
			break;
		}
		h = h->next;
	}
	if(h != NULL) {
		//如果是函数调用的变量		
		fprintf(fp, "subu $v1, $fp, %d\n", h->offset);
		fprintf(fp, "sw %s, 0($v1)\n", reg[index].name);
		h->reg = -1; //已经不在寄存器中
	}
	reg[index].var = NULL;
	reg[index].usedno = 0;
	return index;
}

/*
int print_reg(int index, FILE *fp) {
	fprintf(fp, "$%s" , reg[index].name);
}
* */


int varcount_alloc(InterCode ic) {
	ic=ic->next;
	int count=0;
	while(ic!=NULL&&ic->kind!=FUNCTION_IC)
	{
		count++;
		ic=ic->next;
	}
	return count*8;
}

void reset_reg() {
	int i = 0;
	for(i = 0; i< AVAILABLE_REG; i++) {
		reg[i].var = NULL;
		reg[i].usedno = 0;	
	}
}

void intercodeDeal(InterCode ic, FILE* fp) {
	int ckind = ic->kind;
	int r1, r2, r3;
	int memCount = 0;
	Operand op1, op2, result, label, left, right;
	InterCode icode;
	VarDescript* vhead;
	switch(ckind) {
		case LABEL_IC: {
			vhead = funlist; //变量描述符非空则要写回内存中
			while(vhead!=NULL) {
				if(vhead->reg >= 0) {
					char *buf = (char*)malloc(32);
					memset(buf, 0, 32);
					sprintf(buf, "%d", vhead->offset);
					fprintf(fp, "subu $v1, $fp, %s\n", buf);
					fprintf(fp, "sw %s, 0($v1)\n", reg[vhead->reg].name);
					// 重置
					reg[vhead->reg].var = NULL;
					reg[vhead->reg].usedno = 0;
					vhead->reg = -1;
				}
				vhead = vhead->next;
			}
			// print label
			fprintf(fp, "label%d:\n", ic->u.one.op->u.var_no) ;
		} break;
		case ASSIGN_IC: {
			left = ic->u.two.left;
			right = ic->u.two.right;
			if(left == NULL)
				return;
			if(right == NULL)
				return;
			if(left->kind == TEMP_OP || left->kind == VARIABLE_OP) {
				switch(right->kind) {
					case CONSTANT_OP_FLOAT:
					case CONSTANT_OP_INT: {
						r1 = get_vreg(left, fp);
						fprintf(fp, "li %s, %d\n", reg[r1].name, right->u.value);
					} break;
					case VARIABLE_OP:
					case TEMP_OP: {
						r1 = get_vreg(left, fp);
						r2 = get_vreg(right, fp);
						fprintf(fp, "move %s, %s\n", reg[r1].name, reg[r2].name);
						// printf("HERE?\n");
					} break;
					case VADDRESS_OP:
					case TADDRESS_OP: {
						r1 = get_vreg(left, fp);
						r2 = get_vreg(right->u.str, fp);
						fprintf(fp, "lw %s, 0(%s)\n", reg[r1].name, reg[r2].name);
					} break;
					default: break;
				}				
			}
			else if(left->kind == VADDRESS_OP || left->kind == TADDRESS_OP) {
				switch(right->kind) {
					case VARIABLE_OP:
					case TEMP_OP: {
						r1 = get_vreg(left->u.str, fp);
						r2 = get_vreg(right, fp);
						fprintf(fp, "sw %s, 0(%s)\n", reg[r2].name, reg[r1].name);
					} break;
					case CONSTANT_OP_FLOAT:
					case CONSTANT_OP_INT: {
						r1 = get_vreg(left->u.str, fp);
						Operand tempop = assOpCode(right, fp); 
						r2 = get_vreg(tempop, fp);
						fprintf(fp, "sw %s, 0(%s)\n", reg[r2].name, reg[r1].name);						
					}
					default: break;
				}
			}
		} break;
		case ADD_IC:
		case SUB_IC:
		case MUL_IC:
		case DIV_IC: {
			result = ic->u.three.result;
			op1 = ic->u.three.op1;
			op2 = ic->u.three.op2;
			switch(result->kind) {
				case TEMP_OP:
				case VARIABLE_OP: {
					if((op1->kind == TEMP_OP || op1->kind == VARIABLE_OP)&& (op2->kind == TEMP_OP || op2->kind == VARIABLE_OP)) {
						r1 = get_vreg(result, fp);
						r2 = get_vreg(op1, fp);
						r3 = get_vreg(op2, fp);
						char *sign = (char*)malloc(16);
						if(ckind == ADD_IC) {
							sprintf(sign, "add");
						}
						else if(ckind == SUB_IC) {
							sprintf(sign, "sub");
						}
						else if(ckind == MUL_IC) {
							sprintf(sign, "mul");
						}
						else if(ckind == DIV_IC) {
							sprintf(sign, "div");
						}
						// printf("%s", sign);
						fprintf(fp, "%s %s, %s, %s\n", sign, reg[r1].name, reg[r2].name, reg[r3].name);
					}
					else if((op1->kind == TEMP_OP || op1->kind == VARIABLE_OP) && op2->kind == CONSTANT_OP_INT) {
						if(ckind == ADD_IC) {
							r1 = get_vreg(result, fp);
							r2 = get_vreg(op1, fp);
							fprintf(fp, "addi %s, %s, %d\n", reg[r1].name, reg[r2].name, op2->u.value);
						}
						else if(ckind == SUB_IC) {
							r1 = get_vreg(result, fp);
							r2 = get_vreg(op1, fp);
							fprintf(fp, "addi %s, %s, -%d\n", reg[r1].name, reg[r2].name, op2->u.value);
						}
						else if(ckind == MUL_IC) {						
							r1 = get_vreg(result, fp);
							r2 = get_vreg(op1, fp);
							Operand optemp = assOpCode(op2, fp);
							r3 = get_vreg(optemp, fp);
							fprintf(fp, "mul %s, %s, %s\n", reg[r1].name, reg[r2].name, reg[r3].name);
						}
						else if(ckind == DIV_IC) {
							r1 = get_vreg(result, fp);
							r2 = get_vreg(op1, fp);
							Operand optemp = assOpCode(op2, fp);
							r3 = get_vreg(optemp, fp);
							fprintf(fp, "div %s, %s\n", reg[r2].name, reg[r3].name);
							fprintf(fp, "mflo %s\n", reg[r1].name);
						}
					}
					else if((op1->kind == VADDRESS_OP || op1->kind == TADDRESS_OP) && (op2->kind == VARIABLE_OP || op2->kind == TEMP_OP)) {
						Operand optemp = assOpCode(op1, fp);
						InterCode ictemp = (InterCode)malloc(sizeof(struct InterCode_));
						ictemp->kind = ckind;
						ictemp->u.three.result = result;
						ictemp->u.three.op1 = optemp;
						ictemp->u.three.op2 = op2;
						intercodeDeal(ictemp, fp);
					}
					else if((op2->kind == VADDRESS_OP || op2->kind == TADDRESS_OP) && (op1->kind == VARIABLE_OP || op1->kind == TEMP_OP)) {
						Operand optemp = assOpCode(op2, fp);
						InterCode ictemp = (InterCode)malloc(sizeof(struct InterCode_));
						ictemp->kind = ckind;
						ictemp->u.three.result = result;
						ictemp->u.three.op1 = op1;
						ictemp->u.three.op2 = optemp;
						intercodeDeal(ictemp, fp);
					}
					else if((op1->kind != TEMP_OP && op1->kind != VARIABLE_OP) && (op2->kind != TEMP_OP && op2->kind != VARIABLE_OP)) {
						Operand opt1 = assOpCode(op1, fp);
						Operand opt2 = assOpCode(op2, fp);
						InterCode ictemp = (InterCode)malloc(sizeof(struct InterCode_));
						ictemp->kind = ckind;
						ictemp->u.three.result = result;
						ictemp->u.three.op1 = opt1;
						ictemp->u.three.op2 = opt2;
						intercodeDeal(ictemp, fp);
					}
				} break;
				case TADDRESS_OP:
				case VADDRESS_OP: {
					left = (Operand) malloc(sizeof(struct Operand_));
					left->kind = TEMP_OP;
					left->u.var_no = newtemp();
					icode = (InterCode)malloc(sizeof(struct InterCode_));
					icode->kind = ckind;
					icode->u.three.result = left;
					icode->u.three.op1 = op1;
					icode->u.three.op2 = op2;
					intercodeDeal(icode, fp);
					
					icode->kind = ASSIGN_IC;
					icode->u.two.left = result;
					icode->u.two.right = left;
					intercodeDeal(icode, fp);					
				} break;
				default: break;
			}
		} break;
		case GOTO_IC: {
			vhead = funlist;
			while(vhead!=NULL) {
				if(vhead->reg >= 0) {
					fprintf(fp, "subu $v1, $fp, %d\n", vhead->offset);
					fprintf(fp, "sw %s, 0($v1)\n", reg[vhead->reg].name);
					reg[vhead->reg].var = NULL;
					reg[vhead->reg].usedno = 0;
					vhead->reg = -1;
				}
				vhead = vhead->next;
			}
			fprintf(fp, "j ");
			print_operand(fp, ic->u.one.op);
			fprintf(fp, "\n");
		} break;
		case RELOP_IC: {
			op1 = ic->u.if_relop.op1;
			op2 = ic->u.if_relop.op2;
			label = ic->u.if_relop.label_true;
			char *ops = ic->u.if_relop.relop_sign;
			if(op1->kind != TEMP_OP && op1->kind != VARIABLE_OP) {
				op1= assOpCode(op1, fp);
			}
			if(op2->kind != TEMP_OP && op2->kind != VARIABLE_OP) {
				op2= assOpCode(op2, fp);
			}
			r1 = get_vreg(op1, fp);
			r2 = get_vreg(op2, fp);	
			// 处理变量表		
			vhead = funlist;
			while(vhead != NULL) {
				if(vhead->reg >= 0) {
					fprintf(fp, "subu $v1, $fp, %d\n", vhead->offset);
					fprintf(fp, "sw %s, 0($v1)\n", reg[vhead->reg].name);
					reg[vhead->reg].var = NULL;
					reg[vhead->reg].usedno = 0;
					vhead->reg = -1;
				}
				vhead = vhead->next;
			}
			char *ss = (char*) malloc(16);
			memset(ss, 0, 16);
			if(strcmp(ops, "==")  == 0) {
				strncpy(ss, "beq", 3);
			}
			else if(strcmp(ops, "!=") == 0) {
				strncpy(ss, "bne", 3);
			}
			else if(strcmp(ops, ">") == 0) {
				strncpy(ss, "bgt", 3);
			}
			else if(strcmp(ops, ">=") == 0) {
				strncpy(ss, "bge", 3);
			}
			else if(strcmp(ops, "<") == 0) {
				strncpy(ss, "blt", 3);
			}
			else if(strcmp(ops, "<=") == 0) {
				strncpy(ss, "ble", 3);
			}
			fprintf(fp, "%s %s, %s, ", ss, reg[r1].name, reg[r2].name);
			print_operand(fp, label);
			fprintf(fp, "\n");
		} break;
		case ARG_IC: {
			args_count++;
			if(ic->u.one.op->kind != TEMP_OP && ic->u.one.op->kind != VARIABLE_OP) {
				op1 = assOpCode(op1, fp);
				ic->u.one.op = op1;
			}
			else {
				//分配空间
				get_vreg(ic->u.one.op, fp);
			}
		} break;
		case READ_IC: {
			fprintf(fp, "jal read\n");
			r1 = get_vreg(ic->u.one.op, fp);
			// 存储输入数据
			fprintf(fp, "move %s, $v0\n", reg[r1].name);
		} break;
		case WRITE_IC: {
			if(ic->u.one.op->kind != TEMP_OP && ic->u.one.op->kind != VARIABLE_OP) {
				op1 = assOpCode(op1, fp);
				r1 = get_vreg(op1, fp);
			}
			else {
				r1 = get_vreg(ic->u.one.op, fp);
			}
			fprintf(fp, "move $a0, %s\n", reg[r1].name);
			fprintf(fp, "jal write\n");
		} break;
		case CALL_IC: {
			vhead = funlist;
			while(vhead != NULL) {
				if(vhead->reg >= 0) {
					//将所有的当前寄存器的值压栈
					fprintf(fp, "subu $v1, $fp, %d\n", vhead->offset);
					fprintf(fp, "sw, %s, 0($v1)\n", reg[vhead->reg].name);
				}
				vhead = vhead->next;
			}
			icode = ic;
			int t = 0;
			// 参数个数大于4不予考虑
			for(t = 0; t<4 && t<args_count; t++) {
				icode = icode->prev;
				if(icode->kind != ARG_IC)
					continue;
				if(icode->u.one.op->kind != VARIABLE_OP && icode->u.one.op->kind != TEMP_OP) {
					perror("Error args, not accept!\n");
				}
				else {
					r1 = get_vreg(icode->u.one.op, fp);
				}
				fprintf(fp, "move $a%d, %s\n", t, reg[r1].name);
			}
			vhead = funlist;
			while(vhead != NULL) {
				int reg_index = vhead->reg;
				if(reg_index >= 0) {
					reg[reg_index].usedno = 0;
					reg[reg_index].var = NULL;
					vhead->reg = -1;					
				}
				vhead = vhead->next;
			}
			// 处理完毕，清零
			args_count = 0;
			// 开始进行链接
			fprintf(fp, "jal ");
			print_operand(fp, ic->u.two.right);
			fprintf(fp, "\n");
			// fprintf(fp, "addi $sp, $sp, %d\n", t*4);
			if(ic->u.two.left != NULL) {
				r1 = get_vreg(ic->u.two.left, fp);
				fprintf(fp, "move %s, $v0\n", reg[r1].name);
			}
		} break;
		case RETURN_IC: {
			if(ic->u.one.op->kind != VARIABLE_OP && ic->u.one.op->kind 
			!= TEMP_OP) {
				op1 = assOpCode(ic->u.one.op, fp);
				r1 = get_vreg(op1, fp);
			}
			else {
				r1 = get_vreg(ic->u.one.op, fp);
			}
			//从模拟器中得到
			fprintf(fp,"subu $sp, $fp, 8\n");
			fprintf(fp, "lw $fp, 0($sp)\n");
			fprintf(fp, "addi $sp, $sp, 4\n");
			fprintf(fp, "lw $ra, 0($sp)\n");
			fprintf(fp, "addi $sp, $sp, 4\n");
			fprintf(fp, "move $v0, %s\n", reg[r1].name);
			fprintf(fp, "jr $ra\n");
		} break;
		case PARAM_IC: break;
		case FUNCTION_IC: {
			fprintf(fp, "\n");
			print_operand(fp, ic->u.one.op);
			fprintf(fp, ":\n");
			del_varlist();
			reset_reg();
			current_offset = 8;
			fprintf(fp, "subu $sp, $sp, 4\n");
			fprintf(fp, "sw $ra, 0($sp)\n");
			fprintf(fp, "subu $sp, $sp, 4\n");
			fprintf(fp, "sw, $fp, 0($sp)\n");
			fprintf(fp, "addi $fp, $sp, 8\n");
			memCount = varcount_alloc(ic);
			fprintf(fp, "subu $sp, $sp, %d\n", memCount*4);
			icode = ic->next;
			// if(icode == NULL || icode == head) return;
			int t = 0;
			while(icode->kind == PARAM_IC && t < 4) {
				t++;
				r1 = get_vreg(icode->u.one.op, fp);
				fprintf(fp, "\nmove %s, $a%d\n", reg[r1].name, t);
				icode = icode->next;
			}
		} break;
		case DEC_IC: break;
		case ADDR_IC: {
			icode = ic->prev;
			r1 = get_vreg(ic->u.two.left, fp);
			if(icode->kind != DEC_IC)
				perror("ERROR with missing DEC\n");
			fprintf(fp, "subu $sp, $sp, %d\n", icode->u.dec_code.size);
			current_offset += icode->u.dec_code.size;
			fprintf(fp, "move %s, $sp\n", reg[r1].name);
		} break;
		default: {} break;
	}
}

// 对于立即数的情况需要则要增加一条中间代码、目标代码
Operand assOpCode(Operand o, FILE *fp) {
	Operand op=(Operand) malloc(sizeof(struct Operand_));
	op->kind = TEMP_OP;
	op->u.var_no = newtemp();
	InterCode ictemp = (InterCode) malloc(sizeof(struct InterCode_));
	ictemp->kind = ASSIGN_IC;
	ictemp->u.two.left = op;
	ictemp->u.two.right = o;
	intercodeDeal(ictemp, fp);
	return op;
}

void objectCode(FILE *fp) {
	init_reg();
	init_read_write(fp);
	InterCode cur = head;
	intercodeDeal(cur, fp);
	cur = cur->next;
	while(cur != NULL && cur!= head) {
		intercodeDeal(cur, fp);
		cur = cur->next;
		fflush(fp);
	}
}
