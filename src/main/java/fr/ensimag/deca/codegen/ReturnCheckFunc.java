package fr.ensimag.deca.codegen;

import fr.ensimag.deca.tree.Tree;
import fr.ensimag.deca.tree.TreeFunction;

public class ReturnCheckFunc implements TreeFunction {

    private final String methodClassName;

    public ReturnCheckFunc(String name) {
        methodClassName = name;
    }

    public void apply(Tree node) {
        if(node.isReturn()) {
            node.asReturn().setMethodClassName(methodClassName);
        }
    }
}
