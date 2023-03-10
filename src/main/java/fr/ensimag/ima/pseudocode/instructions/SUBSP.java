package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.UnaryInstructionImmInt;

/**
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class SUBSP extends UnaryInstructionImmInt {

    public SUBSP(ImmediateInteger operand) {
        super(operand);
    }

    public SUBSP(int i) {
        super(i);
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
