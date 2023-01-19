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
    private Label label;
    
    /**
     * 
     * @param type Return type of the method
     * @param location Location of the declaration of the method
     * @param signature List of arguments of the method
     * @param index Index of the method in the class. Starts from 0.
     */
    public MethodDefinition(Type type, Location location, Signature signature, int index) {
        super(type, location);
        this.signature = signature;
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
    public void spotRelatedDefs(AbstractProgram prog) {
        assert(prog instanceof Program);
        // the types of params are spotted at the methodCall and if the type of a param is a class
        // then the class is spotted at the methodCall directly or indirectly by the subclass
        // the return type could be a class but it is spotted in the body
        for (AbstractDeclClass c : ((Program)prog).getClasses().getList()) {
            assert(c instanceof DeclClass);
            // TODO if (/* class is class or subclass */)
            for (AbstractDeclMethod method : ((DeclClass)c).getMethods().getList()) {
                assert(method instanceof DeclMethod);
                // find the corresponding DeclMethod
                // (match the tree location of DeclMethod with the definition location of the current MethodDefinition)
                // Each method as a different location
                LOG.debug("Looking for the method");
                if (this.getLocation() == method.getLocation()) {
                    LOG.debug("Methods matched");
                    // explore the body of the method to spot other useful variables
                    ((DeclMethod)(method)).spotUsedVar(prog);
                    // spot the containing class
                    ((DeclClass)c).getName().getClassDefinition().spotUsedVar(prog);
                } 
                // TODO
                // if the dynamique type of the object calling the method is a subclass then the
                // overriding methods should have been spotted
                // else if (/*method == redéfinition */) { 
                //     LOG.debug("Overriding method found");
                //     // explore the body of the method to spot other useful variables
                //     ((DeclMethod)(method)).spotUsedVar(prog);

                // }
            }
        }
    }
}
