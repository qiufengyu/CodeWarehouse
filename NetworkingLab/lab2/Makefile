# 编译器设定和编译选项
CC = gcc
CFLAGS = -Wall -O2 -g -I./include

# GITFLAGS = -q --author='Godfray <b121220074@114.212.191.43>' --no-verify --allow-empty

GITFLAGS = -q --author='Godfray <qiufengyu1024@hotmail.com>' --no-verify --allow-empty

# 编译目标：.c文件
CFILES = $(shell find -name "*.c")
objects = $(CFILES:.c=.o)

course: $(objects)
	cc -o course $(objects)
	#-rm $(objects)
	-@git add . --ignore-errors &> /dev/null # KEEP IT
	-@(echo "> compile" && uname -a && uptime && pstree -A) | git commit -F 	- $(GITFLAGS) # KEEP IT

.PHONY: clean
clean:
	-rm course $(objects)

#the git option rewrite from the Operating System Labs in 2014 Spring.
