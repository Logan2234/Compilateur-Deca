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

    @Override
    public boolean irrelevant() {
        boolean result = false;
        AbstractExpr expr;
        for (int i = 0; i < getList().size(); i++) {
            expr = getList().get(i);
            if (expr.irrelevant() || expr.isSelection()){
                if (expr.isSelection()){
                    AbstractExpr out = ((Selection) expr).returnIrrelevantFromSelection();
                    if (out != null) {
                        result |= true;
                        set(i, out);
                    } 
                }
                else {
                    if (currentValues.containsKey(((Identifier) expr).getName())){
                        result |= true;
                        set(i, currentValues.get(((Identifier) expr).getName()));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean irrelevant(int j) {
        boolean result = false;
        AbstractExpr expr;
        for (int i = 0; i < getList().size(); i++) {
            expr = getList().get(i);
            if (expr.irrelevant(j) || expr.isSelection()){
                if (expr.isSelection()){
                    AbstractExpr out = ((Selection) expr).returnIrrelevantFromSelection(j);
                    if (out != null) {
                        result |= true;
                        set(i, out);
                    } 
                }
                else {
                    if (irrelevantValuesForIf.get(j).containsKey(((Identifier) expr).getName())){
                        result |= true;
                        set(i, irrelevantValuesForIf.get(j).get(((Identifier) expr).getName()));
                    }
                }
            }
        }
        return result;
    }
}
