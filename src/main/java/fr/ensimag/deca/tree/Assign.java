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

import java.util.List;
import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ParamDefinition;

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
        setRightOperand(getRightOperand().verifyRValue(compiler, localEnv, currentClass, type));
        
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
        // put the result of the right value in the result register, this way it is already were we ask
        this.getRightOperand().codeGenExpr(compiler, resulRegister);
        // then call the code gen assign on the left value, if result register is null it will get it from the stack
        this.getLeftOperand().codeGenAssignLVal(compiler, resulRegister);
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dval) {
        throw new UnsupportedOperationException("This should never be called.");
    }

    @Override
    protected void spotUsedVar() {
        // we don't spot leftOperand
        this.rightOperand.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        this.rightOperand = (AbstractExpr)this.rightOperand.removeUnusedVar();
        if (!this.getLeftOperand().getDefinition().isUsed()) {
            return this.rightOperand;
        }
        return this;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        foundMethodCalls.add(this);
    }

    @Override
    boolean isAssign() {
        return true;
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

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        // An assign could change the state of a parameter.
        // So a method containing an assign should not be substituted.
        assert(false);
        return this;
    }
}
