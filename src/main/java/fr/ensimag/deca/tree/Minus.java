package fr.ensimag.deca.tree;

import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.OpOverflowErr;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class Minus extends AbstractOpArith {
    public Minus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal) {
        // sub the two values
        compiler.addInstruction(new SUB(dVal, register));
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
            return rightCollapsedValue - leftCollapsedValue;
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
            return rightCollapsedValue - leftCollapsedValue;
        }
        return null;
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new Minus(this.leftOperand.substitute(substitutionTable), this.rightOperand.substitute(substitutionTable));
        res.setLocation(this.getLocation());
        return res;
    }
    
}
