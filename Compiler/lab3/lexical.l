%{
#include "syntax.tab.h"
#include "tree.h"
int errors = 0;

int yycolumn = 1;
#define YY_USER_ACTION \
yylloc.first_line = yylloc.last_line = yylineno; \
yylloc.first_column = yycolumn; \
yylloc.last_column = yycolumn+yyleng-1; \
yycolumn += yyleng;
%}

%option yylineno

digit0 [0-9]
digit1 [1-9]
hexdigit [a-fA-F0-9]
letter [a-zA-Z]
letter_ [_a-zA-Z]
delim [" "\t]

%%
struct { yylval.node = init_node("STRUCT", Token, yylineno, ""); return STRUCT; }
return { yylval.node = init_node("RETURN", Token, yylineno, ""); return RETURN; }
if { yylval.node = init_node("IF", Token, yylineno, ""); return IF; }
else { yylval.node = init_node("ELSE", Token, yylineno, ""); return ELSE; }
while { yylval.node = init_node("WHILE", Token, yylineno, ""); return WHILE; }
(int)|(float) { yylval.node = init_node("TYPE", Token, yylineno, yytext); return TYPE; }


0|({digit1}{digit0}*) {  yylval.node = init_node("INT", Token, yylineno, yytext); return INT; } /* <32 bits , by taokun!*/

0(([1-3][0-7]{0,10})|[4-7][0-7]{0,9}) { yylval.node = init_node("INT", Token, yylineno, yytext); return INT; } /* <32 bits */

0[xX]{hexdigit}{hexdigit}{0,7} { yylval.node = init_node("INT", Token, yylineno, yytext); return INT; } /* <32 bits*/

(0|({digit1}{digit0}*))((\.{digit0}+)) { yylval.node = init_node("FLOAT", Token, yylineno, yytext); return FLOAT; }

({digit0}+)(\.{digit0}+)?[Ee][+-]?{digit0}+ { yylval.node = init_node("FLOAT", Token, yylineno, yytext); return FLOAT; }

[_a-zA-Z]([_0-9a-zA-Z]*) { yylval.node = init_node("ID", Token, yylineno, yytext);  return ID; }

";" { yylval.node = init_node("SEMI", Token, yylineno, ""); return SEMI; }
"," { yylval.node = init_node("COMMA", Token, yylineno, ""); return COMMA; }
"=" { yylval.node = init_node("ASSIGNOP", Token, yylineno, ""); return ASSIGNOP; }
>|<|>=|<=|==|!= { yylval.node = init_node("RELOP", Token, yylineno, yytext); return RELOP; }
"+" { yylval.node = init_node("PLUS", Token, yylineno, ""); return PLUS; }
"-" { yylval.node = init_node("MINUS", Token, yylineno, ""); return MINUS; }
"*" { yylval.node = init_node("STAR", Token, yylineno, ""); return STAR; }
"/" { yylval.node = init_node("DIV", Token, yylineno, ""); return DIV; }
"&&" { yylval.node = init_node("AND", Token, yylineno, ""); return AND; }
"||" { yylval.node = init_node("OR", Token, yylineno, ""); return OR; }
"." { yylval.node = init_node("DOT", Token, yylineno, ""); return DOT; }
"!" { yylval.node = init_node("NOT", Token, yylineno, ""); return NOT; }
"(" { yylval.node = init_node("LP", Token, yylineno, ""); return LP; }
")" { yylval.node = init_node("RP", Token, yylineno, ""); return RP; }
"[" { yylval.node = init_node("LB", Token, yylineno, ""); return LB; }
"]" { yylval.node = init_node("RB", Token, yylineno, ""); return RB; }
"{" { yylval.node = init_node("LC", Token, yylineno, ""); return LC; }
"}" { yylval.node = init_node("RC", Token, yylineno, ""); return RC; }
"//" { char c = input(); while(c != '\n' && c!= EOF) c = input(); /* printf("COMMENT1\n"); */}
"/*"([*]*(([^*/])+([/])*)*)*"*/" { /* get from http://bbs.csdn.net/topics/40224740 */ /*printf("COMMENT2\n"); */ }
"\n" { yycolumn = 1; }
{delim}+ {/* do nothing!*/}

.  { printf("Error type A at Line %d: Mysterious characters \'%s\'\n", 
yylineno, yytext); errors++; }

%%
