.data
_prompt: .asciiz "Enter an integer:"
_ret: .asciiz "\n"
.globl main
.text

read:
subu $sp, $sp, 4
sw $ra, 0($sp)
subu $sp, $sp, 4
sw $fp, 0($sp)
addi $fp, $sp, 8
li $v0, 4
la $a0, _prompt
syscall
li $v0, 5
syscall
subu $sp, $fp, 8
lw $fp, 0($sp)
addi $sp, $sp, 4
lw $ra, 0($sp)
addi $sp, $sp, 4
jr $ra

write:
subu $sp, $sp, 4
sw $ra, 0($sp)
subu $sp, $sp, 4
sw $fp, 0($sp)
addi $fp, $sp, 8
li $v0, 1
syscall
li $v0,4
la $a0, _ret
syscall
move $v0, $0
subu $sp, $fp, 8
lw $fp, 0($sp)
addi $sp, $sp, 4
lw $ra, 0($sp)
addi $sp, $sp, 4
jr $ra

fact:
subu $sp, $sp, 4
sw $ra, 0($sp)
subu $sp, $sp, 4
sw, $fp, 0($sp)
addi $fp, $sp, 8
subu $sp, $sp, 416

move $t0, $a1
li $t1, 1
subu $v1, $fp, 12
sw $t0, 0($v1)
subu $v1, $fp, 16
sw $t1, 0($v1)
beq $t0, $t1, label1
j label2
label1:
subu $v1, $fp, 12
lw $t0, 0($v1)
subu $sp, $fp, 8
lw $fp, 0($sp)
addi $sp, $sp, 4
lw $ra, 0($sp)
addi $sp, $sp, 4
move $v0, $t0
jr $ra
subu $v1, $fp, 12
sw $t0, 0($v1)
j label3
label2:
subu $v1, $fp, 12
lw $t1, 0($v1)
addi $t0, $t1, -1
subu $v1, $fp, 12
sw, $t1, 0($v1)
subu $v1, $fp, 20
sw, $t0, 0($v1)
move $a0, $t0
jal fact
move $t0, $v0
subu $v1, $fp, 12
lw $t2, 0($v1)
mul $t1, $t2, $t0
subu $sp, $fp, 8
lw $fp, 0($sp)
addi $sp, $sp, 4
lw $ra, 0($sp)
addi $sp, $sp, 4
move $v0, $t1
jr $ra
subu $v1, $fp, 12
sw $t2, 0($v1)
subu $v1, $fp, 24
sw $t0, 0($v1)
subu $v1, $fp, 28
sw $t1, 0($v1)
label3:

main:
subu $sp, $sp, 4
sw $ra, 0($sp)
subu $sp, $sp, 4
sw, $fp, 0($sp)
addi $fp, $sp, 8
subu $sp, $sp, 448
jal read
move $t0, $v0
move $t1, $t0
li $t2, 1
subu $v1, $fp, 12
sw $t0, 0($v1)
subu $v1, $fp, 16
sw $t1, 0($v1)
subu $v1, $fp, 20
sw $t2, 0($v1)
bgt $t1, $t2, label4
j label5
label4:
subu $v1, $fp, 16
lw $t0, 0($v1)
subu $v1, $fp, 16
sw, $t0, 0($v1)
move $a0, $t0
jal fact
move $t0, $v0
move $t1, $t0
subu $v1, $fp, 24
sw $t0, 0($v1)
subu $v1, $fp, 28
sw $t1, 0($v1)
j label6
label5:
subu $v1, $fp, 28
lw $t0, 0($v1)
li $t0, 1
subu $v1, $fp, 28
sw $t0, 0($v1)
label6:
subu $v1, $fp, 28
lw $t0, 0($v1)
move $a0, $t0
jal write
li $t1, 0
subu $sp, $fp, 8
lw $fp, 0($sp)
addi $sp, $sp, 4
lw $ra, 0($sp)
addi $sp, $sp, 4
move $v0, $t1
jr $ra
