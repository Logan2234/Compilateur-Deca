package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.INT;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Conversion of a float into an int. Used for implicit conversions.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class ConvInt extends AbstractUnaryExpr {
    public ConvInt(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) {
        setType(compiler.environmentType.INT);
        return compiler.environmentType.INT;
    }

    @Override
    protected String getOperatorName() {
        return "/* conv int */";
    }

    @Override
    public void codeGenUnExpr(DecacCompiler compiler, GPRegister resultRegister) {
        compiler.addInstruction(new INT(resultRegister, resultRegister));
    }

}