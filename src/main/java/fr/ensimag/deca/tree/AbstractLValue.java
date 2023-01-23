package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractLValue extends AbstractExpr {

    public abstract Definition getDefinition();

    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        // return nothing ? expect if we find a way to compute methods at compile time...
        return new CollapseResult<CollapseValue>(new CollapseValue(), false);
    }
}
