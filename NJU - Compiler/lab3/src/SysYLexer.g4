lexer grammar SysYLexer;

CONST : 'const';
INT : 'int' ;
VOID : 'void' ;
WHILE : 'while' ;
BREAK : 'break' ;
RETURN : 'return' ;
IF : 'if' ;
ELSE : 'else' ; 
CONTINUE : 'continue' ;

PLUS : '+' ;
MINUS : '-' ;
MUL : '*' ;
DIV : '/' ; 
MOD : '%' ;
ASSIGN : '=' ;
EQ : '==' ;
NEQ : '!=' ;
LT : '<' ; 
GT : '>' ;
LE : '<=' ;
GE : '>=' ;
NOT : '!' ; 
BIT_AND : '&' ;
BIT_OR : '|' ;
LOGICAL_AND : '&&' ;
LOGICAL_OR : '||';
COMMA : ',' ; 
SEMICOLON : ';' ; 

INTEGER_CONST : Decimal
         | Binary
         | Octal
         | Hexadecimal
         ; 

IDENT : '_'+ WORD*
   | (LETTER)+WORD*;

Binary : '0' ('b' | 'B') '1'[1-2]* ;
// Quaternary : [1-3][0-3]* ; 
Octal : '0'[0-7]+ ;
Decimal : [1-9]NUMBER* | '0'; 
Hexadecimal : '0'('x' | 'X')[0-9a-fA-F]+ ; 
 
L_PAREN : '(' ;
R_PAREN : ')' ;
L_BRACE : '{' ;
R_BRACE : '}' ;
L_BRACKT : '[' ;
R_BRACKT : ']' ; 

fragment LETTER : [a-zA-Z] ;
fragment NUMBER : [0-9] ;
fragment WORD : '_' | LETTER | NUMBER ;

WS : [ \t\n\r]+ -> channel(HIDDEN);

DOCS_COMMENT: '/**' .*? '*/' -> skip ;
SL_COMMENT : '//' .*? ('\n' | EOF) -> skip ;
ML_COMMENT : '/*' .*? '*/' -> skip ; 