package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Initialization (of variable, field, ...)
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractInitialization extends Tree {
    
    /**
     * Implements non-terminal "initialization" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param t corresponds to the "type" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     */
    protected abstract void verifyInitialization(DecacCompiler compiler,
            Type t, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Code generation for the initialization.
     * This must writes the value of the initialization on the stack using PUSH.
     * @param compiler Where we write the code.
     */
    public abstract void codeGenInit(DecacCompiler compiler, Type objectType, RegisterOffset resultRegister);

    public abstract CollapseResult<CollapseValue> collapseInit();
    /**
     * @return the expression of the initialization
     */
    protected abstract AbstractExpr getExpression();

    /**
     * Return true if there is an initialization
     * @return false by default, true if the class has an initialization
     */
    public abstract boolean hasInitialization();

}
