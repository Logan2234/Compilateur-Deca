package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
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
        //Validate.isTrue(leftOperand != rightOperand, "Sharing subtrees is forbidden"); //TODO Corriger ca dans multiply
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // if no result register, we must put the result on the stack.
        boolean needNewRegister = register == null;
        // we still need a register for any binary op
        GPRegister leftRegister = needNewRegister ? compiler.allocateRegister() : register;
        boolean needRegisterSpace = leftRegister == null;
        if(needRegisterSpace) {
            // save on the stack R2
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.getR(2)));
            leftRegister = Register.getR(2);
        }
        // call the binary expression code on the given register and the address
        // load left operand in the result register
        leftOperand.codeGenExpr(compiler, leftRegister);
        // load the right operand
        GPRegister rightRegister = compiler.allocateRegister();
        boolean needRightRegisterSpace = rightRegister == null;
        // if right register is null, use R3
        if(needRightRegisterSpace) {
            // need to assert left register is not right register
            if(leftRegister.getNumber() == 2) {
                rightRegister = Register.getR(3);
            }
            else {
                rightRegister = Register.getR(2);
            }
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(rightRegister));
        }
        rightOperand.codeGenExpr(compiler, rightRegister);
        // do the operation
        codeGenBinExp(compiler, leftRegister, rightRegister);
        if(needRightRegisterSpace) {
            // restor R3
            compiler.increaseContextUsedStack(-1);
            compiler.addInstruction(new POP(rightRegister));
        }
        else {
            // free right register
            compiler.freeRegister(rightRegister);
        }
        // restore R2 !
        if(needRegisterSpace) {
            compiler.increaseContextUsedStack(-1);
            compiler.addInstruction(new POP(Register.getR(2)));
        }
        // if the original register is null, load the result on the stack
        if(register == null) {
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(leftRegister));
        }
        // might want to free the allocated register
        if(needNewRegister && !needRegisterSpace) {
            compiler.freeRegister(leftRegister);
        }
    }

    /**
     * do the binary operation between the given register and the given DVal. 
     * @param compiler Where we write the instructions.
     * @param register Not null : contains one of the operands, and where we put the result.
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
