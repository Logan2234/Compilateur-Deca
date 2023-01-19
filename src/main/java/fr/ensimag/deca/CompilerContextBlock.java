package fr.ensimag.deca;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * What we call a context block is a local environment of code generation.
 * It tests it's own register uses, stack overflow and global base offset.
 */
public class CompilerContextBlock {

    public CompilerContextBlock(int maxRegNumber, boolean globalBase) {
        maxRegNumber = maxRegNumber - 2; // remove R0 and R1
        availableRegisters = new boolean[maxRegNumber];
        usedRegisters = new boolean[maxRegNumber];
        for(int i = 0; i < maxRegNumber; i++) {
            availableRegisters[i] = true;
            usedRegisters[i] = false;
        }
        maxStackUseSize = 0;
        stackUsedSizes = 0;
        LBtakenSpace = 0;
        isGlobalBase = globalBase;
        program = new IMAProgram();
    }

    /**
     * Tells whether we are on the global base or on a local base.
     */
    private boolean isGlobalBase;
    /**
     * All the available registers.
     * When generating code, we can ask what registers are used by previous calls.
     * If null is returned, we can then save a register and restore it.
     */
    private boolean[] availableRegisters;
    /**
     * All the registers that have been used for this context.
     * This allow to keep track of which registers to save / load on method calls. 
     */
    private boolean[] usedRegisters;
    /**
     * Stores the max reached stack size for each context (code block).
     * We have one default context when the program starts, and more context with methods calls.
     * Here, a block, or context, is for main program, ot methods, or classes initialization. 
     */
    private int maxStackUseSize;

    /**
     * Store the current number of variables pushed on the stack.
     */
    private int stackUsedSizes;

    /**
     * Where we write the instructions to.
     */
    private IMAProgram program;

    /**
     * Get a available register.
     * 
     * @return the register we can use. Can be null if none are.
     */
    public GPRegister allocateRegister() {
        for (int i = 0; i < availableRegisters.length; i++) {
            if (availableRegisters[i]) {
                availableRegisters[i] = false;
                usedRegisters[i] = true;
                return GPRegister.getR(i + 2);
            }
        }
        return null;
    }

    /**
     * Set the given register as not used anymore.
     * 
     * @param register
     */
    public void freeRegister(GPRegister register) {
        availableRegisters[register.getNumber() - 2] = true;
    }

    /**
     * Returns all the registers that have been used during this context.
     * @return
     */
    public GPRegister[] getAllContextUsedRegister() {
        int usedRegisterAmount = 0;
        for(boolean registerUsed : usedRegisters) {
            if(registerUsed) {
                usedRegisterAmount ++;
            }
        }
        GPRegister[] result = new GPRegister[usedRegisterAmount];
        int index = 0;
        for(int i = 0; i < usedRegisters.length; i++) {
            if(usedRegisters[i]) {
                result[index] = GPRegister.getR(i + 2);
                index++;
            }
        }
        return result;
    }


    /**
     * Increase the size of the use stack of the block 
     * @param increment how much we want to increment the stack
     */
    public void increaseUsedStack(int increment) {
        stackUsedSizes += increment;
        if(stackUsedSizes > maxStackUseSize) {
            maxStackUseSize = stackUsedSizes;
        }
    }

    /**
     * Get the max size of the stack.
     */
    public int getMaxStackUse() {
        return maxStackUseSize;
    }

    /**
     * The amount of values on the local base of this context.
     */
    public int LBtakenSpace;

    /**
     * Ask for a lb register offset to declare variables.
     * @return the lb register offset.
     */
    public RegisterOffset getNextStackSpace() {
        LBtakenSpace += 1;
        return new RegisterOffset(LBtakenSpace, isGlobalBase ? Register.GB : Register.LB);
    }

    /**
     * Read the stack pos withour incrementing it.
     * @return the lb register offset.
     */
    public RegisterOffset readNextStackSpace() {
        return new RegisterOffset(LBtakenSpace + 1, isGlobalBase ? Register.GB : Register.LB);
    }

    /**
     * Increase the taken space on the lb local base stack.
     * Use primarly when generating method tables.
     * @param amount
     */
    public void occupyLBSPace(int amount) {
        LBtakenSpace += amount;
    }

    public IMAProgram getProgram() {
        return program;
    }

}
