package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
    private boolean spotted;

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
    public void optimizeTree() {
        // TODO
        // solve compile time known cases.
        //this.collapse();
        // remove useless variables
        this.optimUnusedVar(); 
    }

    /**
     * Remove all unused variables from the program
     * @return true if one or more variable have been removed
     */
    private void optimUnusedVar() {
        if (this.spotted) {
            this.resetSpottedVar();
        }
        this.spotUsedVar();
        this.removeUnusedVar();
    }

    @Override
    protected void spotUsedVar() {
        this.main.spotUsedVar();
        this.spotFromUsedMethods();
        this.spotOverridingFields();
    }

    /**
     * Reset the used attribute back to false for every Definition in the program
     * Set back program's spotted attribute back to false 
     */
    private void resetSpottedVar() {
        this.iter(new ResetUsedVar());
        this.spotted = false;
    }

    @Override
    protected Tree removeUnusedVar() {
        this.classes = (ListDeclClass) this.classes.removeUnusedVar();
        this.main = (AbstractMain) this.main.removeUnusedVar();
        return this;
    }

    /**
     * Spot useful variables from used methods and spot methods overriding useful methods.
     * At the beginning, some methods have already been spotted from the Main but their body
     * have not been browsed yet for finding useful variables
     */
    private void spotFromUsedMethods() {
        LOG.debug("===================== Spot from methods =====================");

        /* init */
        // at the beginning, some methods are spotted from the Main but their body have not been browsed
        // yet for finding other variables used
        Map<ClassDefinition,Set<Integer>> exploredMethods = new HashMap<ClassDefinition,Set<Integer>>();
        Set<DeclMethod> methodsToSpot = new HashSet<DeclMethod>();
        for (AbstractDeclClass c : this.classes.getList()) {
            exploredMethods.put(((DeclClass)c).getName().getClassDefinition(),new HashSet<Integer>());
            for (AbstractDeclMethod m : ((DeclClass)c).getMethods().getList()) {
                methodsToSpot.add((DeclMethod)m);
            }
        }

        /* spotting */
        // When a method is spotted, its body is spotted and we may find other methods used.
        // So we have to keep spotting until there is no more variable spotted
        boolean varSpotted = true;
        while (varSpotted) {
            varSpotted = false;

            Iterator<DeclMethod> iter = methodsToSpot.iterator();
            while (iter.hasNext()) {
                DeclMethod method = iter.next();
                MethodDefinition methDef= method.getName().getMethodDefinition();
                ClassDefinition containingClass = methDef.getContainingClass();
                if (methDef.isUsed() || (containingClass.isUsed() && methDef.isOverrideOfUsed(exploredMethods))) {
                    // if method used or (containing class used and override)
                    method.spotUsedVar();
                    varSpotted = true;
                    iter.remove();
                    exploredMethods.get(containingClass).add(methDef.getIndex());
                }
            }
        }
    }

    /**
     * Spot fields overriding useful fields.
     * At the beginning, some methods have already been spotted from the Main
     */
    private void spotOverridingFields() {
        LOG.debug("===================== Spot from fields =====================");

        /* init */
        Map<Symbol,Set<ClassDefinition>> usedFields = new HashMap<Symbol,Set<ClassDefinition>>();
        Set<DeclField> fieldsToSpot = new HashSet<DeclField>();
        for (AbstractDeclClass c : this.classes.getList()) {
            DeclClass class_ = (DeclClass) c;
            // if a class is not used, its fields won't be used anyway
            if (!class_.getName().getDefinition().isUsed()) {continue;}
            for (AbstractDeclField field : ((DeclClass)c).getFields().getList()) {
                FieldDefinition fieldDef = ((DeclField)field).getName().getFieldDefinition();
                
                if (fieldDef.isUsed()){
                    // add the class to the table
                    Symbol symb = ((DeclField)field).getName().getName();
                    if (!usedFields.containsKey(symb)) {
                        usedFields.put(symb, new HashSet<ClassDefinition>());
                    }
                    usedFields.get(symb).add(fieldDef.getContainingClass());
                }
                else {
                    fieldsToSpot.add((DeclField)field);
                }
            }
        }

        /* spotting */
        for (DeclField field : fieldsToSpot) {
            Symbol symb = ((DeclField)field).getName().getName();
            if (usedFields.containsKey(symb)) {
                ClassDefinition containingClass = field.getName().getFieldDefinition().getContainingClass();
                ClassDefinition currentClass = containingClass;
                // check if override of used field
                while (currentClass != null && !usedFields.get(symb).contains(currentClass)) {
                    currentClass = currentClass.getSuperClass();
                }
                if (currentClass != null) {
                    field.spotUsedVar();
                    usedFields.get(symb).add(currentClass);
                }
            }
        }
    }

    @Override
    public boolean collapse() {
        return classes.collapse() || main.collapse();
    }
}
