package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.BinaryInstruction;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Register;

/**
 * @author Ensimag
 * @date 01/01/2023
 */
public class STORE extends BinaryInstruction {
    public STORE(Register op1, DAddr op2) {
        super(op1, op2);
    }

    @Override
    public boolean usesRegister(int regNum) {
        if(getOperand1().isGpRegister()) {
            return getOperand1().asGpRegister().getNumber() == regNum;
        }
        return false;
    }
}
