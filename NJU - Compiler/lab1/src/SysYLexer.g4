lexer grammar SysYLexer;

// prog : stat* EOF;

// stat : block
//      | varDecl
//      | funcDecl
//      | 'if' '(' expr ')' stat ('else' block)? stat
//      | RETURN expr? ';'
//      | expr ';'
//      ;

// block : '{' expr* '}' ;

// expr : IDENT '(' exprList? ')'
//      | expr '[' expr ']'
//      | '-' expr
//      | '!' expr
//      | ls = expr (op = '*' | op = '/') rs = expr
//      | ls = expr (op = '+' | op = '-') rs = expr
//      | ls = expr (op = '==' | op = '!=') rs = expr
//      | '(' expr ')'
//      | IDENT
//      | INTEGER_CONST
//      ;

// exprList : expr (',' expr)* ; 

// funcDecl : basicType IDENT '(' parameters? ')' block ;

// parameter : basicType IDENT ;
// parameters : parameter (',' parameter)* ; 

// varDecl : basicType IDENT ('=' expr)? ';' ; 

// basicType : 'int' ;

INT : 'int' ;
RETURN : 'return' ;
IF : 'if' ;
ELSE : 'else' ; 

IDENT : '_'+ WORD*
   | (LETTER)+WORD*;

// INTEGER_CONST : Decimal
//          | '0' ('b' | 'B') Binary
//          | '0' Octal
//          | '0' ('x' | 'X') Hexadecimal
//          | '0'
//          ; 

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
SEMICOLON : ';' ; 
COMMA : ',' ; 

PLUS : '+' ;
MINUS : '-' ;
ASSIGN : '=' ;
NOT : '!' ; 



fragment LETTER : [a-zA-Z] ;
fragment NUMBER : [0-9] ;
fragment WORD : '_' | LETTER | NUMBER ;

WS : [ \t\n\r]+ -> skip;

DOCS_COMMENT: '/**' .*? '*/' -> skip ;
SL_COMMENT : '//' .*? ('\n' | EOF) -> skip ;
ML_COMMENT : '/*' .*? '*/' -> skip ; 