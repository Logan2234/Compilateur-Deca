package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.REM;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.RemByZeroErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

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
        Type typeLeft = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        Location loc = this.getLocation();

        if (!typeLeft.isInt() || !typeRight.isInt())
            throw new ContextualError("A modulo can only be done between 2 int (rule 3.33)", loc);
        
        // Ajout du décor
        this.setType(typeLeft);
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
        if(compiler.getCompilerOptions().getRunTestChecks()) {
            // add runtime division by zero check
            RemByZeroErr error = new RemByZeroErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
        }
    }

    @Override
    public boolean factorised() {
        return false;//TODO
    }

}
