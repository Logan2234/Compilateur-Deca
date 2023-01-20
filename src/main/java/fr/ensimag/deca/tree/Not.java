package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = getOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!type.isBoolean())
            throw new ContextualError("A not is only followed by a boolean (rule 3.37)", getLocation());
        
        // Ajout du décor
        setType(type);
        return type;
    }

    @Override
    protected String getOperatorName() {
        return "!";
    }

    @Override
    public void codeGenUnExpr(DecacCompiler compiler, GPRegister resulRegister) {
        // result expression is a bool and have been put in the register.
        // compare the result with zero, than check equality : true if it was false
        compiler.addInstruction(new CMP(0, resulRegister));
        compiler.addInstruction(new SEQ(resulRegister));
    }

    @Override
    public boolean collapse() {
        return getOperand().collapse();
    }

    @Override
    public Boolean collapseBool() {
        Boolean collapsedValue = getOperand().collapseBool();
        if(collapsedValue != null) {
            Type oldType = getOperand().getType();
            BooleanLiteral newBool = new BooleanLiteral(collapsedValue);
            newBool.setType(oldType);
            setOperand(newBool);
            return !collapsedValue;
        }
        return null;
    }
}
