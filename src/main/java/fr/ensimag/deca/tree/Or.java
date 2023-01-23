package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.ADD;
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
            return rightCollapsedValue || leftCollapsedValue;
        }
        return null;
    }
}
