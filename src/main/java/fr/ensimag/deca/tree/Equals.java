package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Equals extends AbstractOpExactCmp {

    public Equals(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "==";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal) {
        // sub the two values, put the eq flag in register
        compiler.addInstruction(new SUB(dVal, register));
        compiler.addInstruction(new SEQ(register));
    }

    @Override
    public boolean collapse() {
        return getRightOperand().collapse() || getLeftOperand().collapse();
    }

    @Override
    public Boolean collapseBool() {
        if(getRightOperand().getType().isBoolean()) {
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
                return rightCollapsedValue == leftCollapsedValue;
            }
        }
        else if(getRightOperand().getType().isInt()) {
            Integer rightCollapsedValue = getRightOperand().collapseInt();
            if(rightCollapsedValue != null && getRightOperand().collapsable()) {
                IntLiteral newInt = new IntLiteral(rightCollapsedValue);
                newInt.setType(getType());
                setRightOperand(newInt);
            }
            Integer leftCollapsedValue = getLeftOperand().collapseInt();
            if(leftCollapsedValue != null && getLeftOperand().collapsable()) {
                IntLiteral newInt = new IntLiteral(leftCollapsedValue);
                newInt.setType(getType());
                setLeftOperand(newInt);
            }
            if(rightCollapsedValue != null && leftCollapsedValue != null) {
                return rightCollapsedValue == leftCollapsedValue;
            }
        }
        else if(getRightOperand().getType().isFloat()) {
            Float rightCollapsedValue = getRightOperand().collapseFloat();
            if(rightCollapsedValue != null && getRightOperand().collapsable()) {
                FloatLiteral newFloat = new FloatLiteral(rightCollapsedValue);
                newFloat.setType(getType());
                setRightOperand(newFloat);
            }
            Float leftCollapsedValue = getLeftOperand().collapseFloat();
            if(leftCollapsedValue != null && getLeftOperand().collapsable()) {
                FloatLiteral newFloat = new FloatLiteral(leftCollapsedValue);
                newFloat.setType(getType());
                setLeftOperand(newFloat);
            }
            if(rightCollapsedValue != null && leftCollapsedValue != null) {
                return rightCollapsedValue == leftCollapsedValue;
            }
        }
        return null;
    }


    
    @Override
    public boolean factorised() {
        return false;//TODO
    }
}
