package fr.ensimag.deca.tree;

import java.io.Serializable;

/**
 * Location in a file (File, line, positionInLine).
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Location implements Serializable {
    /*
     * Location implements Serializable because it appears as a field
     * of LocationException, which is serializable.
     */
    private static final long serialVersionUID = -2906437663480660298L;

    public static final String NO_SOURCE_NAME = "<no source file>";
    public static final Location BUILTIN = new Location(-1, -1, NO_SOURCE_NAME);

    /**
     * Display the (line, positionInLine) as a String. The file is not
     * displayed.
     */
    @Override
    public String toString() {
        if (this == BUILTIN) {
            return "[builtin]";
        } else {
            return "[" + line + ", " + positionInLine + "]";
        }
    }

    public int getLine() {
        return line;
    }

    public int getPositionInLine() {
        return positionInLine;
    }

    public String getFilename() {
        if (filename != null) {
            return filename;
        } else {
            // we're probably reading from stdin
            return NO_SOURCE_NAME;
        }
    }

    private final int line;
    private final int positionInLine;
    private final String filename;

    public Location(int line, int positionInLine, String filename) {
        super();
        this.line = line;
        this.positionInLine = positionInLine;
        this.filename = filename;
    }

    /**
     * Used to write a unique label.
     * (for example, in if conditions)
     * @return A unique string through the compilation pipeline that can be used as a label.
     */
    public String toLabel() {
        return (filename.substring(filename.lastIndexOf("/") + 1) + "." + line + "." + positionInLine).replaceAll("[^a-zA-Z0-9_.]", "");
    }

}
