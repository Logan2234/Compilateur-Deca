package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.OpOverflowErr;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.INT;
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

    public boolean factorised(DecacCompiler compiler) {
        if (leftOperand.isLiteral() ^ rightOperand.isLiteral()) {
            if (leftOperand.isLiteral() && leftOperand.getType().isInt()) {

                return true;
            }
            if (leftOperand.isLiteral() && leftOperand.getType().isFloat())
                return false; // On ne developpe pas les floats en somme car il faudra dans tous les cas faire
                              // des additions donc pas optim
            if (rightOperand.getType().isInt()) {
                return true;
            }
        }
        return false;
    }

    private void shift(DecacCompiler compiler, AbstractExpr left, AbstractExpr right, ListInst listPlus) {
        int value = ((IntLiteral) right).getValue();
        String[] nbbinaire = Integer.toBinaryString(value).split("");
        int[] binaire = new int[nbbinaire.length];
        ListInst list = new ListInst();
        for (int i = 0; i < nbbinaire.length; i++) {
            binaire[i] = Integer.parseInt(nbbinaire[nbbinaire.length - 1 - i]);
            if (binaire[i] == 1) {
                AbstractExpr puissance = new IntLiteral((int) Math.pow(2, i));
                ((IntLiteral) puissance).setType(compiler.environmentType.INT);
                AbstractExpr multiply = new Multiply(left, puissance);
                ((Multiply) multiply).setType(compiler.environmentType.INT);

                list.add(multiply);
            }
        }
        if (list.size() == 1) {
            listPlus.add(list.getList().get(0));
            ;
        } else {
            Plus plus = new Plus((AbstractExpr) list.getList().get(1), (AbstractExpr) list.getList().get(0));
            listPlus.add(plus);
            if (list.size() > 2) {
                for (int i = 2; i < list.size(); i++) {
                    Plus operand = new Plus((AbstractExpr) list.getList().get(i), (AbstractExpr) plus);
                    ((Plus) operand).setType(compiler.environmentType.INT);
                    plus = new Plus((AbstractExpr) list.getList().get(i), (AbstractExpr) plus);
                    ((Plus) plus).setType(compiler.environmentType.INT);
                    listPlus.add(operand);
                }
            }
        }
        // plus.add(list.getList().get(list.size()));
    }

    private void factocarre(DecacCompiler compiler, AbstractExpr leftOperand, AbstractExpr rightOperand,
            ListInst list) {
        AbstractExpr leftbisoperand;
        if (leftOperand.isLiteral()) {
            leftbisoperand = new IntLiteral(((IntLiteral) leftOperand).getValue());
        } else {
            leftbisoperand = new Identifier(((Identifier) leftOperand).getName());
            ((Identifier) leftbisoperand).setDefinition(((Identifier) leftOperand).getDefinition());
        }

        leftbisoperand.setType(leftOperand.getType());
        if (((IntLiteral) rightOperand).getValue() == 2) {
            Plus fin = new Plus(leftOperand, leftbisoperand);
            fin.setType(compiler.environmentType.INT);
            list.add(fin);
        } else {
            AbstractExpr left = new Plus(leftOperand, leftbisoperand);
            ((Plus) left).setType(compiler.environmentType.INT);
            for (int i = 0; i < ((IntLiteral) rightOperand).getValue() - 2; i++) {
                Plus plus = new Plus(leftOperand, left);
                ((Plus) plus).setType(compiler.environmentType.INT);
                left = new Plus(leftOperand, left);
                ((Plus) left).setType(compiler.environmentType.INT);
                list.add(plus);
            }
        }
    }

    public ListInst factoInst(DecacCompiler compiler) {
        ListInst list = new ListInst();

        // Letter * number
        if (rightOperand.isLiteral() && rightOperand.getType().isInt())
            shift(compiler, leftOperand, rightOperand, list);
        // factocarre(compiler, leftOperand, rightOperand, list);
        // number * Letter
        else {
            try {
                int value = ((IntLiteral) (((UnaryMinus) leftOperand).getOperand())).getValue();
                AbstractExpr leftBisOperand = new IntLiteral(value);
                shift(compiler, rightOperand, leftBisOperand, list);
                // new UnaryMinus(list)
                ((UnaryMinus)leftOperand).setOperand((AbstractExpr)list.getList().get(list.size()-1));
                list.add(leftOperand);
                ((UnaryMinus) list.getList().get(list.size()-1)).setType(compiler.environmentType.INT);
            } catch (ClassCastException e) {
                shift(compiler, rightOperand, leftOperand, list);
                ((AbstractOpArith) list.getList().get(list.size()-1)).setType(compiler.environmentType.INT);
            }
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
