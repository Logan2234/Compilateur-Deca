package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.PrintStream;
import java.util.List;

/**
 * This statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class This extends AbstractExpr {

    private final boolean impl;

    public This(boolean impl) {
        this.impl = impl;
    }

    @Override
    public boolean getImpl() {
        return impl;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        if (currentClass.getType().getName().getName() == "Object")
            throw new ContextualError("This can only be used in a class (rule 3.43)", getLocation());

        setType(currentClass.getType());
        return currentClass.getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (!impl)
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
        throw new UnsupportedOperationException("not yet implemented");
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