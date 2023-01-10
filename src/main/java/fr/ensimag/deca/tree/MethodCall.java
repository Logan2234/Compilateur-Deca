package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

/**
 * Method Call Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class MethodCall extends AbstractExpr {

    private final AbstractExpr obj;
    private final AbstractIdentifier meth; 
    private final ListExpr params;

    public MethodCall(AbstractExpr obj, AbstractIdentifier meth, ListExpr params) {
        Validate.notNull(meth);
        Validate.notNull(params);
        this.obj = obj;
        this.meth = meth;
        this.params = params;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (obj.equals(null) || obj.getImpl()) {
            obj.decompile(s);
            s.print(".");
        }
        meth.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");

    }

    @Override
    protected void iterChildren(TreeFunction f) {
        if (!(obj.equals(null))) {obj.iter(f);}
        meth.iter(f);
        params.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (!(obj.equals(null))) {obj.prettyPrint(s, prefix, false);}
        meth.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        throw new UnsupportedOperationException("not yet implemented");
    }

}