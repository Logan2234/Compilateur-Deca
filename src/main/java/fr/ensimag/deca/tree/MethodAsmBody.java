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
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Method asm Body Statement
 *
 * @author gl03
 * @date 05/01/2023
 */
public class MethodAsmBody extends AbstractMethod {
    private StringLiteral code;

    public MethodAsmBody(StringLiteral code) {
        Validate.notNull(code);
        this.code = code;
    }

    public StringLiteral getCode() {
        return code;
    }

    @Override
    public void verifyMethod(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentclass, Type type)
            throws ContextualError {
        code.verifyExpr(compiler, localEnv, currentclass);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("asm(");
        code.decompile(s);
        s.print(");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        code.iterChildren(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        s.println(prefix + "`> " + code.getLocation() + " StringLiteral");
        code.prettyPrintType(s, prefix + "   ");
        // code.prettyPrint(s, prefix, true);
        code.prettyPrintChildren(s, prefix);
    }

	@Override
    public boolean collapse() {
        // TODO
        return false;
    }

    @Override
    protected void spotUsedVar() {
        // do nothing
    }
    
    @Override
    protected Tree removeUnusedVar() {
        return this;
    }
    
	@Override
    public void codeGenMethod(DecacCompiler compiler) {
        // todo : find a way to insert a string litteral in the code
    }

    @Override
    public void setReturnsNames(String name) {
        // useless here ?
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        return this;
    }

}
