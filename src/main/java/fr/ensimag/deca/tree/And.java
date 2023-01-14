package fr.ensimag.deca.tree;

import org.apache.log4j.lf5.LogLevelFormatException;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.SHR;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dval) {
        // sum both bool as int and shift the result, should be 1
        compiler.addInstruction(new ADD(dval, register));
        compiler.addInstruction(new SHR(register));
        compiler.addInstruction(new SUB(new ImmediateInteger(1), register));
        compiler.addInstruction(new SEQ(register));
    }

    @Override
    public boolean collapse() {
        return getRightOperand().collapse() || getLeftOperand().collapse();
    }

    @Override
    public Boolean collapseBool() {
        Boolean rightCollapsedValue = getRightOperand().collapseBool();
        if(rightCollapsedValue != null && getRightOperand().collapsable()) {
            BooleanLiteral newBool = new BooleanLiteral(rightCollapsedValue);
            newBool.setType(getType());
            setRightOperand(newBool);
        }
        Boolean leftCollapsedValue = getLeftOperand().collapseBool();
        if(leftCollapsedValue != null && getLeftOperand().collapsable()) {
            BooleanLiteral newBool = new BooleanLiteral(leftCollapsedValue);
            newBool.setType(getType());
            setLeftOperand(newBool);
        }
        if(rightCollapsedValue != null && leftCollapsedValue != null) {
            return rightCollapsedValue && leftCollapsedValue;
        }
        return null;
    }


}
