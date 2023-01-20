package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Return statement.
 *
 * @author Jorge
 * @date 09/01/2023
 */
public class Return extends AbstractInst {

    private final AbstractExpr e;

    public Return(AbstractExpr e) {
        Validate.notNull(e);
        this.e = e;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {

        if (returnType.isVoid())
            throw new ContextualError("Return cannot be used when method has void type (rule 3.24)",
                    this.getLocation());

        e.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        e.decompile(s);
        s.print(";");

        // throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        e.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        e.prettyPrint(s, prefix, true);
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        this.e.spotUsedVar(prog);
    }

    public boolean factorised() {
        return false;//TODO
    }
    public boolean collapse() {
        return false;
    }

    @Override
    public ListInst collapseInst() {
        ListInst result = new ListInst();
        result.add(this);
        return result;
    }

    @Override
    public ListInst factoInst() {
        ListInst result = new ListInst();
        result.add(this);
        return result;
    }
}
