package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Opération binaire Boolean and, or
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeLeft = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        if (!typeLeft.isBoolean() || !typeRight.isBoolean()) {
            Location loc = this.getLocation();
            throw new ContextualError("A boolean operation has to be done only between 2 booleans (rule 3.33)",
                    loc);
        }

        // Ajout du décor
        this.setType(typeLeft);
        return typeLeft;
    }

    @Override
    public AbstractExpr skipCalculs(){
        AbstractExpr left = getLeftOperand();
        AbstractExpr right = getRightOperand();
        if (!(left.isLiteral())){
            left = left.skipCalculs();
        }

        if (!(right.isLiteral())){
            right = right.skipCalculs();
        }

        if (left.isLiteral() && right.isLiteral()){
            boolean newValue;
            if (getOperatorName().equals("&&")){
                newValue = ((BooleanLiteral) left).getValue() && ((BooleanLiteral) right).getValue();
            } else {
                newValue = ((BooleanLiteral) left).getValue() || ((BooleanLiteral) right).getValue();
            }
            return new BooleanLiteral(newValue);
        }
        return this;
    }

}
