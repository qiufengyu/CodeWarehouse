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

main:
subu $sp, $sp, 4
sw $ra, 0($sp)
subu $sp, $sp, 4
sw, $fp, 0($sp)
addi $fp, $sp, 8
subu $sp, $sp, 216
li $t0, 0
li $t1, 1
li $t2, 0
jal read
move $t3, $v0
move $t4, $t3
subu $v1, $fp, 12
sw $t0, 0($v1)
subu $v1, $fp, 16
sw $t1, 0($v1)
subu $v1, $fp, 20
sw $t2, 0($v1)
subu $v1, $fp, 24
sw $t3, 0($v1)
subu $v1, $fp, 28
sw $t4, 0($v1)
label1:
subu $v1, $fp, 20
lw $t0, 0($v1)
subu $v1, $fp, 28
lw $t1, 0($v1)
subu $v1, $fp, 20
sw $t0, 0($v1)
subu $v1, $fp, 28
sw $t1, 0($v1)
blt $t0, $t1, label2
j label3
label2:
subu $v1, $fp, 12
lw $t1, 0($v1)
subu $v1, $fp, 16
lw $t2, 0($v1)
add $t0, $t1, $t2
move $a0, $t2
jal write
move $t1, $t2
move $t2, $t0
subu $v1, $fp, 20
lw $t4, 0($v1)
addi $t3, $t4, 1
move $t4, $t3
subu $v1, $fp, 12
sw $t1, 0($v1)
subu $v1, $fp, 16
sw $t2, 0($v1)
subu $v1, $fp, 20
sw $t4, 0($v1)
subu $v1, $fp, 32
sw $t0, 0($v1)
subu $v1, $fp, 36
sw $t3, 0($v1)
j label1
label3:
li $t0, 0
subu $sp, $fp, 8
lw $fp, 0($sp)
addi $sp, $sp, 4
lw $ra, 0($sp)
addi $sp, $sp, 4
move $v0, $t0
jr $ra
