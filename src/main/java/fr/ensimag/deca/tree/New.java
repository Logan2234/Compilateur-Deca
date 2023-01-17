package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * New statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class New extends AbstractExpr {

    private final AbstractIdentifier classe;

    public New(AbstractIdentifier classe) {
        Validate.notNull(classe);
        this.classe = classe;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.classe.verifyType(compiler);
        if (!type.isClass()) {
            Location loc = this.getLocation();
            throw new ContextualError("New is only for classes", loc);
        }
        this.setType(type);
        return type;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new");
        classe.decompile(s);
        s.print("()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        classe.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classe.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        // do nothing
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }
}