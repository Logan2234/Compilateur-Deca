package fr.ensimag.deca.context;

import org.apache.log4j.Logger;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Definition of an identifier.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class Definition {
    private static final Logger LOG = Logger.getLogger(Definition.class);

    @Override
    public String toString() {
        String res;
        res = getNature();
        if (location == Location.BUILTIN) {
            res += " (builtin)";
        } else {
            res += " defined at " + location;
        }
        res += ", type=" + type;
        return res;
    }

    public abstract String getNature();

    public Definition(Type type, Location location) {
        super();
        this.location = location;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private Location location;
    protected Type type;

    public boolean isField() {
        return false;
    }

    public boolean isMethod() {
        return false;
    }

    public boolean isClass() {
        return false;
    }

    public boolean isParam() {
        return false;
    }

    /**
     * Return the same object, as type MethodDefinition, if possible. Throws
     * ContextualError(errorMessage, l) otherwise.
     */
    public MethodDefinition asMethodDefinition(String errorMessage, Location l)
            throws ContextualError {
        throw new ContextualError(errorMessage, l);
    }

    /**
     * Return the same object, as type FieldDefinition, if possible. Throws
     * ContextualError(errorMessage, l) otherwise.
     */
    public FieldDefinition asFieldDefinition(String errorMessage, Location l)
            throws ContextualError {
        throw new ContextualError(errorMessage, l);
    }

    public abstract boolean isExpression();

    /**
     * where the variable was declared in the assembly code.
     * For vars : they are in the form of d(LB) or d(GB).
     * For params : they are in the form of -d(LB).
     * For fields : theyr are in the form of d(XX).
     * We don't care about the register because what matter is the offset.
     */
    private RegisterOffset dAddr;

    /**
     * Get the DAddr of the varaible declaration.
     * 
     * @return the DAddr in the assembly code of the var.
     */
    public RegisterOffset getDAddr() {
        return dAddr;
    }

    /**
     * Set the DAddr of the varaible declaration.
     * 
     * @param dAddr The DAddr in the assembly code of the var.
     */
    public void setDAddr(RegisterOffset dAddr) {
        this.dAddr = dAddr;
    }

    /**
     * Offset of the field from the object memory location.
     * This is to avoid a dummy register when on dAddr when on a field.
     */
    private int fieldOffset;

    /**
     * Set the offset only of field declaration.
     * @param newValue the offset.
     */
    public void setDAddrOffsetOnly(int newValue) {
        fieldOffset = newValue;
    }

    /**
     * Get the offset only of the field declaration.
     * @return the offset.
     */
    public int getDAddrOffsetOnly() {
        return fieldOffset;
    }

    /**
     * If the variable is used in the main program
     */
    boolean used = false;

    /**
     * Get the used attribute
     * @return the used attribute
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * Reset the used attribute back to false
     */
    public void resetUsed() {
        LOG.debug("Reset : "+ this.toString());
        this.used = false;
    }

    /*
     * Set the used attribute to true
     */
    public void setUsed() {
        LOG.debug("Set to used : " + toString());
        used = true;
    }

    /**
     * Set to true its "used" attribute and the one of the defitions in relation with itself
     * and explore related definitions
     * @param compiler
     */
    public boolean spotUsedVar() {
        // prevent looping over methods
        if (!this.isUsed()) {
            this.setUsed();
            return this.spotRelatedDefs();
        }
        return false;
    }

    /**
     * Set to true the "used" attribute of related definitions (super or containing class)
     * @return true if a definition as been set to true
     */
    public boolean spotRelatedDefs() {
        boolean varSpotted = false;
        if (this.type.isClass()) {
            varSpotted = ((ClassType)this.type).getDefinition().spotUsedVar();
        }
        return varSpotted;
    }

}
