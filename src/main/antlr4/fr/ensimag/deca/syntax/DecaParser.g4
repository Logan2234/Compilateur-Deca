parser grammar DecaParser;

options {
    // Default language but name it anyway
    //
    language  = Java;

    // Use a superclass to implement all helper
    // methods, instance variables and overrides
    // of ANTLR default methods, such as error
    // handling.
    //
    superClass = AbstractDecaParser;

    // Use the vocabulary generated by the accompanying
    // lexer. Maven knows how to work out the relationship
    // between the lexer and parser and will build the
    // lexer before the parser. It will also rebuild the
    // parser if the lexer changes.
    //
    tokenVocab = DecaLexer;

}

// which packages should be imported?
@header {
    import fr.ensimag.deca.tree.*;
    import java.io.PrintStream;
    import org.apache.log4j.Logger;
}

@members {
    private static final Logger LOG = Logger.getLogger(DecaParser.class);

    @Override
    protected AbstractProgram parseProgram() {
        return prog().tree;
    }
}

prog returns[AbstractProgram tree]
    : list_classes main EOF {
            assert($list_classes.tree != null);
            assert($main.tree != null);
            $tree = new Program($list_classes.tree, $main.tree);
            setLocation($tree, $list_classes.start);
        }
    ;

main returns[AbstractMain tree]
    : /* epsilon */ {
            $tree = new EmptyMain();
        }
    | block {
            assert($block.decls != null);
            assert($block.insts != null);
            $tree = new Main($block.decls, $block.insts);
            setLocation($tree, $block.start);
        }
    ;

block returns[ListDeclVar decls, ListInst insts]
    : OBRACE list_decl list_inst CBRACE {
            assert($list_decl.tree != null);
            assert($list_inst.tree != null);
            $decls = $list_decl.tree;
            $insts = $list_inst.tree;
        }
    ;

list_decl returns[ListDeclVar tree]
@init   {   $tree = new ListDeclVar();
        }
    : decl_var_set[$tree]*
    ;

decl_var_set[ListDeclVar l]
@init   {   assert($l != null);
        }
    : type list_decl_var[$l,$type.tree] SEMI
    ;

list_decl_var[ListDeclVar l, AbstractIdentifier t]
@init   {   assert($l != null);
            assert($t != null); 
        }
    : dv1=decl_var[$t] {
            assert($dv1.tree != null);
            $l.add($dv1.tree);
        } (COMMA dv2=decl_var[$t] {
            assert($dv2.tree != null);
            $l.add($dv2.tree);
        }
      )*
    ;

decl_var[AbstractIdentifier t] returns[AbstractDeclVar tree]
@init   {   assert($t != null);
        }
    : i=ident {
            assert($i.tree != null);
            AbstractInitialization noInit = new NoInitialization();
            setLocation(noInit,$i.start);
            $tree = new DeclVar($t,$i.tree,noInit);
            setLocation($tree, $i.start);
        }
      (EQUALS e=expr {
            assert($e.tree != null);
            AbstractInitialization init = new Initialization($e.tree);
            setLocation(init,$EQUALS);
            $tree = new DeclVar($t,$i.tree,init);
            setLocation($tree, $i.start);
        }
      )? {
                    
        }
    ;

list_inst returns[ListInst tree]
@init   {   $tree = new ListInst();
        }
    : (inst {
            assert($inst.tree != null);
            $tree.add($inst.tree);
        }
      )*
    ;

inst returns[AbstractInst tree]
    : e1=expr SEMI {
            assert($e1.tree != null);
            $tree = $e1.tree;
        }
    | SEMI {
            $tree = new NoOperation();
            setLocation($tree, $SEMI);
        }
    | PRINT OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Print(false,$list_expr.tree);
            setLocation($tree, $PRINT);
        }
    | PRINTLN OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Println(false,$list_expr.tree);
            setLocation($tree, $PRINTLN);
            }
    | PRINTX OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Print(true,$list_expr.tree);
            setLocation($tree, $PRINTX);
        }
    | PRINTLNX OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Println(true,$list_expr.tree);
            setLocation($tree, $PRINTLNX);
        }
    | if_then_else {
            assert($if_then_else.tree != null);
            $tree = $if_then_else.tree;
        }
    | WHILE OPARENT condition=expr CPARENT OBRACE body=list_inst CBRACE {
            assert($condition.tree != null);
            assert($body.tree != null);
            $tree = new While($condition.tree, $list_inst.tree);
            setLocation($tree, $WHILE);
        }
    | RETURN expr SEMI {
            assert($expr.tree != null);
            $tree = new Return($expr.tree);
            setLocation($tree, $RETURN);
        }
    ;

