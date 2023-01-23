package fr.ensimag.deca.tree;

import java.util.Map;
import java.util.Set;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * List of fields.
 * 
 * @author Jorge
 * @date 05/01/2023
 */
public class ListDeclField extends TreeList<AbstractDeclField> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField i : getList()) {
            i.decompile(s);
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 2
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
    void verifyListDeclField(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclField i : getList())
            i.verifyDeclField(compiler, localEnv, currentClass);
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
    void verifyListInitField(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        for (AbstractDeclField i : getList())
            i.verifyInitField(compiler, localEnv, currentClass);
    }
    
    public CollapseResult<Null> collapseFields() {
        boolean somethingCollapsed = false;
        for(AbstractDeclField f : getList()) {
            somethingCollapsed |= f.collapseDeclField().couldCollapse();
        }
        return new CollapseResult<Null>(null, somethingCollapsed);
    }

    @Override
    protected void getSpottedFields(Map<Symbol,Set<ClassDefinition>> usedFields) {
        for (AbstractDeclField field : this.getList()) {
            ((DeclField)field).getSpottedFields(usedFields);
        }
    }

    @Override
    protected void spotOverridingFields(Map<Symbol,Set<ClassDefinition>> usedFields) {
        for (AbstractDeclField field : this.getList()) {
            ((DeclField)field).spotOverridingFields(usedFields);
        }
    }

}
