package fr.ensimag.deca.optim;

/**
 * Can be a bool, float or int.
 */
public class CollapseValue {
    private final Integer intValue;
    private final Boolean boolValue;
    private final Float floatValue;

    public CollapseValue(int value) {
        intValue = value;
        boolValue = null;
        floatValue = null;
    }

    public CollapseValue(boolean value) {
        intValue = null;
        boolValue = value;
        floatValue = null;
    }

    public CollapseValue(float value) {
        intValue = null;
        boolValue = null;
        floatValue = value;
    }

    public CollapseValue() {
        intValue = null;
        boolValue = null;
        floatValue = null;
    }

    public boolean isInt() {
        return intValue != null;
    }

    public boolean isBool() {
        return boolValue != null;
    }

    public boolean isFloat() {
        return floatValue != null;
    }

    public int asInt() {
        return intValue;
    }

    public boolean asBool() {
        return boolValue;
    }

    public float asFloat() {
        return floatValue;
    }
}
