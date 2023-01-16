package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class DeclClass extends AbstractDeclClass {

    final private AbstractIdentifier name;
    final private AbstractIdentifier superIdentifier;
    final private ListDeclField fields;
    final private ListDeclMethod methods;

    public DeclClass(AbstractIdentifier name, AbstractIdentifier superIdentifier, ListDeclField fields,
            ListDeclMethod methods) {
        Validate.notNull(name);
        Validate.notNull(superIdentifier);
        Validate.notNull(fields);
        Validate.notNull(methods);
        this.name = name;
        this.superIdentifier = superIdentifier;
        this.fields = fields;
        this.methods = methods;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        name.decompile(s);
        s.print(" extends ");
        superIdentifier.decompile(s);
        s.print(" {");
        fields.decompile(s);
        methods.decompile(s);
        s.print("}");

    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        Definition def = compiler.environmentType.defOfType(name.getName());
        if (def != null)
            throw new ContextualError(name.getName().getName() + " is already a class (rule 1.3)", this.getLocation());
        if (!this.superIdentifier.verifyType(compiler).isClass())
            throw new ContextualError(superIdentifier.getName().getName() + " is not a class (rule 1.3)",
                    this.getLocation());

        ClassType classType = new ClassType(name.getName(), this.getLocation(), ((ClassType)superIdentifier.getType()).getDefinition());
        compiler.environmentType.set(name.getName(), new TypeDefinition(classType, this.getLocation()));
        name.verifyType(compiler);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        Definition def = compiler.environmentType.defOfType(name.getName());
        if (def != null)
            throw new ContextualError(name.getName().getName() + " is already a class (rule 2.3)", this.getLocation());
        if (!this.superIdentifier.verifyType(compiler).isClass())
            throw new ContextualError(superIdentifier.getName().getName() + " is not a class (rule 2.3)",
                    this.getLocation());
        
    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        throw new UnsupportedOperationException("not yet implemented"); // Rule 3.5
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, false);
        superIdentifier.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);

        // throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        superIdentifier.iter(f);
        fields.iter(f);
        methods.iter(f);
        // throw new UnsupportedOperationException("Not yet supported");
    }

}
