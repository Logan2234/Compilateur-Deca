package fr.ensimag.deca.tree;

import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.OpOverflowErr;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.context.ParamDefinition;
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
        if (shiftReplacable) {
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

    @Override
    public CollapseResult<CollapseValue> collapseBinExpr() {
        CollapseResult<CollapseValue> leftResult = getLeftOperand().collapseExpr();
        CollapseResult<CollapseValue> rightResult = getRightOperand().collapseExpr();
        if(getType().isFloat() && leftResult.getResult().isFloat() && rightResult.getResult().isFloat()) {
            return new CollapseResult<CollapseValue>(new CollapseValue(leftResult.getResult().asFloat() * rightResult.getResult().asFloat()), true);
        }
        else if(getType().isInt() && leftResult.getResult().isInt() && rightResult.getResult().isInt()) {
            return new CollapseResult<CollapseValue>(new CollapseValue(leftResult.getResult().asInt() * rightResult.getResult().asInt()), true);
        }
        else {
            return new CollapseResult<CollapseValue>(new CollapseValue(), leftResult.couldCollapse() || rightResult.couldCollapse());
        }
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new Multiply(this.leftOperand.substitute(substitutionTable), this.rightOperand.substitute(substitutionTable));
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

    private boolean shiftReplacable = false;

    public void setShiftReplacable() {
        shiftReplacable = true;
    }

    public boolean isSplitable(DecacCompiler compiler) {
        if (leftOperand.isLiteral() ^ rightOperand.isLiteral()) {
            int poids = 0;
            int value;
            if (leftOperand.isLiteral() && leftOperand.getType().isInt()) {
                try {
                    value = ((IntLiteral) ((UnaryMinus) leftOperand).getOperand()).getValue();
                } catch (ClassCastException c) {
                    value = ((IntLiteral) leftOperand).getValue();
                }
            } else if (leftOperand.isLiteral() && !leftOperand.getType().isInt())
                return false;
            else if (rightOperand.getType().isInt()) {
                try {
                    value = ((IntLiteral) ((UnaryMinus) rightOperand).getOperand()).getValue();
                } catch (ClassCastException c) {
                    value = ((IntLiteral) rightOperand).getValue();
                }
            } else
                return false;
            String[] nbbinaire = Integer.toBinaryString(value).split("");
            List<Integer> binaire = new ArrayList<>();
            for (int i = 0; i < nbbinaire.length; i++) {
                binaire.add(Integer.parseInt(nbbinaire[nbbinaire.length - 1 - i]));
                if (binaire.get(i) == 1)
                poids += i;
            }
            poids += Collections.frequency(binaire, 1) - 1;
            return (poids <= 9);
        }
        return super.isSplitable(compiler);
    }

    public AbstractInst splitCalculus(DecacCompiler compiler) {
        if (!this.isSplitable(compiler))
            return this;

        ListInst list = new ListInst();

        // Letter * number
        if (rightOperand.isLiteral() && rightOperand.getType().isInt())
            try {
                int value = ((IntLiteral) (((UnaryMinus) rightOperand).getOperand())).getValue();
                AbstractExpr rightBisOperand = new IntLiteral(value);
                shift(compiler, leftOperand ,rightBisOperand , list);
                ((UnaryMinus) rightOperand).setOperand((AbstractExpr) list.getList().get(list.size() - 1));
                list.add(rightOperand);
                ((UnaryMinus) list.getList().get(list.size() - 1)).setType(compiler.environmentType.INT);
            } catch (ClassCastException e) {
                shift(compiler, leftOperand, rightOperand, list);
                if (list.size() != 0)
                ((AbstractOpArith) list.getList().get(list.size() - 1)).setType(compiler.environmentType.INT);
            }

        // number * Letter
        else if (leftOperand.isLiteral() && rightOperand.getType().isInt()) {
            try {
                int value = ((IntLiteral) (((UnaryMinus) leftOperand).getOperand())).getValue();
                AbstractExpr leftBisOperand = new IntLiteral(value);
                shift(compiler, rightOperand, leftBisOperand, list);
                ((UnaryMinus) leftOperand).setOperand((AbstractExpr) list.getList().get(list.size() - 1));
                list.add(leftOperand);
                ((UnaryMinus) list.getList().get(list.size() - 1)).setType(compiler.environmentType.INT);
            } catch (ClassCastException e) {
                shift(compiler, rightOperand, leftOperand, list);
                if (list.size() != 0)
                    ((AbstractOpArith) list.getList().get(list.size() - 1)).setType(compiler.environmentType.INT);
            }
        } else {
            return super.splitCalculus(compiler);
        }
        if (list.size() != 0)
        return list.getList().get(list.size() - 1);
        return new IntLiteral(0);
    }
}
