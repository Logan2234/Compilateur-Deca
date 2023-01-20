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
 * InstanceOf Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class InstanceOf extends AbstractExpr {

    private final AbstractExpr e;
    private final AbstractIdentifier type;

    public InstanceOf(AbstractExpr e, AbstractIdentifier type) {
        Validate.notNull(e);
        Validate.notNull(type);
        this.e = e;
        this.type = type;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Location loc = this.getLocation();
        Type typeE = this.e.verifyExpr(compiler, localEnv, currentClass);
        Type typeT = this.type.verifyType(compiler);
        if (!typeE.isClassOrNull() || !typeT.isClass()) {
            throw new ContextualError("instanceof argument has to be a class (rule 3.40)", loc);
        }
        
        // Ajout du décor
        this.setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        e.decompile(s);
        s.print(" instanceof ");
        type.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        e.iter(f);
        type.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        e.prettyPrint(s, prefix, false);
        type.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected boolean spotUsedVar() {
        // TODO evaluate to false if class not used and don't spot the right hand side
        // We don't spotUsedVar on the class type.
        // If the class is not used elsewhere then the expression will be evaluated to false.
        boolean varSpotted = this.type.spotUsedVar();
        return this.e.spotUsedVar() || varSpotted;
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // the expression could be obtained via a MethodCall
        this.e.addMethodCalls(foundMethodCalls);
    }

    public AbstractExpr getExpr() {
        return this.e;
    }
}