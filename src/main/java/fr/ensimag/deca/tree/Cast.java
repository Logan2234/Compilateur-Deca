package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Cast Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class Cast extends AbstractExpr {

    private AbstractIdentifier type;
    private AbstractExpr expression;

    public Cast(AbstractIdentifier type, AbstractExpr expression) {
        Validate.notNull(type);
        Validate.notNull(expression);
        this.type = type;
        this.expression = expression;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Location loc = this.getLocation();
        Type typeExp = this.expression.verifyExpr(compiler, localEnv, currentClass);
        Type typeT = this.type.verifyType(compiler);

        if (typeExp.isVoid() || (!typeExp.assignCompatible(typeT) && !typeT.assignCompatible(typeExp)))
            throw new ContextualError("Unable to cast type \"" + typeExp.getName().getName() + "\" to \""
                    + typeT.getName().getName() + "\"", getLocation());
        
        if (typeT.isInt() && typeExp.isFloat()){
            ConvInt convint = new ConvInt(expression);
            convint.setLocation(expression.getLocation());
            convint.verifyExpr(compiler, localEnv, currentClass);
            expression = convint;
        }
        
        if (typeT.isFloat() && typeExp.isInt()){
            ConvFloat convfloat = new ConvFloat(expression);
            convfloat.setLocation(expression.getLocation());
            convfloat.verifyExpr(compiler, localEnv, currentClass);
            expression = convfloat;
        }
            
        // Ajout du décor
        setType(typeT);
        return typeT;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        type.decompile(s);
        s.print(")(");
        expression.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        expression.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // the conetxt told it was valid, only need to compute expression
        expression.codeGenExpr(compiler, resultRegister);
    }

    @Override
    protected void spotUsedVar() {
        this.type.spotUsedVar();
        this.expression.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        this.expression = (AbstractExpr)this.expression.removeUnusedVar();
        return this;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        // the expression could be obtained via a MethodCall
        this.expression.addUnremovableExpr(foundMethodCalls);
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.expression = (AbstractExpr)this.expression.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new Cast((AbstractIdentifier) this.type.substitute(substitutionTable),this.expression.substitute(substitutionTable));
        res.setLocation(this.getLocation());
        return res;
    }
}