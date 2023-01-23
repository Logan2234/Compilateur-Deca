package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of methods.
 * 
 * @author Jorge
 * @date 05/01/2023
 */
public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod i : getList()) {
            s.println();
            i.decompile(s);
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_method" of [SyntaxeContextuelle] in pass 2
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
     */
    void verifyListDeclMethod(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclMethod i : getList())
            i.verifyDeclMethod(compiler, localEnv, currentClass);
    }

    /**
     * Implements non-terminal "list_decl_method" of [SyntaxeContextuelle] in pass 3
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
     */
    void verifyListDeclMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclMethod i : getList())
            i.verifyMethodBody(compiler, localEnv, currentClass);
    }

    public CollapseResult<Null> collapseMethods() {
        boolean somethingCollapsed = false;
        for (AbstractDeclMethod m : getList()) {
            somethingCollapsed |= m.collapseDeclMethod().couldCollapse();
        }
        return new CollapseResult<Null>(null, somethingCollapsed);
    }

    @Override
    public boolean irrelevant() {
        boolean result = false;
        AbstractDeclMethod expr;

        for (int i = 0; i < getList().size(); i++) {
            expr = getList().get(i);
            if (expr.irrelevant()) {
                result |= true;
                set(i, expr);
            }
        }
        return result;
    }

    @Override
    public AbstractInst splitCalculus(DecacCompiler compiler) {
        for (AbstractDeclMethod method : getList())
            if (method.isSplitable(compiler))
                method.splitCalculus(compiler);
        return null;
    }

}
