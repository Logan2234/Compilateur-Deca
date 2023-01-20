package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;
import java.util.List;

import javax.swing.plaf.synth.Region;

import org.apache.commons.lang.Validate;

/**
 * Cast Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class Cast extends AbstractExpr {

    private AbstractIdentifier type;
    private AbstractExpr e;

    public Cast(AbstractIdentifier type, AbstractExpr e) {
        Validate.notNull(type);
        Validate.notNull(e);
        this.type = type;
        this.e = e;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeExp = e.verifyExpr(compiler, localEnv, currentClass);
        Type typeT = type.verifyType(compiler);

        if (typeExp.isVoid() || (!typeExp.assignCompatible(typeT) && !typeT.assignCompatible(typeExp)))
            throw new ContextualError("Unable to cast type \"" + typeExp.getName().getName() + "\" to \""
                    + typeT.getName().getName() + "\"", getLocation());
        
        if (typeT.isInt() && typeExp.isFloat()){
            ConvInt convint = new ConvInt(e);
            convint.setLocation(e.getLocation());
            convint.verifyExpr(compiler, localEnv, currentClass);
            e = convint;
        }
        
        if (typeT.isFloat() && typeExp.isInt()){
            ConvFloat convfloat = new ConvFloat(e);
            convfloat.setLocation(e.getLocation());
            convfloat.verifyExpr(compiler, localEnv, currentClass);
            e = convfloat;
        }
            
        // Ajout du décor
        setType(typeT);
        return typeT;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        type.decompile(s);
        s.print(")(");
        e.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        e.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        e.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // at this point the context told the cast is valid.
        // let's change the value of the vtable pointer to point to the given type.
        e.codeGenExpr(compiler, Register.R1);
        // R1 is now the object, get the pointer to the vtable
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));
        // load the new value in
        compiler.addInstruction(new LOAD(type.getDefinition().getDAddr(), Register.R1));
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        this.type.spotUsedVar(prog);
        this.e.spotUsedVar(prog);
    }

    @Override
    protected void addMethodCalls(List<AbstractExpr> foundMethodCalls) {
        // the expression could be obtained via a MethodCall
        this.e.addMethodCalls(foundMethodCalls);
    }
}