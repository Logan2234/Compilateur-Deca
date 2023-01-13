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
     */
    private RegisterOffset dAddr;

    /**
     * Get the DAddr of the varaible declaration.
     * @return the DAddr in the assembly code of the var.
     */
    public RegisterOffset getDAddr() {
        return dAddr;
    }

    /**
     * Set the DAddr of the varaible declaration.
     * @param dAddr The DAddr in the assembly code of the var. 
     */
    public void setDAddr(RegisterOffset dAddr) {
        this.dAddr = dAddr;
    }

    /**
     * If the variable is used in the main program
     */
    boolean used = false;

    /**
     * Get the used attribute
     * @return the used attribute
     */
    public boolean getUsed() {
        return this.used;
    }

    /**
     * Reset the used attribute back to false
     */
    public void resetUsed() {
        this.used = false;
    }

    /*
     * Set the used attribute to true
     */
    public void setUsed() {
        this.used = true;
    }

}
