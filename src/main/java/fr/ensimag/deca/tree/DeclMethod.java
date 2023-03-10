package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ReturnCheckFunc;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.NoReturnErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import java.util.HashMap;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

import java.util.Iterator;
import java.util.Map;


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
    private AbstractMethod body;

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
    protected void verifyDeclMethod(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.type.verifyType(compiler);

        Signature signature = params.verifyListDeclParam(compiler);
        MethodDefinition methodeDef;

        // Test de la m??thode potentiellement existente dans la classe m??re
        ExpDefinition defExp = currentClass.getSuperClass().getMembers().get(methodName.getName());
        if (defExp != null) {
            // On cherche ?? savoir si c'est bien une m??thode
            MethodDefinition motherMethod = defExp.asMethodDefinition("The name \"" + methodName.getName().getName()
                    + "\" is already used for a field in the superclass (rule 2.7)", getLocation());
            if (!motherMethod.getSignature().sameSignature(signature))
                throw new ContextualError("The method \"" + methodName.getName().getName()
                        + "\" doesn't have the same signature as the method defined it the superclass (rule 2.7)",
                        getLocation());

            Type motherMethodType = motherMethod.getType();
            try {
                ClassType motherMethodClassType = motherMethodType.asClassType("Not a class type", getLocation());
                ClassType classType = type.asClassType("Not a class type", getLocation());
                if (!classType.isSubClassOf(motherMethodClassType))
                    throw new ContextualError(
                            "The return type is not the same as defined in the superclass (or not a subtype) (rule 2.7)",
                            getLocation());
            } catch (ContextualError e) {
                if (!motherMethodType.sameType(type) && !(motherMethodType.isFloat() && type.isInt()))
                    throw new ContextualError(
                            "The return type is not the same as defined in the superclass (or not a subtype) (rule 2.7)",
                            getLocation());
            }
            methodeDef = new MethodDefinition(type, this.getLocation(), signature, motherMethod.getIndex(), currentClass);
        } else {
            currentClass.incNumberOfMethods();
            methodeDef = new MethodDefinition(type, this.getLocation(), signature, currentClass.getNumberOfMethods(), currentClass);
        }
        try {
            localEnv.declare(methodName.getName(), methodeDef);
            methodName.verifyExpr(compiler, localEnv, currentClass);
        } catch (DoubleDefException e) {
            throw new ContextualError(
                    "The method \"" + methodName.getName().getName() + "\" has already been declared (rule 2.6)",
                    getLocation());
        }
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        EnvironmentExp methodEnv = new EnvironmentExp(localEnv);
        params.verifyListParam(compiler, methodEnv, currentClass);
        body.verifyMethod(compiler, methodEnv, currentClass, this.type.getType());
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
    public String getMethodName() {
        return methodName.getName().getName();
    }
    
    public void codeGenMethod(DecacCompiler compiler, String className) {
        // set the labels for the returns
        body.iter(new ReturnCheckFunc(className + "." + methodName.getName().getName()));
        // set the daddr of the params
        for(int i = 0; i < params.size(); i++) {
            params.getList().get(i).SetDAddr(new RegisterOffset(-(3 + i), Register.LB));
        }
        // write down the label
        compiler.addLabel(new Label("code." + className + "." + getMethodName()));
        // let's create a context for the body so we can use addInstruction First 
        compiler.newCodeContext();
        // generate method body
        body.codeGenMethod(compiler);
        // label of end of method
        // if the mehtod does not return void, no return error
        if(!type.getType().isVoid() && compiler.getCompilerOptions().getRunTestChecks()) {
            AbstractRuntimeErr error = new NoReturnErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BSR(error.getErrorLabel()));
        }
        compiler.addLabel(new Label("end." + className + "." + methodName.getName().getName()));
        // save and restore context used registers 
        for(GPRegister usedRegister : compiler.getAllContextUsedRegister()) {
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new POP(usedRegister));
            compiler.addInstructionFirst(new PUSH(usedRegister));
        }
        // add max stack use at the beginning
        if(compiler.getCompilerOptions().getRunTestChecks()) {
            AbstractRuntimeErr error = new StackOverflowErr();
            compiler.useRuntimeError(error);
            compiler.addInstructionFirst(new BOV(error.getErrorLabel()));
            compiler.addInstructionFirst(new TSTO(compiler.getMaxStackUse()));
        }
        // end the context
        compiler.endCodeContext();
        // add return
        compiler.addInstruction(new RTS());
    }
    

    @Override
    public void spotUsedVar() {
        this.type.spotUsedVar();
        this.body.spotUsedVar();
        this.methodName.spotUsedVar();
        // we spot the param when they are used in the body
    }

    @Override
    protected Tree removeUnusedVar(Program prog) {
        if (!this.methodName.getDefinition().isUsed()) {
            prog.setVarRemoved();
            return null;
        }
        this.body = (AbstractMethod)this.body.removeUnusedVar(prog);
        return this;
    }


    public AbstractIdentifier getName() {
        return this.methodName;
    }

    public AbstractMethod getBody() {
        return this.body;
    }

    @Override
    public AbstractInst factorise(DecacCompiler compiler) {
        body.factorise(compiler);
        return null;
    }

    public boolean isSplitable(DecacCompiler compiler) {
        return body.isSplitable(compiler);
    }

    @Override
    public AbstractInst splitCalculus(DecacCompiler compiler) {
        body.splitCalculus(compiler);
        return null;
    }

    @Override
    public CollapseResult<Null> collapseDeclMethod() {
        return new CollapseResult<Null>(null, body.collapseMethodBody().couldCollapse());
    }

    @Override
    protected void spotInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        if (this.body.isInline()) {
            inlineMethods.put(this.methodName.getMethodDefinition(),this);
        }
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.body = (AbstractMethod)this.body.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    /**
     * Method used to substitute inline methods.
     * It returns the expression of the body's Return (its only instruction) with the parameters subsituted
     * @param list of input parameters
     * @return expression with parameters of the method susbituted with input parameters
     */
    public AbstractExpr getSubsitution(ListExpr inputParams) {
        Map<ParamDefinition,AbstractExpr> substitutionTable = new HashMap<ParamDefinition,AbstractExpr>();
        Iterator<AbstractDeclParam> iterParam = this.params.getList().iterator();
        Iterator<AbstractExpr> iterInputParam = inputParams.getList().iterator();
        while (iterParam.hasNext() && iterInputParam.hasNext()) {
            substitutionTable.put(((DeclParam)iterParam.next()).getName().getParamDefinition(), iterInputParam.next());
        }
        AbstractInst inst = ((MethodBody)this.body).getInsts().getList().get(0);
        return ((Return)inst).getExpression().substitute(substitutionTable);
    }

}
