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
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.WINT;

import java.io.PrintStream;
import java.util.List;

/**
 * Integer literal
 *
 * @author gl03
 * @date 01/01/2023
 */
public class IntLiteral extends AbstractExpr {
    public int getValue() {
        return value;
    }

    private int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        setType(compiler.environmentType.INT);
        return compiler.environmentType.INT;
    }

    @Override
    String prettyPrintNode() {
        return "Int (" + getValue() + ")";
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean hex) {
        // put value in the R1 register and print it out with WINT
        compiler.addInstruction(new LOAD(value, Register.R1));
        compiler.addInstruction(new WINT());
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Integer.toString(value));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // check if result register is not null
        if (resultRegister != null) {
            // put the value in the given register
            compiler.addInstruction(new LOAD(value, resultRegister));
        } else {
            // by convention, put the result on the stack.
            compiler.addInstruction(new LOAD(value, Register.R0));
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R0));
        }
    }

    @Override
    public boolean collapse() {
        // every litteral can be trivialy solved.
        return true;
    }

    @Override
    protected boolean spotUsedVar() {
        return false;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }
    
    public Integer collapseInt() {
        return value;
    }

    @Override
    public boolean collapsable() {
        return false;
    }
}
