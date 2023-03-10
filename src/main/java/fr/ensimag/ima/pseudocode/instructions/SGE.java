package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.UnaryInstructionToReg;

/**
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class SGE extends UnaryInstructionToReg {

    public SGE(GPRegister op) {
        super(op);
    }

    @Override
    public boolean usesRegister(int regNum) {
        return false;
    }

}
