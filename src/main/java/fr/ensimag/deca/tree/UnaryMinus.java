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
        Type type = getOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!type.isInt() && !type.isFloat())
            throw new ContextualError("A unary minus is only followed by an int or a float (rule 3.37)", getLocation());

        // Ajout du décor
        setType(type);
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
    public boolean collapse() {
        return getOperand().collapse();
    }

    @Override
    public Integer collapseInt() {
        Integer collapsedValue = getOperand().collapseInt();
        if(collapsedValue != null) {
            Type oldType = getOperand().getType();
            IntLiteral newInt = new IntLiteral(collapsedValue);
            newInt.setType(oldType);
            setOperand(newInt);
            return -collapsedValue;
        }
        return null;
    }

    @Override
    public Float collapseFloat() {
        Float collapsedValue = getOperand().collapseFloat();
        if(collapsedValue != null) {
            Type oldType = getOperand().getType();
            FloatLiteral newInt = new FloatLiteral(collapsedValue);
            newInt.setType(oldType);
            setOperand(newInt);
            return -collapsedValue;
        }
        return null;
    }

}
