package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.NullaryInstruction;

/**
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class WFLOAT extends NullaryInstruction {
    @Override
    public boolean usesRegister(int regNum) {
        return regNum == 1;
    }
}
