package fr.ensimag.ima.pseudocode;

/**
 * Operand of an IMA Instruction.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public abstract class Operand {
    @Override
    public abstract String toString();

    public boolean isIntImmediate() {
        return false;
    }

    public ImmediateInteger asIntImmediate() {
        return null;
    }

    public boolean isFloatImmediate() {
        return false;
    }

    public ImmediateFloat asFloatImmediate() {
        return null;
    }

    public boolean isGpRegister() {
        return false;
    }

    public GPRegister asGpRegister() {
        return null;
    }

    public boolean isRegOffset() {
        return false;
    }

    public RegisterOffset asRegOffset() {
        return null;
    }
}
