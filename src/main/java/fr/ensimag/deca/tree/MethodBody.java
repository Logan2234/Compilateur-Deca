package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Method Body Statement
 *
 * @author gl03
 * @date 05/01/2023
 */
public class MethodBody extends AbstractMethod {

    public MethodBody(ListDeclVar vars, ListInst insts) {
        Validate.notNull(vars);
        Validate.notNull(insts);
        this.vars = vars;
        this.insts = insts;
    }

    private ListDeclVar vars;
    private ListInst insts;
    
    public ListDeclVar getVars() {
        return vars;
    }

    public ListInst getInsts() {
        return insts;
    }

    @Override
    public void verifyMethod(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentclass, Type type)
            throws ContextualError {
        vars.verifyListDeclVariable(compiler, localEnv, currentclass);
        insts.verifyListInst(compiler, localEnv, currentclass, type);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        vars.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        vars.iterChildren(f);
        insts.iterChildren(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        vars.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    @Override
    protected void spotUsedVar() {
        this.vars.spotUsedVar();
        this.insts.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        this.vars = (ListDeclVar)this.vars.removeUnusedVar();
        this.insts = (ListInst)this.insts.removeUnusedVar();
        return this;
    }

	@Override
    public void codeGenMethod(DecacCompiler compiler) {
        // generate code for delcare variables
        vars.codeGenDeclVar(compiler);
        insts.codeGenListInst(compiler);
    }

    @Override
    public void setReturnsNames(String name) {
        for(AbstractInst inst : insts.getList()) {
            if(inst.isReturn()) {
                inst.asReturn().setMethodClassName(name);
            }
        }
    }

    @Override
    public CollapseResult<Null> collapseMethodBody() {
        return new CollapseResult<Null>(null, vars.collapseDeclVars().couldCollapse() || insts.collapseInsts().couldCollapse());
    }
    
    public boolean isInline() {
        if (this.vars.getList().isEmpty()
        && this.insts.getList().size() == 1
        && this.insts.getList().get(0) instanceof Return
        && !((Return)this.insts.getList().get(0)).getExpression().containsField()) {
            // an unremovable expression is an Assign, a MethodCall or a Read
            // an assign could change the state of a parameter
            Return ret = (Return)this.insts.getList().get(0);
            for (AbstractExpr expr : ret.getExpression().getUnremovableExpr()) {
                if (expr instanceof Assign){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

	@Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.vars = (ListDeclVar)this.vars.doSubstituteInlineMethods(inlineMethods);
        this.insts = (ListInst)this.insts.doSubstituteInlineMethods(inlineMethods);
        return this;
    }
    
    public AbstractInst factorise(DecacCompiler compiler) {
        vars.factorise(compiler);
        insts.factorise(compiler);
        return null;
    }

    @Override
    public boolean isSplitable(DecacCompiler compiler){
        return vars.isSplitable(compiler) || insts.isSplitable(compiler);
    }

    @Override
    public AbstractInst splitCalculus(DecacCompiler compiler) {
        vars.splitCalculus(compiler);
        insts.splitCalculus(compiler);
        return null;
    }
    
}

