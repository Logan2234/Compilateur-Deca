lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@members {
}

// Deca lexer rules.

OBRACE: '{';
CBRACE: '}';
   
IF: 'if';
ELSE: 'else';
WHILE: 'while';
OR: '||';
AND: '&&';
TRUE: 'true';
FALSE: 'false';
PRINT: 'print';
PRINTLN: 'println';
PRINTX: 'printx';
PRINTLNX: 'printlnx';
SEMI: ';';
EOL: '\n';
COMMA: ','; 
EQUALS: '=';
OPARENT: '(';
CPARENT: ')';
COMMENT: ('//' .*? '\n' 
         |'/*' .*? '*/'
         ) {skip();};
RETURN: 'return';
EQEQ: '=='; 
NEQ: '!=';
LEQ: '<=';
GEQ: '>=';
GT: '>';
LT: '<';
INSTANCEOF: 'instanceof'; 
INT: DIGIT+;

fragment POSITIVE_DIGIT: '1' .. '9';
fragment DIGIT : ('0'
                  |POSITIVE_DIGIT);
fragment LETTER : ('a' .. 'z'
                  |'A' .. 'Z'
                  );
fragment SIGN: ('+'
               |'-'
               )?;
fragment EXP: ('E' | 'e') SIGN INT;
fragment DEC: INT '.' INT;
fragment FLOATDEC: (DEC | DEC EXP) ('F' | 'f')?;
fragment DIGITHEX : ('0'.. '9'
                     |'A' .. 'F'
                     |'a' .. 'f'
                     );
fragment NUMHEX : DIGITHEX+;
fragment FLOATHEX : ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN INT ('F' | 'f')?;
FLOAT : FLOATDEC | FLOATHEX;

IDENT: (LETTER | '$' | '_')(LETTER | DIGIT | '$' | '_')*;

PLUS: '+';
MINUS: '-';
TIMES: '*';
SLASH: '/';
PERCENT: '%';
EXCLAM: '!';
DOT: '.';
READINT: 'readInt';
READFLOAT: 'readFloat';
NEW: 'new';
THIS: 'this';
NULL: 'null';
CLASS: 'class';
EXTENDS: 'extends';
PROTECTED: 'protected';
ASM: 'asm';
SPACE: ' ';
TAB: '\t';
RESET: '\r';

fragment FILENAME: (LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE: '#include' (' ')* '"' FILENAME '"';

fragment STRING_CAR: ~('"' | '\\' | '\n') ;
STRING: '"' (STRING_CAR | '\\"' | '\\\\')*  '"';
MULTI_LINE_STRING: '"' (STRING_CAR | EOL | '\\"' | '\\\\')*  '"';
DUMMY_TOKEN: .;