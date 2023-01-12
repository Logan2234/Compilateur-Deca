package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import org.apache.commons.lang.Validate;

/**
 * Cast Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class Cast extends AbstractExpr {

    private final AbstractIdentifier type;
    private final AbstractExpr e;

    public Cast(AbstractIdentifier type, AbstractExpr e) {
        Validate.notNull(type);
        Validate.notNull(e);
        this.type = type;
        this.e = e;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Location loc = this.getLocation();
        Type typeExp = this.e.verifyExpr(compiler, localEnv, currentClass);
        Type typeT = this.type.verifyType(compiler);
        if (typeExp.isVoid() || (!assign_compatible(localEnv, typeExp, typeT) && !assign_compatible(localEnv, typeT, typeExp))){
            throw new ContextualError("Cast impossible", loc);
        }
        // Ajout du décor
        this.setType(typeExp);

        return typeT;
        //throw new UnsupportedOperationException("not yet implemented");
    }

    /**
 * Check if the two types are compatible for the cast
 * @param localEnv the local environment
 * @param typeExp the type of the expression to cast
 * @param typeT the type of the expected cast
 * @return true if the two types are compatible, false if not
 * 
 * @author Nils Depuille
 * @date 12/01/2023
 */
    public Boolean assign_compatible(EnvironmentExp localEnv, Type type1, Type type2) {
        if (type1.isFloat() && type2.isInt()){
            return true;
        }
        if(type2.getClass().isAssignableFrom(type1.getClass())){//TODO je dois savoir si t2 est une sous classe de T1 pour le localEnv
            return true;
        }
        return false;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        type.decompile(s);
        s.print(") (");
        e.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        e.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        e.prettyPrint(s, prefix, true);
    }

}