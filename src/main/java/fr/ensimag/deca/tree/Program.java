package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import static org.mockito.Mockito.reset;

import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

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
    private Map<ClassDefinition,Set<Integer>> methodsUsed;

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

    /**
     * Remove all unused variables from the program
     * @return true if one or more variable have been removed
     */
    @Override
    public void optimUnusedVar() {
        if (this.spotted) {
            this.resetSpottedVar();
        }
        this.spotUsedVar();
        this.removeUnusedVar();
    }

    @Override
    protected void spotUsedVar() {
        this.spotted = true;
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
                if (methDef.isUsed() || (containingClass.isUsed() && methDef.isOverridingAMethodInMap(exploredMethods))) {
                    // if method used or (containing class used and override)
                    method.spotUsedVar();
                    varSpotted = true;
                    iter.remove();
                    exploredMethods.get(containingClass).add(methDef.getIndex());
                }
            }
        }
        this.methodsUsed = exploredMethods;
    }

    /**
     * Spot fields overriding useful fields.
     * At the beginning, some methods have already been spotted from the Main
     */
    private void spotOverridingFields() {
        LOG.debug("===================== Spot from fields =====================");
        Map<Symbol,Set<ClassDefinition>> usedFields = new HashMap<Symbol,Set<ClassDefinition>>();
        this.classes.getSpottedFields(usedFields);
        this.classes.spotOverridingFields(usedFields);
    }

    @Override
    public CollapseResult<Null> collapseProgram() {
        return new CollapseResult<Null>(null, main.collapseMain().couldCollapse() || classes.collapseClasses().couldCollapse());
    }

    /**
     * Optimize the program tree with the substitution of inline methods
     */
    public void substituteInlineMethods() {
        Map<MethodDefinition, DeclMethod> inlineMethods = this.spotInlineMethodsFromProg();
        // this phase should come after the spotting of used methods
        // we need to know if a method is overrided
        // if it is, the program could call dynamically the overriding method (which could is not 
        // necessarily inline) instead of the statically subsituted inline method
        assert(this.methodsUsed != null);
        this.removeOverridedInlineMethods(inlineMethods);
        this.doSubstituteInlineMethods(inlineMethods);
    }

    /**
     * Create an hasmap of inline methods and fill it by calling recursively spotInlineMethods
     * @return hasmap of inline methods
     */
    private Map<MethodDefinition, DeclMethod> spotInlineMethodsFromProg() {
        Map<MethodDefinition, DeclMethod> inlineMethods = new HashMap<MethodDefinition, DeclMethod>();
        this.classes.spotInlineMethods(inlineMethods);
        return inlineMethods;
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.main = (AbstractMain) this.main.doSubstituteInlineMethods(inlineMethods);
        this.classes = (ListDeclClass)this.classes.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    /**
     * remove from the given map the methods that are overrider by toher used methods 
     * that are not inline (in the map)
     * @param map of the inline methods spotted
     */
    private void removeOverridedInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        // reshape the map of methods to fit the param of isOverridingMethodInMap()
        Map<ClassDefinition,Set<Integer>> inlineMethodsReshaped = new HashMap<ClassDefinition,Set<Integer>>();
        // The map must have an entry for every class in the program (a Set for each map)
        for (AbstractDeclClass c : this.classes.getList()) {
            inlineMethodsReshaped.put(((DeclClass)c).getName().getClassDefinition(),new HashSet<Integer>());           
        }
        // add indexes of inline methods
        for (MethodDefinition methDef : inlineMethods.keySet()) {
            ClassDefinition containingClass = methDef.getContainingClass();
            inlineMethodsReshaped.get(containingClass).add(methDef.getIndex());
        }
        inlineMethods.clear();

        // remove from the inline methods map the methods we should not substitute for dynamic calling reasons
        for (Map.Entry<ClassDefinition, Set<Integer>> entry : this.methodsUsed.entrySet()) {
            ClassDefinition classDef = entry.getKey();
            for (Integer index : entry.getValue()) {
                if (inlineMethodsReshaped.containsKey(classDef)
                && inlineMethodsReshaped.get(classDef).contains(index)) {
                    // keep the method as it is inline
                }
                ClassDefinition classWithOverridedMethods = MethodDefinition.isOverridingAMethodInMap(inlineMethodsReshaped, classDef, index);
                // a method may override multiple methods in the clas hierarchy
                while (classWithOverridedMethods != null) {
                    // The method is used, is not inline and is an override of an inline method.
                    // It may be used dynamically instead of the method it overrides so we cannot
                    // substitute the latter.
                    assert(inlineMethodsReshaped.get(classDef).contains(index));
                    inlineMethodsReshaped.get(classDef).remove(index);
                    classWithOverridedMethods = MethodDefinition.isOverridingAMethodInMap(inlineMethodsReshaped, classWithOverridedMethods, index);
                }
            }
        }

        // rebuild the map without the methods overrided by a non-inline method
        for (AbstractDeclClass c : this.classes.getList()) {
            ClassDefinition classDef = ((DeclClass)c).getName().getClassDefinition();
            for (AbstractDeclMethod method : ((DeclClass)c).getMethods().getList()) {
                MethodDefinition methDef = ((DeclMethod)method).getName().getMethodDefinition();
                int index = methDef.getIndex();
                if (inlineMethodsReshaped.get(classDef).contains(index)) {
                    inlineMethods.put(methDef,(DeclMethod)method);
                }
            }
        }
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

}
