package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.UserAsmBlock;

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
    protected boolean spotUsedVar() {
        return false;
    }
	@Override
    public void codeGenMethod(DecacCompiler compiler) {
        compiler.add(new UserAsmBlock(code.getValue()));
    }

    @Override
    public void setReturnsNames(String name) {
        // useless here ?
    }

    @Override
    public CollapseResult<Null> collapseMethodBody() {
        // no collapse for asm method
        return new CollapseResult<Null>(null, false);
    }
}
