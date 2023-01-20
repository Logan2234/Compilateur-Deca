package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SLT;

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
        compiler.addInstruction(new CMP(dVal, register));
        compiler.addInstruction(new SLT(register));
    }

}
