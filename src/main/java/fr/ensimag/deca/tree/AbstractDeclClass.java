package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.optim.CollapseResult;

/**
 * Class declaration.
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractDeclClass extends Tree {

    /**
     * Pass 1 of [SyntaxeContextuelle]. Verify that the class declaration is OK
     * without looking at its content.
     */
    protected abstract void verifyClass(DecacCompiler compiler) throws ContextualError;

    /**
     * Pass 2 of [SyntaxeContextuelle]. Verify that the class members (fields and
     * methods) are OK, without looking at method body and field initialization.
     */
    protected abstract void verifyClassMembers(DecacCompiler compiler) throws ContextualError;

    /**
     * Pass 3 of [SyntaxeContextuelle]. Verify that instructions and expressions
     * contained in the class are OK.
     */
    protected abstract void verifyClassBody(DecacCompiler compiler) throws ContextualError;

    /**
     * Set up the class so the code generation is on valid objects.
     * @param compiler where we write the code to.
     */
    public abstract void initClassCodeGen(DecacCompiler compiler);

    /**
     * Generate the vTable for the given class.
     * @param compiler Where we write the instructions to.
     */
    public abstract void codeGenVTable(DecacCompiler compiler);

    /**
     * Generate the code for method classes
     * @param compiler Where we write the instructions to.
     */
    public abstract void codeGenClass(DecacCompiler compiler);

    public abstract CollapseResult<Null> collapseClass();
}
