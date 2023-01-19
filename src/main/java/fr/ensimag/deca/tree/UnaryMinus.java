package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.OPP;
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
            throw new ContextualError("A unary minus is only followed by an int or a float (rule 3.37)", loc);

        // Ajout du décor
        this.setType(type);
        return type;
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    public void codeGenUnExpr(DecacCompiler compiler, GPRegister resulRegister) {
        // put opposite of self in the register
        compiler.addInstruction(new OPP(resulRegister, resulRegister));
    }

    @Override
    public boolean factorised() {
        return false;//TODO
    }
}
