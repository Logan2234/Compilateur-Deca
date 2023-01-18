package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

/**
 * Entry point for contextual verifications and code generation from outside the
 * package.
 * 
 * @author gl03
 * @date 01/01/2023
 *
 */
public abstract class AbstractProgram extends Tree {
    public abstract void verifyProgram(DecacCompiler compiler) throws ContextualError;

    public abstract void codeGenProgram(DecacCompiler compiler);

    /**
     * Optimize the decorated tree until there is no simplification found
     */
    public void optimizeTree() {
        while (removeUnusedVar());
    }

    /**
     * Remove all unused variables from the tree
     * 
     * @return true if tree has been simplified
     */
    public abstract boolean removeUnusedVar();
}
