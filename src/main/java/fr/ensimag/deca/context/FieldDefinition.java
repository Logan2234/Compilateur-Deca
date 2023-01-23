package fr.ensimag.deca.context;

import java.util.Map;
import java.util.Set;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.Visibility;

/**
 * Definition of a field (data member of a class).
 *
 * @author gl03
 * @date 01/01/2023
 */
public class FieldDefinition extends ExpDefinition {
    public int getIndex() {
        return index;
    }

    private int index;
    
    @Override
    public boolean isField() {
        return true;
    }

    private final Visibility visibility;
    private final ClassDefinition containingClass;
    
    public FieldDefinition(Type type, Location location, Visibility visibility,
            ClassDefinition memberOf, int index) {
        super(type, location);
        this.visibility = visibility;
        this.containingClass = memberOf;
        this.index = index;
    }
    
    @Override
    public FieldDefinition asFieldDefinition(String errorMessage, Location l)
            throws ContextualError {
        return this;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public ClassDefinition getContainingClass() {
        return containingClass;
    }

    @Override
    public String getNature() {
        return "field";
    }

    @Override
    public boolean isExpression() {
        return true;
    }

    @Override
    public boolean spotRelatedDefs() {
        boolean varSpotted = super.spotRelatedDefs();
        varSpotted = this.containingClass.spotUsedVar() || varSpotted;
        return varSpotted;
    }

    /**
     * Spot the field if it is an override of a used field.
     * It may become dynamically useful
     * @param map storing all symbols of used fields and associating to those symbols
     * the set of classes definition that have this symbol as a field when this field is used
     */
    public void spotOverridingFields(Symbol symbol, Map<Symbol,Set<ClassDefinition>> usedFields) {
        if (usedFields.containsKey(symbol)) {
            ClassDefinition currentClass = this.containingClass;
            // check if override of used field
            while (currentClass != null && !usedFields.get(symbol).contains(currentClass)) {
                currentClass = currentClass.getSuperClass();
            }
            if (currentClass != null) {
                this.spotUsedVar();
                usedFields.get(symbol).add(currentClass);
            }
        }
    }

}
