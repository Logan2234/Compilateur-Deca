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
                        "L'opérande de droite d'une opération de comparaison avec un booléen doit être un booléen (règle 3.33)",
                        loc);
            }
            return typeLeft;
        }

        // TODO: Il manque le cas ou on veut comparer T1 et / ou T2 est null ou
        // type_class(_)

        if (!typeLeft.isInt() && !typeLeft.isFloat())
            throw new ContextualError(
                    "L'opérande de gauche d'une opération de comparaison doit être un int ou float (règle 3.33)", loc);

        if (!typeRight.isInt() && !typeRight.isFloat())
            throw new ContextualError(
                    "L'opérande de droite d'une opération comparaison doit être un int ou float (règle 3.33)", loc);

        // Ajout du décor
        this.setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

}
