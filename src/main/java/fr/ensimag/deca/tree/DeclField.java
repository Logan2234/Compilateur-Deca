package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Declaration of a field
 * 
 * @author Jorge
 * @date 05/01/2023
 */
public class DeclField extends AbstractDeclField {

    private Visibility visib;
    final private AbstractIdentifier type;
    final private AbstractIdentifier fieldName;
    final private AbstractInitialization initialization;

    public DeclField(AbstractIdentifier type, AbstractIdentifier fieldName, AbstractInitialization initialization, Visibility visib) {
        Validate.notNull(type);
        Validate.notNull(fieldName);
        Validate.notNull(initialization);
        Validate.notNull(visib);
        this.type = type;
        this.fieldName = fieldName;
        this.visib = visib;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclField(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.type.getType();
        try {
            FieldDefinition def = new FieldDefinition(type, this.getLocation(), visib, currentClass, 0);
            localEnv.declare(this.fieldName.getName(), def);
            fieldName.setDefinition(def);
            fieldName.setType(type);
        }
        catch (DoubleDefException e) {
            throw new ContextualError("The field \"" + this.fieldName.getName().getName() + "\" has already been declared (rule 3.17)", this.getLocation());
        }
        initialization.verifyInitialization(compiler, type, localEnv, currentClass);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(visib.toString());
        s.print(' ');
        type.decompile(s);
        s.print(' ');
        fieldName.decompile(s);
        initialization.decompile(s);
        s.print(';');
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        type.iter(f);
        fieldName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
}