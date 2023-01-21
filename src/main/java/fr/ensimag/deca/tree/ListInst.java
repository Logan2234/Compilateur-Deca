package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;

import java.util.List;
import java.util.ListIterator;

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
        for (AbstractInst i : getList())
            i.verifyInst(compiler, localEnv, currentClass, returnType);
    }

    public void codeGenListInst(DecacCompiler compiler) {
        for (AbstractInst i : getList())
            i.codeGenInst(compiler);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractInst i : getList()) {
            i.decompileInst(s);
            s.println();
        }
    }

    @Override
    protected void spotUsedVar() {
        for (AbstractInst inst : this.getList()) {
            if (!(inst instanceof Identifier)) {
                inst.spotUsedVar();
            }
        }
    }

    @Override
    protected Tree simplify() {
        ListIterator<AbstractInst> iter = this.iterator();
        while(iter.hasNext()) {
            AbstractInst tree = (AbstractInst)iter.next().simplify();
            // we have to remove first because it may be a new tree
            iter.remove();
            if (tree == null) {
                // keep it removed
            }
            else if (tree instanceof AbstractExpr) {
                AbstractExpr expr = (AbstractExpr)tree;
                List<AbstractExpr> unremovableExpressions = expr.getUnremovableExpr();
                if (unremovableExpressions.isEmpty()) {
                    // don't add the expression
                }
                else if (expr.getType().isBoolean()) {
                    // replace the expression as it is
                    iter.add(tree);
                    // we cannot break the expression because for instance, the left operand
                    // of an AND shouldn't be evaluated if the right operand is false
                }
                else {
                    for (AbstractExpr expression : unremovableExpressions) {
                        iter.add(expression); // add after the current instruction
                    }
                }
            }
            else {
                iter.add(tree);
            }
        }
        return this;
    }

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
}
