package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.context.MethodDefinition;

/**
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class TreeList<TreeType extends Tree> extends Tree {
    /*
     * We could allow external iterators by adding
     * "implements Iterable<AbstractInst>" but it's cleaner to provide our own
     * iterators, to make sure all callers iterate the same way (Main,
     * IfThenElse, While, ...). If external iteration is needed, use getList().
     */

    private List<TreeType> list = new ArrayList<TreeType>();

    public void add(TreeType i) {
        Validate.notNull(i);
        list.add(i);
    }

    /**
     * @return the list contained in the class, read-only. Use getModifiableList()
     *         if you need to change elements of the list.
     */
    public List<TreeType> getList() {
        return Collections.unmodifiableList(list);
    }

    /**
     * @return the list contained in the class, muable.
     *         Use getList() if you don't need to change elements of the list.
     */
    public List<TreeType> getModifiableList() {
        return this.list;
    }

    public TreeType set(int index, TreeType element) {
        return list.set(index, element);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public ListIterator<TreeType> iterator() {
        return list.listIterator();
    }

    public int size() {
        return list.size();
    }

    /**
     * Do not check anything about the location.
     * 
     * It is possible to use setLocation() on a list, but it is also OK not to
     * set it.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    protected String prettyPrintNode() {
        return super.prettyPrintNode() +
                " [List with " + getList().size() + " elements]";
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        int count = getList().size();
        for (TreeType i : getList()) {
            i.prettyPrint(s, prefix, count == 1, true);
            count--;
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        for (TreeType i : getList())
            i.iter(f);
    }

    @Override
    protected void spotUsedVar() {
        for (TreeType tree : getList()) {
            tree.spotUsedVar();
        }
    }

    @Override
    protected Tree removeUnusedVar() {
        ListIterator<TreeType> iter = this.iterator();
        while(iter.hasNext()) {
            TreeType tree = (TreeType)iter.next().removeUnusedVar();
            iter.remove();
            if (tree != null) {
                iter.add(tree);
            }
        }
        return this;
    }
    
    /**
     * Added to modify lists of insts for otpim.
     * @param node the node to insert in the array
     * @param at where we want to insert it
     */
    protected void insert(TreeType node, int at) {
        list.add(at, node);
    }

    /**
     * Added to modify lists of insts for otpim.
     * @param at the index of the element to remove.
     */
    protected void removeAt(int at) {
        list.remove(at);
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        ListIterator<TreeType> iter = this.iterator();
        while(iter.hasNext()) {
            TreeType currentTree = iter.next();
            iter.remove();
            iter.add((TreeType) currentTree.doSubstituteInlineMethods(inlineMethods));
        }
        return this;
    }

}
