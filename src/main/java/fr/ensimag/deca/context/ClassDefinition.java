package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import org.apache.commons.lang.Validate;

/**
 * Definition of a class.
 *
 * @author gl03
 * @date 01/01/2023
 */
public class ClassDefinition extends TypeDefinition {


    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public void incNumberOfFields() {
        this.numberOfFields++;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int n) {
        Validate.isTrue(n >= 0);
        numberOfMethods = n;
    }
    
    public int incNumberOfMethods() {
        numberOfMethods++;
        return numberOfMethods;
    }

    private int numberOfFields = 0;
    private int numberOfMethods = 0;
    
    @Override
    public boolean isClass() {
        return true;
    }
    
    @Override
    public ClassType getType() {
        // Cast succeeds by construction because the type has been correctly set
        // in the constructor.
        return (ClassType) super.getType();
    };

    public ClassDefinition getSuperClass() {
        return superClass;
    }

    private final EnvironmentExp members;
    private final ClassDefinition superClass; 

    public EnvironmentExp getMembers() {
        return members;
    }

    public ClassDefinition(ClassType type, Location location, ClassDefinition superClass) {
        super(type, location);
        EnvironmentExp parent;
        if (superClass != null) {
            parent = superClass.getMembers();
        } else {
            parent = null;
        }
        members = new EnvironmentExp(parent);
        this.superClass = superClass;
    }


    /**
     * location on the global pile of the vTable.
     */
    private RegisterOffset vTableAddr;

    /**
     * Set the address of the VTable
     * @param value the new address
     */
    public void setVTableAddr(RegisterOffset value) {
        this.vTableAddr = value;
    }

    /**
     * Get the address of the VTable
     * @return the address of the VTable
     */
    public RegisterOffset getVTableAddr() {
        return this.vTableAddr;
    }
    
    @Override
    public boolean spotRelatedDefs() {
        if (this.superClass != null) {
            this.superClass.spotUsedVar();
            return this.superClass.spotUsedVar();
        }
        return false;
    }


}
