package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Declaration of a variable
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class DeclVar extends AbstractDeclVar {

    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.type.verifyType(compiler);

        if (type.isVoid())
            throw new ContextualError("A variable can't be void (rule 3.17)", getLocation());

        try {
            ExpDefinition def = new VariableDefinition(type, getLocation());
            initialization.verifyInitialization(compiler, type, localEnv, currentClass);
            localEnv.declare(varName.getName(), def);
            varName.verifyExpr(compiler, localEnv, currentClass);
        } catch (DoubleDefException e) {
            throw new ContextualError(
                    "The variable \"" + varName.getName().getName() + "\" has already been declared (rule 3.17)",
                    getLocation());
        }

    }

    @Override
    public void codeGenDeclVar(DecacCompiler compiler, RegisterOffset register) {
        // store the register in the definition of the variable, then assign it with the
        // initialization.
        varName.getDefinition().setDAddr(register);
        // store the variable at the address now, using a push as we are declaring all
        // variables.
        initialization.codeGenInit(compiler, type.getType(), register);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(' ');
        varName.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        // We don't spotUsedVar() on the type (it may be a class) or the identifier as
        // they are just declared.
        this.initialization.spotUsedVar(prog);
    }

    public AbstractIdentifier getVar() {
        return this.varName;
    }

    public AbstractInitialization getInit() {
        return this.initialization;
    }
    public boolean collapse() {
        return initialization.collapse();
    }

    @Override
    public boolean factorised(DecacCompiler compiler) {
        return initialization.factorised(compiler);
    }

    @Override
    public AbstractInst factoInst(DecacCompiler compiler){
        initialization.factoInst(compiler);
        return null;
    }
}
