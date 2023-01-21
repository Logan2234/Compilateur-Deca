package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Binary expressions.
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractBinaryExpr extends AbstractExpr {

    public AbstractExpr getLeftOperand() {
        return leftOperand;
    }

    public AbstractExpr getRightOperand() {
        return rightOperand;
    }

    protected void setLeftOperand(AbstractExpr leftOperand) {
        Validate.notNull(leftOperand);
        this.leftOperand = leftOperand;
    }

    protected void setRightOperand(AbstractExpr rightOperand) {
        Validate.notNull(rightOperand);
        this.rightOperand = rightOperand;
    }

    protected AbstractExpr leftOperand;
    protected AbstractExpr rightOperand;

    public AbstractBinaryExpr(AbstractExpr leftOperand,
            AbstractExpr rightOperand) {
        Validate.notNull(leftOperand, "left operand cannot be null");
        Validate.notNull(rightOperand, "right operand cannot be null");
        Validate.isTrue(leftOperand != rightOperand, "Sharing subtrees is forbidden"); // TODO Corriger ca dans multiply
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // we still need a register for any binary op
        GPRegister leftRegister = register == null ? compiler.allocateRegister() : register;
        // call the binary expression code on the given register and the address
        // load left operand in the result register
        leftOperand.codeGenExpr(compiler, leftRegister);
        // load the right operand
        GPRegister rightRegister = compiler.allocateRegister();
        rightOperand.codeGenExpr(compiler, rightRegister);
        // do the operation
        codeGenBinExp(compiler, leftRegister, rightRegister);
        // free right register
        compiler.freeRegister(rightRegister);
        // if the original register is null, load the result on the stack (also need to
        // free the register)
        if (register == null) {
            // load the rsesult in R1 to free the register (free might pop the stack)
            compiler.addInstruction(new LOAD(leftRegister, Register.R1));
            compiler.freeRegister(leftRegister);
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(leftRegister));
        }
    }

    /**
     * do the binary operation between the given register and the given DVal.
     * 
     * @param compiler Where we write the instructions.
     * @param register Not null : contains one of the operands, and where we put the
     *                 result.
     */
    public abstract void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal);

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        getLeftOperand().decompile(s);
        s.print(" " + getOperatorName() + " ");
        getRightOperand().decompile(s);
        s.print(")");
    }

    abstract protected String getOperatorName();

    @Override
    protected void iterChildren(TreeFunction f) {
        leftOperand.iter(f);
        rightOperand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        leftOperand.prettyPrint(s, prefix, false);
        rightOperand.prettyPrint(s, prefix, true);
    }

    @Override
    public boolean factorised(DecacCompiler compiler) {
        return leftOperand.factorised(compiler) || rightOperand.factorised(compiler);
    }

    @Override
    public ListInst factoInst(DecacCompiler compiler) {
        ListInst listLeft = leftOperand.factoInst(compiler);
        ListInst listRight = rightOperand.factoInst(compiler);
        leftOperand = ((AbstractExpr) listLeft.getList().get(listLeft.getList().size() - 1));
        rightOperand = ((AbstractExpr) listRight.getList().get(listRight.getList().size() - 1));

        ListInst list = new ListInst();
        list.add(this);
        return list;
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        this.leftOperand.spotUsedVar(prog);
        this.rightOperand.spotUsedVar(prog);
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        this.leftOperand.addMethodCalls(foundMethodCalls);
        this.rightOperand.addMethodCalls(foundMethodCalls);
    }

}
