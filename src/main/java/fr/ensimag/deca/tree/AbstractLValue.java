package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractLValue extends AbstractExpr {

    public abstract Definition getDefinition();

    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        // return nothing ? expect if we find a way to compute methods at compile time...
        return new CollapseResult<CollapseValue>(new CollapseValue(), false);
    }

    /**
     * Generates the code that assign the value in the given register in the l value.
     * @param compiler
     * @param register
     */
    public abstract void codeGenAssignLVal(DecacCompiler compiler, GPRegister register);

    
    public abstract Symbol getName();
}
