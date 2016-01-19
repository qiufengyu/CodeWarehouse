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
subu $sp, $sp, 156
li $t0, 2
addi $t1, $t0, 1
li $t3, 4
mul $t2, $t1, $t3
li $t5, 5
mul $t4, $t2, $t5
addi $t6, $t4, -9
addi $t7, $t4, 3
li $t9, 3
div $t7, $t9
mflo $t8
li $s1, 7
mul $s0, $t8, $s1
move $a0, $t4
jal write
move $a0, $t6
jal write
move $a0, $t8
jal write
move $a0, $s0
jal write
subu $sp, $fp, 8
lw $fp, 0($sp)
addi $sp, $sp, 4
lw $ra, 0($sp)
addi $sp, $sp, 4
move $v0, $t1
jr $ra
