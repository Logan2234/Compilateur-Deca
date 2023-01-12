package fr.ensimag.deca.codegen.runtimeErrors;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;

public abstract class AbstractRuntimeErr {
    /**
     * Get a unique runtime error id.
     * @return the id of the runtime error.
     */
    public abstract int errorId();

    /**
     * Generates the code for this error.
     * @param compiler Where we write the assembly code.
     */
    public abstract void codeGenErr(DecacCompiler compiler);

    /**
     * Get the label error.
     * @return
     */
    public abstract Label getErrorLabel();
}