if_then_else returns[IfThenElse tree]
@init   {
            // variables to link the "else" instructions to the previous "if" statement
            ListInst prevListInst;
            ListInst currListInst;
            IfThenElse tempTree;
        }
    : if1=IF OPARENT condition=expr CPARENT OBRACE li_if=list_inst CBRACE {
            assert($condition.tree != null);
            assert($li_if.tree != null);
            // "if" with no instruction in its "else"
            prevListInst = new ListInst();
            $tree = new IfThenElse($condition.tree, $li_if.tree, prevListInst);
            setLocation($tree, $if1);
        }
      (ELSE elsif=IF OPARENT elsif_cond=expr CPARENT OBRACE elsif_li=list_inst CBRACE {
            assert($elsif_cond.tree != null);
            assert($elsif_li.tree != null);
            // "if" with no instruction in its "else"
            currListInst = new ListInst();
            tempTree = new IfThenElse($elsif_cond.tree, $elsif_li.tree, currListInst);
            setLocation(tempTree, $elsif);
            // the only instruction of the ListInst of the "else" of the previous "if" is the current IfThenElse
            prevListInst.add(tempTree);
            // update the linking variables
            prevListInst = currListInst;
        }
      )*
      (ELSE OBRACE li_else=list_inst CBRACE {
            assert($li_else.tree != null);
            for (AbstractInst t : $li_else.tree.getList()) {
                prevListInst.add(t);
            }
            
        }
      )?
    ;

list_expr returns[ListExpr tree]
@init   {   $tree = new ListExpr();
        }
    : (e1=expr {
            assert($e1.tree != null);
            $tree.add($e1.tree);
        }
       (COMMA e2=expr {
            assert($e2.tree != null);
            $tree.add($e2.tree);
        }
       )* )?
    ;

expr returns[AbstractExpr tree]
    : assign_expr {
            assert($assign_expr.tree != null);
            $tree = $assign_expr.tree;
        }
    ;

assign_expr returns[AbstractExpr tree]
    : e=or_expr (
        /* condition: expression e must be a "LVALUE" */ {
            if (! ($e.tree instanceof AbstractLValue)) {
                throw new InvalidLValue(this, $ctx);
            }
        }
        EQUALS e2=assign_expr {
            assert($e.tree != null);
            assert($e2.tree != null);
            $tree = new Assign((AbstractLValue)$e.tree, $e2.tree);
            setLocation($tree, $EQUALS);
        }
      | /* epsilon */ {
            assert($e.tree != null);
            $tree = $e.tree;
        }
      )
    ;

or_expr returns[AbstractExpr tree]
    : e=and_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=or_expr OR e2=and_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Or($e1.tree, $e2.tree);
            setLocation($tree, $OR);
       }
    ;

and_expr returns[AbstractExpr tree]
    : e=eq_neq_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    |  e1=and_expr AND e2=eq_neq_expr {
            assert($e1.tree != null);                         
            assert($e2.tree != null);
            $tree = new And($e1.tree, $e2.tree);
            setLocation($tree, $AND);
        }
    ;

eq_neq_expr returns[AbstractExpr tree]
    : e=inequality_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=eq_neq_expr EQEQ e2=inequality_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Equals($e1.tree, $e2.tree);
            setLocation($tree, $EQEQ);
        }
    | e1=eq_neq_expr NEQ e2=inequality_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new NotEquals($e1.tree, $e2.tree);
            setLocation($tree, $NEQ);
        }
    ;

inequality_expr returns[AbstractExpr tree]
    : e=sum_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=inequality_expr LEQ e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new LowerOrEqual($e1.tree, $e2.tree);
            setLocation($tree, $LEQ);
        }
    | e1=inequality_expr GEQ e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new GreaterOrEqual($e1.tree, $e2.tree);
            setLocation($tree, $GEQ);
        }
    | e1=inequality_expr GT e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Greater($e1.tree, $e2.tree);
            setLocation($tree, $GT);
        }
    | e1=inequality_expr LT e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Lower($e1.tree, $e2.tree);
            setLocation($tree, $LT);
        }
    | e1=inequality_expr INSTANCEOF type {
            assert($e1.tree != null);
            assert($type.tree != null);
            $tree = new InstanceOf($e1.tree, $type.tree);
            setLocation($tree, $INSTANCEOF);
        }
    ;


