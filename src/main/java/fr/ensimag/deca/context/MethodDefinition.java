package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.*;
import fr.ensimag.ima.pseudocode.Label;
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

    @Override
    public String getNature() {
        return "method";
    }

    @Override
    public boolean isExpression() {
        return false;
    }

    @Override
    public boolean spotRelatedDefs() {
        boolean varSpotted = super.spotRelatedDefs();
        varSpotted = this.containingClass.spotUsedVar() || varSpotted;
        return varSpotted;
    }

}
