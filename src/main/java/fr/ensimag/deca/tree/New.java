package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.FullHeapErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * New statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class New extends AbstractExpr {

    private final AbstractIdentifier classe;

    public New(AbstractIdentifier classe) {
        Validate.notNull(classe);
        this.classe = classe;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = classe.verifyType(compiler);

        if (!type.isClass())
            throw new ContextualError("New is only for classes", getLocation());

        setType(type);
        return type;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        classe.decompile(s);
        s.print("()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        classe.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classe.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        try {
            GPRegister register = resultRegister == null ? compiler.allocateRegister() : resultRegister;
            // create an object on the heap, initialize it, return it in the result register
            // get the class we want to create
            ClassType type = classe.getType().asClassType("Unable to get as class type while generating code for 'new' statement", getLocation());
            compiler.addComment("New at line " + getLocation().getLine());
            compiler.addInstruction(new NEW(new ImmediateInteger(type.getDefinition().getNumberOfFields() + 1), register));
            // manage heap overflow error
            if(compiler.getCompilerOptions().getRunTestChecks()) {
                AbstractRuntimeErr error = new FullHeapErr();
                compiler.useRuntimeError(error);
                compiler.addInstruction(new BOV(error.getErrorLabel()));
            }
            // set the pointer to the vtable
            compiler.addInstruction(new LEA(classe.getClassDefinition().getDAddr(), Register.R1));
            compiler.addInstruction(new STORE(Register.R1, new RegisterOffset(0, register)));
            // call init ?
            compiler.addComment("call init");
            if(resultRegister == null) {
                compiler.addInstruction(new LOAD(register, Register.R1)); 
                compiler.freeRegister(register);
                compiler.incrementContextUsedStack();
                compiler.addInstruction(new PUSH(Register.R1));
            }
            else {
                compiler.incrementContextUsedStack();
                compiler.addInstruction(new PUSH(register));
            }
            // branch to init
            compiler.addInstruction(new BSR(new Label("init." + type.getName().getName())));
            // pop the result if we needed it in a register
            compiler.addInstruction(new POP(resultRegister == null ? Register.R1 : resultRegister));
        }
        catch(ContextualError e) {
            throw new UnsupportedOperationException(e.toString());
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