package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;

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
    protected Tree removeUnusedVar() {
        ListIterator<AbstractInst> iter = this.iterator();
        while(iter.hasNext()) {
            AbstractInst tree = (AbstractInst)iter.next().removeUnusedVar();
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

    public CollapseResult<ListInst> collapseInsts() {
        boolean somethingCollapsed = false;
        for (int i = 0; i < getList().size(); i++) {
            AbstractInst toCollapse = getList().get(i);
            CollapseResult<ListInst> result = toCollapse.collapseInst();
            somethingCollapsed |= result.couldCollapse();
            // remove this inst, replace it with it's collapsed form
            removeAt(i);
            int offset = 0;
            for(AbstractInst newInst : result.getResult().getList()) {
                insert(newInst, i + offset);
                offset ++;
            }
        }
        return new CollapseResult<ListInst>(this, somethingCollapsed);
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
