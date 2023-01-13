package fr.ensimag.deca.optim;

import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 * Class that optimize an IMAProgram.
 * What can be optimize ?
 *      - LOAD #0 RX
 *          -> SUB RX RX
 *      - LOAD RX RY [IF RX UNCHANGED UNTIL RY CHANGED]
 *          -> remove this line, and replace every RY by RX until RX change
 *      - Scc R1, OPP R1, Bcc
 *          -> Bcc
 */
public class AssemblyOptimizer {

    /**
     * Idea : loop through the program and try to compact instructions.
     * @param program The IMA program to optimize.
     */
    public static void Optimize(IMAProgram program) {
        boolean optimizing = true;
        while(optimizing) {
            optimizing = OptLoadSub(program) || OptCond(program);
        }
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

