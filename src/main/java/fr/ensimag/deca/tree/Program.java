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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
        classes.verifyListClass(compiler);
        classes.verifyListClassMembers(compiler);
        classes.verifyListClassBody(compiler);
        main.verifyMain(compiler);
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // initialize code gen
        classes.initClassCodeGen(compiler);
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
        if(compiler.getCompilerOptions().getRunTestChecks()) {
            StackOverflowErr ovError = new StackOverflowErr();
            compiler.useRuntimeError(ovError);
            compiler.addInstructionFirst(new BOV(ovError.getErrorLabel()));
            compiler.addInstructionFirst(new TSTO(compiler.getMaxStackUse()));
        }
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
    
    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        main.spotUsedVar(prog);
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
    }

    @Override
    public boolean removeUnusedVar() {
        spotUsedVar(this); // browse the main program
        boolean simplified = optimizeClasses();
        if (main instanceof Main) {
            LOG.debug("Optimizing body of Main");
            simplified = simplified || optimizeBlock(((Main) main).getListDeclVar(), ((Main) main).getListInst());
        }
        return simplified;
    }

    /**
     * Remove all useless variables from the block (declaration or useless
     * instructions)
     * 
     * @return true if the block has been simplified
     */
    private boolean optimizeBlock(ListDeclVar listDecls, ListInst listInsts) {
        boolean simplified = false;
        /* remove useless declarations from the main */
        Iterator<AbstractDeclVar> iterDecl = listDecls.iterator();
        while (iterDecl.hasNext()) {
            DeclVar decl = (DeclVar) iterDecl.next();
            if (decl.getVar().getDefinition().isUsed()) {
                // the variable is used
            } else if (decl.getInit() instanceof Initialization
                    && !((Initialization) (decl.getInit())).getExpression().getMethodCalls().isEmpty()) {
                // the variable is not used but is Initialized with a MethodCall
            } else {
                // the variable is not used and (not initialized or initialized with no
                // methodCall)
                iterDecl.remove();
                simplified = true;
                LOG.debug("Remove the decl of " + decl.getVar().getDefinition().toString());
            }
        }

        /* remove useless instructions form the main */
        ListIterator<AbstractInst> iterInst = listInsts.iterator();
        while (iterInst.hasNext()) {
            AbstractInst inst = iterInst.next();

            if ((inst instanceof Assign && !((Assign) inst).getLeftOperand().getDefinition().isUsed())
                    || (inst instanceof AbstractExpr && !(inst instanceof Assign) && !(inst instanceof AbstractReadExpr)
                            && !(inst instanceof MethodCall))) {
                /*
                 * The instruction can either be an assignation with a useless left operand or
                 * an expression (but not a method call, a read or an assignement expression
                 * with
                 * a useful leftoperand).
                 * 
                 * In both cases we can remove the instruction but we have to keep the method
                 * call
                 * that appear in them because they can possibly change the state of an object
                 * or
                 * interact with the user.
                 * 
                 * The left operand of the assign could either be an Identifier or a Selection
                 * The only difference is that the object of the Selection could be obtained via
                 * a MethodCall but this MethodCall will be contained in the list bellow as
                 * well.
                 * 
                 * Not treating the Reads and MethodCall prevents from looping on the
                 * optimization
                 * of the tree by setting simplified to true for no reason.
                 */
                List<AbstractExpr> methods = ((AbstractExpr) (inst)).getMethodCalls();
                iterInst.remove();
                LOG.debug("Remove expr at " + inst.getLocation() + " : " + inst.getClass());
                for (AbstractExpr methodCall : methods) {
                    // add after the current instruction
                    iterInst.add(methodCall);
                    LOG.debug("Add method call at " + inst.getLocation() + " : " + inst.getClass());
                }
                simplified = true;
            }

            else if (inst instanceof NoOperation) {
                iterInst.remove();
                simplified = true;
                LOG.debug("Remove NoOp at " + inst.getLocation() + " : " + inst.getClass());
            }
        }
        return simplified;
    }

    /**
     * Remove all useless classes, methods and fields
     * 
     * @return true if ListDeclClass has been simplified
     */
    private boolean optimizeClasses() {
        // TODO remove useless methods, classes and fields (be carefull with methods and
        // fields indexes)
        // TODO simplify methods (be carefull with params simplification and params
        // required for the call)
        // TODO be carefull with override
        boolean simplified = false;
        Iterator<AbstractDeclClass> iterClasses = this.classes.iterator();
        while (iterClasses.hasNext()) {
            DeclClass currentClass = (DeclClass) iterClasses.next();

            /* remove useless classes */
            // A used class should have spotUsedVar() its superclass
            if (!currentClass.getName().getDefinition().isUsed()) {
                iterClasses.remove();
                simplified = true;
                LOG.debug("Remove class : " + currentClass.getName().getDefinition().toString());
            }

            else {
                // TODO remove don't remove overriding methods of used methods
                /* remove useless methods */
                // Iterator<AbstractDeclMethod> iterMethods =
                // currentClass.getMethods().iterator();
                // while(iterMethods.hasNext()){
                // DeclMethod method = (DeclMethod)iterMethods.next();
                // if (!method.getName().getDefinition().isUsed()) {
                // iterMethods.remove();
                // simplified = true;
                // LOG.debug("Remove method : " + method.getName().getDefinition().toString());
                // }
                // else if (method.getBody() instanceof MethodBody){
                // // TODO How to handle parameters ???
                // MethodBody body = (MethodBody) method.getBody();
                // LOG.debug("Optimizing body of : " +
                // method.getName().getDefinition().toString());
                // this.optimizeBlock(body.getVars(), body.getInsts());
                // }
                // }

                // TODO remove don't remove overriding fields of used fields
                /* remove useless fields */
                // Iterator<AbstractDeclField> iterFields = currentClass.getFields().iterator();
                // while(iterFields.hasNext()){
                // DeclField field= (DeclField)iterFields.next();
                // if (!field.getName().getDefinition().isUsed()) {
                // iterFields.remove();
                // simplified = true;
                // LOG.debug("Remove field : " + field.getName().getDefinition().toString());
                // }
                // }
            }
        }
        return simplified;
    }

    @Override
    public AbstractInst factorise(DecacCompiler compiler) {
        main.factorise(compiler);
        classes.factorise(compiler);
        return null;
    }

    @Override
    public AbstractInst splitCalculus(DecacCompiler compiler) {
        main.splitCalculus(compiler);
        classes.splitCalculus(compiler);
        return null;
    }
        
    public boolean collapse() {
        return classes.collapse() || main.collapse();
    }

}
