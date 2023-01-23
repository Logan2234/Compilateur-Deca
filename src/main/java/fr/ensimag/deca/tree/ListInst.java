package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;

/**
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class ListInst extends TreeList<AbstractInst> {

    /**
     * Implements non-terminal "list_inst" of [SyntaxeContextuelle] in pass 3
     * 
     * @param compiler     contains "env_types" attribute
     * @param localEnv     corresponds to "env_exp" attribute
     * @param currentClass
     *                     corresponds to "class" attribute (null in the main bloc).
     * @param returnType
     *                     corresponds to "return" attribute (void in the main
     *                     bloc).
     */
    public void verifyListInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
        for (AbstractInst i : getList()) {
            i.verifyInst(compiler, localEnv, currentClass, returnType);
        }
    }

    public void codeGenListInst(DecacCompiler compiler) {
        for (AbstractInst i : getList()) {
            i.codeGenInst(compiler);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractInst i : getList()) {
            i.decompileInst(s);
            s.println();
        }
    }

    @Override
    public boolean collapse() {
        // try to collapse each instruction into a list of instructions
        boolean collapse = false;
        for (int i = 0; i < getList().size(); i++) {
            AbstractInst toCollapse = getList().get(i);
            if(toCollapse.collapse()) {
                collapse = true;
                // remove this inst, replace it with it's collapsed form
                removeAt(i);
                int offset = 0;
                for(AbstractInst newInst : toCollapse.collapseInst().getList()) {
                    insert(newInst, i + offset);
                    offset ++;
                }
            }
        }
        return collapse;
    }

    @Override
    public boolean irrelevant() {
        boolean result = false;
        AbstractInst expr;
        
        for (int i = 0; i < getList().size(); i++) {
            expr = getList().get(i);
            if (expr.irrelevant()){
                result |= true;
                set(i, expr);
            }
        }

        return result;
    }

    @Override
    public boolean irrelevant(int j) {
        boolean result = false;
        AbstractInst expr;
        
        for (int i = 0; i < getList().size(); i++) {
            expr = getList().get(i);
            if (expr.irrelevant(j)){
                result |= true;
                set(i, expr);
            }
        }

        return result;
    }
}
