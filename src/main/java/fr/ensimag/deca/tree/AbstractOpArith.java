package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;

import java.beans.Expression;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!typeLeft.isInt() && !typeLeft.isFloat())
            throw new ContextualError(
                    "The left operand of an arithmetical operation has to be an int or a float (rule 3.33)",
                    getLocation());

        if (!typeRight.isInt() && !typeRight.isFloat())
            throw new ContextualError(
                    "The right operand of an arithmetical operation has to be an int or a float (rule 3.33)",
                    getLocation());

        // Ajout du décor et renvoie du type
        if (typeLeft.isFloat() && typeRight.isInt()) {
            ConvFloat convFloat = new ConvFloat(getRightOperand());
            convFloat.verifyExpr(compiler, localEnv, currentClass);
            convFloat.setLocation(getRightOperand().getLocation());
            setRightOperand(convFloat);
        } else if (typeRight.isFloat() && typeLeft.isInt()) {
            ConvFloat convFloat = new ConvFloat(getLeftOperand());
            convFloat.verifyExpr(compiler, localEnv, currentClass);
            convFloat.setLocation(getLeftOperand().getLocation());
            setLeftOperand(convFloat);
            typeLeft = typeRight;
        }

        // Ajout du décor
        setType(typeLeft);
        return typeLeft;
    }

    public boolean isFacto() {
        if ((leftOperand.isLiteral() && leftOperand.getType().isInt())
                ^ (rightOperand.isLiteral() && rightOperand.getType().isInt()))
            return true;
        return false;
    }
}
