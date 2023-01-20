package fr.ensimag.deca;

import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

/**
 * What we call a context block is a local environment of code generation.
 * It tests it's own register uses, stack overflow and global base offset.
 */
public class CompilerContextBlock {

    public CompilerContextBlock(int maxRegNumber, boolean globalBase) {
        maxRegNumber = maxRegNumber - 2; // remove R0 and R1
        availableRegisters = new int[maxRegNumber];
        usedRegisters = new boolean[maxRegNumber];
        for(int i = 0; i < maxRegNumber; i++) {
            availableRegisters[i] = -1;
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
     * All the available registers. -1 means the register is available, 
     * 0 means it is taken, otherwise n > 0 means it have been pushed n times on the stack.
     */
    private int[] availableRegisters;
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
    public GPRegister allocateRegister(DecacCompiler compiler) {
        for (int i = 0; i < availableRegisters.length; i++) {
            if (availableRegisters[i] < 0) {
                // found a free register !
                availableRegisters[i] += 1;
                usedRegisters[i] = true;
                return GPRegister.getR(i + 2);
            }
        }
        // look for the least currently used register, save it and return it.
        int minUsedReg = 0;
        int minUsedValue = availableRegisters[minUsedReg];
        for (int i = 1; i < availableRegisters.length; i++) {
            if (availableRegisters[i] < minUsedValue) {
                // set new min used reg
                minUsedReg = i;
                minUsedValue = availableRegisters[i];
            }
        }
        // use the min used register !
        GPRegister result = Register.getR(minUsedReg + 2);
        availableRegisters[minUsedReg] += 1;
        compiler.addInstruction(new PUSH(result));
        increaseUsedStack(1);
        return result;
    }

    /**
     * Set the given register as not used anymore.
     * 
     * @param register
     */
    public void freeRegister(DecacCompiler compiler, GPRegister register) {
        if(availableRegisters[register.getNumber() - 2] > 0) {
            // need to restore it !
            compiler.addInstruction(new POP(register));
            increaseUsedStack(-1);
        }
        // decrease it's free value
        availableRegisters[register.getNumber() - 2] -= 1;
        if(availableRegisters[register.getNumber() - 2] < -1) {
            throw new UnsupportedOperationException("Register have been freed too many times !");
        }
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

    public boolean checkAllRegisterAreFree() {
        for(int i = 0; i < availableRegisters.length; i++) {
            if(availableRegisters[i] != -1) {
                return false;
            }
        }
        return true;
    }

}
