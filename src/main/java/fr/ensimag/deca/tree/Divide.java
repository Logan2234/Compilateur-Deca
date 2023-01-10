package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
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
        // depending on self type, float or int div
        // todo : in both case, divide by zero error gen
        if(getType().isInt()) {
            compiler.addInstruction(new QUO(dval, register));
        }
        else if(getType().isFloat()) {
            compiler.addInstruction(new DIV(dval, register));
        }
    }
}
