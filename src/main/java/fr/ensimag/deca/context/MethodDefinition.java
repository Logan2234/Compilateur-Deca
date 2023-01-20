package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.*;
import fr.ensimag.ima.pseudocode.Label;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Definition of a method
 *
 * @author gl03
 * @date 01/01/2023
 */
public class MethodDefinition extends ExpDefinition {
    private static final Logger LOG = Logger.getLogger(MethodDefinition.class);

    @Override
    public boolean isMethod() {
        return true;
    }

    public Label getLabel() {
        Validate.isTrue(label != null,
                "setLabel() should have been called before");
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public int getIndex() {
        return index;
    }

    private int index;

    @Override
    public MethodDefinition asMethodDefinition(String errorMessage, Location l)
            throws ContextualError {
        return this;
    }

    private final Signature signature;
    private ClassDefinition containingClass;
    private Label label;
    
    /**
     * 
     * @param type Return type of the method
     * @param location Location of the declaration of the method
     * @param signature List of arguments of the method
     * @param index Index of the method in the class. Starts from 0.
     */
    public MethodDefinition(Type type, Location location, Signature signature, int index, ClassDefinition containingClass) {
        super(type, location);
        this.signature = signature;
        this.containingClass = containingClass;
        this.index = index;
    }

    public Signature getSignature() {
        return signature;
    }

    public ClassDefinition getContainingClass() {
        return this.containingClass;
    }

    @Override
    public String getNature() {
        return "method";
    }

    @Override
    public boolean isExpression() {
        return false;
    }

    private String methodName;

    public void setName(String name) {
        this.methodName = name;
    }

    public String getName() {
        return this.methodName;
    }

    @Override
    public boolean spotRelatedDefs() {
        boolean varSpotted = super.spotRelatedDefs();
        varSpotted = this.containingClass.spotUsedVar() || varSpotted;
        return varSpotted;
    }

    /**
     * If the unspotted method is an override of a useful method, it may be dynamically useful.
     * @return true if the method is an override of a used method
     */
    public boolean isOverrideOfUsed(Map<ClassDefinition,Set<Integer>> exploredMethods) {
        boolean res = false;
        ClassDefinition superClass = this.containingClass.getSuperClass();
        while(superClass != null && !res && this.index<=superClass.getNumberOfMethods()) {
            res = exploredMethods.get(superClass).contains(this.index);
            superClass = superClass.getSuperClass();
        }
        return res;
    }

}
