package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * Empty main Deca program
 *
 * @author gl03
 * @date 01/01/2023
 */
public class EmptyMain extends AbstractMain {
    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        // nothing
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) {
        // nothing
    }

    /**
     * Contains no real information => nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // no main program => nothing
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
    public CollapseResult<Null> collapseMain() {
        // empty main do not collapse, return false
        return new CollapseResult<Null>(null, false);
    }

    @Override
    protected void spotUsedVar() {
        // do nothing
    }
    
    @Override
    protected Tree removeUnusedVar(Program prog) {
        return this;
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        return this;
    }
}
