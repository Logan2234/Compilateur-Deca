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
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

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
            // create an object on the heap, initialize it, return it in the result register
            // get the class we want to create
            ClassType type = classe.getType().asClassType("null", getLocation());
            compiler.addComment("New at line " + getLocation().getLine());
            compiler.addInstruction(new NEW(new ImmediateInteger(type.getDefinition().getNumberOfFields() + 1), resultRegister));
            AbstractRuntimeErr error = new FullHeapErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
            compiler.addComment("call init");
            // for calling init, the only param is the object : push it on the stack, than branch to init
            compiler.addInstruction(new PUSH(resultRegister));
            compiler.addInstruction(new BSR(new Label("init." + type.getName().getName())));
        }
        catch(ContextualError e) {
            throw new UnsupportedOperationException(e.toString());
        }
    }

}