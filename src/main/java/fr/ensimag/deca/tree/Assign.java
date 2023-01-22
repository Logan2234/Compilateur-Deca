package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
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
        Type type = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        getRightOperand().verifyRValue(compiler, localEnv, currentClass, type);
        
        // Ajout du décor et renvoie du type
        setType(type);
        return type;
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    public void codeGenExpr(DecacCompiler compiler, GPRegister resulRegister) {
        // put the right value in the left value !
        // put the result of the right value in a register
        GPRegister register = compiler.allocateRegister();
        this.getRightOperand().codeGenExpr(compiler, register);
        if(getLeftOperand().getDefinition().isField()) {
            // if we have a field, we are in a method. load object from -2(SP) and then get the field from offset.
            GPRegister classPointerRegister = compiler.allocateRegister();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), classPointerRegister));
            compiler.addInstruction(new STORE(register, new RegisterOffset(getLeftOperand().getDefinition().getDAddrOffsetOnly(), classPointerRegister)));
            compiler.freeRegister(classPointerRegister);
            if(resulRegister == null) {
                compiler.incrementContextUsedStack();
                compiler.addInstruction(new PUSH(register));
            }
            else {
                compiler.addInstruction(new LOAD(register, resulRegister));
            }
        }
        else {
            compiler.addInstruction(new STORE(register, getLeftOperand().getDefinition().getDAddr()));
            if(resulRegister == null) {
                compiler.incrementContextUsedStack();
                compiler.addInstruction(new PUSH(register));
            }
            else {
                compiler.addInstruction(new LOAD(register, resulRegister));
            }
        }
        // free the alocated register
        compiler.freeRegister(register);
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dval) {
        throw new UnsupportedOperationException("This should never be called.");
    }

    @Override
    protected boolean spotUsedVar() {
        // we don't spot leftOperand
        return this.rightOperand.spotUsedVar();
    }
    
    @Override 
    public CollapseResult<CollapseValue> collapseBinExpr() {
        CollapseResult<CollapseValue> result = getRightOperand().collapseExpr();
        if(getLeftOperand().getType().isBoolean() && result.getResult().isBool()) {
            setRightOperand(new BooleanLiteral(result.getResult().asBool()));
            // tell we collapsed but no result, too dangerous to remove assignments
            return new CollapseResult<CollapseValue>(new CollapseValue(), true);
        }
        else if(getLeftOperand().getType().isFloat() && result.getResult().isFloat()) {
            setRightOperand(new FloatLiteral(result.getResult().asFloat()));
            // tell we collapsed but no result, too dangerous to remove assignments
            return new CollapseResult<CollapseValue>(new CollapseValue(), true);
        }
        else if(getLeftOperand().getType().isInt() && result.getResult().isInt()) {
            setRightOperand(new IntLiteral(result.getResult().asInt()));
            // tell we collapsed but no result, too dangerous to remove assignments
            return new CollapseResult<CollapseValue>(new CollapseValue(), true);
        }
        else {
            // tell if at some point the expression collapsed
            return new CollapseResult<CollapseValue>(new CollapseValue(), result.couldCollapse());
        }
    }

}
