package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SHR;
import fr.ensimag.ima.pseudocode.instructions.SNE;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dval) {
        // sum both bool as int and shift the result, should be 1
        compiler.addInstruction(new ADD(dval, register));
        compiler.addInstruction(new SHR(register)); // raise flags for CMP #0, val
        compiler.addInstruction(new SNE(register)); // so true if neq (false if eq)
    }

    @Override
    public void lazyEvaluation(DecacCompiler compiler, GPRegister resultRegister, Label toLabel) {
        // and : if the result of register is false, branch to label
        compiler.addInstruction(new CMP(0, resultRegister));
        // if result register contains zero, return false in it 
        compiler.addInstruction(new BEQ(toLabel));
    } 
}
