package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import java.util.HashMap;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import java.util.Map;

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
    private AbstractInitialization initialization;

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
        ExpDefinition defExp = currentClass.getSuperClass().getMembers().get(fieldName.getName());
        if (defExp != null) {
            // On cherche à savoir si c'est bien un Field
            FieldDefinition motherField = defExp.asFieldDefinition("The name \"" + fieldName.getName().getName()
                    + "\" is already used for a method in the superclass (rule 2.5)", getLocation());
            def = new FieldDefinition(type, getLocation(), visib, currentClass, motherField.getIndex());
        } else {
            currentClass.incNumberOfFields();
            def = new FieldDefinition(type, getLocation(), visib, currentClass, currentClass.getNumberOfFields());
        }

        try {
            localEnv.declare(fieldName.getName(), def);
            fieldName.verifyExpr(compiler, localEnv, currentClass);
        } catch (DoubleDefException e) {
            throw new ContextualError(
                    "The field \"" + fieldName.getName().getName() + "\" has already been declared (rule 2.4)",
                    getLocation());
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
    public void codeGenField(DecacCompiler compiler, RegisterOffset resultRegister) {
        initialization.codeGenInit(compiler, type.getType(), resultRegister);
    }

    @Override
    public void setFieldOffset(DecacCompiler compiler, int offset) {
        fieldName.getDefinition().setDAddrOffsetOnly(offset);
    }


    @Override
    protected void spotUsedVar() {
        this.type.spotUsedVar();
        this.fieldName.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        if (!this.fieldName.getDefinition().isUsed()) {
            return null;
        }
        this.initialization = (AbstractInitialization)this.initialization.removeUnusedVar();
        return this;
    }

    public AbstractIdentifier getName() {
        return this.fieldName;
    }

    @Override
    public CollapseResult<Null> collapseDeclField() {
        CollapseResult<CollapseValue> result = initialization.collapseInit();
        // look in the collapse value if we can change the init node
        if(type.getType().isBoolean() && result.getResult().isBool()) {
            initialization = new Initialization(new BooleanLiteral(result.getResult().asBool()));
        }
        else if(type.getType().isFloat() && result.getResult().isFloat()) {
            initialization = new Initialization(new FloatLiteral(result.getResult().asFloat()));
        }
        if(type.getType().isInt() && result.getResult().isInt()) {
            initialization = new Initialization(new IntLiteral(result.getResult().asInt()));
        }
        return new CollapseResult<Null>(null, result.couldCollapse());
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.initialization = (AbstractInitialization)this.initialization.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    @Override
    public boolean irrelevant(){
        if (initialization.hasInitialization()) {
            inField = true;
            AbstractExpr expr = ((Initialization) initialization).getExpression();
            HashMap<Symbol, AbstractExpr> actualDico = varModels.get(actualClass);

            if (expr.isNew()){
                declaredClasses.put(fieldName.getName(), varModels.get(((New) expr).getClasse().getName()));
                return false;
            }

            if (expr.isSelection()){
                AbstractExpr out = ((Selection) expr).returnIrrelevantFromSelection();
                if (out != null) ((Initialization) initialization).setExpression(out);
                return false;
            }

            if (expr.irrelevant()){
                System.out.println("irrelevant");
                ((Initialization) initialization).setExpression(actualDico.get(((Identifier) expr).getName()));
            }
            if (!expr.isReadExpr()){
                actualDico.put(fieldName.getName(), ((Initialization) initialization).getExpression());
    
            } else if (actualDico.containsKey(fieldName.getName())){
                actualDico.remove(fieldName.getName());
            } 
            varModels.put(actualClass, actualDico);
            inField = false;
        }   
        return false;
    }
}