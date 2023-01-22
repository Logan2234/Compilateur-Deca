package fr.ensimag.deca.tree;

import java.util.List;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
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
    protected boolean spotUsedVar() {
        return false;
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }

    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        // nothing to do
        return new CollapseResult<CollapseValue>(new CollapseValue(), false);
    }
}
