package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.*;
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
    protected boolean spotUsedVar() {
        boolean varSpotted = true;
        while (varSpotted) {
            varSpotted = this.main.spotUsedVar();
            varSpotted = this.spotUsedVarFromClasses() || varSpotted;
        }
        return varSpotted;
    }

    /**
     * Remove all useless variables (variables, classes, methods and fields from the list of
     * classes and the main program
     * @return true if the program have been simplified
     */
    private boolean removeUnusedVar() {
        boolean simplified = this.optimizeClasses();
        if (this.main instanceof Main) {
            LOG.debug("Optimizing body of Main");
            Main mainNotEmpty = (Main)(this.main);
            if(mainNotEmpty.getListDeclVar().isEmpty() && mainNotEmpty.getListInst().isEmpty()) {
                this.main = new EmptyMain();
                simplified = true;
            } else {
                simplified = this.optimizeBlock(((Main)this.main).getListDeclVar(),((Main)this.main).getListInst())
                            || simplified;
            }
        }
        return simplified;
    }

    @Override
    public void optimizeTree() {
        boolean simplified = true;
        while (simplified) {
            // TODO this.resetSpottedVar();
            this.spotUsedVar();
            simplified = this.removeUnusedVar();
        }
    }

    /**
     * Remove all useless variables from the block (declaration or useless instructions)
     * @return true if the block has been simplified
     */
    private boolean optimizeBlock(ListDeclVar listDecls, ListInst listInsts) {
        boolean res = false;
        boolean simplified = true;
        while (simplified) {
            simplified = false;
            /* remove useless declarations from the main */
            if (listDecls != null) {
                Iterator<AbstractDeclVar> iterDecl = listDecls.iterator();
                while(iterDecl.hasNext()){
                    DeclVar decl = (DeclVar) iterDecl.next();
                    if (decl.getVar().getDefinition().isUsed()) {
                        // the variable is used
                    } else if (decl.getInit() instanceof Initialization
                            && !((Initialization)(decl.getInit())).getExpression().getMethodCalls().isEmpty()) {
                        // the variable is not used but is Initialized with a MethodCall
                    } else {
                        // the variable is not used and (not initialized or initialized with no methodCall)
                        iterDecl.remove();
                        simplified = true;
                        LOG.debug("Remove the decl of "+decl.getVar().getDefinition().toString());
                    }
                }
            }

            /* remove useless instructions form the main */
            // the order of if statements is important
            ListIterator<AbstractInst> iterInst = listInsts.iterator();
            while(iterInst.hasNext()){
                AbstractInst inst = iterInst.next();

                if (inst instanceof MethodCall || inst instanceof AbstractReadExpr) {
                    // keep it
                    // prevents from looping by setting simplified to true in the AbstractExpr case
                }

                else if (inst instanceof Not) {
                    iterInst.remove();
                    iterInst.add(((Not)inst).getOperand()); // add after the current instruction
                    simplified = true;
                    LOG.debug("Break Not at "+inst.getLocation() + " : " + inst.getClass());
                }

                else if (inst instanceof InstanceOf) {
                    iterInst.remove();
                    iterInst.add(((InstanceOf)inst).getExpr()); // add after the current instruction
                    simplified = true;
                    LOG.debug("Break InstanceOf at "+inst.getLocation() + " : " + inst.getClass());
                }

                else if (inst instanceof Assign) {
                    // don't group the if because it prevents from entering the next if
                    Assign assign = (Assign)inst;
                    if (!assign.getLeftOperand().getDefinition().isUsed()) {
                        iterInst.remove();
                        iterInst.add(assign.getRightOperand()); // add after the current instruction
                        simplified = true;
                        LOG.debug("Break Assign at "+inst.getLocation() + " : " + inst.getClass());
                    }
                }

                else if (inst instanceof AbstractExpr) {
                    AbstractExpr expr = (AbstractExpr)inst;
                    List<AbstractExpr> methods = expr.getMethodCalls();
                    if (methods.isEmpty()) {
                        iterInst.remove();
                        simplified = true;
                        LOG.debug("Remove expr at "+inst.getLocation() + " : " + inst.getClass());
                    }
                    else if (expr.getType().isBoolean()) {
                        // we cannot break the expression because for instance, the left operand
                        // of an AND shouldn't be evaluated if the right operand is false
                    }
                    else {
                        iterInst.remove();
                        for (AbstractExpr methodCall : methods) {
                            iterInst.add(methodCall); // add after the current instruction
                        }
                        simplified = true;
                        LOG.debug("Break expr at "+inst.getLocation() + " : " + inst.getClass());
                    }
                }

                else if (inst instanceof NoOperation) {
                    iterInst.remove();
                    simplified = true;
                    LOG.debug("Remove NoOp at "+inst.getLocation() + " : " + inst.getClass());
                }

                else if (inst instanceof While) {
                    While while_ = (While)inst;
                    simplified = optimizeBlock(null, while_.getBody());
                    // We keep the while if the condition have a method call
                    if (while_.getBody().isEmpty() && while_.getCondition().getMethodCalls().isEmpty()) {
                        iterInst.remove();
                        simplified = true;
                        // the condition will be optimized at the next optimizeBlock call
                        iterInst.add(while_.getCondition()); // add after the current instruction
                    }
                    if (simplified) LOG.debug("Optimize While at "+inst.getLocation() + " : " + inst.getClass());
                }

                else if (inst instanceof IfThenElse){
                    IfThenElse ifThenElse = (IfThenElse)inst;
                    simplified = optimizeBlock(null, ((IfThenElse)inst).getThenInst());
                    simplified = optimizeBlock(null, ((IfThenElse)inst).getElseInst())
                                || simplified;
                    if (ifThenElse.getThenInst().isEmpty() && ifThenElse.getElseInst().isEmpty()) {
                        iterInst.remove();
                        simplified = true;
                        // the condition will be optimized at the next optimizeBlock call
                        iterInst.add(ifThenElse.getCondition()); // add after the current instruction
                    }
                    if (simplified) LOG.debug("Optimize if at "+inst.getLocation() + " : " + inst.getClass());
                }
                res = res || simplified;
            }
        }
        return res;
    }

    /**
     * Remove all useless classes, methods and fields
     * @return true if ListDeclClass has been simplified
     */
    private boolean optimizeClasses() {
        boolean simplified = false;
        Iterator<AbstractDeclClass> iterClasses = this.classes.iterator();
        while(iterClasses.hasNext()){
            DeclClass currentClass = (DeclClass)iterClasses.next();
            
            /* remove useless classes */
            // A used class should have spotUsedVar() its superclass
            if (!currentClass.getName().getDefinition().isUsed()) {
                iterClasses.remove();
                simplified = true;
                LOG.debug("Remove class : " + currentClass.getName().getDefinition().toString());
            } 

            else {
                /* remove useless methods */
                Iterator<AbstractDeclMethod> iterMethods = currentClass.getMethods().iterator();
                while(iterMethods.hasNext()){
                    DeclMethod method = (DeclMethod)iterMethods.next();
                    if (!method.getName().getDefinition().isUsed()) {
                        iterMethods.remove();
                        simplified = true;
                        LOG.debug("Remove method : " + method.getName().getDefinition().toString());
                    }
                    else if (method.getBody() instanceof MethodBody){
                        MethodBody body = (MethodBody) method.getBody();
                        LOG.debug("Optimizing body of : " + method.getName().getDefinition().toString());
                        this.optimizeBlock(body.getVars(), body.getInsts());
                    }
                }

                /* remove useless fields */
                Iterator<AbstractDeclField> iterFields = currentClass.getFields().iterator();
                while(iterFields.hasNext()){
                    DeclField field= (DeclField)iterFields.next();
                    if (!field.getName().getDefinition().isUsed()) {
                        iterFields.remove();
                        simplified = true;
                        LOG.debug("Remove field : " + field.getName().getDefinition().toString());
                    }
                }
            }
        }
        return simplified;
    }

    /**
     * Spot all useful variables from classes
     * @return
     */
    private boolean spotUsedVarFromClasses() {
        boolean res = false;
        boolean varSpotted = true;

        /* init */
        Map<Definition,Set<Integer>> exploredMethods = new HashMap<Definition,Set<Integer>>();
        Set<DeclMethod> methodsToSpot = new HashSet<DeclMethod>();
        for (AbstractDeclClass c : this.classes.getList()) {
            for (AbstractDeclMethod m : ((DeclClass)c).getMethods().getList()) {
                methodsToSpot.add((DeclMethod)m);
            }
        }

        while (varSpotted) {
            varSpotted = false;

            Iterator<DeclMethod> iter = methodsToSpot.iterator();
            while (iter.hasNext()) {
                DeclMethod method = iter.next();
                if (method.getName().getDefinition().isUsed()
                    || (method.getName().getMethodDefinition().getContainingClass().isUsed()
                    && ((Identifier)method.getName()).getMethodDefinition().isOverrideOfUsed(exploredMethods))) {
                    // if method used or (containing class used and override)
                    varSpotted = method.spotUsedVar() || varSpotted;
                    iter.remove();
                    if (!exploredMethods.containsKey(method.getName().getDefinition())) {
                        exploredMethods.put(method.getName().getDefinition(),new HashSet<Integer>());
                    }
                    exploredMethods.get(method.getName().getDefinition()).add(method.getName().getMethodDefinition().getIndex());
                }
            }
            res = res || varSpotted;
        }
        return res;
    }



}
