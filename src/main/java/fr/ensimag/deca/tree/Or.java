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
    public boolean factorised() {
        return false;//TODO
    }
}
