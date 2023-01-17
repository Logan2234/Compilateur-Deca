package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.OpOverflowErr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.MUL;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "*";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal) {
        // mult
        compiler.addInstruction(new MUL(dVal, register));
        // check overflow
        if(getType().isFloat() && compiler.getCompilerOptions().getRunTestChecks()) {
            OpOverflowErr error = new OpOverflowErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
        }
    }

    @Override
    public boolean collapse() {
        return getLeftOperand().collapse() || getRightOperand().collapse();
    }

    @Override
    public Float collapseFloat() {
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
            return rightCollapsedValue * leftCollapsedValue;
        }
        return null;
    }

    @Override
    public Integer collapseInt() {
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
            return rightCollapsedValue * leftCollapsedValue;
        }
        return null;
    }

    @Override
    public boolean irrelevant() {
        return getLeftOperand().irrelevant() || getRightOperand().irrelevant();
    }

    @Override
    public Float irrelevantFloat() {
        Float rightIrrelevantdValue = getRightOperand().irrelevantFloat();
        if(rightIrrelevantdValue != null && getRightOperand().irrelevantable()) {
            FloatLiteral newFloat = new FloatLiteral(rightIrrelevantdValue);
            newFloat.setType(getType());
            setRightOperand(newFloat);
        }
        Float leftIrrelevantValue = getLeftOperand().irrelevantFloat();
        if(leftIrrelevantValue != null && getLeftOperand().irrelevantable()) {
            FloatLiteral newFloat = new FloatLiteral(leftIrrelevantValue);
            newFloat.setType(getType());
            setLeftOperand(newFloat);
        }
        if(rightIrrelevantdValue != null && leftIrrelevantValue != null) {
            return rightIrrelevantdValue * leftIrrelevantValue;
        }
        return null;
    }

    @Override
    public Integer irrelevantInt() {
        Integer rightIrrelevantdValue = getRightOperand().irrelevantInt();
        if(rightIrrelevantdValue != null && getRightOperand().irrelevantable()) {
            IntLiteral newInt = new IntLiteral(rightIrrelevantdValue);
            newInt.setType(getType());
            setRightOperand(newInt);
        }
        Integer leftIrrelevantValue = getLeftOperand().irrelevantInt();
        if(leftIrrelevantValue != null && getLeftOperand().irrelevantable()) {
            IntLiteral newInt = new IntLiteral(leftIrrelevantValue);
            newInt.setType(getType());
            setLeftOperand(newInt);
        }
        if(rightIrrelevantdValue != null && leftIrrelevantValue != null) {
            return rightIrrelevantdValue * leftIrrelevantValue;
        }
        return null;
    }

}
