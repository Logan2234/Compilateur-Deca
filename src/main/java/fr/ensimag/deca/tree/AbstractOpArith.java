package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
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
                    "L'opérande de gauche d'une opération arithmétique doit être un int ou float (règle 3.33)", loc);

        if (!typeRight.isInt() && !typeRight.isFloat())
            throw new ContextualError(
                    "L'opérande de droite d'une opération arithmétique doit être un int ou float (règle 3.33)", loc);

        // Ajout du décor et renvoie du type
        if (typeLeft.isFloat() || typeRight.isFloat()) {
            this.setType(typeLeft);
            return typeLeft;
        }

        // Ajout du décor
        this.setType(typeLeft);
        return typeLeft;
    }
}
