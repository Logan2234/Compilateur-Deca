package fr.ensimag.deca.context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 * 
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 * 
 * The dictionary at the head of this list thus corresponds to the "current"
 * block (eg class).
 * 
 * Searching a definition (through method get) is done in the "current"
 * dictionary and in the parentEnvironment if it fails.
 * 
 * Insertion (through method declare) is always done in the "current"
 * dictionary.
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class EnvironmentExp {
    public EnvironmentExp parentEnvironment;
    private Map<Symbol, ExpDefinition> dico = new HashMap<>();

    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     */
    public ExpDefinition get(Symbol key) {
        Map<Symbol, ExpDefinition> dico = this.dico;
        EnvironmentExp current = this;
        while (dico != null) {
            if (dico.containsKey(key))
                return dico.get(key);
            if (current.parentEnvironment != null) {
                current = current.parentEnvironment;
                dico = current.dico;
            } else
                break;
        }
        return null;
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary
     * - or, hides the previous declaration otherwise.
     * 
     * @param name
     *             Name of the symbol to define
     * @param def
     *             Definition of the symbol
     * @throws DoubleDefException
     *                            if the symbol is already defined at the "current"
     *                            dictionary
     *
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        if (dico.containsKey(name))
            throw new DoubleDefException();
        dico.put(name, def);
    }


    public List<MethodDefinition> getMethods() {
        List<MethodDefinition> methods = new LinkedList<>();
        for(Symbol sym : dico.keySet()) {
            ExpDefinition def = dico.get(sym);
            if(def.isMethod()) {
                try {
                    MethodDefinition method = def.asMethodDefinition(null, null);
                    method.setName(sym.getName());
                    methods.add(method);
                }
                catch(ContextualError e) {
                    continue;
                }
            }
        }
        return methods;
    }
}
