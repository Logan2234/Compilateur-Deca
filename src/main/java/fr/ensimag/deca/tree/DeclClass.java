package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.StackOverflowErr;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import java.util.HashMap;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

import java.util.Map;
import java.util.Set;

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
            fields.getList().get(i).setFieldOffset(compiler, i + 1 + superIdentifier.getClassDefinition().getNumberOfFields());
        }
    }

    @Override
    protected void spotUsedVar() {
        // We don't spotUsedVar() on classes. We spot them indirectly from the main
    }

    @Override
    protected Tree removeUnusedVar(Program prog) {
        if (!this.name.getDefinition().isUsed()) {
            return null;
        }
        this.fields = (ListDeclField)this.fields.removeUnusedVar(prog);
        this.methods = (ListDeclMethod)this.methods.removeUnusedVar(prog);
        return this;
    }

    public AbstractIdentifier getName() {
        return name;
    }
    
    @Override
    public CollapseResult<Null> collapseClass() {
        return new CollapseResult<Null>(null, fields.collapseFields().couldCollapse() || methods.collapseMethods().couldCollapse());
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
                    // don't generate address if the method is never used (and if we are optimizing);
                    if (method.isUsed() || !compiler.getCompilerOptions().getOptimize()) {
                        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code." + currentClass.getType().getName().getName() + "." + method.getName())), Register.R0));
                        compiler.addInstruction(new STORE(Register.R0, methodAddr));
                    }
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
        compiler.addLabel(new Label("init." + name.getName().getName()));
        compiler.newCodeContext();
        // the location of the object to init is at -2(LB).
        // call parent init
        if(superIdentifier.getName().getName() != "Object") {
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
            compiler.addInstruction(new PUSH(Register.R1));
            compiler.addInstruction(new BSR(new Label("init." + superIdentifier.getName().getName())));
            compiler.addInstruction(new POP(Register.R1));
        }
        // let's load the daddr on R1 !
        GPRegister fieldGenRegister = compiler.allocateRegister(); 
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), fieldGenRegister));
        // for each field, compute it and store it at its offset
        for(int i = 0; i < fields.size(); i++) {
            fields.getList().get(i).codeGenField(compiler, new RegisterOffset(i + 1 + superIdentifier.getClassDefinition().getNumberOfFields(), fieldGenRegister));
        }
        compiler.freeRegister(fieldGenRegister);
        // save and restore all register used by context
        // save and restore context used registers 
        for(GPRegister usedRegister : compiler.getAllContextUsedRegister()) {
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new POP(usedRegister));
            compiler.addInstructionFirst(new PUSH(usedRegister));
        }
        // add max stack use at the beginning
        if(compiler.getCompilerOptions().getRunTestChecks()) {
            AbstractRuntimeErr error = new StackOverflowErr();
            compiler.useRuntimeError(error);
            compiler.addInstructionFirst(new BOV(error.getErrorLabel()));
            compiler.addInstructionFirst(new TSTO(compiler.getMaxStackUse()));
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

    @Override
    protected void getSpottedFields(Map<Symbol,Set<ClassDefinition>> usedFields) {
        if (this.getName().getClassDefinition().isUsed()) {
            this.fields.getSpottedFields(usedFields);
        }
    }

    @Override
    protected void spotOverridingFields(Map<Symbol,Set<ClassDefinition>> usedFields) {
        if (this.getName().getClassDefinition().isUsed()) {
            this.fields.spotOverridingFields(usedFields);
        }
    }

    @Override
    protected void spotInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        for (AbstractDeclMethod method : this.methods.getList()) {
            method.spotInlineMethods(inlineMethods);
        }
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.fields = (ListDeclField)this.fields.doSubstituteInlineMethods(inlineMethods);
        this.methods = (ListDeclMethod)this.methods.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    public AbstractInst factorise(DecacCompiler compiler) {
        fields.factorise(compiler);
        methods.factorise(compiler);
        return null;
    }

    @Override
    public boolean isSplitable(DecacCompiler compiler) {
        return methods.isSplitable(compiler) || fields.isSplitable(compiler);
    }

    @Override
    public AbstractInst splitCalculus(DecacCompiler compiler) {
        fields.splitCalculus(compiler);
        methods.splitCalculus(compiler);
        return null;
    }
}
