package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.log4j.Logger;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        // LOG.debug("verify listClass: start");
        for (AbstractDeclClass c : getList())
            c.verifyClass(compiler);
        // LOG.debug("verify listClass: end");
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClass c : getList())
            c.verifyClassMembers(compiler);
    }

    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClass c : getList())
            c.verifyClassBody(compiler);
    }

    /**
     * Generate the vTables for all the classes.
     * 
     * @param compiler where we write the instructions to.
     */
    public void initClassCodeGen(DecacCompiler compiler) {
        for (AbstractDeclClass c : getList()) {
            c.initClassCodeGen(compiler);
        }
    }

    /**
     * Generate the vTables for all the classes.
     * @param compiler where we write the instructions to.
     */
    public void codeGenVTables(DecacCompiler compiler) {
        for (AbstractDeclClass c : getList()) {
            c.codeGenVTable(compiler);
        }
    }

    /**
     * Generates the methods code.
     * 
     * @param compiler where we write the code to.
     */
    public void codeGenClasses(DecacCompiler compiler) {
        for (AbstractDeclClass c : getList()) {
            c.codeGenClass(compiler);
        }
    }
	
    public CollapseResult<Null> collapseClasses() {
        boolean somethingCollapsed = false;
        for(AbstractDeclClass c: getList()) {
            somethingCollapsed |= c.collapseClass().couldCollapse();
        }
        return new CollapseResult<Null>(null, somethingCollapsed);
    }
}
