package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.OpOverflowErr;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.MUL;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "*";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal) {
        // mult
        compiler.addInstruction(new MUL(dVal, register));
        // check overflow
        if(getType().isFloat() && compiler.getCompilerOptions().getRunTestChecks()) {
            OpOverflowErr error = new OpOverflowErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
        }
    }

    @Override
    public boolean factorised() {
        if (leftOperand.isLiteral() ^ rightOperand.isLiteral()) {
            if (leftOperand.isLiteral() && leftOperand.getType().isInt())
                if (((IntLiteral) leftOperand).getValue() < 4)
                    //Developper en somme
                    return true;
            if (leftOperand.isLiteral() && leftOperand.getType().isFloat())
                return false; // On ne developpe pas les floats en somme car il faudra dans tous les cas faire des additions donc pas optim
            if (rightOperand.getType().isInt())
                if (((IntLiteral) rightOperand).getValue() < 4)
                        //Developper en somme
                        return true;
            return false;
        }
        return false;
    }
}
