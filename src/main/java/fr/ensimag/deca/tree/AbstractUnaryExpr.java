package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
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
        if(resultRegister != null) {
            getOperand().codeGenExpr(compiler, resultRegister);
            codeGenUnExpr(compiler, resultRegister);
        }
        else {
            // we need to put the result on the stack
            // try to allocate a register to compute the result
            GPRegister register = compiler.allocateRegister();
            if(register != null) {
                getOperand().codeGenExpr(compiler, resultRegister);
                codeGenUnExpr(compiler, resultRegister);
                compiler.addInstruction(new PUSH(register));
                compiler.freeRegister(register);
            }
            else {
                // save R2
                compiler.addInstruction(new PUSH(Register.getR(2)));
                getOperand().codeGenExpr(compiler, Register.getR(2));
                codeGenUnExpr(compiler, Register.getR(2));
                // save the result in R1 (R1 <- R2)
                compiler.addInstruction(new LOAD(Register.getR(2), Register.R1));
                // restore r2
                compiler.addInstruction(new POP(Register.getR(2)));
                // load the result on the stack
                compiler.addInstruction(new PUSH(Register.R1));
            }
        }
    }

    /**
     * Generate the code for the unary expression, with the result regsiter being not null,
     * and the expression being already computed and in the register.
     * @param compiler Where we write the instructions to
     * @param resulRegister not null. the expression have been computed and is in this register.
     */
    public abstract void codeGenUnExpr(DecacCompiler compiler, GPRegister resulRegister);

}
