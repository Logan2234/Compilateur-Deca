package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import java.util.HashMap;
import java.util.Map;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

// A FAIRE: étendre cette classe pour traiter la partie "avec objet" de Déca
/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl03
 * @date 01/01/2023
 */
public class EnvironmentType {
    public EnvironmentType(DecacCompiler compiler) {
        
        envTypes = new HashMap<Symbol, TypeDefinition>();

        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.
        
        Symbol object = compiler.createSymbol("Object");
        OBJECT = new ClassType(object, Location.BUILTIN, null);
        envTypes.put(object, new TypeDefinition(OBJECT, Location.BUILTIN));

        Symbol Null = compiler.createSymbol("null");
        NULL = new ClassType(Null, Location.BUILTIN, null);
        envTypes.put(object, new TypeDefinition(NULL, Location.BUILTIN));
    }

    private final Map<Symbol, TypeDefinition> envTypes;

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    public void set(Symbol s, TypeDefinition def) {
        envTypes.put(s, def);
    }

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final StringType  STRING;
    public final BooleanType BOOLEAN;
    public final ClassType   OBJECT;
    public final ClassType   NULL;
}
