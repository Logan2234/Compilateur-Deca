package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.BinaryInstructionDValToReg;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;

/**
 * @author Ensimag
 * @date 01/01/2023
 */
public class LOAD extends BinaryInstructionDValToReg {

    public LOAD(DVal op1, GPRegister op2) {
        super(op1, op2);
    }

    public LOAD(int i, GPRegister r) {
        this(new ImmediateInteger(i), r);
    }

    public LOAD(float f, GPRegister r) {
        this(new ImmediateFloat(f), r);
    }

    @Override
    public boolean isLOAD() {
        return true;
    }

    @Override
    public LOAD asLOAD() {
        return this;
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
        // load can predict it's result if it has an immediate
        if(getOperand1().isIntImmediate()) {
            return getOperand1().asIntImmediate();
        }
        else if(getOperand1().isFloatImmediate()) {
            return getOperand1().asFloatImmediate();
        }
        else {
            return null;
        }
    }

}
