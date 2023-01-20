package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.List;

/**
 * Integer literal
 *
 * @author Jorge Luri Vañó
 * @date 08/01/2023
 */
public class Null extends AbstractExpr {

    public Null() {
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // Ajout du décor
        setType(compiler.environmentType.NULL);
        return compiler.environmentType.NULL;
    }

    @Override
    String prettyPrintNode() {
        return "null";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("null");
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
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        if(resultRegister == null) {
            // push null immediate on the stack
            compiler.addInstruction(new LOAD(new NullOperand(), Register.R1));
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            compiler.addInstruction(new LOAD(new NullOperand(), resultRegister));
        }
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        // do nothing
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }

}