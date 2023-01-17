package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
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
        classes.verifyListClass(compiler);
        classes.verifyListClassMembers(compiler);
        classes.verifyListClassBody(compiler);
        main.verifyMain(compiler);
        // LOG.debug("verify program: end");

    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // generate the vtables
        compiler.addComment("+--------------------------------------+  \\o/");
        compiler.addComment("|        Virtual method tables         |   | ");
        compiler.addComment("+--------------------------------------+  / \\");
        codeGenDefaultObject(compiler);
        classes.codeGenVTables(compiler);
        // stack overflow management
        compiler.addComment("+--------------------------------------+");
        compiler.addComment("|             Main Program             |  _o");
        compiler.addComment("+--------------------------------------+ /\\)");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());
        // stack overflow mangement
        StackOverflowErr ovError = new StackOverflowErr();
        compiler.useRuntimeError(ovError);
        compiler.addInstructionFirst(new BOV(ovError.getErrorLabel()));
        compiler.addInstructionFirst(new TSTO(compiler.endCodeContext()));
        // generate class codes
        compiler.addComment("+--------------------------------------+ __o");
        compiler.addComment("|             Methods Code             |   |\\");
        compiler.addComment("+--------------------------------------+  / \\");
        classes.codeGenClasses(compiler);
        // generate the errors
        compiler.addComment("+--------------------------------------+   \\_");
        compiler.addComment("|           Error Management           | __(");
        compiler.addComment("+--------------------------------------+   o\\");
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

    /**
     * Generate the code for the always existing default class "Object".
     * @param compiler where we write the code to. 
     */
    public void codeGenDefaultObject(DecacCompiler compiler) {
        // always the same code ?
        compiler.addComment("VTABLE of 'Object'");
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        compiler.addInstruction(new PUSH(Register.R0));
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
        compiler.addInstruction(new PUSH(Register.R0));
        compiler.occupyLBSPace(2); // we pushed 2 variables
    } 
}
