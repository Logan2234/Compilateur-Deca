package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractExpr extends AbstractInst {
    /**
     * @return true if the expression does not correspond to any concrete token
     *         in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /*
     * Used by This class for telling if the This is implicit or not.
     * getImpl return false and it will be override by This class
     * 
     * @return false if the expression is not a This class and true if the
     * expression is an implicit This
     */
    public boolean getImpl() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed
     * by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }

    private Type type;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue"
     * of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     (contains the "env_types" attribute)
     * @param localEnv
     *                     Environment in which the expression should be checked
     *                     (corresponds to the "env_exp" attribute)
     * @param currentClass
     *                     Definition of the class containing the expression
     *                     (corresponds to the "class" attribute)
     *                     is null in the main bloc.
     * @return the Type of the expression (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     contains the "env_types" attribute
     * @param localEnv     corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type expectedType) throws ContextualError {
        Type rtype = verifyExpr(compiler, localEnv, currentClass);

        if (!expectedType.assignCompatible(rtype)) {
            throw new ContextualError(
                    "An assignation between a " + expectedType + " and a " + rtype + " is not possible (rule 3.32)",
                    getLocation());
        }

        // Ajout du décor
        setType(expectedType);

        if (expectedType.isFloat() && rtype.isInt()) {
            AbstractExpr convFloat = new ConvFloat(this);
            convFloat.verifyExpr(compiler, localEnv, currentClass);
            convFloat.setLocation(getLocation());
            return convFloat;
        }
        return this;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
        verifyExpr(compiler, localEnv, currentClass);
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *                     Environment in which the condition should be checked.
     * @param currentClass
     *                     Definition of the class containing the expression, or
     *                     null in the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        verifyExpr(compiler, localEnv, currentClass);
        if (!getType().isBoolean())
            throw new ContextualError("The condition is not a boolean (rule 3.29)", getLocation());
    }

    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler, boolean hex) {
        // we can safely assume this is only called if the result is an integer or
        // float, with context check
        // so compute ourself in R1, then depending on our type display it !
        GPRegister register = compiler.allocateRegister();
        codeGenExpr(compiler, register);
        compiler.addInstruction(new LOAD(register, Register.R1));
        compiler.freeRegister(register);
        if (type.isInt()) {
            compiler.addInstruction(new WINT());
        } else if (type.isFloat()) {
            if (hex) {
                compiler.addInstruction(new WFLOATX());
            } else {
                compiler.addInstruction(new WFLOAT());
            }
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // by default, put the result on the scratch register R1 to avoid pushing
        // nonsense on the stack.
        codeGenExpr(compiler, Register.R1);
    }

    /**
     * Generate code for the expression. The result is put in the result register.
     * if the result register is null, the result is on the stack.
     * 
     * @param compiler       Where we write instructions.
     * @param resultRegister The register to put the result of the instruction.
     */
    protected abstract void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister);

    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }

    /**
     * Fin recursively all method calls and Reads in the expression and add them on
     * top of the list
     * This method is used for optimizing the Program tree.
     * Instructions should not be removed if they contains a MethodCall or a Read
     * that could
     * potentially print/read on stdin/stdout or change the state of an object.
     * It had the methods found on top of the list
     * 
     * @param the list of MethodCalls and Reads ordered by order of apparition
     */
    protected abstract void addMethodCalls(List<AbstractExpr> foundMethodCalls);

    /**
     * Fin recursively all method calls and reads in the expression
     * This method is used for optimizing the Program tree.
     * Instructions should not be removed if they contains a MethodCall or a Read
     * that could
     * potentially print/read on stdin/stdout or change the state of an object.
     * 
     * @return the list of MethodCall ordered by order of apparition
     */
    protected final List<AbstractExpr> getMethodCalls() {
        List<AbstractExpr> foundMethodCalls = new LinkedList<AbstractExpr>();
        this.addMethodCalls(foundMethodCalls);
        return foundMethodCalls;
    }

    @Override
    public boolean collapse() {
        // by default, return false.
        return false;
        // expressions that can collapse will override this.
    }

    @Override
    public ListInst collapseInst() {
        // by default, return empty list of instructions.
        return new ListInst();
        // expressions that can collapse will override this.
    }

    protected void shift(DecacCompiler compiler, AbstractExpr left, AbstractExpr right, ListInst listPlus) {
        int value = ((IntLiteral) right).getValue();
        String[] nbbinaire = Integer.toBinaryString(value).split("");
        int[] binaire = new int[nbbinaire.length];
        ListInst list = new ListInst();
        for (int i = 0; i < nbbinaire.length; i++) {
            binaire[i] = Integer.parseInt(nbbinaire[nbbinaire.length - 1 - i]);
            if (binaire[i] == 1) {
                AbstractExpr puissance = new IntLiteral((int) Math.pow(2, i));
                ((IntLiteral) puissance).setType(compiler.environmentType.INT);
                AbstractExpr multiply = new Multiply(left, puissance);
                ((Multiply) multiply).setShiftReplacable();
                ((Multiply) multiply).setType(compiler.environmentType.INT);

                list.add(multiply);
            }
        }
        if (list.size() == 0) {

        } else if (list.size() == 1) {
            listPlus.add(list.getList().get(0));
        } else {
            Plus plus = new Plus((AbstractExpr) list.getList().get(1), (AbstractExpr) list.getList().get(0));
            plus.setType(compiler.environmentType.INT);
            listPlus.add(plus);
            if (list.size() > 2) {
                for (int i = 2; i < list.size(); i++) {
                    Plus operand = new Plus((AbstractExpr) list.getList().get(i), (AbstractExpr) plus);
                    ((Plus) operand).setType(compiler.environmentType.INT);
                    plus = new Plus((AbstractExpr) list.getList().get(i), (AbstractExpr) plus);
                    ((Plus) plus).setType(compiler.environmentType.INT);
                    listPlus.add(operand);
                }
            }
        }
    }

    /**
     * @param map
     * @param mapSymbol
     * @param symbolToIdent
     * @param exprList
     * @param node
     * @param op
     */
    private void addMap(Map<Symbol, Integer> map, Map<Symbol, List<AbstractExpr>> mapSymbol,
            Map<Symbol, Identifier> symbolToIdent, ListExpr exprList, AbstractInst node, String op) {
        AbstractExpr leftOperand = ((AbstractOpArith) node).getLeftOperand();
        AbstractExpr rightOperand = ((AbstractOpArith) node).getRightOperand();
        String plusOrMinus = ((AbstractOpArith) node).getOperatorName();
        int leftmultiplier = (op == "-" ? -1 : 1);
        int rightmultiplier = (plusOrMinus == "-" ? -1 : 1);
        boolean leftIsMinus = false, rightIsMinus = false;

        try {
            leftOperand = (UnaryMinus) leftOperand;
            leftmultiplier *= -1;
            leftIsMinus = true;
        } catch (ClassCastException e) {
        }
        try {
            rightOperand = (UnaryMinus) rightOperand;
            rightmultiplier *= -1;
            rightIsMinus = true;
        } catch (ClassCastException e) {
        }

        int multiplier = leftmultiplier * rightmultiplier;

        try {
            Divide divide = (Divide) node;
            exprList.add(divide);
            return;
        } catch (ClassCastException e) {
            try {
                Modulo modulo = (Modulo) node;
                exprList.add(modulo);
                return;
            } catch (ClassCastException e2) {
            }
        }
        try {
            Multiply mult = (Multiply) node;
            if (!leftOperand.isLiteral() && !rightOperand.isLiteral()) {
                Identifier identLeft;
                Identifier identRight;
                if (leftIsMinus) {
                    identLeft = (Identifier) ((UnaryMinus) leftOperand).getOperand();
                    Type type = rightOperand.getType();
                    rightOperand = new UnaryMinus(rightOperand);
                    rightOperand.setType(type);
                    rightIsMinus = true;
                } else
                    identLeft = (Identifier) leftOperand;
                if (rightIsMinus)
                    identRight = (Identifier) ((UnaryMinus) rightOperand).getOperand();
                else
                    identRight = (Identifier) rightOperand;

                symbolToIdent.put(identRight.getName(), identRight);
                symbolToIdent.put(identLeft.getName(), identLeft);

                if (!mapSymbol.containsKey(identLeft.getName()))
                    mapSymbol.put(identLeft.getName(), new ArrayList<>());
                mapSymbol.get(identLeft.getName()).add(rightOperand);

            } else if (leftOperand.isLiteral() && !rightOperand.isLiteral())
                if (leftIsMinus && rightIsMinus)
                    incOccur(map, symbolToIdent, ((Identifier) ((UnaryMinus) (rightOperand)).getOperand()),
                            multiplier * ((IntLiteral) ((UnaryMinus) leftOperand).getOperand()).getValue());
                else if (leftIsMinus)
                    incOccur(map, symbolToIdent, (Identifier) rightOperand,
                            multiplier * ((IntLiteral) ((UnaryMinus) leftOperand).getOperand()).getValue());
                else if (rightIsMinus)
                    incOccur(map, symbolToIdent, ((Identifier) ((UnaryMinus) (rightOperand)).getOperand()),
                            multiplier * ((IntLiteral) leftOperand).getValue());
                else
                    incOccur(map, symbolToIdent, (Identifier) rightOperand,
                            multiplier * ((IntLiteral) leftOperand).getValue());
            else if (rightOperand.isLiteral() && !leftOperand.isLiteral())
                if (rightIsMinus && leftIsMinus)
                    incOccur(map, symbolToIdent, ((Identifier) ((UnaryMinus) leftOperand).getOperand()),
                            multiplier * ((IntLiteral) ((UnaryMinus) rightOperand).getOperand()).getValue());
                else if (leftIsMinus)
                    incOccur(map, symbolToIdent, ((Identifier) ((UnaryMinus) leftOperand).getOperand()),
                            multiplier * ((IntLiteral) rightOperand).getValue());
                else if (rightIsMinus)
                    incOccur(map, symbolToIdent, (Identifier) leftOperand,
                            multiplier * ((IntLiteral) ((UnaryMinus) rightOperand).getOperand()).getValue());
                else
                    incOccur(map, symbolToIdent, (Identifier) leftOperand,
                            multiplier * ((IntLiteral) rightOperand).getValue());
        } catch (ClassCastException e) {
            try {
                if (leftIsMinus)
                    incOccur(map, symbolToIdent, (Identifier) ((UnaryMinus) (leftOperand)).getOperand(), leftmultiplier);
                else
                    incOccur(map, symbolToIdent, (Identifier) leftOperand, leftmultiplier);
            } catch (ClassCastException e1) {
                try {
                    addMap(map, mapSymbol, symbolToIdent, exprList, leftOperand, op);
                } catch (ClassCastException e3) {
                    exprList.add((AbstractExpr) ((AbstractOpArith) (node)).getLeftOperand());
                    return;
                }
            }
            try {
                if (rightIsMinus)
                    incOccur(map, symbolToIdent, (Identifier) ((UnaryMinus) rightOperand).getOperand(), rightmultiplier);
                else
                    incOccur(map, symbolToIdent, (Identifier) rightOperand, rightmultiplier);
            } catch (ClassCastException e2) {
                try {
                    addMap(map, mapSymbol, symbolToIdent, exprList, rightOperand, plusOrMinus);
                } catch (ClassCastException e3) {
                    exprList.add((AbstractExpr) ((AbstractOpArith) (node)).getRightOperand());
                }
            }
        }
    }

    private void incOccur(Map<Symbol, Integer> map, Map<Symbol, Identifier> symbolToIdent, Identifier ident, int num) {
        symbolToIdent.put(ident.getName(), ident);
        map.merge(ident.getName(), num, Integer::sum);
    }

    /**
     * @param compiler
     * @param classChoser
     * @return
     */
    public AbstractExpr facto(DecacCompiler compiler, boolean classChoser) {
        Map<Symbol, Integer> map = new HashMap<>();
        Map<Symbol, List<AbstractExpr>> mapSymbol = new HashMap<>();
        Map<Symbol, Identifier> symbolToIdent = new HashMap<>();
        ListExpr exprList = new ListExpr();
        addMap(map, mapSymbol, symbolToIdent, exprList, this, "+");
        ListExpr multiply = new ListExpr();
        for (Map.Entry<Symbol, Integer> entry : map.entrySet()) {
            Multiply mult = new Multiply(symbolToIdent.get(entry.getKey()), new IntLiteral(entry.getValue()));
            mult.setType(compiler.environmentType.INT);
            mult.rightOperand.setType(compiler.environmentType.INT);
            multiply.add(mult);
        }

        ListExpr multiplySymbol = new ListExpr();
        for (Map.Entry<Symbol, List<AbstractExpr>> entry : mapSymbol.entrySet()) {
            for (AbstractExpr expr : entry.getValue()) {
                Multiply mult = new Multiply(symbolToIdent.get(entry.getKey()), expr);
                mult.setType(compiler.environmentType.INT);
                mult.leftOperand.setType(compiler.environmentType.INT);
                multiplySymbol.add(mult);
            }
        }

        AbstractExpr expr = null;
        expr = appendToExpr(compiler, multiply, expr, classChoser);
        expr = appendToExpr(compiler, multiplySymbol, expr, classChoser);
        expr = appendToExpr(compiler, exprList, expr, classChoser);
        return expr;
    }

    private AbstractExpr appendToExpr(DecacCompiler compiler, ListExpr listExpr, AbstractExpr expr,
            boolean classChoser) {
        if (listExpr.size() > 0) {
            if (expr == null)
                expr = listExpr.getList().get(0);
            else {
                if (classChoser)
                    expr = new Plus(expr, listExpr.getList().get(0));
                else
                    expr = new Minus(expr, listExpr.getList().get(0));

                expr.setType(compiler.environmentType.INT);
            }

            for (int i = 1; i < listExpr.size(); i++) {
                if (classChoser)
                    expr = new Plus(expr, listExpr.getList().get(i));
                else
                    expr = new Minus(expr, listExpr.getList().get(i));
                expr.setType(compiler.environmentType.INT);
            }
        }
        return expr;
    }
}