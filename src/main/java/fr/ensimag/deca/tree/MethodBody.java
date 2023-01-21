package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
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
    protected Tree simplify() {
        this.vars = (ListDeclVar)this.vars.simplify();
        this.insts = (ListInst)this.insts.simplify();
        return this;
    }

    @Override
    public boolean collapse() {
        // TODO
        return false;
    }
	@Override
    public void codeGenMethod(DecacCompiler compiler) {
        // generate code for delcare variables
        vars.codeGenDeclVar(compiler);
        insts.codeGenListInst(compiler);
    }

    @Override
    public void setReturnsNames(String name) {
        System.out.print("setting method name : ");
        System.out.println(name);
        for(AbstractInst inst : insts.getList()) {
            if(inst.isReturn()) {
                inst.asReturn().setMethodClassName(name);
            }
        }
    }
}
