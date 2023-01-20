package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
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

    final private Visibility visib;
    final private AbstractIdentifier type;
    final private AbstractIdentifier fieldName;
    final private AbstractInitialization initialization;

    public DeclField(AbstractIdentifier type, AbstractIdentifier fieldName, AbstractInitialization initialization,
            Visibility visib) {
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
    protected void verifyDeclField(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.type.verifyType(compiler);

        if (type.isVoid())
            throw new ContextualError("The field's type can't be void (rule 2.5)", getLocation());

        FieldDefinition def;

        // Si le nom existe déjà dans une classe parente
        ExpDefinition defExp = currentClass.getSuperClass().getMembers().get(this.fieldName.getName());
        if (defExp != null) {
            // On cherche à savoir si c'est bien un Field
            FieldDefinition motherField = defExp.asFieldDefinition("The name \"" + fieldName.getName().getName()
                    + "\" is already used for a method in the superclass (rule 2.5)", this.getLocation());
            def = new FieldDefinition(type, this.getLocation(), visib, currentClass, motherField.getIndex());
        }
        currentClass.incNumberOfFields();
        def = new FieldDefinition(type, this.getLocation(), visib, currentClass, currentClass.getNumberOfFields());
        try {
            localEnv.declare(this.fieldName.getName(), def);
            fieldName.verifyExpr(compiler, localEnv, currentClass);
        } catch (DoubleDefException e) {
            throw new ContextualError(
                    "The field \"" + this.fieldName.getName().getName() + "\" has already been declared (rule 2.4)",
                    this.getLocation());
        }
    }

    @Override
    protected void verifyInitField(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        initialization.verifyInitialization(compiler, type.getType(), localEnv, currentClass);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print((visib == Visibility.PROTECTED ? "protected " : ""));
        type.decompile(s);
        s.print(' ');
        fieldName.decompile(s);
        initialization.decompile(s);
        s.print(';');
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        fieldName.iter(f);
        initialization.iter(f);
    }

    @Override
    String prettyPrintNode() {
        return "[visibility=" + visib + "] DeclField";
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    protected boolean spotUsedVar() {
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
        return false;
    }

    public AbstractIdentifier getName() {
        return this.fieldName;
    }
    public boolean collapse() {
        initialization.collapse();
        return false;
    }
}