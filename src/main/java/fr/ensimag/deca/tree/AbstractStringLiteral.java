package fr.ensimag.deca.tree;

import java.util.List;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractStringLiteral extends AbstractExpr {

    public abstract String getValue();

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
