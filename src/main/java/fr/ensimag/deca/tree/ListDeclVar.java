package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclVar i : getList()) {
            i.decompile(s);
            s.println();
        }
    }

    /**
     * Code generatio to declare all variables.
     * This also keep track of the number of variables, and assign to each of them a
     * DAddr to keep track of where they are.
     * 
     * @param compiler Where we write our instructions to.
     */
    public void codeGenDeclVar(DecacCompiler compiler) {
        for (AbstractDeclVar i : getList()) {
            // create the DAddr that references the variable
            i.codeGenDeclVar(compiler, compiler.getNextStackSpace());
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
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
    void verifyListDeclVariable(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclVar i : getList())
            i.verifyDeclVar(compiler, localEnv, currentClass);
    }

    public CollapseResult<Null> collapseDeclVars() {
        boolean somethingCollapsed = false;
        for(AbstractDeclVar v : getList()) {
            somethingCollapsed |= v.collapseDeclVar().couldCollapse();
        }
        return new CollapseResult<Null>(null, somethingCollapsed);
        }

        public AbstractInst splitCalculus(DecacCompiler compiler) {
        for (AbstractDeclVar var : getList())
            if (var.isSplitable(compiler))
                var.splitCalculus(compiler);
        return null;
    }
    @Override
    public boolean irrelevant() {
        boolean result = false;
        AbstractDeclVar expr;
        
        for (int i = 0; i < getList().size(); i++) {
            expr = getList().get(i);
            if (expr.irrelevant()){
                result |= true;
                set(i, expr);
            }
        }

        return result;
    }

}
