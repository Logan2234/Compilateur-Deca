package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.NullReferenceErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Method Call Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class MethodCall extends AbstractExpr {

    private AbstractExpr obj;
    private final AbstractIdentifier meth;
    private ListExpr params;

    public MethodCall(AbstractExpr obj, AbstractIdentifier meth, ListExpr params) {
        Validate.notNull(meth);
        Validate.notNull(params);
        this.obj = obj;
        this.meth = meth;
        this.params = params;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeClass = obj.verifyExpr(compiler, localEnv, currentClass);
        if (!typeClass.isClass())
            throw new ContextualError("The object of the method call is not of type class (rule 3.71)", getLocation());

        // On s'occupe de récuperer la signature et le type de retour de la methode
        // on verify l'expression de la methode
        Type typeReturn = meth.verifyExpr(compiler,
                typeClass.asClassType(null, getLocation()).getDefinition().getMembers(), currentClass);
        Signature sig = meth.getMethodDefinition().getSignature();

        if (sig.size() != params.getList().size())
            throw new ContextualError(
                    "The method " + meth.getName().getName() + " needs " + sig.size() + " params (rule 3.28)",
                    getLocation());
        for (int i = 0; i < sig.size(); i++) {
            Type type = params.getList().get(i).verifyExpr(compiler, localEnv, currentClass);
            if (!sig.paramNumber(i).assignCompatible(type))
                throw new ContextualError(
                        "The parameter number " + (i + 1) + " does not have the correct type (rule 3.28)",
                        getLocation());
        }

        setType(typeReturn);
        return typeReturn;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (!obj.getImpl()) {
            obj.decompile(s);
            s.print(".");
        }
        meth.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        if (!(obj.equals(null)))
            obj.iter(f);
        meth.iter(f);
        params.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        if (!(obj.equals(null)))
            obj.prettyPrint(s, prefix, false);
        meth.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // put the params on the stack, in reverse order
        compiler.addComment("Method call : " + meth.getName().getName());
        for(int i = params.size() - 1; i >= 0; i--) {
            params.getList().get(i).codeGenExpr(compiler, null); // null so the result will be on the stack !
        }
        // push the object on the stack
        obj.codeGenExpr(compiler, null);
        // call the bsr with the correct method adress
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.R1));
        // null reference exception
        if(compiler.getCompilerOptions().getRunTestChecks()) {
            compiler.addInstruction(new CMP(new NullOperand(), Register.R1));
            AbstractRuntimeErr error = new NullReferenceErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BEQ(error.getErrorLabel()));
        }
        // go to the method table !
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));
        // then get the method with it's correct offset.
        // the offset is the index !
        compiler.addInstruction(new BSR(new RegisterOffset(meth.getMethodDefinition().getIndex(), Register.R1)));
        // pop everything in R1
        for(int i = params.size(); i >= 0; i--) { // +1 for the object !
            compiler.addInstruction(new POP(Register.R1));
        }
        // if the method returned something, it is now in R0 ! put it as a result
        if(resultRegister == null) {
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R0));
        }
        else {
            compiler.addInstruction(new LOAD(Register.R0, resultRegister));
        }
    }

    @Override
    protected void spotUsedVar() {
        this.obj.spotUsedVar();
        this.meth.spotUsedVar();
        this.params.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        this.obj = (AbstractExpr)this.obj.removeUnusedVar();
        this.params = (ListExpr)this.params.removeUnusedVar();
        return this;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        foundMethodCalls.add(this);
    }

    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        // return nothing ? expect if we find a way to compute methods at compile time...
        return new CollapseResult<CollapseValue>(new CollapseValue(), false);
    }
    
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.params = (ListExpr)this.params.doSubstituteInlineMethods(inlineMethods);
        if (!inlineMethods.containsKey(this.meth.getMethodDefinition())) {
            return this;
        }
        // An inline methode should not be susbtituted if it takes as a parameter a method call,
        // an assign or a read because it may be duplicated.
        if(this.params.getUnremovableExpr().isEmpty()) {
            return inlineMethods.get(this.meth.getMethodDefinition()).getSubsitution(this.params);
        }
        return this;
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        ListExpr listExpr = new ListExpr();
        for(AbstractExpr expr : this.params.getList()) {
            listExpr.add(expr.substitute(substitutionTable));
        }
        AbstractExpr res = new MethodCall(this.obj.substitute(substitutionTable),(AbstractIdentifier) this.meth.substitute(substitutionTable),listExpr);
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

    @Override
    protected boolean containsField() {
        return this.obj.containsField() || this.meth.containsField() || this.params.containsField();
    }
}