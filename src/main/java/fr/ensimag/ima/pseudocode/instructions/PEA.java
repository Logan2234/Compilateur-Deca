package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.UnaryInstruction;

/**
 * @author Ensimag
 * @date 01/01/2023
 */
public class PEA extends UnaryInstruction {

    public PEA(DAddr operand) {
        super(operand);
    }

    @Override
    public boolean alterRegister(int regNum) {
        return false;
    }

    @Override
    public boolean usesRegister(int regNum) {
        return false;
    }

}