sum_expr returns[AbstractExpr tree]
    : e=mult_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=sum_expr PLUS e2=mult_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Plus($e1.tree, $e2.tree);
            setLocation($tree, $PLUS);
        }
    | e1=sum_expr MINUS e2=mult_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Minus($e1.tree, $e2.tree);
            setLocation($tree, $MINUS);
        }
    ;

mult_expr returns[AbstractExpr tree]
    : e=unary_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=mult_expr TIMES e2=unary_expr {
            assert($e1.tree != null);                                         
            assert($e2.tree != null);
            $tree = new Multiply($e1.tree, $e2.tree);
            setLocation($tree, $TIMES);
        }
    | e1=mult_expr SLASH e2=unary_expr {
            assert($e1.tree != null);                                         
            assert($e2.tree != null);
            $tree = new Divide($e1.tree, $e2.tree);
            setLocation($tree, $SLASH);
        }
    | e1=mult_expr PERCENT e2=unary_expr {
            assert($e1.tree != null);                                                                          
            assert($e2.tree != null);
            $tree = new Modulo($e1.tree, $e2.tree);
            setLocation($tree, $PERCENT);
        }
    ;

unary_expr returns[AbstractExpr tree]
    : op=MINUS e=unary_expr {
            assert($e.tree != null);
            $tree = new UnaryMinus($e.tree);
            setLocation($tree, $op);
        }
    | op=EXCLAM e=unary_expr {
            assert($e.tree != null);
            $tree = new Not($e.tree);
            setLocation($tree, $op);
        }
    | select_expr {
            assert($select_expr.tree != null);
            $tree = $select_expr.tree;
        }
    ;

select_expr returns[AbstractExpr tree]
    : e=primary_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=select_expr DOT i=ident {
            assert($e1.tree != null);
            assert($i.tree != null);
        }
        (o=OPARENT args=list_expr CPARENT {
            // we matched "e1.i(args)"
            assert($args.tree != null);
            $tree = new MethodCall($e1.tree, $i.tree, $args.tree);
            setLocation($tree, $o);
        }
        | /* epsilon */ {
            // we matched "e.i"
            $tree = new Selection($e1.tree, $i.tree);
            setLocation($tree, $DOT);
        }
        )
    ;

primary_expr returns[AbstractExpr tree]
    : ident {
            assert($ident.tree != null);
            $tree = $ident.tree;
        }
    | m=ident OPARENT args=list_expr CPARENT {
            assert($args.tree != null);
            assert($m.tree != null);
            This _this = new This(true);
            setLocation(_this, $m.start);
            $tree = new MethodCall(_this, $m.tree, $args.tree);
            setLocation($tree, $m.start);

        }
    | OPARENT expr CPARENT {
            assert($expr.tree != null);
            $tree = $expr.tree;
        }
    | READINT OPARENT CPARENT {
            $tree = new ReadInt();
            setLocation($tree,$READINT);
        }
    | READFLOAT OPARENT CPARENT {
            $tree = new ReadFloat();
            setLocation($tree, $READFLOAT);
        }
    | NEW ident OPARENT CPARENT {
            assert($ident.tree != null);
            $tree = new New($ident.tree);
            setLocation($tree,$NEW);
        }
    | cast=OPARENT type CPARENT OPARENT expr CPARENT {
            assert($type.tree != null);
            assert($expr.tree != null);
            $tree = new Cast($type.tree, $expr.tree);
            setLocation($tree,$cast);
        }
    | literal {
            assert($literal.tree != null);
            $tree = $literal.tree;
        }
    ;

type returns[AbstractIdentifier tree]
    : ident {
            assert($ident.tree != null);
            $tree = $ident.tree;
        }
    ;

