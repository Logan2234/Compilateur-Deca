package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Unary expression.
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractUnaryExpr extends AbstractExpr {

    public AbstractExpr getOperand() {
        return operand;
    }

    private AbstractExpr operand;

    public AbstractUnaryExpr(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }

    public void setOperand(AbstractExpr operand){
        Validate.notNull(operand);
        this.operand = operand;
    }


    protected abstract String getOperatorName();

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        s.print(getOperatorName());
        getOperand().decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        operand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        operand.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // as for binary exp, put expr in register then apply codeGenUnExpr
        GPRegister register  = resultRegister == null ? compiler.allocateRegister() : resultRegister;
        // generate code for the expression in the register
        getOperand().codeGenExpr(compiler, register);
        // generate our code on the register containing the result
        codeGenUnExpr(compiler, register);
        // if original register was null, we allocated a register and we need to push result on the stack
        if(resultRegister == null) {
            // need to free result register and put the result on the stack
            compiler.addInstruction(new LOAD(register, Register.R1));
            compiler.freeRegister(register); // pops
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            compiler.addInstruction(new LOAD(register, resultRegister));
        }
    }

    /**
     * Generate the code for the unary expression, with the result regsiter being
     * not null,
     * and the expression being already computed and in the register.
     * 
     * @param compiler      Where we write the instructions to
     * @param resulRegister not null. the expression have been computed and is in
     *                      this register.
     */
    public abstract void codeGenUnExpr(DecacCompiler compiler, GPRegister resulRegister);
    
    @Override
    protected boolean spotUsedVar() {
        return this.operand.spotUsedVar();
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        this.operand.addMethodCalls(foundMethodCalls);
    }

}
