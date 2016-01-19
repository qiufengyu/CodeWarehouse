all:
	bison -d -v syntax.y
	flex lexical.l
	gcc -g -O2 syntax.tab.c tree.c semantic.c intercode.c generate.c -lfl -o parser

clean:
	rm lex.yy.c syntax.tab.c syntax.tab.h parser