literal returns[AbstractExpr tree]
    : INT {
            try {
                $tree = new IntLiteral(Integer.parseInt($INT.text));
                setLocation($tree, $INT);
            } catch (NumberFormatException e) {
                // The integer could not be parsed (it's probably too large).
                // set $tree to null, and then fail with the semantic predicate
                // {$tree != null}?. In decac, we'll have a more advanced error
                // management.
                $tree = null;
            }
        } {$tree != null}?
    | FLOAT 
        {
            LOG.debug("\n================== Parsing float ==================");
            float f = Float.POSITIVE_INFINITY;

            try {
                f = Float.parseFloat($FLOAT.text);
                $tree = new FloatLiteral(f);
                setLocation($tree, $FLOAT);
            } catch (NumberFormatException e) {
                // The float could not be parsed.
                // set $tree to null, and then fail with the semantic predicate
                // {$tree != null}?. In decac, we'll have a more advanced error
                // management.
                $tree = null;
            }

            if (f==Float.POSITIVE_INFINITY || f==Float.NEGATIVE_INFINITY || f==Float.NaN) {
                $tree = null;
            }

            else if (f==0) {
                // error if non 0 rounded to 0
                String s = $FLOAT.text;
                assert(s.length() >= 2);

                // FLOATHEX
                if(s.charAt(1)=='x' || s.charAt(1)=='X') {
                    // get the float without the power
                    int indexp = (s.indexOf('p')==-1 ? s.length(): s.indexOf('p'));
                    int indexP = (s.indexOf('P')==-1 ? s.length(): s.indexOf('P'));
                    int indexEnd = Math.min(indexp, indexP);
                    String floatWithoutPower = s.substring(2, indexEnd);
                    LOG.debug("floatWithoutPower : "+floatWithoutPower);
                    if (Float.parseFloat(floatWithoutPower) != 0) {
                        $tree = null;
                    }
                }

                // FLOATDEC
                else {
                    // get the float without the power
                    int indexe = (s.indexOf('e')==-1 ? s.length(): s.indexOf('e'));
                    int indexE = (s.indexOf('E')==-1 ? s.length(): s.indexOf('E'));
                    int indexf = (s.indexOf('f')==-1 ? s.length(): s.indexOf('f'));
                    int indexF = (s.indexOf('F')==-1 ? s.length(): s.indexOf('F'));
                    int indexEnd = Math.min(indexe, indexE);
                    indexEnd = Math.min(indexEnd, indexf);
                    indexEnd = Math.min(indexEnd, indexF);
                    String floatWithoutPower = s.substring(0, indexEnd);
                    LOG.debug("floatWithoutPower : "+floatWithoutPower);
                    if (Float.parseFloat(floatWithoutPower) != 0) {
                        $tree = null;
                    }
                }
            }
        } {$tree != null}?
    | STRING {
            $tree = new StringLiteral($STRING.text.substring(1, $STRING.text.length() - 1));
            setLocation($tree, $STRING);
        }
    | TRUE {
            $tree = new BooleanLiteral(true);
            setLocation($tree, $TRUE);
        }
    | FALSE {
            $tree = new BooleanLiteral(false);
            setLocation($tree, $FALSE);
        }
    | THIS {
            $tree = new This(false);
            setLocation($tree, $THIS);
        }
    | NULL {
            $tree = new Null();
            setLocation($tree, $NULL);
        }
    ;

ident returns[AbstractIdentifier tree]
    : IDENT {
            $tree = new Identifier(this.getDecacCompiler().createSymbol($IDENT.text));
            // the createSymbol methods add the Symbol to the table only if it is not already in it
            //$tree = new Identifier(this.getDecacCompiler().symbolTable.create($IDENT.text));
            // it seems to be equivalent but it is deeper in the class hierarchy
            setLocation($tree, $IDENT);
        }
    ;

/****     Class related rules     ****/

list_classes returns[ListDeclClass tree]
@init   {   $tree = new ListDeclClass();
        }
    :(c1=class_decl {
            assert($c1.tree != null);
            $tree.add($c1.tree);
        }
      )*
    ;

class_decl returns[AbstractDeclClass tree]
    : CLASS name=ident superclass=class_extension OBRACE class_body CBRACE {
            assert($name.tree != null);
            assert($superclass.tree != null);
            assert($class_body.fields != null);
            assert($class_body.methods != null);
            $tree = new DeclClass($name.tree, $superclass.tree, $class_body.fields, $class_body.methods); 
            setLocation($tree, $CLASS);
        }
    ;

