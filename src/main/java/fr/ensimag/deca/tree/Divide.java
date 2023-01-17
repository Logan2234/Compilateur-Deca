package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.DivByZeroErr;
import fr.ensimag.deca.codegen.runtimeErrors.OpOverflowErr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dval) {
        
        if(getType().isInt()) {
            compiler.addInstruction(new QUO(dval, register));
            // depending on self type, float or int div
            if(compiler.getCompilerOptions().getRunTestChecks()) {
                // add runtime division by zero check
                DivByZeroErr error = new DivByZeroErr();
                compiler.useRuntimeError(error);
                compiler.addInstruction(new BOV(error.getErrorLabel()));
            }
        }
        else if(getType().isFloat()) {
            compiler.addInstruction(new DIV(dval, register));
            // depending on self type, float or int div
            if(compiler.getCompilerOptions().getRunTestChecks()) {
                // add runtime division by zero check
                OpOverflowErr error = new OpOverflowErr();
                compiler.useRuntimeError(error);
                compiler.addInstruction(new BOV(error.getErrorLabel()));
            }
        }

    }
}
