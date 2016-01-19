# 编译器设定和编译选项
CC = gcc
CFLAGS = -Wall -O2 -g -lpthread -I./include

GITFLAGS = -q --author='qfy <qiufengyu1024@gmail.com>' --no-verify --allow-empty

# 编译目标：.c文件
CFILES = $(shell find -name "*.c")
objects = $(CFILES:.c=.o)

server: $(objects)
	cc -o server $(objects) -lpthread
	-@git add . --ignore-errors &> /dev/null # KEEP IT
	-@(echo "> compile" && uname -a && uptime && pstree -A) | git commit -F 	- $(GITFLAGS) # KEEP IT

.PHONY: clean
clean:
	-rm server $(objects)

#the git option rewrite from the Operating System Labs in 2014 Spring.
