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
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.getOperand().getType();
        Location loc = this.getLocation();

        if (type != compiler.environmentType.BOOLEAN)
            throw new ContextualError(loc.getFilename() + ":" + loc.getLine() + ":" + loc.getPositionInLine()
                    + ": Un not ne peut être fait qu'avec un booléen (règle 3.37)", loc);

        return type;
    }

    @Override
    protected String getOperatorName() {
        return "!";
    }
}
