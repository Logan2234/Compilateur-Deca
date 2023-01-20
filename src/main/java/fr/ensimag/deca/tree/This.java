package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.NullReferenceErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.List;

/**
 * This statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class This extends AbstractExpr {

    private final boolean implicit;

    public This(boolean implicit) {
        this.implicit = implicit;
    }

    private ClassDefinition currentClass;

    @Override
    public boolean getImpl() {
        return implicit;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Location loc = this.getLocation();
        if (currentClass.getType().getName().getName() == "Object") {
            throw new ContextualError("This can only be used in a class (rule 3.43)", loc);
        }
        this.currentClass = currentClass;

        this.setType(currentClass.getType());
        return currentClass.getType();
        // throw new UnsupportedOperationException("not yet implemented");

    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (!implicit)
            s.print("this");
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
        // put pointer in the result register
        if(resultRegister == null) {
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
            AbstractRuntimeErr error = new NullReferenceErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new CMP(new NullOperand(), Register.R1));
            compiler.addInstruction(new BEQ(error.getErrorLabel()));
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), resultRegister));
            AbstractRuntimeErr error = new NullReferenceErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new CMP(new NullOperand(), resultRegister));
            compiler.addInstruction(new BEQ(error.getErrorLabel()));
        }
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        // do nothing
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }

}