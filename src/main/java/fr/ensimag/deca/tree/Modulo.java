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
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeLeft = this.getLeftOperand().getType();
        Type typeRight = this.getRightOperand().getType();
        Location loc = this.getLocation();

        if (!typeLeft.isInt() || !typeRight.isInt())
            throw new ContextualError("A modulo is only between 2 int (rule 3.33)", loc);
        
        // Ajout du décor
        this.setType(typeLeft);
        return typeLeft;
    }

    @Override
    protected String getOperatorName() {
        return "%";
    }

}
