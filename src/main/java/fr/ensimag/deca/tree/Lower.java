package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.SLT;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Lower extends AbstractOpIneq {

    public Lower(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "<";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal) {
        // sub the two values, put the strictly less than flag in register
        compiler.addInstruction(new SUB(dVal, register));
        compiler.addInstruction(new SLT(register));
    }

    @Override
<<<<<<< HEAD
    public boolean factorised() {
        return false;//TODO
    }
=======
    public boolean collapse() {
        return getRightOperand().collapse() || getLeftOperand().collapse();
    }

    @Override
    public Boolean collapseBool() {
        if(getRightOperand().getType().isInt()) {
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
                return rightCollapsedValue < leftCollapsedValue;
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
                return rightCollapsedValue < leftCollapsedValue;
            }
        }
        return null;
    }

>>>>>>> feature/optim
}
