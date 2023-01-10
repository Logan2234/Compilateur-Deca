package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import static org.mockito.Mockito.after;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) {
        this.setType(compiler.environmentType.FLOAT);
        return compiler.environmentType.FLOAT;
    }

    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // compute operand and put it in the result register, or R1 of not
        if(resultRegister != null) {
            getOperand().codeGenExpr(compiler, resultRegister);
            compiler.addInstruction(new FLOAT(resultRegister, resultRegister));
        }
        else {
            // compute the float in R1, then push it on the stack
            getOperand().codeGenExpr(compiler, null);
            compiler.addInstruction(new FLOAT(new RegisterOffset((-1), Register.SP), Register.R1));
            // push R1
            compiler.addInstruction(new PUSH(Register.R1));
        }
    }

}
