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
            } else if (typeRight.isInt()) {
                convFloat = new ConvFloat(this.getRightOperand());
                this.setRightOperand(convFloat);
                convFloat.setType(compiler.environmentType.FLOAT);
            }
            this.setType(compiler.environmentType.FLOAT);
            return compiler.environmentType.FLOAT;
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
            if (getType().isInt()){
                int newValue;
                if (getOperatorName().equals("+")){
                    newValue = ((IntLiteral) (left)).getValue() + ((IntLiteral) (left)).getValue();
                } else if (getOperatorName().equals("-")){
                    newValue = ((IntLiteral) (left)).getValue() - ((IntLiteral) (left)).getValue();
                } else if (getOperatorName().equals("*")){
                    newValue = ((IntLiteral) (left)).getValue() * ((IntLiteral) (left)).getValue();
                } else if (getOperatorName().equals("/")){
                    newValue = ((IntLiteral) (left)).getValue() / ((IntLiteral) (left)).getValue();
                } else if (getOperatorName().equals("%")){
                    newValue = ((IntLiteral) (left)).getValue() % ((IntLiteral) (left)).getValue();
                } else {
                    throw new UnsupportedOperationException("Unsupported operation: " + getOperatorName()); // cette ligne ne devrait jamais être appelée
                } 
                return new IntLiteral(newValue);
            }

            if (getType().isFloat()){
                float newValue;
                float leftValue;
                float rightValue;
                if (left.getType().isInt()){
                    leftValue = (float) ((IntLiteral) (left)).getValue();
                } else {
                    leftValue = ((FloatLiteral) (left)).getValue();
                }

                if (right.getType().isInt()){
                    rightValue = (float) ((IntLiteral) (right)).getValue();
                } else {
                    rightValue = ((FloatLiteral) (right)).getValue();
                }
                
                if (getOperatorName().equals("+")){
                    newValue = leftValue + rightValue;
                } else if (getOperatorName().equals("-")){
                    newValue = leftValue - rightValue;
                } else if (getOperatorName().equals("*")){
                    newValue = leftValue * rightValue;
                } else if (getOperatorName().equals("/")){
                    newValue = leftValue / rightValue;
                } else if (getOperatorName().equals("%")){
                    newValue = leftValue % rightValue;
                } else {
                    throw new UnsupportedOperationException("Unsupported operation: " + getOperatorName()); // cette ligne ne devrait jamais être appelée
                } 
                return new FloatLiteral(newValue);
            }
        }
        return this;
    }
}
