package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class NoOperation extends AbstractInst {

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
        // nothing
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // nothing to do...
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(";");
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
    protected void spotUsedVar() {
        // do nothing
    }
    
    @Override
    protected Tree removeUnusedVar() {
        return null;
    }

    @Override
    public boolean collapse() {
        // TODO
        return false;
    }

    @Override
    public ListInst collapseInst() {
        return new ListInst();
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        return this;
    }

}
