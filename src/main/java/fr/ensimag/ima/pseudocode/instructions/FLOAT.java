package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.BinaryInstructionDValToReg;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;

/**
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class FLOAT extends BinaryInstructionDValToReg {

    public FLOAT(DVal op1, GPRegister op2) {
        super(op1, op2);
    }

    @Override
    public boolean usesRegister(int regNum) {
        if(getOperand1().isGpRegister()) {
            return getOperand1().asGpRegister().getNumber() == regNum;
        }
        return false;
    }

    @Override
    public DVal tryComputeSelf() {
        if(getOperand1().isIntImmediate()) {
            return new ImmediateFloat(getOperand1().asIntImmediate().getValue());
        }
        else {
            return null;
        }
    }

}
