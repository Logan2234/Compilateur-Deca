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
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * This statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class This extends AbstractExpr {

    private final boolean implicit;

    public This(boolean implicit) {
        this.implicit = implicit;
    }

    @Override
    public boolean getImpl() {
        return implicit;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        if (currentClass.getType().getName().getName() == "Object")
            throw new ContextualError("This can only be used in a class (rule 3.43)", getLocation());

        setType(currentClass.getType());
        return currentClass.getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (!implicit)
            s.print("this");
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
        // put pointer in the result register
        if(resultRegister == null) {
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), resultRegister));
        }
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
        // return nothing ? expect if we find a way to compute methods at compile time...
        return new CollapseResult<CollapseValue>(new CollapseValue(), false);
    }
    
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new This(this.implicit);
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

    @Override
    protected boolean containsField() {
        return false;
    }

    @Override
    public boolean isThis(){
        return true;
    }

}