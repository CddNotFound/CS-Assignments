parser grammar SysYParser;

options {
   tokenVocab = SysYLexer;
}

prog : stat* EOF;

stat : block                                              # CodeBlock
     | varDecl                                            # VariableDecl
     | funcDecl                                           # FunctionDecl
     | IF L_PAREN cond R_PAREN stat (ELSE stat)?          # IfElse
     | RETURN exp? ';'                                    # Return
     | exp ';'                                            # Expd
     | WHILE L_PAREN cond R_PAREN stat                    # WhileLoop
     | lVal ASSIGN exp SEMICOLON                          # VarAssign
     | CONTINUE ';'                                       # Continue
     | BREAK ';'                                          # Break
     ;

block : L_BRACE stat* R_BRACE ;

exp
   : L_PAREN exp R_PAREN                                # PAREN 
   | lVal                                               # VarCall
   | number                                             # ConstInt
   | IDENT L_PAREN funcRParams? R_PAREN                 # FunctionCall
   | unaryOp exp                                        # UnaryOp1
   | exp (MUL | DIV | MOD) exp                          # MulOp
   | exp (PLUS | MINUS) exp                             # PlusOp
   | exp (BIT_AND | BIT_OR) exp                         # AndOp
   ;

cond 
   : exp                             # ExpToCond
   | cond (LT | GT | LE | GE) cond   # LessThan
   | cond (EQ | NEQ) cond            # Equal
   | cond LOGICAL_AND cond           # LogicalAnd
   | cond LOGICAL_OR cond            # LogicalOr
   ;

lVal
   : IDENT (L_BRACKT exp R_BRACKT)*
   ;

number
   : Decimal       # Decimal
   | Binary        # Binary
   | Octal         # Octal
   | Hexadecimal   # Hexadecimal
   ; 

unaryOp
   : PLUS
   | MINUS
   | NOT
   ;

funcRParams
   : param (COMMA param)*
   ;

param
   : exp
   ;

constExp
   : exp
   ;

// expr : IDENT L_PAREN exprList? R_PAREN
//      | expr L_BRACKT expr R_BRACKT
//      | '-' expr
//      | '!' expr
//      | ls = expr (op = MUL | op = DIV) rs = expr
//      | ls = expr (op = PLUS | op = MINUS) rs = expr
//      | ls = expr (op = EQ | op = NEQ) rs = expr
//      | L_PAREN expr R_PAREN
//      | IDENT
//      | INTEGER_CONST
//      ;

// cond : expr 
//      | cond (LT | GT | LE | GE) cond
//      | cond (EQ | NEQ) cond
//      | cond AND cond
//      | cond OR cond
//      ;

// exprList : exp (',' exp)* ; 

basicType : CONST? INT;
allType : CONST? INT | VOID;

funcDecl : (INT | VOID) IDENT L_PAREN parameters? R_PAREN block ;

parameter : INT IDENT (L_BRACKT R_BRACKT)   # ParameterArray
          | INT IDENT                       # ParameterInt
          ;

parameters : parameter (COMMA parameter)* ; 

varDecl : (CONST)? INT IDENT (ASSIGN exp)? SEMICOLON                              # IntDeclare
        | INT IDENT (L_BRACKT number R_BRACKT)+ (ASSIGN arrayAssign)? SEMICOLON   # ArrayDeclare
        ;

arrayAssign : arrayNumber
            | L_BRACE arrayNumber (COMMA arrayNumber)* R_BRACE ;
arrayNumber : L_BRACE exp (COMMA exp)* R_BRACE;