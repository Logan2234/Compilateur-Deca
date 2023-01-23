package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Return statement.
 *
 * @author Jorge
 * @date 09/01/2023
 */
public class Return extends AbstractInst {

    private AbstractExpr expression;

    public Return(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    private String methodClassName;

    public void setMethodClassName(String name) {
        this.methodClassName = name;
    }

    public AbstractExpr getExpression() {
        return this.expression;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {

        if (returnType.isVoid())
            throw new ContextualError("Return cannot be used when method has void type (rule 3.24)", getLocation());

        expression = expression.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // load the result in R0, then branch to method end
        GPRegister register = compiler.allocateRegister();
        expression.codeGenExpr(compiler, register);
        compiler.addInstruction(new LOAD(register, Register.R0));
        compiler.freeRegister(register);
        compiler.addInstruction(new BRA(new Label("end." + methodClassName)));

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expression.decompile(s);
        s.println(";");
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
    public boolean isReturn() {
        return true;
    }

    @Override
    public Return asReturn() {
        return this;
    }

    @Override
    protected void spotUsedVar() {
        this.expression.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        this.expression = (AbstractExpr)this.expression.removeUnusedVar();
        return this;

    }

    @Override
    public CollapseResult<ListInst> collapseInst() {
        // return nothing ? expect if we find a way to compute methods at compile time...
        ListInst result = new ListInst();
        result.add(this);
        return new CollapseResult<ListInst>(result, false);
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.expression = (AbstractExpr)this.expression.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    @Override
    public boolean irrelevant(){
        if (inWhile) return false;
        if (expression.irrelevant() || expression.isSelection()){
            if (expression.isSelection()){
                AbstractExpr out = ((Selection) expression).returnIrrelevantFromSelection();
                if (out != null) {
                    expression = out;
                }
            }
            else {
                expression = currentValues.get(((Identifier) expression).getName());
            }
        }
        return expression.irrelevant();
    }

    @Override
    public boolean irrelevant(int i){
        if (expression.irrelevant(i) || expression.isSelection()){
            if (expression.isSelection()){
                AbstractExpr out = ((Selection) expression).returnIrrelevantFromSelection(i);
                if (out != null) {
                    expression = out;
                }
            }
            else {
                expression = irrelevantValuesForIf.get(i).get(((Identifier) expression).getName());
            }
        }
        return expression.irrelevant(i);
    }

    @Override
    public AbstractInst factorise(DecacCompiler compiler) {
        expression.factorise(compiler);
        return this;
    }

    @Override
    public boolean isSplitable(DecacCompiler compiler){
        return expression.isSplitable(compiler);
    }

    @Override
    public AbstractInst splitCalculus(DecacCompiler compiler) {
        expression = (AbstractExpr)expression.splitCalculus(compiler);
        return this;
    }
}
