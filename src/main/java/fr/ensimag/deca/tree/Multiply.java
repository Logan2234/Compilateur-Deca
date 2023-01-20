package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.OpOverflowErr;
import fr.ensimag.deca.context.Type;
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
        if (getType().isFloat() && compiler.getCompilerOptions().getRunTestChecks()) {
            OpOverflowErr error = new OpOverflowErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
        }
    }

    public boolean factorised() {
        if (leftOperand.isLiteral() ^ rightOperand.isLiteral()) {
            if (leftOperand.isLiteral() && leftOperand.getType().isInt()) {
                if (((IntLiteral) leftOperand).getValue() < 10)
                    return true;
                return false;
            }
            if (leftOperand.isLiteral() && leftOperand.getType().isFloat())
                return false; // On ne developpe pas les floats en somme car il faudra dans tous les cas faire
                              // des additions donc pas optim
            if (rightOperand.getType().isInt())

                if (((IntLiteral) rightOperand).getValue() < 10)
                    return true;
        }
        return false;
    }

    public ListInst factoInst() {
        // Letter * number
        if (rightOperand.isLiteral() && rightOperand.getType().isInt()) {
            ListInst list = new ListInst();
            
            AbstractExpr leftbisoperand;
            
            if (leftOperand.isLiteral()) {
                leftbisoperand = new IntLiteral(((IntLiteral) leftOperand).getValue());
            } else {
                leftbisoperand = new Identifier(((Identifier) leftOperand).getName());
            }

            if (((IntLiteral) rightOperand).getValue() == 2) {
                leftbisoperand.setType(leftOperand.getType());
                list.add(new Plus(leftOperand, leftbisoperand));
                return list;
            }

            AbstractExpr left = new Plus(leftOperand, leftbisoperand);
            for (int i = 0; i < ((IntLiteral) rightOperand).getValue() - 2; i++) {
                list.add(new Plus(leftOperand, left));
                left = new Plus(leftOperand, left);
            }
            return list;
        }

        // Number * letter
        ListInst list = new ListInst();

        AbstractExpr rightbisoperand;
            
        if (rightOperand.isLiteral()) {
            rightbisoperand = new IntLiteral(((IntLiteral) rightOperand).getValue());
        } else {
            rightbisoperand = new Identifier(((Identifier) rightOperand).getName());
        }

        if (((IntLiteral) leftOperand).getValue() == 2) {
            list.add(new Plus(rightOperand, rightbisoperand));
            return list;
        }
        AbstractExpr left = new Plus(rightOperand, rightbisoperand);
        for (int i = 0; i < ((IntLiteral) leftOperand).getValue() - 2; i++) {
            list.add(new Plus(rightOperand, left));
            left = new Plus(rightOperand, left);
        }
        return list;
    }

    public boolean collapse() {
        return getLeftOperand().collapse() || getRightOperand().collapse();
    }

    @Override
    public Float collapseFloat() {
        Float rightCollapsedValue = getRightOperand().collapseFloat();
        if (rightCollapsedValue != null && getRightOperand().collapsable()) {
            FloatLiteral newFloat = new FloatLiteral(rightCollapsedValue);
            newFloat.setType(getType());
            setRightOperand(newFloat);
        }
        Float leftCollapsedValue = getLeftOperand().collapseFloat();
        if (leftCollapsedValue != null && getLeftOperand().collapsable()) {
            FloatLiteral newFloat = new FloatLiteral(leftCollapsedValue);
            newFloat.setType(getType());
            setLeftOperand(newFloat);
        }
        if (rightCollapsedValue != null && leftCollapsedValue != null) {
            return rightCollapsedValue * leftCollapsedValue;
        }
        return null;
    }

    @Override
    public Integer collapseInt() {
        Integer rightCollapsedValue = getRightOperand().collapseInt();
        if (rightCollapsedValue != null && getRightOperand().collapsable()) {
            IntLiteral newInt = new IntLiteral(rightCollapsedValue);
            newInt.setType(getType());
            setRightOperand(newInt);
        }
        Integer leftCollapsedValue = getLeftOperand().collapseInt();
        if (leftCollapsedValue != null && getLeftOperand().collapsable()) {
            IntLiteral newInt = new IntLiteral(leftCollapsedValue);
            newInt.setType(getType());
            setLeftOperand(newInt);
        }
        if (rightCollapsedValue != null && leftCollapsedValue != null) {
            return rightCollapsedValue * leftCollapsedValue;
        }
        return null;
    }

}
