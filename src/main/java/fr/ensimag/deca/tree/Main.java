package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);

    private ListDeclVar declVariables;
    private ListInst insts;

    public Main(ListDeclVar declVariables, ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        // LOG.debug("verify Main: start");
        // Création d'un EnvironnementExp vide
        EnvironmentExp localEnv = new EnvironmentExp(null);
        ClassDefinition object = new ClassDefinition(compiler.environmentType.OBJECT, getLocation(), null);
        declVariables.verifyListDeclVariable(compiler, localEnv, object);
        insts.verifyListInst(compiler, localEnv, object, compiler.environmentType.VOID);
        // LOG.debug("verify Main: end");
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) {
        compiler.addComment("Beginning of main variable declaration:");
        declVariables.codeGenDeclVar(compiler);
        compiler.addComment("Beginning of main instructions:");
        insts.codeGenListInst(compiler);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    @Override
    protected boolean spotUsedVar() {
        boolean varSpotted = declVariables.spotUsedVar();
        varSpotted = insts.spotUsedVar() || varSpotted;
        return varSpotted;
    }
    
    @Override
    protected Tree simplify() {
        this.declVariables = (ListDeclVar)this.declVariables.simplify();
        this.insts = (ListInst)this.insts.simplify();
        return this;
    }

    public ListDeclVar getListDeclVar() {
        return declVariables;
    }

    public ListInst getListInst() {
        return insts;
    }

    @Override
    public boolean collapse() {
        // if either one of the declaration or instructions collapsed, we collapsed
        return declVariables.collapse() || insts.collapse();
    }
}
