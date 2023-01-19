package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.*;
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
    protected void spotUsedVar(AbstractProgram prog) {
        main.spotUsedVar(prog);
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
    }

    @Override
    public boolean removeUnusedVar() {
        this.spotUsedVar(this); // browse the main program
        boolean simplified = this.optimizeClasses();
        if (this.main instanceof Main) {
            LOG.debug("Optimizing body of Main");
            Main mainNotEmpty = (Main)(this.main);
            if(mainNotEmpty.getListDeclVar().isEmpty() && mainNotEmpty.getListInst().isEmpty()) {
                this.main = new EmptyMain();
                simplified = true;
            } else {
                simplified = simplified || this.optimizeBlock(((Main)this.main).getListDeclVar(),((Main)this.main).getListInst());
            }
        }
        return simplified;
    }

    /**
     * Remove all useless variables from the block (declaration or useless instructions)
     * @return true if the block has been simplified
     */
    private boolean optimizeBlock(ListDeclVar listDecls, ListInst listInsts) {
        boolean simplified = false;
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
                    iterInst.add(assign.getLeftOperand()); // add after the current instruction
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
                simplified = (simplified || optimizeBlock(null, ((IfThenElse)inst).getElseInst()));
                if (ifThenElse.getThenInst().isEmpty() && ifThenElse.getElseInst().isEmpty()) {
                    iterInst.remove();
                    simplified = true;
                    // the condition will be optimized at the next optimizeBlock call
                    iterInst.add(ifThenElse.getCondition()); // add after the current instruction
                }
                if (simplified) LOG.debug("Optimize if at "+inst.getLocation() + " : " + inst.getClass());
            }
        }
        return simplified;
    }

    /**
     * Remove all useless classes, methods and fields
     * @return true if ListDeclClass has been simplified
     */
    private boolean optimizeClasses() {
        // TODO remove useless methods, classes and fields (be carefull with methods and fields indexes)
        // TODO simplify methods (be carefull with params simplification and params required for the call)
        // TODO be carefull with override
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
                // TODO remove don't remove overriding methods of used methods
                /* remove useless methods */
                // Iterator<AbstractDeclMethod> iterMethods = currentClass.getMethods().iterator();
                // while(iterMethods.hasNext()){
                //     DeclMethod method = (DeclMethod)iterMethods.next();
                //     if (!method.getName().getDefinition().isUsed()) {
                //         iterMethods.remove();
                //         simplified = true;
                //         LOG.debug("Remove method : " + method.getName().getDefinition().toString());
                //     }
                //     else if (method.getBody() instanceof MethodBody){
                //         // TODO How to handle parameters ???
                //         MethodBody body = (MethodBody) method.getBody();
                //         LOG.debug("Optimizing body of : " + method.getName().getDefinition().toString());
                //         this.optimizeBlock(body.getVars(), body.getInsts());
                //     }
                // }

                // TODO remove don't remove overriding fields of used fields
                /* remove useless fields */
                // Iterator<AbstractDeclField> iterFields = currentClass.getFields().iterator();
                // while(iterFields.hasNext()){
                //     DeclField field= (DeclField)iterFields.next();
                //     if (!field.getName().getDefinition().isUsed()) {
                //         iterFields.remove();
                //         simplified = true;
                //         LOG.debug("Remove field : " + field.getName().getDefinition().toString());
                //     }
                // }
            }
        }
        return simplified;
    }
}
