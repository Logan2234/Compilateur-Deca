package fr.ensimag.ima.pseudocode;

import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.instructions.LOAD;

/**
 * IMA instruction.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public abstract class Instruction {
    String getName() {
        return this.getClass().getSimpleName();
    }
    abstract void displayOperands(PrintStream s);
    void display(PrintStream s) {
        s.print(getName());
        displayOperands(s);
    }

    public boolean isLOAD() {
        return false;
    }

    public LOAD asLOAD() {
        return null;
    }

    public abstract boolean alterRegister(int regNum);


}
