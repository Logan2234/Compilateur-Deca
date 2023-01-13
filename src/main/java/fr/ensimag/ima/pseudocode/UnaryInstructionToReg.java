package fr.ensimag.ima.pseudocode;

/**
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class UnaryInstructionToReg extends UnaryInstruction {

    public UnaryInstructionToReg(GPRegister op) {
        super(op);
    }

    @Override
    public boolean alterRegister(int regNum) {
        return getOperand().asGpRegister().getNumber() == regNum;
    }

    @Override
    public boolean usesRegister(int regNum) {
        return getOperand().asGpRegister().getNumber() == regNum;
    }

}
