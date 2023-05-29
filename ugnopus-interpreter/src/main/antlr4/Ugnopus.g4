grammar Ugnopus;

program : statement+ EOF ;

statement
    : variableDeclaration ';'
    | assignment ';'
    | ifStatement
    | whileLoop
    | forLoop
    | function
    | returnStatment ';'
    | printStatement ';'
    | useFunction ';'
    | fileOutputStatement ';'
    ;

variableDeclaration : TYPE ID ('=' expression)? ;

assignment : ID '=' expression ;

useFunction : functionResult;

expression
    : INT                               #intExpression
    | ID                                #idExpression
    | BOOL                              #boolExpression
    | STRING                            #stringExpression
    | CHAR                              #charExpression
    | DOUBLE                            #doubleExpression
    | '(' expression ')'                #parenthesesExpression
    | expression multiOp expression     #multiOpExpression
    | expression addOp expression       #addOpExpression
    | expression relationOp expression  #relationOpExpression
    | functionResult                    #functionResultExpression
    | fileInputStatement                #fileInputExpression
    | readConsoleStatment               #readConsoleExpression
    | '(' TYPE ')' expression           #castingExpression
    ;

multiOp : '*' | '/' | '%' ;
addOp : '+' | '-' ;

ifStatement : 'jeigu' '(' expression ')' block
    ( 'darJeigu' '(' expression ')' block )?
    ('neJeigu' block )? ;


forLoop : 'sukisTik' '('  (variableDeclaration | assignment )  ';' assignment ';' expression ')'
    block;

whileLoop : 'sukisKol' '(' expression ')'
    block;


function : 'funkcijus' '(' ((ID ':' TYPE ',')* ID ':' TYPE)? ')' ID ':' TYPE
     functionBlock;

functionResult : ID '(' ((ID ',')* ID)? ')' ;

returnStatment: RETURN expression ;

block: '{' statement+ '}';
functionBlock: '{' statement+ '}';

relationOp : '==' | '!=' | '<' | '>' | '<=' | '>=' ;

printStatement : PRINT '(' expression ')' ;

readConsoleStatment : READCONSOLE;

fileOutputStatement : FILEOUTPUT '(' expression ',' ID ')' ;

fileInputStatement : READFILE '(' expression ')';

TYPE    : 'Intas'
        | 'Bulynas'
        | 'Stringas'
        | 'Charas'
        | 'Doublas'
        ;

PRINT       : 'print';
READCONSOLE : 'readConsole()';
FILEOUTPUT  : 'out';
READFILE    : 'readFile';
RETURN      : 'return';
BOOL        : 'tru'|'fols' ;
ID          : [a-zA-Z]+ ;
INT         : [-]?[0-9]+ ;
STRING      : '"' (~["\\\r\n] | EscapeSequence)* '"';
CHAR        : '\'' (~['\\\r\n] | EscapeSequence) '\'';
DOUBLE      : [-]?[0-9]+.[0-9]+ ;


COMMENT : ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip ;
WS      : [ \t\r\n]+ -> skip ;


fragment EscapeSequence
    : '\\' 'u005c'? [btnfr"'\\]
    | '\\' 'u005c'? ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;