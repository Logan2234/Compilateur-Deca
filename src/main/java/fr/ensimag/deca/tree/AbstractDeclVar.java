package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Variable declaration
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractDeclVar extends Tree {
    
    /**
     * Implements non-terminal "decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to the "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the synthetized attribute
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     */    
    protected abstract void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Non terminal code gen. 
     * @param compiler where we write instructions to generate assembly code.
     * @param offset The register offset, so a DAddr, where the variable will be registered.
     */
    public abstract void codeGenDeclVar(DecacCompiler compiler, RegisterOffset register);

    public abstract CollapseResult<Null> collapseDeclVar();
}
