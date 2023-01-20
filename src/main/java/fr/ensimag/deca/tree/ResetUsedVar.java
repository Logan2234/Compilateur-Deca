package fr.ensimag.deca.tree;

/**
 * Function applied to a tree to reset the used attribute back to false for every related Definition 
 * This function is meant to be used with {@link #iter(TreeFunction)} to reset all used attributes of
 * a program to remove all useless variables again
 * @author gl03
 * @date 20/01/2023
 */
public class ResetUsedVar implements TreeFunction {

    @Override
    public void apply(Tree t) {
        if (t.isIdentifier()) {
            ((Identifier)t).getDefinition().resetUsed();
        }
    }
}
