package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Single precision, floating-point literal
 *
 * @author gl03
 * @date 01/01/2023
 */
public class FloatLiteral extends AbstractExpr {

    public float getValue() {
        return value;
    }

    private float value;

    public FloatLiteral(float value) {
        Validate.isTrue(!Float.isInfinite(value), "literal values cannot be infinite");
        Validate.isTrue(!Float.isNaN(value), "literal values cannot be NaN");
        this.value = value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        setType(compiler.environmentType.FLOAT);
        return compiler.environmentType.FLOAT;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(java.lang.Float.toString(value));
    }

    @Override
    String prettyPrintNode() {
        return "Float (" + getValue() + ")";
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
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // load ourselves in the given register, or on the stack
        if (resultRegister != null) {
            compiler.addInstruction(new LOAD(value, resultRegister));
        } else {
            // put it in R1 then on the stack
            compiler.addInstruction(new LOAD(value, Register.R1));
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean hex) {
        // load ourselves in R1, then print it with WFLOAT
        compiler.addInstruction(new LOAD(value, Register.R1));
        if (hex) {
            compiler.addInstruction(new WFLOATX());
        } else {
            compiler.addInstruction(new WFLOAT());
        }
    }

    @Override
    public boolean collapse() {
        return true;
    }

    @Override
    protected void spotUsedVar() {
        // do nothing
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }
    public Float collapseFloat() {
        return value;
    }

    @Override
    public boolean collapsable() {
        return false;
    }

}
