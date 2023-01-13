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
 * Declaration of a method (for a class)
 * 
 * @author Jorge
 * @date 08/01/2023
 */
public class DeclMethod extends AbstractDeclMethod {

    final private AbstractIdentifier type;
    final private AbstractIdentifier methodName;
    final private ListDeclParam params;
    final private AbstractMethod body;

    public DeclMethod(AbstractIdentifier type, AbstractIdentifier methodName, ListDeclParam params,
            AbstractMethod body) {
        Validate.notNull(type);
        Validate.notNull(methodName);
        Validate.notNull(params);
        Validate.notNull(body);
        this.type = type;
        this.methodName = methodName;
        this.params = params;
        this.body = body;
    }

    @Override
    protected void verifyDeclMethod(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(' ');
        methodName.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");
        body.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        methodName.iter(f);
        params.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

    @Override
    protected void spotUsedVar() {
        // do nothing
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
    }
}
