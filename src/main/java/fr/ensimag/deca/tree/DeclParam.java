package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Declaration of a parameter
 * 
 * @author Jorge
 * @date 08/01/2023
 */
public class DeclParam extends AbstractDeclParam {

    final private AbstractIdentifier type;
    final private AbstractIdentifier paramName;

    public DeclParam(AbstractIdentifier type, AbstractIdentifier paramName) {
        Validate.notNull(type);
        Validate.notNull(paramName);
        this.type = type;
        this.paramName = paramName;
    }

    public AbstractIdentifier getName() {
        return this.paramName;
    }

    @Override
    protected Type verifyDeclParam(DecacCompiler compiler) throws ContextualError {
        Type type = this.type.verifyType(compiler);
        if (type.isVoid())
            throw new ContextualError("The parameter's type can't be void (rule 2.9)", getLocation());
        return type;
    }

    @Override
    protected void verifyParam(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        try {
            ParamDefinition def = new ParamDefinition(this.type.getType(), getLocation());
            localEnv.declare(paramName.getName(), def);
            paramName.verifyExpr(compiler, localEnv, currentClass);
        } catch (DoubleDefException e) {
            throw new ContextualError(
                    "The parameter \"" + paramName.getName().getName() + "\" has already been declared (rule 3.12)",
                    getLocation());
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(' ');
        paramName.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        paramName.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        paramName.prettyPrint(s, prefix, true);
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
    public void SetDAddr(RegisterOffset dAddr) {
        paramName.getDefinition().setDAddr(dAddr);
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        return this;
    }
}
