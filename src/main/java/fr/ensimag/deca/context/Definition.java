package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Definition of an identifier.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public abstract class Definition {
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
    private Type type;

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

}
