package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.codegen.runtimeErrors.NullReferenceErr;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import java.util.HashMap;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Selection statment
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Selection extends AbstractLValue {

    private AbstractExpr obj;
    private AbstractIdentifier field;

    public Selection(AbstractExpr obj, AbstractIdentifier field) {
        Validate.notNull(obj);
        Validate.notNull(field);
        this.obj = obj;
        this.field = field;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        ClassType currentClassType = currentClass.getType();
        Type type = obj.verifyExpr(compiler, localEnv, currentClass);

        if (!type.isClass())
            throw new ContextualError("The object of the selection is not of type class (rule 3.65)", getLocation());

        EnvironmentExp exp = type.asClassType("Not a class type", getLocation()).getDefinition().getMembers();
        Type typeField = field.verifyExpr(compiler, exp, currentClass);

        Visibility vis = field.getFieldDefinition().getVisibility();

        // Ajout du décor
        setType(typeField);

        if (vis == Visibility.PUBLIC)
            return typeField;

        boolean bool1 = type.asClassType("", getLocation()).isSubClassOf(currentClassType);
        boolean bool2 = currentClassType.isSubClassOf(
                field.getDefinition().asFieldDefinition("null", getLocation()).getContainingClass().getType());

        if (!bool1 || !bool2)
            throw new ContextualError("The variable is protected (rule 3.66)", getLocation());

        return typeField;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        if(resultRegister == null) {
            // we need a register
            GPRegister register = compiler.allocateRegister();
            obj.codeGenExpr(compiler, register);
            // null reference test
            if(compiler.getCompilerOptions().getRunTestChecks()) {
                AbstractRuntimeErr error = new NullReferenceErr();
                compiler.useRuntimeError(error);
                compiler.addInstruction(new CMP(new NullOperand(), register));
                compiler.addInstruction(new BEQ(error.getErrorLabel()));
            }
            // save in R1 because freeing the register may pop the stack
            compiler.addInstruction(new LOAD(new RegisterOffset(field.getDefinition().getDAddrOffsetOnly(), register), Register.R1));
            compiler.freeRegister(register);
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            // put the object in the result register
            obj.codeGenExpr(compiler, resultRegister);
            // null reference test
            if(compiler.getCompilerOptions().getRunTestChecks()) {
                AbstractRuntimeErr error = new NullReferenceErr();
                compiler.useRuntimeError(error);
                compiler.addInstruction(new CMP(new NullOperand(), resultRegister));
                compiler.addInstruction(new BEQ(error.getErrorLabel()));
            }
            // load the value of the field in it, and we're good to go
            compiler.addInstruction(new LOAD(new RegisterOffset(field.getDefinition().getDAddrOffsetOnly(), resultRegister), resultRegister));
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        obj.decompile(s);
        s.print(".");
        field.decompile(s);

        // throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        obj.iter(f);
        field.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        obj.prettyPrint(s, prefix, false);
        field.prettyPrint(s, prefix, true);
    }

    @Override
    public Definition getDefinition() {
        // ? pas trop sur de moi la dessus
        return field.getDefinition();
    }

    @Override
    public Symbol getName() {
        return field.getName();
    }

    @Override
    public boolean isSelection(){
        return true;
    }

    /**
     * Return the value of the selection for otpimization. WARNING : Multiple Selection not supported
     * @return the expression value for the selection
     */
    public AbstractExpr returnIrrelevantFromSelection(){
        if (defClass) {
            if (obj.isThis()){
                return varModels.get(actualClass).get(field.getName());
            } else {
                if (!obj.isSelection())
                    if (declaredClassesInMethod.containsKey(((Identifier) obj).getName())) {return declaredClassesInMethod.get(((Identifier) obj).getName()).get(field.getName());}
                    else return declaredClasses.get(((Identifier) obj).getName()).get(field.getName());
                else return null;
            }
        }
        else {
            if (obj.isThis()){
                return currentValues.get(field.getName());
            } else {
                if (!obj.isSelection())
                    return declaredClasses.get(((Identifier) obj).getName()).get(field.getName());
                else return null;
            }
        }
    }

    /**
     * Return the value of the selection for otpimization in If/Else statments. WARNING : Multiple Selection not supported
     * @return the expression value for the selection
     */
    public AbstractExpr returnIrrelevantFromSelection(int i){
        if (defClass) {
            if (obj.isThis()){
                return varModels.get(actualClass).get(field.getName());
            } else {
                if (!obj.isSelection())
                    if (declaredClassesInMethod.containsKey(((Identifier) obj).getName())) {return declaredClassesInMethod.get(((Identifier) obj).getName()).get(field.getName());}
                    else return declaredClasses.get(((Identifier) obj).getName()).get(field.getName());
                else return null;
            }
        }
        else {
            if (obj.isThis()){
                return irrelevantValuesForIf.get(i).get(field.getName());
            } else {
                if (!obj.isSelection())
                    return declaredClasses.get(((Identifier) obj).getName()).get(field.getName());
                else return null;
            }
        }
    }

    /**
     * Function allowing to change a value of an external class
     * @param e : The expression to set
     */
    public void putIrrelevantFromSelection(AbstractExpr e){
        HashMap<Symbol, AbstractExpr> dico = declaredClasses.get(((Identifier) obj).getName());
        dico.put(field.getName(), e);
        declaredClasses.put(((Identifier) obj).getName(), dico);
    }

    /**
     * Same as putIrrelevantFromSelection but for methods
     * @param e : The expression to set
     */
    public void putIrrelevantFromSelectionInMethod(AbstractExpr e){
        HashMap<Symbol, AbstractExpr> dico = declaredClassesInMethod.get(((Identifier) obj).getName());
        dico.put(field.getName(), e);
        declaredClassesInMethod.put(((Identifier) obj).getName(), dico);
    }

    /**
     * Function allowing to errase a field of a known variables for optimization
     */
    public void erraseIrrelevantFromSelection(){
        HashMap<Symbol, AbstractExpr> dico = declaredClasses.get(((Identifier) obj).getName());
        if (dico.containsKey(field.getName())) {
            dico.remove(field.getName());
            declaredClasses.put(((Identifier) obj).getName(), dico);
        }
    }

    /**
     * Same as erraseIrrelevantFromSelection but for methods
     */
    public void erraseIrrelevantFromSelectionInMethod(){
        HashMap<Symbol, AbstractExpr> dico = declaredClassesInMethod.get(((Identifier) obj).getName());
        if (dico.containsKey(field.getName())) {
            dico.remove(field.getName());
            declaredClassesInMethod.put(((Identifier) obj).getName(), dico);
        }
    }

    /**
     * Calls erraseIrrelevantFromSelection or erraseIrrelevantFromSelectionInMethod depending on the context
     */
    public void erraseIrrelevant(){
        if (defMethod) erraseIrrelevantFromSelectionInMethod(); else erraseIrrelevantFromSelection();
    }

    /**
     * Calls putIrrelevantFromSelection or putIrrelevantFromSelectionInMethod depending on the context
     * @param e : The expression to set
     */
    public void putIrrelevant(AbstractExpr e){
        if (defMethod) putIrrelevantFromSelectionInMethod(e); else putIrrelevantFromSelection(e);
    }

    /**
     * Function allowing to know if a selection is known or not
     * @return if selection is known
     */
    public boolean isKnown(){
        if (defClass) {
            if (obj.isThis()){
                return varModels.get(actualClass).containsKey(field.getName());
            } else return declaredClasses.get(((Identifier) obj).getName()).containsKey(field.getName()) || declaredClassesInMethod.get(((Identifier) obj).getName()).containsKey(field.getName());
        }
        else {
            if (obj.isThis()){
                return currentValues.containsKey(field.getName());
            } else return declaredClasses.get(((Identifier) obj).getName()).containsKey(field.getName());
        }
    }

    /**
     * Function allowing to know if a selection is known or not in if/else statements
     * @return if selection is known
     */
    public boolean isKnown(int i){
        if (defClass) {
            if (obj.isThis()){
                return varModels.get(actualClass).containsKey(field.getName());
            } else return declaredClasses.get(((Identifier) obj).getName()).containsKey(field.getName()) || declaredClassesInMethod.get(((Identifier) obj).getName()).containsKey(field.getName());
        }
        else {
            if (obj.isThis()){
                return irrelevantValuesForIf.get(i).containsKey(field.getName());
            } else return declaredClasses.get(((Identifier) obj).getName()).containsKey(field.getName());
        }
    }

	@Override
    protected void spotUsedVar() {
        this.obj.spotUsedVar();
        this.field.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        this.obj = (AbstractExpr)this.obj.removeUnusedVar();
        return this;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        // the object could be obtained via a MethodCall
        this.obj.addUnremovableExpr(foundMethodCalls);
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.obj = (AbstractExpr)this.obj.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new Selection(this.obj.substitute(substitutionTable),(AbstractIdentifier)this.field.substitute(substitutionTable));
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

    @Override
    protected boolean containsField() {
        return this.obj.containsField() || this.field.containsField();
    }

}
