package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.util.List;

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
        Type type = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        rightOperand.verifyRValue(compiler, localEnv, currentClass, type);
        
        // Ajout du décor et renvoie du type
        setType(type);
        return type;
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
        this.rightOperand.codeGenExpr(compiler, resultRegister);
        if(getLeftOperand().getDefinition().isField()) {
            // if we have a field, we are in a method. load object from -2(SP) and then get the field from offset.
            GPRegister classPointerRegister = compiler.allocateRegister();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), classPointerRegister));
            compiler.addInstruction(new STORE(resultRegister, new RegisterOffset(getLeftOperand().getDefinition().getDAddrOffsetOnly(), classPointerRegister)));
            compiler.freeRegister(classPointerRegister);
        }
        else {
            // compute right expression in the register
            this.rightOperand.codeGenExpr(compiler, resultRegister);
            compiler.addInstruction(new STORE(resultRegister, getLeftOperand().getDefinition().getDAddr()));
        }
        // free the alocated register
        compiler.freeRegister(resultRegister);
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dval) {
        throw new UnsupportedOperationException("This should never be called.");
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        // we don't spot leftOperand
        this.rightOperand.spotUsedVar(prog);
    }
    
    @Override
    public AbstractInst factorise(DecacCompiler compiler) {
        return rightOperand.factorise(compiler);
    }

    @Override
    public boolean isSplitable(DecacCompiler compiler) {
        return rightOperand.isSplitable(compiler);
    }

    @Override 
    public boolean collapse() {
        if(rightOperand.collapse()) {
            if(rightOperand.getType().isBoolean()) {
                // try to collapse to bool expr
                Boolean collapsedValue = rightOperand.collapseBool();
                if(collapsedValue != null) {
                    setRightOperand(new BooleanLiteral(collapsedValue));
                }
            }
            else if(rightOperand.getType().isInt()) {
                // try to collapse to a int expr
                Integer collapsedValue = rightOperand.collapseInt();
                if(collapsedValue != null) {
                    setRightOperand(new IntLiteral(collapsedValue));
                }
            }
            else if(rightOperand.getType().isFloat()) {
                // try to collapse to a float
                Float collapsedValue = rightOperand.collapseFloat();
                if(collapsedValue != null) {
                    setRightOperand(new FloatLiteral(collapsedValue));
                }
            }
            return true;
        }
        return false;
    }

}
