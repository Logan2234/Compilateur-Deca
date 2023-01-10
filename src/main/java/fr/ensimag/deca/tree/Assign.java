package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue) super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type2 = this.getRightOperand().verifyRValue(compiler, localEnv, currentClass, type).getType();
        
        // Ajout du décor et renvoie du type
        if (type.sameType(type2)){
            this.setType(type);
            return type;
        }
        // TODO: Tester le cas où type2 est une sous classe de type1
        throw new ContextualError("An assignation between a " + type + " and a " + type2 + " is not possible (rule 3.32)", this.getLocation());
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister _r) {
        // put the right value in the left value !
        // put the result of the right value in a register
        GPRegister resultRegister = compiler.allocateRegister();
        if(resultRegister == null) {
            // todo : free a register then restore it
        }
        else {
            // compute right expression in the register
            this.getRightOperand().codeGenExpr(compiler, resultRegister);
            compiler.addInstruction(new STORE(resultRegister, getLeftOperand().getDefinition().getDAddr()));
        }
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dval) {
        throw new UnsupportedOperationException("This should never be called.");
    }
}
