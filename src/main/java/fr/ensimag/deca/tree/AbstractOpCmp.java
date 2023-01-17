package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeLeft = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Location loc = this.getLocation();

        if ((this.getOperatorName() == "==" || this.getOperatorName() == "!=") && typeLeft.isBoolean()) {
            if (!typeRight.isBoolean())
                throw new ContextualError("A boolean can only be compared to another boolean (rule 3.33)", loc);
            this.setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }

        if ((this.getOperatorName() == "==" || this.getOperatorName() == "!=") && typeLeft.isClassOrNull()) {
            if (!typeRight.isClassOrNull())
                throw new ContextualError("A class (or null) can only be compared to another class (rule 3.33)", loc);
            this.setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }

        if (!typeLeft.isInt() && !typeLeft.isFloat())
            throw new ContextualError(
                    "The left operand of a comparaison operation has to be an int or a float (rule 3.33)", loc);

        if (!typeRight.isInt() && !typeRight.isFloat())
            throw new ContextualError(
                    "The right operand of a comparaison operation has to be an int or a float (rule 3.33)", loc);

        ConvFloat convFloat;
        if (typeLeft.isFloat() && typeRight.isInt()) {
            convFloat = new ConvFloat(this.getRightOperand());
            this.setRightOperand(convFloat);
            convFloat.setType(compiler.environmentType.FLOAT);
        }

        else if (typeLeft.isInt() && typeRight.isFloat()) {
            convFloat = new ConvFloat(this.getLeftOperand());
            this.setLeftOperand(convFloat);
            convFloat.setType(compiler.environmentType.FLOAT);
        }

        // Ajout du décor
        this.setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
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

            if (left.getType().isBoolean()){
                boolean newValue = false; // This might be changed later
                if (getOperatorName().equals("==")){
                    newValue = ((BooleanLiteral) left).getValue() == ((BooleanLiteral) right).getValue();
                } else if (getOperatorName().equals("!=")){
                    newValue = ((BooleanLiteral) left).getValue() != ((BooleanLiteral) right).getValue();
                }
                return new BooleanLiteral(newValue);
            }

            if (left.getType().isInt()){
                boolean newValue;
                if (getOperatorName().equals("<")){
                    newValue = ((IntLiteral) left).getValue() < ((IntLiteral) right).getValue();
                } else if (getOperatorName().equals("<=")){
                    newValue = ((IntLiteral) left).getValue() <= ((IntLiteral) right).getValue();
                } else if (getOperatorName().equals(">")){
                    newValue = ((IntLiteral) left).getValue() > ((IntLiteral) right).getValue();
                } else if (getOperatorName().equals(">=")){
                    newValue = ((IntLiteral) left).getValue() >= ((IntLiteral) right).getValue();
                } else if (getOperatorName().equals("!=")){
                    newValue = ((IntLiteral) left).getValue() != ((IntLiteral) right).getValue();
                } else {
                    newValue = ((IntLiteral) left).getValue() == ((IntLiteral) right).getValue();
                }
                return new BooleanLiteral(newValue);
            }

            if (left.getType().isFloat()){
                boolean newValue;
                if (getOperatorName().equals("<")){
                    newValue = ((FloatLiteral) left).getValue() < ((FloatLiteral) right).getValue();
                } else if (getOperatorName().equals("<=")){
                    newValue = ((FloatLiteral) left).getValue() <= ((FloatLiteral) right).getValue();
                } else if (getOperatorName().equals(">")){
                    newValue = ((FloatLiteral) left).getValue() > ((FloatLiteral) right).getValue();
                } else if (getOperatorName().equals(">=")){
                    newValue = ((FloatLiteral) left).getValue() >= ((FloatLiteral) right).getValue();
                } else if (getOperatorName().equals("!=")){
                    newValue = ((FloatLiteral) left).getValue() != ((FloatLiteral) right).getValue();
                } else {
                    newValue = ((FloatLiteral) left).getValue() == ((FloatLiteral) right).getValue();
                }
                return new BooleanLiteral(newValue);
            }
        }
        return this;
    }

}
