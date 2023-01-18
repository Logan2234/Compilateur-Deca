package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

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
    public void codeGenInit(DecacCompiler compiler) {
        // call the code gen of the expression, and push it on the stack.
        // get a register to store the result in.
        GPRegister register = compiler.allocateRegister();
        if(register == null) {
            // save R2 on the stack
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.getR(2)));
            // use R2
            expression.codeGenExpr(compiler, Register.getR(2));
            // save the result on the stack, and restore R2
            compiler.addInstruction(new LOAD(Register.getR(2), Register.R1));
            compiler.increaseContextUsedStack(-1);
            compiler.addInstruction(new POP(Register.getR(2)));
            // no stack size increment here, because we poped right before it
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            // get the expression to solve itself in the given register
            expression.codeGenExpr(compiler, register);
            // save the given register on the stack
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(register));
            // free the register
            compiler.freeRegister(register);
        }
        
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        this.expression.spotUsedVar(prog);
    }
}