package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author Jorge
 * @date 08/01/2023
 */
public class ListDeclParam extends TreeList<AbstractDeclParam> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclParam i : getList()) {
            if (!(getList().get(0).equals(i)))
                s.print(", ");

            i.decompile(s);
        }
    }

    /**
     * Implements non-terminal "list_decl_param" of [SyntaxeContextuelle] in pass 2
     * 
     * @param compiler     contains the "env_types" attribute
     * @param localEnv
     *                     its "parentEnvironment" corresponds to "env_exp_sup"
     *                     attribute
     *                     in precondition, its "current" dictionary corresponds to
     *                     the "env_exp" attribute
     *                     in postcondition, its "current" dictionary corresponds to
     *                     the "env_exp_r" attribute
     * @param currentClass
     *                     corresponds to "class" attribute (null in the main bloc).
     * 
     * @return signature of the method
     */
    Signature verifyListDeclParam(DecacCompiler compiler) throws ContextualError {
        Signature signature = new Signature();
        for (AbstractDeclParam i : getList())
            signature.add(i.verifyDeclParam(compiler));
        return signature;
    }

    void verifyListParam(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclParam i : getList())
            i.verifyParam(compiler, localEnv, currentClass);
    }
}
