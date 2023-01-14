package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.*;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Method Body Statement
 *
 * @author gl03
 * @date 05/01/2023
 */
public class MethodBody extends AbstractMethod {
    
    public MethodBody(ListDeclVar vars, ListInst insts) {
        Validate.notNull(vars);
        Validate.notNull(insts);
        this.vars = vars;
        this.insts = insts;
    }
    private ListDeclVar vars;
    private ListInst insts;
    
    public ListDeclVar vars() {
        return vars;
    }

    public ListInst getInsts() {
        return insts;
    }

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        //TODO
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
       //TODO
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("{");
        vars.decompile(s);
        insts.decompile(s);
        s.print("}");
    }
    
    @Override 
    protected void iterChildren(TreeFunction f) {
        vars.iterChildren(f);
        insts.iterChildren(f);
    }
    @Override //? Is it necessary ?
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        vars.prettyPrintChildren(s, prefix);
        insts.prettyPrintChildren(s, prefix);
    }

    @Override
    public boolean collapse() {
        // TODO
        return false;
    }
}
