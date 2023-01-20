package fr.ensimag.deca.tree;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractOpExactCmp extends AbstractOpCmp {
    public AbstractOpExactCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
}
