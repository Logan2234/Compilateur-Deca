package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Definition;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractLValue extends AbstractExpr {

    public abstract Definition getDefinition();
}
