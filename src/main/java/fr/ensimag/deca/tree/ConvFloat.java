package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) {
        setType(compiler.environmentType.FLOAT);
        return compiler.environmentType.FLOAT;
    }

    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

    @Override
    public void codeGenUnExpr(DecacCompiler compiler, GPRegister resultRegister) {
        compiler.addInstruction(new FLOAT(resultRegister, resultRegister));
    }

    public boolean collapse() {
        return getOperand().collapse();
    }

    @Override
    public Float collapseFloat() {
        Integer collapsedValue = getOperand().collapseInt();
        if(collapsedValue != null && getOperand().collapsable()) {
            FloatLiteral newFLoat = new FloatLiteral(collapsedValue);
            newFLoat.setType(getType());
            setOperand(newFLoat);
        }
        if(collapsedValue != null) {
            return (float)collapsedValue;
        }
        return null;
    }
}
