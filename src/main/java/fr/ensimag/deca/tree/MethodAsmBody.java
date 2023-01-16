package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Method asm Body Statement
 *
 * @author gl03
 * @date 05/01/2023
 */
public class MethodAsmBody extends AbstractMethod {
    
    public MethodAsmBody(StringLiteral code) {
        Validate.notNull(code);
        this.code = code;
    }
    private StringLiteral code;
    
    public StringLiteral getCode() {
        return code;
    }

    @Override
    public void verifyMethod(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentclass, Type type) throws ContextualError {
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
       //TODO
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
    @Override //? Is it necessary ?
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        code.prettyPrintChildren(s, prefix);
    }

    @Override
    public boolean collapse() {
        // TODO
        return false;
    }
}
