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
            simplified = simplified || this.optimizeBlock(((Main)this.main).getListDeclVar(),((Main)this.main).getListInst());
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
        Iterator<AbstractDeclVar> iterDecl = listDecls.iterator();
        while(iterDecl.hasNext()){
            DeclVar decl = (DeclVar) iterDecl.next();
            if (decl.getVar().getDefinition().isUsed()) {
                break;
            } else if (decl.getInit() instanceof Initialization
                       && ((Initialization)(decl.getInit())).getExpression().containsMethodCall()) {
                break;
            } else {
                iterDecl.remove();
                simplified = true;
                LOG.debug("Remove the decl of "+decl.getVar().getDefinition().toString());
            }
        }
        // Equivalent loop
        // List listDecl = ((Main)this.main).getListDeclVar().getModifiableList();
        // if (listDecl.removeIf(decl -> !((DeclVar)decl).getVar().getDefinition().isUsed())) {
        //     LOG.debug("Some var removed : " + listDecl.toString());
        // }

        /* remove useless instructions form the main */
        Iterator<AbstractInst> iterInst = listInsts.iterator();
        while(iterInst.hasNext()){
            AbstractInst inst = iterInst.next();

            if (inst instanceof Assign && !((AbstractExpr)inst).containsMethodCall()) {

                if (((Assign)inst).getLeftOperand() instanceof Identifier) {
                    Identifier ident = (Identifier)((Assign)inst).getLeftOperand();
                    if (!ident.getDefinition().isUsed()){
                        iterInst.remove();
                        simplified = true;
                        LOG.debug("Remove inst at "+inst.getLocation() + " : " + inst.getClass());
                    } 
                }
                // ? refer to the getDefinition() method of Selection as I the following code depends on it
                // ? and we had doubt on it
                else if (((Assign)inst).getLeftOperand() instanceof Selection) {
                    Selection select = (Selection)((Assign)inst).getLeftOperand();
                    if (!select.getDefinition().isUsed()){
                        iterInst.remove();
                        simplified = true;
                        LOG.debug("Remove inst at "+inst.getLocation() + " : " + inst.getClass());
                    } 
                }
            }

            else if (inst instanceof AbstractExpr && !((AbstractExpr)inst).containsMethodCall()){
                iterInst.remove();
                simplified = true;
                LOG.debug("Remove inst at "+inst.getLocation() + " : " + inst.getClass());
            }

            else if (inst instanceof NoOperation){
                iterInst.remove();
                simplified = true;
                LOG.debug("Remove inst at "+inst.getLocation() + " : " + inst.getClass());
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
        // TODO Check if we don't remove super classes
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
                        // TODO How to handle parameters ???
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

    //private boolean optimizeBlock() {}
}
