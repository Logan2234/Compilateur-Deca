package fr.ensimag.deca.codegen.runtimeErrors;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

public class InvalidReadErr extends AbstractRuntimeErr {
    public int errorId() {
        return 3;
    }

    public void codeGenErr(DecacCompiler compiler) {
        compiler.addInstruction(new WSTR("Error : Wrong input format."));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());
    }

    public Label getErrorLabel() {
        return new Label("Error.ReadError");
    }
}
