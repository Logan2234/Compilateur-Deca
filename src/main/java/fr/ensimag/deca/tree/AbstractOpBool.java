package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Opération binaire Boolean and, or
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type typeLeft = this.getLeftOperand().getType();
        Type typeRight = this.getRightOperand().getType();
        if (typeLeft != compiler.environmentType.BOOLEAN && typeRight != compiler.environmentType.BOOLEAN){
            Location loc = this.getLeftOperand().getLocation();
            throw new ContextualError(
                    loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                                + ": Les arguments doivent être des booleans pour faire les opérations booleans",//
                        loc);
        }
        return compiler.environmentType.BOOLEAN;
    }

}
