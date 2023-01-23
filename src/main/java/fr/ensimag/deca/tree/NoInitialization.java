package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import java.io.PrintStream;
import java.util.Map;

/**
 * Absence of initialization (e.g. "int x;" as opposed to "int x =
 * 42;").
 *
 * @author gl03
 * @date 01/01/2023
 */
public class NoInitialization extends AbstractInitialization {

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        // nothing
    }

    /**
     * Node contains no real information, nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // nothing
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void codeGenInit(DecacCompiler compiler, Type type, RegisterOffset resultRegister) {
        // push a zero to the stack : LOAD #0 R0; PUSH R0;
        if(type.isInt()) {
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0));
        }
        else if(type.isFloat()) {
            compiler.addInstruction(new LOAD(new ImmediateFloat(0), Register.R0));
        }
        else if(type.isBoolean()) {
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0));
        }
        else if(type.isClass()) {
            compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        }
        else {
            throw new UnsupportedOperationException("Default init not available for object " + type + " (at" + getLocation() + ").");
        }
        if(resultRegister == null) {
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R0));
        }
        else {
            compiler.addInstruction(new STORE(Register.R0, resultRegister));
        }
    }

    @Override
    protected void spotUsedVar() {
        // do nothing
    }
    
    @Override
    public CollapseResult<CollapseValue> collapseInit() {
        return new CollapseResult<CollapseValue>(new CollapseValue(), false);
    }
    
    protected Tree removeUnusedVar() {
        return this;
    }
    
    @Override
    protected AbstractExpr getExpression() {
        return null;
    }


    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        return this;
    }

    @Override
    public boolean hasInitialization(){
        return false;
    }

}
