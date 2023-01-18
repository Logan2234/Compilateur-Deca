package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.SNE;

import java.io.PrintStream;
import java.io.PushbackInputStream;

import org.apache.commons.lang.Validate;

/**
 * InstanceOf Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class InstanceOf extends AbstractExpr {

    private final AbstractExpr e;
    private final AbstractIdentifier type;

    public InstanceOf(AbstractExpr e, AbstractIdentifier type) {
        Validate.notNull(e);
        Validate.notNull(type);
        this.e = e;
        this.type = type;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Location loc = this.getLocation();
        Type typeE = this.e.verifyExpr(compiler, localEnv, currentClass);
        Type typeT = this.type.verifyType(compiler);
        if (!typeE.isClassOrNull() || !typeT.isClass()) {
            throw new ContextualError("instanceof argument has to be a class (rule 3.40)", loc);
        }
        
        // Ajout du décor
        this.setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        e.decompile(s);
        s.print(" instanceof ");
        type.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        e.iter(f);
        type.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        e.prettyPrint(s, prefix, false);
        type.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // compute the result of the expression (pointer to object) in a register.
        e.codeGenExpr(compiler, Register.R1);
        // get a register to put the comparing value in 
        GPRegister register = compiler.allocateRegister();
        boolean needRegisterSpace = register == null;
        if(needRegisterSpace) {
            compiler.addInstruction(new PUSH(Register.getR(3)));
            register = Register.getR(2);
        }
        // put the comparing value in it
        compiler.addInstruction(new LEA(type.getDefinition().getDAddr(), register));
        // load the pointer to the class of the object in that same register
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));
        // if R1 is null, return false. if R1 is the type, return true. else, loop
        compiler.addLabel(new Label("instanceof.loop." + getLocation().toLabel()));
        compiler.addInstruction(new CMP(new NullOperand(), Register.R1));
        compiler.addInstruction(new SNE(Register.R0)); // if R1 is null, than a sne will load a zero in R0 for result.
        compiler.addInstruction(new BEQ(new Label("instanceof.end." + getLocation().toLabel())));
        compiler.addInstruction(new CMP(register, Register.R1)); // test against the obj
        compiler.addInstruction(new SEQ(Register.R0)); // if equals, put 1 in R0 as the result ! 
        compiler.addInstruction(new BEQ(new Label("instanceof.end." + getLocation().toLabel())));
        // still not branching out ? keep looping !
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));
        compiler.addInstruction(new BRA(new Label("instanceof.loop." + getLocation().toLabel())));
        compiler.addLabel(new Label("instanceof.end." + getLocation().toLabel()));
        if(needRegisterSpace) {
            // restore r2
            compiler.addInstruction(new POP(Register.getR(2)));
        }
        else {
            compiler.freeRegister(register);
        }
        if(resultRegister == null) {
            // push result on the stack
            compiler.addInstruction(new PUSH(Register.R0));
        }
        else {
            // load the result in the result register
            compiler.addInstruction(new LOAD(Register.R0, resultRegister));
        }
    }

}