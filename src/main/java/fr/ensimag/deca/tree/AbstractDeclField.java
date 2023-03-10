package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Field declaration
 *
 * @author Jorge
 * @date 05/01/2023
 */
public abstract class AbstractDeclField extends Tree {

    /**
     * Implements non-terminal "decl_field" of [SyntaxeContextuelle] in pass 3
     * 
     * @param compiler     contains "env_types" attribute
     * @param localEnv
     *                     its "parentEnvironment" corresponds to the "env_exp_sup"
     *                     attribute
     *                     in precondition, its "current" dictionary corresponds to
     *                     the "env_exp" attribute
     *                     in postcondition, its "current" dictionary corresponds to
     *                     the synthetized attribute
     * @param currentClass
     *                     corresponds to the "class" attribute (null in the main
     *                     bloc).
     */

    protected abstract void verifyInitField(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError;

    /**
     * Implements non-terminal "decl_field" of [SyntaxeContextuelle] in pass 3
     * 
     * @param compiler     contains "env_types" attribute
     * @param localEnv
     *                     its "parentEnvironment" corresponds to the "env_exp_sup"
     *                     attribute
     *                     in precondition, its "current" dictionary corresponds to
     *                     the "env_exp" attribute
     *                     in postcondition, its "current" dictionary corresponds to
     *                     the synthetized attribute
     * @param currentClass
     *                     corresponds to the "class" attribute (null in the main
     *                     bloc).
     */
    protected abstract void verifyDeclField(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError;

    /**
     * Generates the code to initialize this field. This is similar to code gen init 
     */
    public abstract void codeGenField(DecacCompiler compiler, RegisterOffset resultRegister);

    public abstract void setFieldOffset(DecacCompiler compiler, int offset);

    public abstract CollapseResult<Null> collapseDeclField();
}
