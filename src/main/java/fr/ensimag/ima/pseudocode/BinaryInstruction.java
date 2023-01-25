package fr.ensimag.ima.pseudocode;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Base class for instructions with 2 operands.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class BinaryInstruction extends Instruction {
    private Operand operand1, operand2;

    public Operand getOperand1() {
        return operand1;
    }

    public Operand getOperand2() {
        return operand2;
    }

    protected void setOperand1(Operand operand) {
        operand1 = operand;
    }

    protected void setOperand2(Operand operand) {
        operand2 = operand;
    }

    @Override
    void displayOperands(PrintStream s) {
        s.print(" ");
        s.print(operand1);
        s.print(", ");
        s.print(operand2);
    }

    protected BinaryInstruction(Operand op1, Operand op2) {
        Validate.notNull(op1);
        Validate.notNull(op2);
        this.operand1 = op1;
        this.operand2 = op2;
    }

    @Override
    public boolean alterRegister(int regNum) {
        if(operand2.isGpRegister()) {
            return operand2.asGpRegister().getNumber() == regNum;
        }
        return false; // should not be called
    }

    @Override
    public boolean usesRegister(int regNum) {
        if(operand1.isGpRegister()) {
            if(operand1.asGpRegister().getNumber() == regNum) {
                return true;
            }
        }
        if(operand2.isGpRegister()) {
            if(operand2.asGpRegister().getNumber() == regNum) {
                return true;
            }
        }
        if(operand1.isRegOffset()) {
            if(operand1.asRegOffset().getRegister() == Register.getR(regNum)) {
                return true;
            }
        }
        if(operand2.isRegOffset()) {
            if(operand1.asRegOffset().getRegister() == Register.getR(regNum)) {
                return true;
            }
        }
        return false;
    }
}
