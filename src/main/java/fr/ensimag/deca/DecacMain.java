package fr.ensimag.deca;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
            System.out.println("\u001B[36m" + "     Jorge LURI VAÑÓ : Git master & DRH");
            System.out.println("\u001B[36m" + "     Vianney VOUTERS : Secrétaire Général");
            System.out.println("\u001B[36m" + "     Logan WILLEM    : Test master");
            System.out.println("\u001B[35m" + "================================================");
        }

        List<File> fichiers = options.getSourceFiles();
        if (fichiers.isEmpty()) {
            if (!options.getPrintBanner()) {
                System.err.println("\u001B[31m/!\\ There is no file to compile.\u001B[37m");
                options.displayUsage();
                System.exit(1);
            }
        }

        else if (options.getParallel()) {
            int nbProcesseurs = java.lang.Runtime.getRuntime().availableProcessors();
            ExecutorService filsExec = Executors.newFixedThreadPool(nbProcesseurs);
            for (int i = 0; i < fichiers.size(); i++) {
                DecacCompiler compiler = new DecacCompiler(options, fichiers.get(i));
                Future<Boolean> future = filsExec.submit(() -> {
                    return compiler.compile();
                }); // TODO: Vérifier que le parallele se fait bien
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Set<File> treatedFiles = new HashSet<File>();
            for (File source : options.getSourceFiles())
                if (!treatedFiles.contains(source)) {
                    treatedFiles.add(source);
                    DecacCompiler compiler = new DecacCompiler(options, source);
                    error = compiler.compile();
                }
        }
        System.exit(error ? 1 : 0);
    }
}
