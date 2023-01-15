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
     * Optimize the decorated tree.
     * 
     * @param prog
     */
     public void optimizeTree(){
        removeUnusedVar();
    }

    /**
     * Remove all unused variables from the tree
     * 
     * @param compiler
     */
    public void removeUnusedVar(){
        this.spotUsedVar(this); // browse the main program
        this.removeUnspottedVar();
    }

    /**
     * Remove from the tree the variables, classes and methodes that are unused
     * 
     * @param compiler
     */
    protected abstract void removeUnspottedVar();
}
