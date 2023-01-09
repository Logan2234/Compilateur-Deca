package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.getOperand().verifyExpr(compiler, localEnv, currentClass);
        Location loc = this.getLocation();

        if (!type.isInt() && !type.isFloat())
            throw new ContextualError("Un moins unaire ne peut se faire qu'avec un int ou un float (règle 3.37)", loc);

        // Ajout du décor
        this.setType(type);
        return type;
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }

}
