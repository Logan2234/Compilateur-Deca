package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.OPP;

import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ParamDefinition;

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
    public CollapseResult<CollapseValue> collapseUnExpr() {
        CollapseResult<CollapseValue> result = getOperand().collapseExpr();
        if(getType().isInt() && result.getResult().isInt()) {
            return new CollapseResult<CollapseValue>(new CollapseValue(- result.getResult().asInt()), true);
        }
        else if(getType().isFloat() && result.getResult().isFloat()) {
            return new CollapseResult<CollapseValue>(new CollapseValue(- result.getResult().asFloat()), true);
        }
        else {
            return new CollapseResult<CollapseValue>(new CollapseValue(), result.couldCollapse());
        }
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new UnaryMinus(this.operand.substitute(substitutionTable));
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

}