class_extension returns[AbstractIdentifier tree]
    : EXTENDS ident {
            assert($ident.tree != null);
            $tree = $ident.tree;
        }
    | /* epsilon */ {
            $tree = new Identifier(this.getDecacCompiler().createSymbol("Object"));
            // the createSymbol methods add the Symbol to the table only if it is not already in it
            $tree.setLocation(Location.BUILTIN);
        }
    ;

class_body returns[ListDeclField fields, ListDeclMethod methods] // ListDeclMethod to implement
@init   {   $fields = new ListDeclField();
            $methods = new ListDeclMethod();
        }
    : (m=decl_method {
            assert($m.tree != null);
            $methods.add($decl_method.tree);
        }
      | f=decl_field_set[$fields]
      )*
    ;

decl_field_set[ListDeclField l]
@init   {   assert($l != null);
        }
    : visib=visibility t=type list_decl_field[$l, $type.tree, $visib.v] SEMI
    ;

visibility returns[Visibility v]
    : /* epsilon */ {
            $v = Visibility.PUBLIC;
        }
    | PROTECTED {
            $v = Visibility.PROTECTED;
        }
    ;

list_decl_field[ListDeclField l, AbstractIdentifier t, Visibility v]
@init   {   assert($l != null);
            assert($t != null); 
            assert($v != null); 
        }
    : df1=decl_field[$t,$v] {
            assert($df1.tree != null);
            $l.add($df1.tree);
        }
        (COMMA df2=decl_field[$t,$v] {
            assert($df2.tree != null);
            $l.add($df2.tree);
        }
      )*
    ;

decl_field[AbstractIdentifier t, Visibility v] returns[AbstractDeclField tree]
@init   {   assert($t != null);
            assert($v != null); 
        }
    : i=ident {
            assert($i.tree != null);
            NoInitialization noInit = new NoInitialization();
            setLocation(noInit,$i.start);
            $tree = new DeclField($t,$i.tree,noInit,$v);
            setLocation($tree,$i.start);
        }
      (EQUALS e=expr {
            assert($e.tree != null);
            Initialization init = new Initialization($e.tree);
            setLocation(init,$EQUALS);
            $tree = new DeclField($t,$i.tree,init,$v);
            setLocation($tree,$i.start);
        }
      )? {
        }
    ;

decl_method returns[AbstractDeclMethod tree]
    : type ident OPARENT params=list_params CPARENT (block {
            assert($type.tree != null);
            assert($ident.tree != null);
            assert($params.tree != null);
            assert($block.decls != null);
            assert($block.insts != null);
            AbstractMethod body = new MethodBody($block.decls, $block.insts);
            setLocation(body,$block.start);
            $tree = new DeclMethod($type.tree, $ident.tree, $list_params.tree, body);
            setLocation($tree,$type.start);
        }
      | ASM OPARENT code=multi_line_string CPARENT SEMI {
            assert($type.tree != null);
            assert($ident.tree != null);
            assert($params.tree != null);
            assert($code.text != null);
            StringLiteral codeAsm = new StringLiteral($code.text);
            setLocation(codeAsm,$code.start);
            AbstractMethod asmBody = new MethodAsmBody(codeAsm);
            setLocation(asmBody,$ASM);
            $tree = new DeclMethod($type.tree, $ident.tree, $list_params.tree, asmBody);
            setLocation($tree,$type.start);
        }
      ) {
        }
    ;

list_params returns[ListDeclParam tree]
@init   {   $tree = new ListDeclParam();
        }
    : (p1=param {
            assert($p1.tree != null);
            $tree.add($p1.tree);
        } (COMMA p2=param {
            assert($p2.tree != null);
            $tree.add($p2.tree);
        }
      )*)?
    ;

multi_line_string returns[String text, Location location]
    : s=STRING {
            $text = $s.text;
            $location = tokenLocation($s);
        }
    | s=MULTI_LINE_STRING {
            $text = $s.text;
            $location = tokenLocation($s);
        }
    ;

param returns[AbstractDeclParam tree]
    : type ident {
            assert($type.tree != null);
            assert($ident.tree != null);
            $tree = new DeclParam($type.tree, $ident.tree);
            setLocation($tree,$type.start);
        }
    ;
