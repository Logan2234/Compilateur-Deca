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
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class Initialization extends AbstractInitialization {

    @Override
    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        expression = expression.verifyRValue(compiler, localEnv, currentClass, t);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(" = ");
        expression.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }

    @Override
    public void codeGenInit(DecacCompiler compiler, Type type, RegisterOffset resultRegister) {
        // call the code gen of the expression, and put it in the result register
        // get a register to store the result in.
        GPRegister register = compiler.allocateRegister();
        // get the expression to solve itself in the given register
        expression.codeGenExpr(compiler, register);
        // save the given register on the stack
        compiler.incrementContextUsedStack();
        if(resultRegister == null) {
            // free before pushing
            compiler.addInstruction(new LOAD(register, Register.R1));
            compiler.freeRegister(register);
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            compiler.addInstruction(new STORE(register, resultRegister));
            // free the register
            compiler.freeRegister(register);
        }
    }

    @Override
    protected void spotUsedVar() {
        this.expression.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar(Program prog) {
        this.expression = (AbstractExpr)this.expression.removeUnusedVar(prog);
        return this;
    }
    
    @Override
    public CollapseResult<CollapseValue> collapseInit() {
        return expression.collapseExpr();
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.expression = (AbstractExpr)this.expression.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    @Override
    public boolean hasInitialization() {
        return true;
    }
    
    @Override
    public AbstractInst factorise(DecacCompiler compiler) {
        expression = (AbstractExpr)expression.factorise(compiler);
        return null;
    }

    public boolean isSplitable(DecacCompiler compiler) {
        return expression.isSplitable(compiler);
    }
    
    public AbstractInst splitCalculus(DecacCompiler compiler){
        expression = (AbstractExpr)expression.splitCalculus(compiler);
        return null;
    }

}