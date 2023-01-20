package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

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

    private String methodClassName;

    public void setMethodClassName(String name) {
        this.methodClassName = name;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {

        if (returnType.isVoid())
            throw new ContextualError("Return cannot be used when method has void type (rule 3.24)", getLocation());

                    expression.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // load the result in R0, then branch to method end
        GPRegister register = compiler.allocateRegister();
        expression.codeGenExpr(compiler, register);
        compiler.addInstruction(new LOAD(register, Register.R0));
        compiler.freeRegister(register);
        compiler.addInstruction(new BRA(new Label("end." + methodClassName)));
        
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expression.decompile(s);
        s.println(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }

    @Override
    public boolean isReturn() {
        return true;
    }

    @Override
    public Return asReturn() {
        return this;
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        this.expression.spotUsedVar(prog);
    }
}
