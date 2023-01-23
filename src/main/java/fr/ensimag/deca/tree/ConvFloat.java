package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;

import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ParamDefinition;

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

    @Override
    public CollapseResult<CollapseValue> collapseUnExpr() {
        CollapseResult<CollapseValue> result = getOperand().collapseExpr();
        if(result.getResult().isInt()) {
            return new CollapseResult<CollapseValue>(new CollapseValue((float)result.getResult().asInt()), true);
        }
        else {
            return new CollapseResult<CollapseValue>(new CollapseValue(), result.couldCollapse());
        }
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new ConvFloat(this.operand.substitute(substitutionTable));
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }
}
