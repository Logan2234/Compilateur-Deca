package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Print statement (print, println, ...).
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();

    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
        for (AbstractExpr a : arguments.getList()) {
            Type type = a.verifyExpr(compiler, localEnv, currentClass);
            if (!type.isInt() && !type.isFloat() && !type.isString())
                throw new ContextualError("Arguments of a print can only be float, int or string (rules 3.31)",
                        a.getLocation());
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        for (AbstractExpr a : getArguments().getList())
            a.codeGenPrint(compiler, getPrintHex());
    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("print");
        s.print(getSuffix());
        if (getPrintHex())
            s.print("x");
        s.print("(");
        arguments.decompile(s);
        s.print(");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        this.arguments.spotUsedVar(prog);
    }

    @Override
    public boolean factorised(DecacCompiler compiler) {
        return arguments.factorised(compiler);
    }

    @Override
    public AbstractInst factoInst(DecacCompiler compiler) {
        arguments.factoInst(compiler);
        return this;
    }
}
