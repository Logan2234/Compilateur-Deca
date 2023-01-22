package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SNE;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class NotEquals extends AbstractOpExactCmp {

    public NotEquals(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "!=";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal) {
        // sub the two values, put the neq flag in register
        compiler.addInstruction(new CMP(dVal, register));
        compiler.addInstruction(new SNE(register));
    }

    @Override
    public CollapseResult<CollapseValue> collapseBinExpr() {
        CollapseResult<CollapseValue> leftResult = getLeftOperand().collapseExpr();
        CollapseResult<CollapseValue> rightResult = getRightOperand().collapseExpr();
        if(leftResult.getResult().isFloat() && rightResult.getResult().isFloat()) {
            return new CollapseResult<CollapseValue>(new CollapseValue(leftResult.getResult().asFloat() != rightResult.getResult().asFloat()), true);
        }
        else if(leftResult.getResult().isInt() && rightResult.getResult().isInt()) {
            return new CollapseResult<CollapseValue>(new CollapseValue(leftResult.getResult().asInt() != rightResult.getResult().asInt()), true);
        }
        else if(leftResult.getResult().isBool() && rightResult.getResult().isBool()) {
            return new CollapseResult<CollapseValue>(new CollapseValue(leftResult.getResult().asBool() != rightResult.getResult().asBool()), true);
        }
        else {
            return new CollapseResult<CollapseValue>(new CollapseValue(), leftResult.couldCollapse() || rightResult.couldCollapse());
        }
    }

}
