package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
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

    public DeclClass(AbstractIdentifier name, AbstractIdentifier superIdentifier, ListDeclField fields, ListDeclMethod methods) {
        Validate.notNull(name);
        Validate.notNull(superIdentifier);
        Validate.notNull(fields);
        Validate.notNull(methods);
        this.name = name;
        this.superIdentifier = superIdentifier;
        this.fields = fields;
        this.methods = methods;
    }

    public ListDeclMethod getMethods() {
        return this.methods;
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
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, false);
        superIdentifier.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);

        //throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        superIdentifier.iter(f);
        fields.iter(f);
        methods.iter(f);
        //throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    protected void spotUsedVar() {
        // do nothing
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
    }
}
