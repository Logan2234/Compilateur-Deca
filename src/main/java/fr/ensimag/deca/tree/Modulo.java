package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.REM;

import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.RemByZeroErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ParamDefinition;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!typeLeft.isInt() || !typeRight.isInt())
            throw new ContextualError("A modulo can only be done between 2 int (rule 3.33)", getLocation());

        // Ajout du décor
        setType(typeLeft);
        return typeLeft;
    }

    @Override
    protected String getOperatorName() {
        return "%";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal) {
        // mod op
        compiler.addInstruction(new REM(dVal, register));
        if (compiler.getCompilerOptions().getRunTestChecks()) {
            // add runtime division by zero check
            RemByZeroErr error = new RemByZeroErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
        }
    }

    @Override
    public boolean collapse() {
        return getLeftOperand().collapse() || getRightOperand().collapse();
    }

    @Override
    public Integer collapseInt() {
        Integer rightCollapsedValue = getRightOperand().collapseInt();
        if(rightCollapsedValue != null && getRightOperand().collapsable()) {
            IntLiteral newInt = new IntLiteral(rightCollapsedValue);
            newInt.setType(getType());
            setRightOperand(newInt);
        }
        Integer leftCollapsedValue = getLeftOperand().collapseInt();
        if(leftCollapsedValue != null && getLeftOperand().collapsable()) {
            IntLiteral newInt = new IntLiteral(leftCollapsedValue);
            newInt.setType(getType());
            setLeftOperand(newInt);
        }
        if(rightCollapsedValue != null && leftCollapsedValue != null) {
            return rightCollapsedValue % leftCollapsedValue;
        }
        return null;
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new Modulo(this.leftOperand.substitute(substitutionTable), this.rightOperand.substitute(substitutionTable));
        res.setLocation(this.getLocation());
        return res;
    }
}
