lexer grammar SysYLexer;

@header {
    package sysylexer;
}

// prog : stat* EOF;

// stat : block
//      | varDecl
//      | funcDecl
//      | 'if' '(' expr ')' stat ('else' block)? stat
//      | RETURN expr? ';'
//      | expr ';'
//      ;

// block : '{' expr* '}' ;

// expr : ID '(' exprList? ')'
//      | expr '[' expr ']'
//      | '-' expr
//      | '!' expr
//      | ls = expr (op = '*' | op = '/') rs = expr
//      | ls = expr (op = '+' | op = '-') rs = expr
//      | ls = expr (op = '==' | op = '!=') rs = expr
//      | '(' expr ')'
//      | ID
//      | ConstInt
//      ;

// exprList : expr (',' expr)* ; 

// funcDecl : basicType ID '(' parameters? ')' block ;

// parameter : basicType ID ;
// parameters : parameter (',' parameter)* ; 

// varDecl : basicType ID ('=' expr)? ';' ; 

// basicType : 'int' ;

ID : '_'+ WORD*
   | (LETTER)+WORD*;

ConstInt : Decimal
         | '0' ('b' | 'B') Binary
         | '0' Octal
         | '0' ('x' | 'X') Hexadecimal
         | '0'
         ; 

Binary : '1'[1-2]* ;
// Quaternary : [1-3][0-3]* ; 
Octal : [1 - 7][0 - 7]* ;
Decimal : [1-9]NUMBER* ; 
Hexadecimal : [1-9a-fA-F][0-9a-fA-F]* ; 

INT : 'int' ;
RETURN : 'return' ; 
L_PAPEN : '(' ;
R_PAPEN : ')' ;
L_BRACE : '{' ;
R_BRACE : '}' ;

fragment LETTER : [a-zA-Z] ;
fragment NUMBER : [0-9] ;
fragment WORD : '_' | LETTER | NUMBER ;