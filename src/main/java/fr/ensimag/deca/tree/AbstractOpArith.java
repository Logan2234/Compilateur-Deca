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
        Type typeLeft = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Location loc = this.getLocation();

        if (!typeLeft.isInt() && !typeLeft.isFloat())
            throw new ContextualError(
                    "The left operand of an arithmetical operation has to be an int or a float (rule 3.33)", loc);

        if (!typeRight.isInt() && !typeRight.isFloat())
            throw new ContextualError(
                    "The right operand of an arithmetical operation has to be an int or a float (rule 3.33)", loc);

        // Ajout du décor et renvoie du type
        if (typeLeft.isFloat() || typeRight.isFloat()) {
            ConvFloat convFloat;
            if (typeLeft.isInt()) {
                convFloat = new ConvFloat(this.getLeftOperand());
                this.setLeftOperand(convFloat);
                convFloat.setType(compiler.environmentType.FLOAT);
                convFloat.setLocation(getLeftOperand().getLocation());
            } else if (typeRight.isInt()) {
                convFloat = new ConvFloat(this.getRightOperand());
                this.setRightOperand(convFloat);
                convFloat.setType(compiler.environmentType.FLOAT);
                convFloat.setLocation(getRightOperand().getLocation());
            }
            this.setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
        }

        // Ajout du décor
        this.setType(typeLeft);
        return typeLeft;
    }
}
