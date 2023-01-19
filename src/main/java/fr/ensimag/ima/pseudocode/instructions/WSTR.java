package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.UnaryInstruction;

/**
 * @author Ensimag
 * @date 01/01/2023
 */
public class WSTR extends UnaryInstruction {
    public WSTR(ImmediateString op) {
        super(op);
    }
    
    public WSTR(String message) {
        super(new ImmediateString(message));
    }

    @Override
    public boolean alterRegister(int regNum) {
        return false;
    }

    @Override
    public boolean usesRegister(int regNum) {
        return regNum == 1;
    }
    
}
