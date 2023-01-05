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
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type typeLeft = this.getLeftOperand().getType();
        Type typeRight = this.getRightOperand().getType();
        if (typeLeft != compiler.environmentType.INT && typeLeft != compiler.environmentType.FLOAT){
            Location loc = this.getLeftOperand().getLocation();
            throw new ContextualError(
                    loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                                + ": L'argument de gauche d'une opération athmétique doit être un int ou float",//
                        loc);
        }
        if (typeRight != compiler.environmentType.INT && typeRight != compiler.environmentType.FLOAT){
            Location loc = this.getRightOperand().getLocation();
            throw new ContextualError(
                    loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                                + ": L'argument de droite d'une opération athmétique doit être un int ou float",//
                        loc);
        }
        return compiler.environmentType.BOOLEAN;
    }    


}
