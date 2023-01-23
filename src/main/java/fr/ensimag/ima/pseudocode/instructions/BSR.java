package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.UnaryInstruction;

/**
 * @author Ensimag
 * @date 01/01/2023
 */
public class BSR extends UnaryInstruction {

    public BSR(DVal operand) {
        super(operand);
    }
    
    public BSR(Label target) {
        super(new LabelOperand(target));
    }

    @Override
    public boolean alterRegister(int regNum) {
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
