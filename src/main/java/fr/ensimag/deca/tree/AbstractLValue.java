package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractLValue extends AbstractExpr {
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public abstract Definition getDefinition();
}
