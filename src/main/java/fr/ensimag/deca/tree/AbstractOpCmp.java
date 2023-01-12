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

        if (typeLeft.isBoolean()) {
            if (!typeRight.isBoolean()) {
                throw new ContextualError(
                        "The right operand of a boolean operation has to be a boolean (rule 3.33)",
                        loc);
            }
            this.setType(typeLeft);
            return typeLeft;
        }

        // TODO: Il manque le cas ou on veut comparer T1 et / ou T2 est null ou
        // type_class(_)

        if (!typeLeft.isInt() && !typeLeft.isFloat())
            throw new ContextualError(
                    "The left operand of a comparaison operation has to be an int or a float (rule 3.33)", loc);

        if (!typeRight.isInt() && !typeRight.isFloat())
            throw new ContextualError(
                    "The right operand of a comparaison operation has to be an int or a float (rule 3.33)", loc);

        ConvFloat convFloat;
        if (typeLeft.isFloat() && typeRight.isInt()){
            convFloat = new ConvFloat(this.getLeftOperand());
            this.setRightOperand(convFloat);
            convFloat.setType(compiler.environmentType.FLOAT);
        }
        
        else if (typeLeft.isInt() && typeRight.isFloat()){
            convFloat = new ConvFloat(this.getLeftOperand());
            this.setLeftOperand(convFloat);
            convFloat.setType(compiler.environmentType.FLOAT);    
        }

        // Ajout du décor
        this.setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

}
