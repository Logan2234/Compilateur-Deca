package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;
import net.bytebuddy.asm.AsmVisitorWrapper;

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
        // create the main context
        compiler.newGlobalCodeContext();
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
        // add first the stack offset (addsp) as the amount of space we took on the stack in the main context
        compiler.addInstructionFirst(new ADDSP(new ImmediateInteger(compiler.readNextStackSpace().getOffset() - 1)));
        // stack overflow mangement
        StackOverflowErr ovError = new StackOverflowErr();
        compiler.useRuntimeError(ovError);
        compiler.addInstructionFirst(new BOV(ovError.getErrorLabel()));
        compiler.addInstructionFirst(new TSTO(compiler.getMaxStackUse()));
        // generate class codes (need a quick context for this beautiful comments)
        compiler.newCodeContext();
        compiler.addComment("+--------------------------------------+ __o");
        compiler.addComment("|             Methods Code             |   |\\");
        compiler.addComment("+--------------------------------------+  / \\");
        codeGenDefaultEquals(compiler);
        compiler.endCodeContext();
        classes.codeGenClasses(compiler);
        // create a code context for error management
        compiler.newCodeContext();
        // generate the errors
        compiler.addComment("+--------------------------------------+   \\_");
        compiler.addComment("|           Error Management           | __(");
        compiler.addComment("+--------------------------------------+   o\\");
        for(AbstractRuntimeErr error : compiler.getAllErrors().values()) {
            compiler.addLabel(error.getErrorLabel());
            error.codeGenErr(compiler);
        }
        compiler.endCodeContext();
        //end the main context, copying the code to the actual ima prog
        compiler.endCodeContext();
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
        compiler.addComment("========== VTable for Object ==========");
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, compiler.getNextStackSpace()));
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, compiler.getNextStackSpace()));
    }


    public void codeGenDefaultEquals(DecacCompiler compiler) {
        compiler.addComment("========== Class Object ==========");
        compiler.addLabel(new Label("code.Object.equals"));
        // compare -2(LB) (object on which method was called) and -3(LB) param
        // put the result in R0 (return register)
        compiler.addInstruction(new LOAD(new RegisterOffset(-3, Register.LB), Register.R1));
        compiler.addInstruction(new CMP(new RegisterOffset(-2, Register.LB), Register.R1));
        // check eq in R0
        compiler.addInstruction(new SEQ(Register.R0));
        compiler.addInstruction(new RTS());
    }
}
