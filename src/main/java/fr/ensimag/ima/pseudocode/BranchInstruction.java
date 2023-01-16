package fr.ensimag.ima.pseudocode;

/**
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class BranchInstruction extends UnaryInstruction {

    public BranchInstruction(Label op) {
        super(op);
    }

    @Override
    public boolean alterRegister(int _regNum) {
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
