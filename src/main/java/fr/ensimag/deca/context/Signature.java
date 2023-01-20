package fr.ensimag.deca.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Signature of a method (i.e. list of arguments)
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Signature {
    List<Type> args = new ArrayList<Type>();

    public void add(Type t) {
        args.add(t);
    }

    public Type paramNumber(int n) {
        return args.get(n);
    }

    public int size() {
        return args.size();
    }

    public boolean sameSignature(Signature s) {
        if (size() != s.size())
            return false;
        for (int i = 0; i < args.size(); i++)
            if (!paramNumber(i).sameType(s.paramNumber(i)))
                return false;
        return true;
    }
}
