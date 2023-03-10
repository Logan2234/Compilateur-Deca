package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Base class for any node in the Deca abstract syntax tree.
 *
 * Factors all the common elements and utility functions to manipulate trees
 * (location in source-code, pretty-printing, ...).
 *
 * @author gl03
 * @date 01/01/2023
 *
 */
public abstract class Tree {

    private static final Logger LOG = Logger.getLogger(Main.class);

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLocation(int line, int column, String filename) {
        this.location = new Location(line, column, filename);
    }

    private Location location;

    /**
     * Display the tree as a (compilable) source program
     *
     * @param s Buffer to which the result will be written.
     */
    public abstract void decompile(IndentPrintStream s);

    public void decompile(PrintStream s) {
        decompile(new IndentPrintStream(s));
    }

    /**
     * Display the tree as a (compilable) source program
     */
    public String decompile() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream s = new PrintStream(out);
        decompile(s);
        return out.toString();
    }

    /**
     * wrapper for
     * {@link #printNodeLine(PrintStream, String, boolean, boolean, String)},
     * calling {@link #prettyPrintNode()} to display the node element.
     * 
     * @param s
     * @param prefix
     * @param last
     * @param inlist
     * @return The prefix to use for the next recursive calls to
     *         {@link #prettyPrint()}.
     */
    protected String printNodeLine(PrintStream s, String prefix, boolean last,
            boolean inlist) {
        return printNodeLine(s, prefix, last, inlist, prettyPrintNode());
    }

    /**
     * Print the line corresponding to the current node.
     *
     * This displays the prefix (to show the tree hierarchy in ASCII-art), and
     * the node name and information.
     *
     * @param s
     * @param prefix
     * @param last
     * @param inlist
     * @param nodeName
     * @return The prefix to use for the next recursive calls to
     *         {@link #prettyPrint()}.
     */
    String printNodeLine(PrintStream s, String prefix, boolean last,
            boolean inlist, String nodeName) {
        s.print(prefix);
        if (inlist) {
            s.print("[]>");
        } else if (last) {
            s.print("`>");
        } else {
            s.print("+>");
        }
        if (getLocation() != null) {
            s.print(" " + getLocation().toString());
        }
        s.print(" ");
        s.print(nodeName);
        s.println();
        String newPrefix;
        if (last) {
            if (inlist) {
                newPrefix = prefix + "    ";
            } else {
                newPrefix = prefix + "   ";
            }
        } else {
            if (inlist) {
                newPrefix = prefix + "||  ";
            } else {
                newPrefix = prefix + "|  ";
            }
        }
        prettyPrintType(s, newPrefix);
        return newPrefix;
    }

    /**
     * Pretty-print the type of the tree, if applicable
     */
    protected void prettyPrintType(PrintStream s, String prefix) {
        // Nothing by default
    }

    /**
     * Print the node information on a single line.
     *
     * Does not print the children (the recursive call is done by prettyPrint).
     */
    String prettyPrintNode() {
        return this.getClass().getSimpleName();
    }

    /**
     * Pretty-print tree (see {@link #prettyPrint()}), sending output to
     * PrintStream s.
     *
     * @param s
     */
    public final void prettyPrint(PrintStream s) {
        prettyPrint(s, "", true, false);
    }

    /**
     * Pretty-print the tree as a String, using ASCII-art to show the tree
     * hierarchy. Useful for debugging.
     */
    public final String prettyPrint() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream s = new PrintStream(out);
        prettyPrint(s);
        return out.toString();
    }

    protected final void prettyPrint(PrintStream s, String prefix,
            boolean last) {
        prettyPrint(s, prefix, last, false);
    }

    /**
     * Pretty-print tree (see {@link #prettyPrint()}). This is an internal
     * function that should usually not be called directly.
     *
     * @param s      Stream to send the output to
     * @param prefix Prefix (ASCII-art showing hierarchy) to print for this
     *               node.
     * @param last   Whether the node being displayed is the last child of a tree.
     * @param inlist Whether the node is being displayed as part of a list.
     */
    protected final void prettyPrint(PrintStream s, String prefix,
            boolean last, boolean inlist) {
        String next = printNodeLine(s, prefix, last, inlist);
        prettyPrintChildren(s, next);
    }

    /**
     * Used internally by {@link #prettyPrint}. Must call prettyPrint() on each
     * children.
     *
     * @param s
     * @param prefix
     */
    protected abstract void prettyPrintChildren(PrintStream s, String prefix);

    /**
     * Call function f on each node of the tree.
     *
     * @param f
     */
    public void iter(TreeFunction f) {
        f.apply(this);
        iterChildren(f);
    }

    /**
     * Function used internally by {@link #iter(TreeFunction)}. Must call iter() on
     * each
     * child of the tree.
     *
     * @param f
     */
    protected abstract void iterChildren(TreeFunction f);

    /**
     * Check that the current node has correctly been decorated, and throws an
     * error if not.
     *
     * This is used only for defensive programming, each node type can add
     * checks by overloading this method. Does nothing by default.
     *
     * The method is called automatically by {@link #checkAllDecorations()}.
     */
    protected void checkDecoration() {
        // Nothing by default. Override to add checks for specific nodes.
    }

    /**
     * Check that all nodes of the tree have been correctly decorated.
     *
     * Useful for debugging/defensive programming.
     *
     * @return true. Raises an exception in case of error. The return value is
     *         meant to allow assert(tree.checkAllLocations()), to enable the
     *         defensive
     *         check only if assertions are enabled.
     */
    public boolean checkAllDecorations() {
        iter(new TreeFunction() {
            @Override
            public void apply(Tree t) {
                t.checkDecoration();
            }
        });
        return true;
    }

    /**
     * Check that the location has been correctly set for this tree.
     *
     * By default, this checks that getLocation() does not return null, but can
     * be overridden for particular classes that do not require location
     * information.
     */
    protected void checkLocation() {
        if (getLocation() == null) {
            LOG.info(prettyPrint());
            throw new DecacInternalError("Tree "
                    + getClass().getName()
                    + " has no location set");
        }
    }

    /**
     * Check that all nodes of the tree have a location correctly set.
     *
     * Useful for debugging/defensive programming.
     *
     * @return true. Raises an exception in case of error. The return value is
     *         meant to allow assert(tree.checkAllLocations()), to enable the
     *         defensive
     *         check only if assertions are enabled.
     */
    public boolean checkAllLocations() {
        iter(new TreeFunction() {
            @Override
            public void apply(Tree t) {
                t.checkLocation();
            }
        });
        return true;
    }

    /**
     * Call decompile() if the compiler has a debug level greater than 1.
     * 
     * Useful for debugging.
     * 
     * @param compiler
     * @return Decompilation, or the empty string.
     */
    protected String decompileIfDebug(DecacCompiler compiler) {
        if (compiler.getCompilerOptions().getDebug() > 1) {
            return decompile();
        } else {
            return "";
        }
    }

    /**
     * Tells if we are actually defining classes
     */
    public static boolean defClass = true;

    /**
     * Tells if we are actually defining methods
     */
    public static boolean defMethod = false;

    /**
     * Tells which class we are actually defining
     */
    public static Symbol actualClass;


    /**
     * Tells if we are in a while loop
     */
    public static boolean inWhile = false;

    /**
     * Dictionary of the current values of the variables in the Main program
     */
    public static HashMap<Symbol, AbstractExpr> currentValues = new HashMap<Symbol, AbstractExpr>();

    /**
     * Dictionary of the values defined by the class definition, before entering the Main program
     */
    public static HashMap<Symbol, HashMap<Symbol, AbstractExpr>> varModels = new HashMap<Symbol, HashMap<Symbol, AbstractExpr>>();

    /*
     * Dictionary of the values of a class, after have entered the Main program
     */
    public static HashMap<Symbol, HashMap<Symbol, AbstractExpr>> declaredClasses = new HashMap<Symbol, HashMap<Symbol, AbstractExpr>>();

    /**
     * Parameters of the method we are currently defining
     */
    public static Set<Symbol> paramMethod = new HashSet<Symbol>();

    /**
     * Dictionary of the values of a class in the method we are currently defining
     */
    public static HashMap<Symbol, HashMap<Symbol, AbstractExpr>> declaredClassesInMethod = new HashMap<Symbol, HashMap<Symbol, AbstractExpr>>();


    /**
     * Check if the class is a read expression.
     * @return if the class is a read expression.
     */
    public boolean isReadExpr() {
        return false;
    }

    public boolean isSplitable(DecacCompiler compiler){
        return false;
    }

    public AbstractInst splitCalculus(DecacCompiler compiler){
        return null;
    }

    public AbstractInst factorise(DecacCompiler compiler){
        try {
            return (AbstractInst)this;
        } catch (ClassCastException e){
            return null;
        }
    }

    /**
     * Check if the class is a "New" expression.
     * @return if the class is a "New" expression.
     */
    public boolean isNew() {
        return false;
    }

    /**
     * Number of the if/else statement.
     */
    public static Integer ifNumber = 0;

    /**
     * Tells if we are in an if/else statement.
     */
    public static boolean inIf = false;
    
    /**
     * Tells if we are creating a field
     */
    public static boolean inField = false;

    protected Boolean isLiteral() {
        return false;
    }

    protected Boolean isIdentifier() {
        return false;
    }

    /**
     * Set to true the "used" attribute of definitions of used variables
     */
    protected abstract void spotUsedVar();

    /**
     * Only keep the right hand side of useless assignements and evaluates
     * to false the expressions "a instanceof B" when B is unused
     * @return the simplified expression
     */
    protected abstract Tree removeUnusedVar(Program prog);

    public boolean isReturn() {
        return false;
    }

    public Return asReturn() {
        return null;
    }

    /**
     * Store all inline functions in the given map, mapping its definition to its delcaration
     * @param Map<MethodDefinition, ListDeclParam> in which are stored the inline methods 
     */
    protected void spotInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        // do nothing by default
    }

    /**
     * Browse recursively the tree for inline methods spotted previously and substitute them
     * @param inlineMethods map of the inline methods spotted
     * @return Tree used to give the calling method the substitution of the method
     */
    protected abstract Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods);

    /**
     * Add all spotted fields to the Map.
     * @param map storing all symbols of used fields and associating to those symbols
     * the set of classes definition that have this symbol as a field when this field is used
     */
    protected void getSpottedFields(Map<Symbol,Set<ClassDefinition>> usedFields) {
        throw new UnsupportedOperationException("This method should not be called on this tree.");
    }

    /**
     * Spot fields overriding useful fields as they may become dynamically useful
     * @param map storing all symbols of used fields and associating to those symbols
     * the set of classes definition that have this symbol as a field when this field is used
     */
    protected void spotOverridingFields(Map<Symbol,Set<ClassDefinition>> usedFields) {
        throw new UnsupportedOperationException("This method should not be called on this tree.");
    }
}
