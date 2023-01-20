package fr.ensimag.deca.context;

import java.util.Map;
import java.util.Set;
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
     * If the unspotted field is an override of a useful method, it may be dynamically useful.
     * @return true if the method is an override of a used method
     */
    public boolean isOverrideOfUsed(Map<ClassDefinition,Set<Integer>> exploredFields) {
        boolean res = false;
        ClassDefinition superClass = this.containingClass.getSuperClass();
        while(superClass != null && !res && this.index<=superClass.getNumberOfFields()) {
            res = exploredFields.get(superClass).contains(this.index);
            superClass = superClass.getSuperClass();
        }
        return res;
    }
}
