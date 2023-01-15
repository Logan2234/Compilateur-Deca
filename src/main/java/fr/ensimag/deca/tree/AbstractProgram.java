package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;

/**
 * Entry point for contextual verifications and code generation from outside the package.
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
     * @param prog
     */
     public void optimizeTree(){
        while(removeUnusedVar());
    }

    /**
     * Remove all unused variables from the tree
     * @param compiler
     * @return true if tree has been simplified
     */
    public boolean removeUnusedVar(){
        this.spotUsedVar(this); // browse the main program
        return this.removeUnspottedVar();
    }

    /**
     * Remove from the tree the variables, classes and methodes that are unused
     * @param compiler
     * @return true if tree has been simplified
     */
    protected abstract boolean removeUnspottedVar();
}
