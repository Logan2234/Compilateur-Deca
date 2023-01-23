package fr.ensimag.deca.optim;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractProgram;

/*
 * Class for optimizing a deca tree with our methods.
 */
public class TreeOptimizer {
    public static void Optimize(AbstractProgram program, DecacCompiler compiler) {
        boolean optimized = true;
        int i = 0;
        while(optimized && i<10) {
            optimized = false;
            // solve compile time known cases.
            optimized |= program.collapseProgram().couldCollapse();
            // remove useless variables
            program.factorise(compiler);
            program.splitCalculus(compiler);
            optimized = program.optimUnusedVar(); 
            program.substituteInlineMethods();
            i++;
        }
    }
}
