package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.ADD;
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
        Type type = this.getOperand().verifyExpr(compiler, localEnv, currentClass);
        Location loc = this.getLocation();

        if (!type.isBoolean())
            throw new ContextualError("A not is only followed by a boolean (rule 3.37)", loc);
        
        // Ajout du décor
        this.setType(type);
        return type;
    }

    @Override
    protected String getOperatorName() {
        return "!";
    }

    @Override
    public void codeGenUnExpr(DecacCompiler compiler, GPRegister resulRegister) {
        // result expression is a bool and have been put in the register.
        // let's add 0 and check eq ? if 1, than the result was 0 : 0 + 0 = 0, therefore previous was 0
        compiler.addInstruction(new ADD(resulRegister, resulRegister));
        compiler.addInstruction(new SEQ(resulRegister));
    }

    @Override
    public AbstractExpr skipCalculs(){
        AbstractExpr operand = this.getOperand();
        if (!(operand.isLiteral())){

        }
        return this;
    }
}
