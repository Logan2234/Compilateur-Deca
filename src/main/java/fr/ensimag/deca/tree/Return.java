package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Return statement.
 *
 * @author Jorge
 * @date 09/01/2023
 */
public class Return extends AbstractInst {

    private final AbstractExpr expression;

    public Return(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {

        if (returnType.isVoid())
            throw new ContextualError("Return cannot be used when method has void type (rule 3.24)",
                    this.getLocation());

                    expression.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // load the result in R0, then branch to method end
        expression.codeGenExpr(compiler, Register.R0);

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expression.decompile(s);
        s.println(";");

        // throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }
}
