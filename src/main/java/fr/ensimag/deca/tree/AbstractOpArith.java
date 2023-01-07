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
        Type typeLeft = this.getLeftOperand().getType();
        Type typeRight = this.getRightOperand().getType();
        Location loc = this.getLocation();

        if (typeLeft != compiler.environmentType.INT && typeLeft != compiler.environmentType.FLOAT)
            throw new ContextualError(loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                    + ": L'opérande de gauche d'une opération arithmétique doit être un int ou float (règle 3.33)",
                    loc);

        if (typeRight != compiler.environmentType.INT && typeRight != compiler.environmentType.FLOAT)
            throw new ContextualError(loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                    + ": L'opérande de droite d'une opération arithmétique doit être un int ou float (règle 3.33)",
                    loc);

        if (typeLeft == compiler.environmentType.FLOAT || typeLeft == compiler.environmentType.FLOAT)
            return compiler.environmentType.FLOAT;

        return compiler.environmentType.INT;
    }
}
