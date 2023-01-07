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
        Type typeLeft = this.getLeftOperand().getType();
        Type typeRight = this.getRightOperand().getType();
        Location loc = this.getLocation();

        if (typeLeft == compiler.environmentType.BOOLEAN) {
            if (typeRight != compiler.environmentType.BOOLEAN) {
                throw new ContextualError(loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                        + ": L'opérande de droite d'une opération de comparaison avec un booléen doit être un booléen (règle 3.33)",
                        loc);
            }
            return compiler.environmentType.BOOLEAN;
        }

        // TODO: Il manque le cas ou on veut comparer T1 et / ou T2 est null ou type_class(_)

        if (typeLeft != compiler.environmentType.INT && typeLeft != compiler.environmentType.FLOAT)
            throw new ContextualError(loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                    + ": L'opérande de gauche d'une opération de comparaison doit être un int ou float (règle 3.33)",
                    loc);

        if (typeRight != compiler.environmentType.INT && typeRight != compiler.environmentType.FLOAT)
            throw new ContextualError(loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                    + ": L'opérande de droite d'une opération comparaison doit être un int ou float (règle 3.33)", loc);

        return compiler.environmentType.BOOLEAN;
    }

}
