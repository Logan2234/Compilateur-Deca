package fr.ensimag.deca.tree;

import java.util.List;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractStringLiteral extends AbstractExpr {

    public abstract String getValue();

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        // do nothing
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }
}
