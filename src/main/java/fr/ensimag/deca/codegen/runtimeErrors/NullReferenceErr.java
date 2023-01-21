package fr.ensimag.deca.codegen.runtimeErrors;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

public class NullReferenceErr extends AbstractRuntimeErr {
    
    @Override
    public int errorId() {
        return 5;
    }

    public void codeGenErr(DecacCompiler compiler) {
        compiler.addInstruction(new WSTR("Error : Null reference exception."));
        compiler.addInstruction(new WNL());
        compiler.addInstruction(new ERROR());
    }

    public Label getErrorLabel() {
        return new Label("Error.NullReference");
    }
}
