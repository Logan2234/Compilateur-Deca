package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
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

    public ListDeclMethod getMethods() {
        return methods;
    }

    public ListDeclField getFields() {
        return fields;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        name.decompile(s);
        s.print(" extends ");
        superIdentifier.decompile(s);
        s.println(" {");
        s.indent();
        fields.decompile(s);
        methods.decompile(s);
        s.unindent();
        s.print("}\n");
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        Definition def = compiler.environmentType.defOfType(name.getName());

        if (def != null)
            throw new ContextualError("\"" + name.getName().getName() + "\" is already a type/class (rule 1.3)",
                    getLocation());
        if (!superIdentifier.verifyType(compiler).isClass())
            throw new ContextualError("\"" + superIdentifier.getName().getName() + "\" is not a class (rule 1.3)",
                    getLocation());

        ClassDefinition superClassDef = superIdentifier.getType().asClassType("Not a class type", getLocation())
                .getDefinition();
        ClassType classType = new ClassType(name.getName(), getLocation(), superClassDef);
        compiler.environmentType.set(name.getName(), classType.getDefinition());

        // Ajout du décor
        name.verifyType(compiler);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler) throws ContextualError {
        // On reprend les fields et methods de la classe mère
        name.getType().asClassType(null, getLocation()).getDefinition().setNumberOfFields(
                superIdentifier.getType().asClassType(null, getLocation()).getDefinition().getNumberOfFields());
        name.getType().asClassType(null, getLocation()).getDefinition().setNumberOfMethods(
                superIdentifier.getType().asClassType(null, getLocation()).getDefinition().getNumberOfMethods());

        ClassDefinition def = name.getClassDefinition();
        fields.verifyListDeclField(compiler, def.getMembers(), def); // TODO: Vérifier la condition de la règle 2.3
        methods.verifyListDeclMethod(compiler, def.getMembers(), def);
    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        ClassDefinition def = name.getClassDefinition();
        fields.verifyListInitField(compiler, def.getMembers(), def);
        methods.verifyListDeclMethodBody(compiler, def.getMembers(), def);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, false);
        superIdentifier.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        superIdentifier.iter(f);
        fields.iter(f);
        methods.iter(f);
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        // do nothing
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
    }

    public AbstractIdentifier getName() {
        return name;
    }
}
