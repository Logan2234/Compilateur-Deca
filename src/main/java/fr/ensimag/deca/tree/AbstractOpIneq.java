package fr.ensimag.deca.tree;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractOpIneq extends AbstractOpCmp {
    public AbstractOpIneq(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
}
