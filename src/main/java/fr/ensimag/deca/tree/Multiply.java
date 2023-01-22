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
        // we can set it to be replaced with a shift after compilation.
        MUL mulInst = new MUL(dVal, register);
        if(shiftReplacable) {
            mulInst.setShiftReplacable();
        }
        compiler.addInstruction(mulInst);
        // check overflow
        if (getType().isFloat() && compiler.getCompilerOptions().getRunTestChecks()) {
            OpOverflowErr error = new OpOverflowErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
        }
    }

    private boolean shiftReplacable = false;

    public void setShiftReplacable() {
        shiftReplacable = true;
    }

    public boolean factorised(DecacCompiler compiler) {
        if (leftOperand.isLiteral() ^ rightOperand.isLiteral()) {
            if (leftOperand.isLiteral() && leftOperand.getType().isInt()) {
                int value;
                try {
                    value = ((IntLiteral)((UnaryMinus)leftOperand).getOperand()).getValue();
                } catch (ClassCastException c) {
                    value = ((IntLiteral) leftOperand).getValue();
                }
                String[] nbbinaire = Integer.toBinaryString(value).split("");
                int[] binaire = new int[nbbinaire.length];
                int poids = 0;
                for (int i = 0; i < nbbinaire.length; i++) {
                    binaire[i] = Integer.parseInt(nbbinaire[nbbinaire.length - 1 - i]);
                    if (binaire[i] == 1) 
                        poids++;   
                }
                return (poids<=5);
            }
            if (leftOperand.isLiteral() && leftOperand.getType().isFloat())
                return false; // On ne developpe pas les floats en somme car il faudra dans tous les cas faire
                              // des additions donc pas optim
            if (rightOperand.getType().isInt()) {
                int value = ((IntLiteral) rightOperand).getValue();
                String[] nbbinaire = Integer.toBinaryString(value).split("");
                int[] binaire = new int[nbbinaire.length];
                int poids = 0;
                for (int i = 0; i < nbbinaire.length; i++) {
                    binaire[i] = Integer.parseInt(nbbinaire[nbbinaire.length - 1 - i]);
                    if (binaire[i] == 1) 
                        poids++;   
                }
                return (poids<=5);
            }
        }
        return super.factorised(compiler);
    }

    public AbstractInst factoInst(DecacCompiler compiler) {
        ListInst list = new ListInst();

        // Letter * number
        if (rightOperand.isLiteral() && rightOperand.getType().isInt())
            try {
                int value = ((IntLiteral) (((UnaryMinus) rightOperand).getOperand())).getValue();
                AbstractExpr rightBisOperand = new IntLiteral(value);
                shift(compiler, rightBisOperand, leftOperand, list);
                ((UnaryMinus)rightOperand).setOperand((AbstractExpr)list.getList().get(list.size()-1));
                list.add(rightOperand);
                ((UnaryMinus) list.getList().get(list.size()-1)).setType(compiler.environmentType.INT);
            } catch (ClassCastException e) {
                shift(compiler, leftOperand, rightOperand, list);
                ((AbstractOpArith) list.getList().get(list.size()-1)).setType(compiler.environmentType.INT);
            }

        // number * Letter
        else if (leftOperand.isLiteral() && rightOperand.getType().isInt()){
            try {
                int value = ((IntLiteral) (((UnaryMinus) leftOperand).getOperand())).getValue();
                AbstractExpr leftBisOperand = new IntLiteral(value);
                shift(compiler, rightOperand, leftBisOperand, list);
                ((UnaryMinus)leftOperand).setOperand((AbstractExpr)list.getList().get(list.size()-1));
                list.add(leftOperand);
                ((UnaryMinus) list.getList().get(list.size()-1)).setType(compiler.environmentType.INT);
            } catch (ClassCastException e) {
                shift(compiler, rightOperand, leftOperand, list);
                ((AbstractOpArith) list.getList().get(list.size()-1)).setType(compiler.environmentType.INT);
            }
        }
        else {
            return super.factoInst(compiler);
        }
        return list.getList().get(list.size()-1);
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
