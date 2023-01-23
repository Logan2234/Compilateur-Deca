package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Opération binaire Boolean and, or
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeLeft = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type typeRight = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!typeLeft.isBoolean() || !typeRight.isBoolean())
            throw new ContextualError("A boolean operation has to be done only between 2 booleans (rule 3.33)",
                    getLocation());

        // Ajout du décor
        setType(typeLeft);
        return typeLeft;
    }

    /**
     * Override this to to lazy static evaluation
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // we still need a register for any binary op
        GPRegister leftRegister = register == null ? compiler.allocateRegister() : register;
        // call the binary expression code on the given register and the address
        // load left operand in the result register
        leftOperand.codeGenExpr(compiler, leftRegister);
        // lazy eval
        lazyEvaluation(compiler, leftRegister, new Label("lazyEval." + getLocation().toLabel()));
        // load the right operand
        GPRegister rightRegister = compiler.allocateRegister();
        rightOperand.codeGenExpr(compiler, rightRegister);
        // do the operation
        codeGenBinExp(compiler, leftRegister, rightRegister);
        // free right register
        compiler.freeRegister(rightRegister);
        // if the original register is null, load the result on the stack (also need to free the register)
        compiler.addLabel(new Label("lazyEval." + getLocation().toLabel()));
        if(register == null) {
            // load the rsesult in R1 to free the register (free might pop the stack)
            compiler.addInstruction(new LOAD(leftRegister, Register.R1));
            compiler.freeRegister(leftRegister);
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(leftRegister));
        }
    }

    public abstract void lazyEvaluation(DecacCompiler compiler, GPRegister resultRegister, Label toLabel);
}
