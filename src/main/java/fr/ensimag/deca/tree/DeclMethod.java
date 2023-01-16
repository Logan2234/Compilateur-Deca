package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Declaration of a method (for a class)
 * 
 * @author Jorge
 * @date 08/01/2023
 */
public class DeclMethod extends AbstractDeclMethod {

    final private AbstractIdentifier type;
    final private AbstractIdentifier methodName;
    final private ListDeclParam params;
    final private AbstractMethod body;

    public DeclMethod(AbstractIdentifier type, AbstractIdentifier methodName, ListDeclParam params,
            AbstractMethod body) {
        Validate.notNull(type);
        Validate.notNull(methodName);
        Validate.notNull(params);
        Validate.notNull(body);
        this.type = type;
        this.methodName = methodName;
        this.params = params;
        this.body = body;
    }

    @Override
    protected void verifyDeclMethod(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.type.verifyType(compiler);
        
        Signature signature = params.verifyListDeclParam(compiler);
        MethodDefinition methodeDef;
        
        // Test de la méthode potentiellement existente dans la classe mère
        ExpDefinition defExp = currentClass.getSuperClass().getMembers().get(this.methodName.getName());
        if (defExp != null){
            // On cherche à savoir si c'est bien une méthode
            MethodDefinition motherMethod = defExp.asMethodDefinition("The name \"" + methodName.getName().getName() + "\" is already used for a field in the superclass (rule 2.7)", this.getLocation());
            if (!motherMethod.getSignature().sameSignature(signature)){
                throw new ContextualError("The method \"" + this.methodName.getName().getName() + "\" doesn't have the same signature as the method defined it the superclass (rule 2.7)", getLocation());
            }
            
            Type motherMethodType = motherMethod.getType();
            try {
                ClassType motherMethodClassType = motherMethodType.asClassType("Not a class type", getLocation());
                ClassType classType = type.asClassType("Not a class type", getLocation());
                if (!classType.isSubClassOf(motherMethodClassType)){
                    throw new ContextualError("The return type is not the same as defined in the superclass (or not a subtype) (rule 2.7)", getLocation());
                }
            } catch (ContextualError e) {
                if (!motherMethodType.sameType(type) && !(motherMethodType.isFloat() && type.isInt())){
                    throw new ContextualError("The return type is not the same as defined in the superclass (or not a subtype) (rule 2.7)", getLocation());
                }
            }
            methodeDef = new MethodDefinition(type, this.getLocation(), signature, motherMethod.getIndex());
        }
        else {
            methodeDef = new MethodDefinition(type, this.getLocation(), signature, currentClass.getNumberOfMethods());
            currentClass.incNumberOfMethods();
        }
        try {
            localEnv.declare(this.methodName.getName(), methodeDef);
            methodName.verifyExpr(compiler, localEnv, currentClass);
        } catch (DoubleDefException e) {
            throw new ContextualError(
                    "The method \"" + methodName.getName().getName() + "\" has already been declared (rule 2.6)",
                    this.getLocation());
        }
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        EnvironmentExp methodEnv = new EnvironmentExp(localEnv);
        params.verifyListParam(compiler, methodEnv, currentClass);
        body.verifyMethod(compiler, methodEnv, currentClass, this.type.getType());
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(' ');
        methodName.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");
        body.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        methodName.iter(f);
        params.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }
}
