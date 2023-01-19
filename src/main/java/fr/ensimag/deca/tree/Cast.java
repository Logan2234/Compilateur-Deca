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
 * Cast Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class Cast extends AbstractExpr {

    private final AbstractIdentifier type;
    private final AbstractExpr e;

    public Cast(AbstractIdentifier type, AbstractExpr e) {
        Validate.notNull(type);
        Validate.notNull(e);
        this.type = type;
        this.e = e;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Location loc = this.getLocation();
        Type typeExp = this.e.verifyExpr(compiler, localEnv, currentClass);
        Type typeT = this.type.verifyType(compiler);

        if (typeExp.isVoid()
                || (!typeExp.assignCompatible(typeT) && !typeT.assignCompatible(typeExp))) {
            throw new ContextualError("Unable to cast type \"" + typeExp.getName().getName() + "\" to \""
                    + typeT.getName().getName() + "\"", loc);
        }

        // Ajout du décor
        this.setType(typeT);
        return typeT;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        type.decompile(s);
        s.print(")(");
        e.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        e.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        e.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        this.type.spotUsedVar(prog);
        this.e.spotUsedVar(prog);
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // the expression could be obtained via a MethodCall
        this.e.addMethodCalls(foundMethodCalls);
    }
}