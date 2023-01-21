package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.Location;

/**
 * Definition of a method parameter.
 *
 * @author gl03
 * @date 01/01/2023
 */
public class ParamDefinition extends ExpDefinition {

    public ParamDefinition(Type type, Location location) {
        super(type, location);
    }

    @Override
    public String getNature() {
        return "parameter";
    }

    @Override
    public boolean isExpression() {
        return true;
    }

    @Override
    public boolean isParam() {
        return true;
    }

}
