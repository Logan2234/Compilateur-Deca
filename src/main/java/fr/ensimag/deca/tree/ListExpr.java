package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl03
 * @date 01/01/2023
 */
public class ListExpr extends TreeList<AbstractExpr> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractExpr i : getList()) {
            if (!(getList().get(0).equals(i))) // ? Not sure if we have param1,param2,param3 ... at the end
                s.print(", ");
            i.decompile(s);
        }
    }

    /**
     * Find recursively all method calls and reads in the list of expressions
     * This method is used for optimizing the Program tree.
     * Instructions should not be removed if they contains a MethodCall or a Read
     * that could potentially print/read on stdin/stdout or change the state of an object.
     * An inline methode should not be susbtituted if it takes as a parameter a method call,
     * an assign or a read
     * 
     * @return the list of MethodCall ordered by order of apparition
     */
    protected final List<AbstractExpr> getUnremovableExpr() {
        List<AbstractExpr> foundMethodCalls = new LinkedList<AbstractExpr>();
        for (AbstractExpr expr : this.getList()) {
            expr.addUnremovableExpr(foundMethodCalls);
        }
        return foundMethodCalls;
    }

    /**
     * Check recursively if an expression in the list contains a field
     * @return true if an expression contains a field
     */
    protected boolean containsField() {
        for (AbstractExpr expr : this.getList()) {
            if (expr.containsField()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method used to know if all the expressions in the list are "atomic", meaning that it is not expensive for
     * ima to compute. This way, we know if an inline substitution will introduce more complexity
     * if a parameter appears multiple times in the method.
     * @return true if the expression is "atomic"
     */
    protected boolean isAtomic() {
        for (AbstractExpr expr : this.getList()) {
            if (expr.isAtomic()) {
                return false;
            }
        }
        return true;
    }

}
