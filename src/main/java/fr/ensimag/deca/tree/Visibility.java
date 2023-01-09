package fr.ensimag.deca.tree;

/**
 * Visibility of a field.
 *
 * @author gl03
 * @date 01/01/2023
 */

public enum Visibility {
    PUBLIC,
    PROTECTED;

    @Override
    public String toString() {
        switch (this) {
            case PUBLIC : return "public";
            case PROTECTED : return "protected";
            default : throw new IllegalArgumentException();
        }
    }
}
