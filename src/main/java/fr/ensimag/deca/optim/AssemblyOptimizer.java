package fr.ensimag.deca.optim;

import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.BinaryInstructionDValToReg;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 * Class that optimize an IMAProgram.
 * What can be optimize ?
 *      - value replacement, by immediates or registers
 *      - write read simplification
 *      - conditions optimizations
 *      - instructions replacement
 */
public class AssemblyOptimizer {

    /**
     * Idea : loop through the program and try to compact instructions.
     * @param program The IMA program to optimize.
     */
    public static void Optimize(IMAProgram program) {
        // replace known values
        while(OptReplaceImm(program)) { }
        // write read simplifications
        while(OptRemoveUselessWrites(program)) { }
        // optimize conditions with immediates
        while(OptCond(program)) { }
        // finally, replace loads by subs
        while(OptLoadSub(program)) { }
    }

    private static boolean OptReplaceImm(IMAProgram program) {
        // try to replace all use of register with immediates values.
        // let's keep track of known values in the registers.
        DVal[] knownRegisters = new DVal[16];
        for(int i = 0; i < 16; i++) {
            knownRegisters[i] = null;
        }
        boolean optimizing = false;
        for(int i = 0; i < program.size(); i++) {
            // loop through all lines
            AbstractLine line = program.getLine(i);
            if(line.isInstruction()) {
                // check if it is an instructions that alter registers
                Instruction instruction = line.asInstruction();
                // loop through all registers
                for(int r = 0; r < 16; r++) {
                    // if we alter that register, check if we already know how we are going to affect it
                    if(instruction.alterRegister(r)) {
                        // can we compute r's value ?
                        if(instruction.isDvalToReg()) {
                            BinaryInstructionDValToReg dvalToReg = instruction.asDvalToReg();
                            if(dvalToReg.getOperand1().isGpRegister()) {
                                // if we know the value of that register, we can put an immediate in it
                                if(knownRegisters[dvalToReg.getOperand1().asGpRegister().getNumber()] != null) {
                                    optimizing = true;
                                    dvalToReg.setDvalOp(knownRegisters[dvalToReg.getOperand1().asGpRegister().getNumber()]);
                                }
                            }
                            knownRegisters[dvalToReg.getGPRegister().getNumber()] = dvalToReg.tryComputeSelf();
                        }
                        else {
                            // instruction altered register r but we don't know what's in it know
                            knownRegisters[r] = null;
                        }
                    }
                }
            }
        }
        return optimizing;
    }

    private static boolean OptRemoveUselessWrites(IMAProgram program) {
        // for this one we'll need to loop backward
        // when a register is used, set it to true
        // when it is written, set it to false
        // if a register to false is written to, remove that line : it will be overwrite
        boolean optimizing = false;
        boolean[] usedRegisters = new boolean[16];
        for(int i = 0; i < 16; i++) {
            usedRegisters[i] = false;
        }
        for(int i = program.size() - 1; i >= 0; i--) {
            AbstractLine line = program.getLine(i);
            if(line.isInstruction()) {
                Instruction instruction = line.asInstruction();
                for(int r = 0; r < 16; r++) {
                    if(instruction.usesRegister(r)) {
                        // we are using the register ! 
                        usedRegisters[r] = true;
                    }
                    else {
                        if(instruction.alterRegister(r)) {
                            // we are changing this register without reading it.
                            if(usedRegisters[r]) {
                                // don't use it anymore, as we init it here
                                usedRegisters[r] = false;
                            }
                            else {
                                // this line is useless, as it writes to a unread register.
                                program.removeLine(i);
                            }
                        }
                    }
                }
            }
        }
        return optimizing;
    }

    private static boolean OptLoadSub(IMAProgram program) {
        boolean optimizing = false;
        for(int i = 0; i < program.size(); i++) {
            AbstractLine line = program.getLine(i);
            if(line.isInstruction()) {
                Instruction instruction = line.asInstruction();
                if(instruction.isLOAD()) {
                    // check it loads 0 in a register
                    LOAD load = instruction.asLOAD();
                    boolean validOp1 = load.getOperand1().isIntImmediate()
                        && load.getOperand1().asIntImmediate().getValue() == 0;
                    boolean validOp2 = load.getOperand2().isGpRegister();
                    if(validOp1 && validOp2) {
                        // check the register have already been loaded at some point
                        // otherwise, we are doing an op on unloaded register
                        boolean registerInitialized = false;
                        for(int j = 0; j < i; j++) {
                            if(program.getLine(j).isInstruction()) {
                                if(program.getLine(j).asInstruction().alterRegister(load.getOperand2().asGpRegister().getNumber())) {
                                    registerInitialized = true;
                                    break;
                                }
                            }
                        }
                        if(registerInitialized) {
                            // replace LOAD #0 RX by SUB RX RX
                            program.replaceInstructionAt(
                                new SUB(load.getOperand2().asGpRegister(), load.getOperand2().asGpRegister()), 
                                "LOAD #0 " + load.getOperand2().asGpRegister().toString(), 
                                i);
                            optimizing = true;
                        }
                    } 

                }
            }
        }
        return optimizing;
    }

    private static boolean OptCond(IMAProgram program) {
        boolean optimizing = false;
        for(int i = 0; i < program.size(); i++) {
            AbstractLine line = program.getLine(i);
            if(line.isInstruction()) {

            }
        }
        return optimizing;
    }

}

