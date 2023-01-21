package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;

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
    private ListDeclField fields;
    private ListDeclMethod methods;

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
    public void initClassCodeGen(DecacCompiler compiler) {
        // give each field it's offset before we generate any code
        for(int i = 0; i < fields.size(); i++) {
            fields.getList().get(i).setFieldOffset(compiler, i + 1);
        }
    }

    @Override
    protected void spotUsedVar() {
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
    }

    @Override
    protected Tree simplify() {
        if (!this.name.getDefinition().isUsed()) {
            return null;
        }
        this.fields = (ListDeclField)this.fields.simplify();
        this.methods = (ListDeclMethod)this.methods.simplify();
        return this;
    }

    public AbstractIdentifier getName() {
        return name;
    }
    
    @Override
    public boolean collapse() {
        fields.collapse();
        methods.collapse();
        return false;
    }

    @Override
    public void codeGenVTable(DecacCompiler compiler) {
        // generate the vtable for that class.
        // get the VTable addr
        RegisterOffset VTableDAddr = compiler.getNextStackSpace();
        name.getClassDefinition().setDAddr(VTableDAddr);
        // generate the VTable
        compiler.addComment("========== VTable for " + name.getName() + " ==========");
        // load pointer to parent
        compiler.addInstruction(new LEA(superIdentifier.getDefinition().getDAddr(), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, VTableDAddr));
        // get method number
        int methodNumber = name.getClassDefinition().getNumberOfMethods();
        boolean[] writtenMethods = new boolean[methodNumber];
        // write each method if it has not been written already
        ClassDefinition currentClass = name.getClassDefinition();
        while(currentClass != null) {
            for(MethodDefinition method : currentClass.getMembers().getMethods()) {
                int methodIndex = method.getIndex() - 1;
                if(!writtenMethods[methodIndex]) {
                    //method was not yet written in it !
                    writtenMethods[methodIndex] = true;
                    RegisterOffset methodAddr = new RegisterOffset(compiler.readNextStackSpace().getOffset() + methodIndex, Register.GB);
                    method.setDAddr(methodAddr);
                    compiler.addInstruction(new LOAD(new LabelOperand(new Label("code." + currentClass.getType().getName().getName() + "." + method.getName())), Register.R0));
                    compiler.addInstruction(new STORE(Register.R0, methodAddr));
                }
            }
            currentClass = currentClass.getSuperClass();
        }
        compiler.occupyLBSPace(methodNumber);
    }

    @Override
    public void codeGenClass(DecacCompiler compiler) {
        compiler.addComment("========== Class " + name.getName() + " ==========");
        // generate the methods code
        // init func, we need a context for it
        compiler.newCodeContext();
        compiler.addLabel(new Label("init." + name.getName().getName()));
        // the location of the object to init is at -2(LB).
        // let's load the daddr on R1 !
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
        // for each field, compute it and store it at its offset
        for(int i = 0; i < fields.size(); i++) {
            fields.getList().get(i).codeGenField(compiler, new RegisterOffset(i + 1, Register.R1));
        }
        compiler.addInstruction(new RTS());
        compiler.endCodeContext();
        // for each method, generate the code for it.
        for(AbstractDeclMethod method : methods.getList()) {
            compiler.newCodeContext();
            method.codeGenMethod(compiler, name.getName().getName());
            compiler.endCodeContext();
        }
    }

}
