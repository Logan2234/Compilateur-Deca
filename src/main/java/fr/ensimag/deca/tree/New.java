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
        Type type = this.classe.verifyType(compiler);
        if (!type.isClass()) {
            Location loc = this.getLocation();
            throw new ContextualError("New is only for classes", loc);
        }
        this.setType(type);
        return type;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new");
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
            boolean needRegisterSpace = resultRegister == null;
            if(needRegisterSpace) {
                // save R2
                compiler.addInstruction(new PUSH(Register.getR(2)));
                resultRegister = Register.getR(2);
            }
            // create an object on the heap, initialize it, return it in the result register
            // get the class we want to create
            ClassType type = classe.getType().asClassType("null", getLocation());
            compiler.addComment("New at line " + getLocation().getLine());
            compiler.addInstruction(new NEW(new ImmediateInteger(type.getDefinition().getNumberOfFields() + 1), resultRegister));
            // manage heap overflow error
            AbstractRuntimeErr error = new FullHeapErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
            // set the pointer to the vtable
            compiler.addInstruction(new LEA(classe.getClassDefinition().getDAddr(), Register.R0));
            compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(0, resultRegister)));
            // we'll push the result before call anyway, so restore R2 now anyway
            if(needRegisterSpace) {
                compiler.addInstruction(new POP(Register.R0));
            }
            compiler.addComment("call init");
            // for calling init, the only param is the object : push it on the stack, than branch to init
            compiler.addInstruction(new PUSH(resultRegister));
            if(needRegisterSpace) {
                // load R0 in R2 to finally restore it
                compiler.addInstruction(new LOAD(Register.R0, Register.getR(2)));
            }
            compiler.addInstruction(new BSR(new Label("init." + type.getName().getName())));
            compiler.addInstruction(new POP(resultRegister));
        }
        catch(ContextualError e) {
            throw new UnsupportedOperationException(e.toString());
        }
    }

}