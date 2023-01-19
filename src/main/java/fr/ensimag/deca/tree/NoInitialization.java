package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.Register;

import java.io.PrintStream;

/**
 * Absence of initialization (e.g. "int x;" as opposed to "int x =
 * 42;").
 *
 * @author gl03
 * @date 01/01/2023
 */
public class NoInitialization extends AbstractInitialization {

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
    }

    /**
     * Node contains no real information, nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // nothing
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void codeGenInit(DecacCompiler compiler) {
        // push a zero to the stack : LOAD #0 R0; PUSH R0;
        compiler.addInstruction(new LOAD(0, Register.R0));
        compiler.incrementContextUsedStack();
        compiler.addInstruction(new PUSH(Register.R0));
    }

    @Override
    public boolean collapse() {
        // TODO
        return false;
    }

    @Override
    public boolean irrelevant() {
        return false;
    }

    @Override
    public boolean hasInitialization(){
        return false;
    }

}
