package fr.ensimag.ima.pseudocode;

import fr.ensimag.ima.pseudocode.instructions.MUL;

/**
 * Base class for instructions with 2 operands, the first being a
 * DVal, and the second a Register.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class BinaryInstructionDValToReg extends BinaryInstruction {
    public BinaryInstructionDValToReg(DVal op1, GPRegister op2) {
        super(op1, op2);
    }

    public GPRegister getGPRegister() {
        return (GPRegister)getOperand2();
    }

    @Override
    public boolean isDvalToReg() {
        return true;
    }

    @Override
    public BinaryInstructionDValToReg asDvalToReg() {
        return this;
    }

    public void setDvalOp(DVal newDval) {
        setOperand1(newDval);
    }

    /**
     * Try to compute self from what we know of our two values !
     * @return
     */
    public DVal tryComputeSelf() {
        // by default, we can't predict what our result will be !
        return null;
    }

    public boolean isMul() {
        return false;
    }

    public MUL asMul() {
        return null;
    }

    
}
