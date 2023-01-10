package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * read...() statement.
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractReadExpr extends AbstractExpr {

    public AbstractReadExpr() {
        super();
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        codeGenRead(compiler);
        // put R1 in the asked result
        if(resultRegister != null) {
            compiler.addInstruction(new LOAD(Register.R1, resultRegister));
        }
        else {
            compiler.addInstruction(new PUSH(Register.R1));
        }
    }

    /**
     * Generate the instruction to read a value and put it in R1.
     * @param compiler Where we write the instructions to.
     */
    protected abstract void codeGenRead(DecacCompiler compiler);


}
