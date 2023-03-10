package fr.ensimag.deca.tree;

import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SNE;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dval) {
        // the sum should not be zero, that's it
        compiler.addInstruction(new ADD(dval, register));
        compiler.addInstruction(new SNE(register));
    }
    
    public CollapseResult<CollapseValue> collapseBinExpr() {
        CollapseResult<CollapseValue> leftResult = getLeftOperand().collapseExpr();
        CollapseResult<CollapseValue> rightResult = getRightOperand().collapseExpr();
        if(leftResult.getResult().isBool() && rightResult.getResult().isBool()) {
            return new CollapseResult<CollapseValue>(new CollapseValue(leftResult.getResult().asBool() || rightResult.getResult().asBool()), true);
        }
        else {
            return new CollapseResult<CollapseValue>(new CollapseValue(), leftResult.couldCollapse() || rightResult.couldCollapse());
        }
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new Or(this.leftOperand.substitute(substitutionTable), this.rightOperand.substitute(substitutionTable));
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

    @Override
    public void lazyEvaluation(DecacCompiler compiler, GPRegister resultRegister, Label toLabel) {
        // and : if the result of register is false, branch to label
        compiler.addInstruction(new CMP(1, resultRegister));
        // if result register contains zero, return false in it 
        compiler.addInstruction(new BEQ(toLabel));
    } 
}
