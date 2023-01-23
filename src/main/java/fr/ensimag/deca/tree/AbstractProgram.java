package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.optim.CollapseResult;

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

    public abstract CollapseResult<Null> collapseProgram();

    public abstract void optimUnusedVar();

    public abstract void substituteInlineMethods();
}
