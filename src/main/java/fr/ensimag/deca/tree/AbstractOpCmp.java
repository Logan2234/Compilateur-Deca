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
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Location loc = getLocation();

        if ((getOperatorName() == "==" || getOperatorName() == "!=") && typeLeft.isBoolean()) {
            if (!typeRight.isBoolean())
                throw new ContextualError("A boolean can only be compared to another boolean (rule 3.33)", loc);
            setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }

        if ((getOperatorName() == "==" || getOperatorName() == "!=") && typeLeft.isClassOrNull()) {
            if (!typeRight.isClassOrNull())
                throw new ContextualError("A class (or null) can only be compared to another class (rule 3.33)", loc);
            setType(compiler.environmentType.BOOLEAN);
            return compiler.environmentType.BOOLEAN;
        }

        if (!typeLeft.isInt() && !typeLeft.isFloat())
            throw new ContextualError(
                    "The left operand of a comparison operation has to be an int or a float (rule 3.33)", loc);

        if (!typeRight.isInt() && !typeRight.isFloat())
            throw new ContextualError(
                    "The right operand of a comparison operation has to be an int or a float (rule 3.33)", loc);

        ConvFloat convFloat;
        if (typeLeft.isFloat() && typeRight.isInt()) {
            convFloat = new ConvFloat(getRightOperand());
            convFloat.verifyExpr(compiler, localEnv, currentClass);
            convFloat.setLocation(getRightOperand().getLocation());
            setRightOperand(convFloat);
        }

        else if (typeLeft.isInt() && typeRight.isFloat()) {
            convFloat = new ConvFloat(getLeftOperand());
            convFloat.verifyExpr(compiler, localEnv, currentClass);
            convFloat.setLocation(getLeftOperand().getLocation());
            setLeftOperand(convFloat);
        }

        // Ajout du décor
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }
}
