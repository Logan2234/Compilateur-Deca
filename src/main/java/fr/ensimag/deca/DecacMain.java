package fr.ensimag.deca;

import java.io.File;
import org.apache.log4j.Logger;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl03
 * @date 01/01/2023
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);

    public static void main(String[] args) {
        // example log4j message.
        LOG.info("Decac compiler started");
        boolean error = false;
        final CompilerOptions options = new CompilerOptions();

        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n" + e.getMessage());
            options.displayUsage();
            System.exit(1);
        }

        if (options.getPrintBanner()) {
            System.out.println("\u001B[35m" + "================================================");
            System.out.println("\033[0;1m\u001B[36m Groupe 1 - GL03:\033[0;0m");
            System.out.println("\u001B[36m" + "     Nils DEPUILLE   : Chef de projet");
            System.out.println("\u001B[36m" + "     Virgile HENRY   : Code master");
            System.out.println("\u001B[36m" + "     Jorge LURI-VANO : Git master");
            System.out.println("\u001B[36m" + "     Vianney VOUTERS : Secrétaire Général");
            System.out.println("\u001B[36m" + "     Logan WILLEM    : Test master");
            System.out.println("\u001B[35m" + "================================================");
        }

        if (options.getSourceFiles().isEmpty()) {
            if (!options.getPrintBanner())
                options.displayUsage();
            // throw new UnsupportedOperationException("decac without argument not yet
            // implemented");
        }

        if (options.getParallel()) {
            // A FAIRE : instancier DecacCompiler pour chaque fichier à
            // compiler, et lancer l'exécution des méthodes compile() de chaque
            // instance en parallèle. Il est conseillé d'utiliser
            // java.util.concurrent de la bibliothèque standard Java.
            throw new UnsupportedOperationException("Parallel build not yet implemented");
        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                if (compiler.compile()) {
                    error = true;
                }
            }
        }
        System.exit(error ? 1 : 0);
    }
}
