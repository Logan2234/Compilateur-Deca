package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.UnaryInstruction;

/**
 * @author Ensimag
 * @date 01/01/2023
 */
public class PUSH extends UnaryInstruction {
    public PUSH(Register op1) {
        super(op1);
    }

    @Override
    public boolean alterRegister(int regNum) {
        return false;
    }

    @Override
    public boolean usesRegister(int regNum) {
        if(getOperand().isGpRegister()) {
            return getOperand().asGpRegister().getNumber() == regNum;
        }
        return false;
    }
}
