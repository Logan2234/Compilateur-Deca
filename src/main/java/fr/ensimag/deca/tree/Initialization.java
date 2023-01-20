package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class Initialization extends AbstractInitialization {

    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        expression.verifyRValue(compiler, localEnv, currentClass, t);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(" = ");
        expression.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }

    @Override
    public void codeGenInit(DecacCompiler compiler, Type type, RegisterOffset resultRegister) {
        // call the code gen of the expression, and put it in the result register
        // get a register to store the result in.
        GPRegister register = compiler.allocateRegister();
        // get the expression to solve itself in the given register
        expression.codeGenExpr(compiler, register);
        // save the given register on the stack
        compiler.incrementContextUsedStack();
        if(resultRegister == null) {
            // free before pushing
            compiler.addInstruction(new LOAD(register, Register.R1));
            compiler.freeRegister(register);
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            compiler.addInstruction(new STORE(register, resultRegister));
            // free the register
            compiler.freeRegister(register);
        }
    }

    @Override
    protected boolean spotUsedVar() {
        return this.expression.spotUsedVar();
    }
    
    @Override 
    public boolean collapse(){
        boolean collapsing = false;
        if(expression.collapse()) {
            if(expression.getType().isBoolean()) {
                // try to collapse to bool expr
                Boolean collapsedValue = expression.collapseBool();
                if(collapsedValue != null && expression.collapsable()) {
                    Type type = expression.getType();
                    expression = new BooleanLiteral(collapsedValue);
                    expression.setType(type);
                    collapsing = true;
                }
            }
            else if(expression.getType().isInt()) {
                // try to collapse to a int expr
                Integer collapsedValue = expression.collapseInt();
                if(collapsedValue != null && expression.collapsable()) {
                    Type type = expression.getType();
                    expression = new IntLiteral(collapsedValue);
                    expression.setType(type);
                    collapsing = true;
                }
            }
            else if(expression.getType().isFloat()) {
                // try to collapse to a float
                Float collapsedValue = expression.collapseFloat();
                if(collapsedValue != null && expression.collapsable()) {
                    Type type = expression.getType();
                    expression = new FloatLiteral(collapsedValue);
                    expression.setType(type);
                    collapsing = true;
                }
            }
        }
        return collapsing;
    }
    
}