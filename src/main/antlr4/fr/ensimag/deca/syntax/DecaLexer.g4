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

// Ignore spaces, tabs, newlines and whitespaces
WS:   ( ' '
         | '//' .*? '\n'
         | '//' .*? EOF
         | '/*' .*? '*/'
         | '\t'
         | '\r'
         | '\n'
      ) { skip(); // avoid producing a token 
      };

// Mots réservés

ASM: 'asm';
CLASS: 'class';
EXTENDS: 'extends';
ELSE: 'else';
FALSE: 'false';
IF: 'if';
INSTANCEOF: 'instanceof';
NEW: 'new';
NULL: 'null';
READINT: 'readInt';
READFLOAT: 'readFloat';
PRINT: 'print';
PRINTLN: 'println';
PRINTLNX: 'printlnx';
PRINTX: 'printx';
PROTECTED: 'protected';
RETURN: 'return';
THIS: 'this';
TRUE: 'true';
WHILE: 'while';

// Symboles spéciaux

GT: '>';
LT: '<';
EQUALS: '=';
PLUS: '+';
MINUS: '-';
TIMES: '*';
SLASH: '/';
PERCENT: '%';
DOT: '.';
COMMA: ','; 
OPARENT: '(';
CPARENT: ')';
OBRACE: '{';
CBRACE: '}';
EXCLAM: '!';
SEMI: ';';
EQEQ: '=='; 
NEQ: '!=';
GEQ: '>=';
LEQ: '<=';
AND: '&&';
OR: '||';

EOL: '\n';

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

INT: DIGIT+;
FLOAT : FLOATDEC | FLOATHEX;

IDENT: (LETTER | '$' | '_')(LETTER | DIGIT | '$' | '_')*;

fragment FILENAME: (LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE: ('#include' (' ')* '"' FILENAME '"')
         {  String s = getText();
            int startIndex = s.indexOf('"')-1;
            int endIndex = s.length();
            String file = s.substring(startIndex + 1, endIndex);
            doInclude(file);
         };

fragment STRING_CAR: ~('"' | '\\' | '\n') ;
STRING: '"' (STRING_CAR | '\\"' | '\\\\')*  '"';
MULTI_LINE_STRING: '"' (STRING_CAR | EOL | '\\"' | '\\\\')*  '"';

//DUMMY_TOKEN: .;