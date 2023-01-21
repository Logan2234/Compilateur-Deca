package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 * Entry point for contextual verifications and code generation from outside the
 * package.
 * 
 * @author Jorge
 * @date 05/01/2023
 *
 */
public abstract class AbstractMethod extends Tree {
    public abstract void verifyMethod(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentclass,
            Type type) throws ContextualError;

    /**
     * Generate the code for this method body.
     * 
     * @param compiler where we write the code to.
     */
    public abstract void codeGenMethod(DecacCompiler compiler);

    /**
     * Set the name of the class + method for the returns to know the labels.
     */
    public abstract void setReturnsNames(String name);
}