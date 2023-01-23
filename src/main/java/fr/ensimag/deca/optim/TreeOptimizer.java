package fr.ensimag.deca.optim;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.Program;

/*
 * Class for optimizing a deca tree with our methods.
 */
public class TreeOptimizer {

    public static void Optimize(AbstractProgram prog, DecacCompiler compiler) {
        Program program = (Program) prog;
        boolean optimized = true;
        int i = 0;
        while(optimized && i<10) {
            optimized = false;
            // solve compile time known cases.
            optimized |= program.collapseProgram().couldCollapse();
            // remove useless variables
            program.factorise(compiler);
            program.splitCalculus(compiler);
            optimized |= program.optimUnusedVar(); 
            program.substituteInlineMethods();
            i++;
        }
    }
}
