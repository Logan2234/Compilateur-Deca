package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Boolean literal
 *
 * @author gl03
 * @date 01/01/2023
 */
public class BooleanLiteral extends AbstractExpr {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // Ajout du décor
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        if (resultRegister != null) {
            // put it in the result register
            compiler.addInstruction(new LOAD(value ? 1 : 0, resultRegister));
        } else {
            // push it on the stack
            compiler.addInstruction(new LOAD(value ? 1 : 0, Register.R1));
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Boolean.toString(value));
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
    String prettyPrintNode() {
        return "BooleanLiteral (" + value + ")";
    }

    @Override
    protected void spotUsedVar() {
        // do nothing
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }

    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        // can't collapse, but is a boolean value to collapse !
        return new CollapseResult<CollapseValue>(new CollapseValue(value), false);
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition, AbstractExpr> substitutionTable) {
        AbstractExpr res = new BooleanLiteral(this.value);
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

    @Override
    protected boolean containsField() {
        return false;
    }

    @Override
    protected boolean isAtomic() {
        return true;
    }
}
