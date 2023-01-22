package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.INT;

import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ParamDefinition;

/**
 * Conversion of a float into an int. Used for implicit conversions.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class ConvInt extends AbstractUnaryExpr {
    public ConvInt(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) {
        setType(compiler.environmentType.INT);
        return compiler.environmentType.INT;
    }

    @Override
    protected String getOperatorName() {
        return "/* conv int */";
    }

    @Override
    public void codeGenUnExpr(DecacCompiler compiler, GPRegister resultRegister) {
        compiler.addInstruction(new INT(resultRegister, resultRegister));
    }

    @Override
    public CollapseResult<CollapseValue> collapseUnExpr() {
        CollapseResult<CollapseValue> result = getOperand().collapseExpr();
        if(result.getResult().isFloat()) {
            return new CollapseResult<CollapseValue>(new CollapseValue((int)result.getResult().asFloat()), true);
        }
        else {
            return new CollapseResult<CollapseValue>(new CollapseValue(), result.couldCollapse());
        }
    }
    
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new ConvInt(this.operand.substitute(substitutionTable));
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

}
