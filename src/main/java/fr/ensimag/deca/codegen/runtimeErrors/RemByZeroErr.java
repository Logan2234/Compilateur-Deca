package fr.ensimag.deca.codegen.runtimeErrors;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

public class RemByZeroErr extends AbstractRuntimeErr {
    public int errorId() {
        return 5;
    }

    public void codeGenErr(DecacCompiler compiler) {
        compiler.addInstruction(new WSTR("Error : remainer by zero"));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());
    }

    public Label getErrorLabel() {
        return new Label("Error.RemByZero");
    }
}
