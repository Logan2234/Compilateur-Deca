package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.*;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        // LOG.debug("verify program: start");
        // classes.verifyListClassBody(compiler); // TODO: Manque les deux autres passes (passe 1 et 2)
        main.verifyMain(compiler);
        // LOG.debug("verify program: end");

    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // TODO : generate method tables for classes
        // stack overflow management
        compiler.newCodeContext();
        compiler.addComment("Main program");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());
        // stack overflow mangement
        StackOverflowErr ovError = new StackOverflowErr();
        compiler.useRuntimeError(ovError);
        compiler.addInstructionFirst(new BOV(ovError.getErrorLabel()));
        compiler.addInstructionFirst(new TSTO(compiler.endCodeContext()));
        // generate the errors
        compiler.addComment("Error management");
        for(AbstractRuntimeErr error : compiler.getAllErrors().values()) {
            compiler.addLabel(error.getErrorLabel());
            error.codeGenErr(compiler);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        classes.decompile(s);
        main.decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }

    @Override
    protected void spotUsedVar() {
        main.spotUsedVar();
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
    }
    
    /**
     * Optimize the decorated tree.
     * 
     * @param prog
     */

     public void optimizeTree(){
        removeUnusedVar();
    }

    /**
     * Remove all unused variables from the tree
     * 
     * @param compiler
     */
    public void removeUnusedVar(){
        spotUsedVar(); // browse the main program
        // TODO add function to keep browsing from the spotted classes and methods
        //removeUnspottedVar();
    }

    /**
     * Remove from the tree the variables, classes and methodes that are unused
     * 
     * @param compiler
     */
    //protected abstract void removeUnspottedVar();
}
