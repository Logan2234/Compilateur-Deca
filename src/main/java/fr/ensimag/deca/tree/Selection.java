package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import java.util.HashMap;

import org.apache.commons.lang.Validate;

/**
 * Selection statment
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Selection extends AbstractLValue {

    private final AbstractExpr obj;
    private final AbstractIdentifier field;

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
            throw new ContextualError("The object of the selection is not of type class (rule 3.65)",
                    this.getLocation());

        EnvironmentExp exp = type.asClassType("Not a class type", getLocation()).getDefinition().getMembers();
        Type typeField = field.verifyExpr(compiler, exp, currentClass);

        Visibility vis = field.getFieldDefinition().getVisibility();

        // Ajout du décor
        this.setType(typeField);
        
        if (vis == Visibility.PUBLIC)
            return typeField;

        boolean bool1 = type.asClassType("", getLocation()).isSubClassOf(currentClassType);
        boolean bool2 = currentClassType.isSubClassOf(
                field.getDefinition().asFieldDefinition("null", getLocation()).getContainingClass().getType());

        if (!bool1 || !bool2) {
            throw new ContextualError("The variable is protected (rule 3.66)", getLocation());
        }
        return typeField;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
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

}
