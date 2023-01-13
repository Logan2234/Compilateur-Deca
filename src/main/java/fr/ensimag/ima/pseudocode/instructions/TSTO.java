package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.UnaryInstructionImmInt;

/**
 * @author Ensimag
 * @date 01/01/2023
 */
public class TSTO extends UnaryInstructionImmInt {
    public TSTO(ImmediateInteger i) {
        super(i);
    }

    public TSTO(int i) {
        super(i);
    }

    @Override
    public boolean alterRegister(int regNum) {
        return false;
    }
}
