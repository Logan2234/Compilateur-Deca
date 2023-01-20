package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Cast Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class Cast extends AbstractExpr {

    private final AbstractIdentifier type;
    private final AbstractExpr expression;

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

        if (typeExp.isVoid()
                || (!typeExp.assignCompatible(localEnv, typeT) && !typeT.assignCompatible(localEnv, typeExp))) {
            throw new ContextualError("Unable to cast type \"" + typeExp.getName().getName() + "\" to \""
                    + typeT.getName().getName() + "\"", loc);
        }

        // Ajout du décor
        this.setType(typeT);
        return typeT;
    }

    /**
     * Check if the two types are compatible for the cast
     * 
     * @param localEnv the local environment
     * @param typeExp  the type of the expression to cast
     * @param typeT    the type of the expected cast
     * @return true if the two types are compatible, false if not
     * 
     * @author Nils Depuille
     * @date 12/01/2023
     */

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
    protected void spotUsedVar(AbstractProgram prog) {
        this.type.spotUsedVar(prog);
        this.expression.spotUsedVar(prog);
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // the expression could be obtained via a MethodCall
        this.expression.addMethodCalls(foundMethodCalls);
    }
}