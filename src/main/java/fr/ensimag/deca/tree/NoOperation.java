package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
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
    protected Tree removeUnusedVar(Program prog) {
        prog.setVarRemoved();
        return null;
    }

    @Override
    public CollapseResult<ListInst> collapseInst() {
        // collapse no op into... no op.
        return new CollapseResult<ListInst>(new ListInst(), true); // true because we removed ourself
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        return this;
    }
}
