lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@header  {
   import java.util.Set;
   import java.util.HashSet;
}

@members {
}

// Ignore spaces, tabs, newlines, whitespaces and comments
WS:   ( ' '
         | '//' .*? '\n'
         | '//' .*? EOF
         | '/*' .*? '*/'
         | '\t'
         | '\r'
         | '\n'
      )
      {
         skip(); // avoid producing a token 
      };

// Special symbols
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

// Reserved words
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

// Numbers
fragment POSITIVE_DIGIT: '1' .. '9';
fragment DIGIT :  ('0' | POSITIVE_DIGIT);
fragment LETTER : ('a' .. 'z'|'A' .. 'Z');

fragment NUM:  DIGIT+;
fragment SIGN: ('+'|'-')?;
fragment EXP:  ('E' | 'e') SIGN NUM;
fragment DEC:  NUM '.' NUM;
fragment FLOATDEC:   (DEC | DEC EXP) ('F' | 'f')?;

fragment DIGITHEX:   ('0'.. '9'|'A' .. 'F'|'a' .. 'f');
fragment NUMHEX:     DIGITHEX+;
fragment FLOATHEX:   ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN? NUM ('F' | 'f')?;

INT: ('0' | POSITIVE_DIGIT DIGIT*);
FLOAT: (FLOATDEC | FLOATHEX);

// Identifier
IDENT: (LETTER | '$' | '_')(LETTER | DIGIT | '$' | '_')*;

// Include
fragment FILENAME: (LETTER | DIGIT | '.' | '-' | '_')+;

INCLUDE: ('#include' (' ')* '"' FILENAME '"')
         {
            String s = getText();
            int startIndex = s.indexOf('"');
            int endIndex = s.length();
            String file = s.substring(startIndex, endIndex);
            doInclude(file);
         };



// String
fragment STRING_CAR: ~('"' | '\\' | '\n');

STRING: '"' (STRING_CAR | '\\"' | '\\\\')*  '"';
MULTI_LINE_STRING: '"' (STRING_CAR | EOL | '\\"' | '\\\\')*  '"';

//DUMMY_TOKEN: .;