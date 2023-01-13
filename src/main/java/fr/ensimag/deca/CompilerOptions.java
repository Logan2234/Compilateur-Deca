package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl03
 * @date 01/01/2023
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    // goes up to 4 because defaults to "all" in logger level
    public static final int MAX_DEBUG_MODE = 4;

    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }

    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    public CompileMode getCompileMode() {
        return compileMode;
    }

    public boolean getRunTestChecks() {
        return runTestChecks;
    }

    public int getUsedRegisterNumber() {
        return usedRegisterNumber;
    }

    public boolean getDisplayWarnings() {
        return displayWarnings;
    }

    public boolean getOptimize() {
        return optimize;
    }

    // added to support compile options
    enum CompileMode {
        Compile,
        ParseOnly,
        Verify,
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private List<File> sourceFiles = new ArrayList<File>();
    private CompileMode compileMode = CompileMode.Compile;
    private boolean runTestChecks = true;
    private int usedRegisterNumber = 16;
    private boolean displayWarnings = false;
    private boolean optimize = false;

    public void parseArgs(String[] args) throws CLIException {

        // parse each argument with the following rules:

        // [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]

        // -b (banner) : display group name
        // -p (parse) : only build the tree, and decompile it
        // -v (verifiation) : only verify, do not output files
        // -n (no check) : remove test on execution (1.11, 1.13)
        // -r X (register) : limits the use of registers R0 to RX-1 (with 4<=X<=16)
        // -d (debug) : display debug trace. Can be repeated.
        // -P (parallel) compile all deca files in parallel.
        // -w (warnings) (optional) : display warnings
        // -o (optimize) : optimize the code

        // Note : -p and -v are incompatible.

        int arg_index = 0;
        // start by parsing options :
        while (arg_index < args.length) {
            String current_arg = args[arg_index];
            if (current_arg.startsWith("-")) {
                // parse arg
                switch (current_arg) {
                    case "-b": {
                        // -b an only be used as an alone option, so checks args length is 1
                        if (args.length > 1) {
                            throw new CLIException("\u001B[31m/!\\ The option -b can only be used alone.\u001B[37m");
                        }
                        printBanner = true;
                        break;
                    }
                    case "-p": {
                        // check there were no -v option
                        switch (compileMode) {
                            case Compile: {
                                compileMode = CompileMode.ParseOnly;
                                break;
                            }
                            case ParseOnly: {
                                /* ?? maybe throw don't repeat args exception ? */ break;
                            }
                            case Verify: {
                                throw new CLIException(
                                        "\u001B[31m/!\\ The option -p is not compatible with the -v option.\u001B[37m");
                            }
                        }
                        break;
                    }
                    case "-v": {
                        // check there were no -p option
                        switch (compileMode) {
                            case Compile: {
                                compileMode = CompileMode.Verify;
                                break;
                            }
                            case ParseOnly: {
                                throw new CLIException(
                                        "\u001B[31m/!\\ The option -v is not compatible with the -p option.\u001B[37m");
                            }
                            case Verify: {
                                /* ?? maybe throw don't repeat args exception ? */ break;
                            }
                        }
                        break;
                    }
                    case "-n": {
                        runTestChecks = false;
                        break;
                    }
                    case "-r": {
                        // try to read the register number
                        try {
                            int registerNumber = Integer.parseInt(args[arg_index + 1]);
                            if (registerNumber < 4 || registerNumber > 16) {
                                throw new CLIException(
                                        "le nombre de registre à utiliser (spécifié avec -r) doit être compris entre 4 et 16.");
                            }
                            usedRegisterNumber = registerNumber;
                            break;
                        } catch (NumberFormatException e) {
                            throw new CLIException(
                                    "L'option '-r' doit être suivie du nombre de registre à utiliser (compris entre 4 et 16).");
                        }
                    }
                    case "-d": {
                        debug++;
                        break;
                    }
                    case "-P": {
                        parallel = true;
                        break;
                    }
                    case "-w": {
                        displayWarnings = true;
                    }

                    case "-o": {
                        optimize = true;
                    }
                }
                arg_index++;
            } else {
                // finished options, now on parsing files
                break;
            }
        }

        // now let's read the files for the remaining arguments
        if (!printBanner) {
            // there are files to read !
            for (int i = arg_index; i < args.length; i++) {
                // check the file extension is .deca
                if (args[i].endsWith(".deca")) {
                    // read the file
                    sourceFiles.add(new File(args[i]));
                }
            }

            Logger logger = Logger.getRootLogger();
            // map command-line debug option to log4j's level.
            switch (getDebug()) {
                case QUIET:
                    break; // keep default
                case INFO:
                    logger.setLevel(Level.INFO);
                    break;
                case DEBUG:
                    logger.setLevel(Level.DEBUG);
                    break;
                case TRACE:
                    logger.setLevel(Level.TRACE);
                    break;
                default:
                    logger.setLevel(Level.ALL);
                    break;
            }
            logger.info("Application-wide trace level set to " + logger.getLevel());

            boolean assertsEnabled = false;
            assert assertsEnabled = true; // Intentional side effect!!!
            if (assertsEnabled) {
                logger.info("Java assertions enabled");
            } else {
                logger.info("Java assertions disabled");
            }
        }
    }

    protected void displayUsage() {
        System.out.println();
        System.out.println("Correct usage: decac [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]");
        System.out.println();
        System.out.println("-b (banner)              : display group name");
        System.out.println("-p (parse)               : only build the tree, and decompile it");
        System.out.println("-v (verification)        : only verify, do not output files");
        System.out.println("-n (no check)            : remove test on execution (1.11, 1.13)");
        System.out.println("-r X (register)          : limits the use of registers R0 to RX-1 (with 4<=X<=16)");
        System.out.println("-d (debug)               : display debug trace. Can be repeated.");
        System.out.println("-P (parallel)            : compile all deca files in parallel.");
        System.out.println("-w (warnings) (optional) : display warnings");
        System.out.println("-o (optimize)            : optimize the code");
        System.out.println();
        System.out.println("Note : -p and -v are incompatible.");

    }
}
