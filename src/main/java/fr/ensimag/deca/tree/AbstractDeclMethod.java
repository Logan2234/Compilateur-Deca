package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Method declaration
 *
 * @author Jorge
 * @date 08/01/2023
 */
public abstract class AbstractDeclMethod extends Tree {

        /**
         * Implements non-terminal "decl_method" of [SyntaxeContextuelle] in pass 2
         * 
         * @param compiler     contains "env_types" attribute
         * @param localEnv
         *                     its "parentEnvironment" corresponds to the "env_exp_sup"
         *                     attribute
         *                     in precondition, its "current" dictionary corresponds to
         *                     the "env_exp" attribute
         *                     in postcondition, its "current" dictionary corresponds to
         *                     the synthetized attribute
         * @param currentClass
         *                     corresponds to the "class" attribute (null in the main
         *                     bloc).
         */
        protected abstract void verifyDeclMethod(DecacCompiler compiler, EnvironmentExp localEnv,
                        ClassDefinition currentClass) throws ContextualError;

        /**
         * Implements non-terminal "decl_method" of [SyntaxeContextuelle] in pass 3
         * 
         * @param compiler     contains "env_types" attribute
         * @param localEnv
         *                     its "parentEnvironment" corresponds to the "env_exp_sup"
         *                     attribute
         *                     in precondition, its "current" dictionary corresponds to
         *                     the "env_exp" attribute
         *                     in postcondition, its "current" dictionary corresponds to
         *                     the synthetized attribute
         * @param currentClass
         *                     corresponds to the "class" attribute (null in the main
         *                     bloc).
         */
        protected abstract void verifyMethodBody(DecacCompiler compiler, EnvironmentExp locaEnv,
                        ClassDefinition currentClass) throws ContextualError;

        /**
         * Get the name of the method. Used to generate the vTable.
         * 
         * @return
         */
        public abstract String getMethodName();

        /**
         * Code generation for the methods.
         * 
         * @param compiler where we write the codes to.
         */
        public abstract void codeGenMethod(DecacCompiler compiler, String className);
}
