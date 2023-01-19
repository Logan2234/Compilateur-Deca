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

    public abstract void codeGenProgram(DecacCompiler compiler);
}