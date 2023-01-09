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
        type.verifyType(compiler);

        Type type = this.type.getType();
        
        if (type.isVoid())
        throw new ContextualError("Une variable ne peut pas être de type void (règle 3.17)", this.getLocation());
        
        try {
            ExpDefinition def = new VariableDefinition(type, this.getLocation());
            localEnv.declare(this.varName.getName(), def);
            varName.setDefinition(def);
            varName.setType(type);
        }
        catch (DoubleDefException e) {
            throw new ContextualError("La variable \"" + this.varName.getName().getName() + "\" a déjà été déclarée (règle 3.17)", this.getLocation());
        }
        
        initialization.verifyInitialization(compiler, type, localEnv, currentClass);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(' ');
        varName.decompile(s);
        initialization.decompile(s);
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
}
