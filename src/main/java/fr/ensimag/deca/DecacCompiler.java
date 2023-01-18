package fr.ensimag.deca;

import fr.ensimag.deca.CompilerOptions.CompileMode;
import fr.ensimag.deca.codegen.runtimeErrors.AbstractRuntimeErr;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.optim.AssemblyOptimizer;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.GPRegister;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.log4j.Logger;

/**
 * Decac compiler instance.
 *
 * This class is to be instantiated once per source file to be compiled. It
 * contains the meta-data used for compiling (source file name, compilation
 * options) and the necessary utilities for compilation (symbol tables, abstract
 * representation of target file, ...).
 *
 * It contains several objects specialized for different tasks. Delegate methods
 * are used to simplify the code of the caller (e.g. call
 * compiler.addInstruction() instead of compiler.getProgram().addInstruction()).
 *
 * @author gl03
 * @date 01/01/2023
 */
public class DecacCompiler {
    private static final Logger LOG = Logger.getLogger(DecacCompiler.class);

    /**
     * Portable newline character.
     */
    private static final String nl = System.getProperty("line.separator", "\n");

    public DecacCompiler(CompilerOptions compilerOptions, File source) {
        super();
        this.compilerOptions = compilerOptions;
        if (compilerOptions != null) {
            availableRegisters = new boolean[compilerOptions.getUsedRegisterNumber() - 2];
        } else {
            availableRegisters = new boolean[14];
        }
        for (int i = 0; i < availableRegisters.length; i++) {
            availableRegisters[i] = true;
        }
        this.usedErrors = new HashMap<>();
        this.stackUsedSizes = new ArrayList<Integer>();
        this.maxStackUseSize = new ArrayList<>();
        this.source = source;
    }

    /**
     * Source file associated with this compiler instance.
     */
    public File getSource() {
        return source;
    }

    /**
     * Compilation options (e.g. when to stop compilation, number of registers
     * to use, ...).
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#add(fr.ensimag.ima.pseudocode.AbstractLine)
     */
    public void add(AbstractLine line) {
        program.add(line);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */
    public void addComment(String comment) {
        program.addComment(comment);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabel(Label label) {
        program.addLabel(label);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addInstruction(Instruction instruction) {
        program.addInstruction(instruction);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     *      java.lang.String)
     */
    public void addInstruction(Instruction instruction, String comment) {
        program.addInstruction(instruction, comment);
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#addFirst(fr.ensimag.ima.pseudocode.Line)
     */
    public void addInstructionFirst(Instruction instruction) {
        program.addFirst(new Line(instruction));
    }

    /**
     * @see
     *      fr.ensimag.ima.pseudocode.IMAProgram#display()
     */
    public String displayIMAProgram() {
        return program.display();
    }

    private final CompilerOptions compilerOptions;
    private final File source;
    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private final IMAProgram program = new IMAProgram();

    /**
     * All the available registers.
     * When generating code, we can ask what registers are used by previous calls.
     * If null is returned, we can then save a register and restore it.
     */
    private boolean[] availableRegisters;
    /**
     * Stores the max reached stack size for each context (code block).
     * We have one default context when the program starts, and more context with
     * methods calls.
     * Here, a block, or context, is for main program, ot methods, or classes
     * initialization.
     */
    private List<Integer> maxStackUseSize;

    /**
     * Store the current number of variables pushed on the stack.
     */
    private List<Integer> stackUsedSizes;

    /**
     * Get a available register.
     * 
     * @return the register we can use. Can be null if none are.
     */
    public GPRegister allocateRegister() {
        for (int i = 0; i < availableRegisters.length; i++) {
            if (availableRegisters[i]) {
                availableRegisters[i] = false;
                return GPRegister.getR(i + 2);
            }
        }
        return null;
    }

    /**
     * Set the given register as not used anymore.
     * 
     * @param register
     */
    public void freeRegister(GPRegister register) {
        availableRegisters[register.getNumber() - 2] = true;
    }

    /**
     * Creates a new code context block.
     */
    public void newCodeContext() {
        stackUsedSizes.add(0);
        maxStackUseSize.add(0);
    }

    /**
     * Increase the size of the use stack of the block
     * 
     * @param increment how much we want to increment the stack
     */
    public void increaseContextUsedStack(int increment) {
        if(stackUsedSizes.size() == 0 || maxStackUseSize.size() == 0) {
            throw new RuntimeException("No current context to increment !");
        }
        else {
            int value = stackUsedSizes.get(stackUsedSizes.size() - 1) + increment;
            stackUsedSizes.set(stackUsedSizes.size() - 1, value);
            if (value > maxStackUseSize.get(maxStackUseSize.size() - 1)) {
                maxStackUseSize.set(maxStackUseSize.size() - 1, value);
            }
        }
    }

    /**
     * Increase the size of the use stack of the block by 1.
     */
    public void incrementContextUsedStack() {
        increaseContextUsedStack(1);
    }

    /**
     * finish the current context.
     * 
     * @return the value of the max stack size of that context.
     */
    public int endCodeContext() {
        stackUsedSizes.remove(stackUsedSizes.size() - 1);
        return maxStackUseSize.remove(maxStackUseSize.size() - 1);
    }

    /**
     * The errors the assembly program can throw at runtime.
     */
    public HashMap<Integer, AbstractRuntimeErr> usedErrors;

    /**
     * Add an error to the used errors. They will then be generated at the end of
     * the assembly code.
     */
    public void useRuntimeError(AbstractRuntimeErr error) {
        // check we are not using that error already
        if (!usedErrors.containsKey(error.errorId())) {
            usedErrors.put(error.errorId(), error);
        }
    }

    public HashMap<Integer, AbstractRuntimeErr> getAllErrors() {
        return usedErrors;
    }

    /** The global environment for types (and the symbolTable) */
    public final SymbolTable symbolTable = new SymbolTable();
    public final EnvironmentType environmentType = new EnvironmentType(this);

    public Symbol createSymbol(String name) {
        return symbolTable.create(name);
    }

    /**
     * Run the compiler (parse source file, generate code)
     *
     * @return true on error
     */
    public boolean compile() {
        String sourceFile = source.getAbsolutePath();
        String destFile;
        destFile = sourceFile.substring(0, sourceFile.length() - 4) + "ass";
        PrintStream err = System.err;
        PrintStream out = System.out;
        LOG.debug("Compiling file " + sourceFile + " to assembly file " + destFile);
        try {
            return doCompile(sourceFile, destFile, out, err);
        } catch (LocationException e) {
            e.display(err);
            return true;
        } catch (DecacFatalError e) {
            err.println(e.getMessage());
            return true;
        } catch (StackOverflowError e) {
            LOG.debug("stack overflow", e);
            err.println("Stack overflow while compiling file " + sourceFile + ".");
            return true;
        } catch (Exception e) {
            LOG.fatal("Exception raised while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        } catch (AssertionError e) {
            LOG.fatal("Assertion failed while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        }
    }

    /**
     * Internal function that does the job of compiling (i.e. calling lexer,
     * verification and code generation).
     *
     * @param sourceName name of the source (deca) file
     * @param destName   name of the destination (assembly) file
     * @param out        stream to use for standard output (output of decac -p)
     * @param err        stream to use to display compilation errors
     *
     * @return true on error
     */
    private boolean doCompile(String sourceName, String destName, PrintStream out, PrintStream err)
            throws DecacFatalError, LocationException {
        AbstractProgram prog = doLexingAndParsing(sourceName, err);
        assert (prog.checkAllLocations());

        if (prog == null) {
            LOG.info("Parsing failed");
            return true;
        }

        if (compilerOptions.getCompileMode() != CompileMode.ParseOnly || compilerOptions.getOptimize()) {
            assert (prog.checkAllLocations());

            prog.verifyProgram(this);
            assert (prog.checkAllDecorations());
        }

        if (compilerOptions.getCompileMode() != CompileMode.Verify) {
            if (compilerOptions.getOptimize()){
                prog.optimizeTree();
            }
            if (compilerOptions.getCompileMode() != CompileMode.ParseOnly) {
                addComment("start main program");
                prog.codeGenProgram(this);
                addComment("end main program");
                LOG.debug("Generated assembly code:" + nl + program.display());
                LOG.info("Output file assembly file is: " + destName);
            }
            if(compilerOptions.getOptimize()) {
                AssemblyOptimizer.Optimize(program);
            }

            if (compilerOptions.getCompileMode() == CompileMode.ParseOnly) {
                LOG.info("Writing deca file ...");
                prog.decompile(out);
                LOG.info("Decompilation of " + sourceName + " successful.");
            } else {
                FileOutputStream fstream = null;
                try {
                    fstream = new FileOutputStream(destName);
                } catch (FileNotFoundException e) {
                    throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
                }
                LOG.info("Writing assembler file ...");
                program.display(new PrintStream(fstream));
                LOG.info("Compilation of " + sourceName + " successful.");
            }
        }
        return false;
    }

    /**
     * Build and call the lexer and parser to build the primitive abstract
     * syntax tree.
     *
     * @param sourceName Name of the file to parse
     * @param err        Stream to send error messages to
     * @return the abstract syntax tree
     * @throws DecacFatalError    When an error prevented opening the source file
     * @throws DecacInternalError When an inconsistency was detected in the
     *                            compiler.
     * @throws LocationException  When a compilation error (incorrect program)
     *                            occurs.
     */
    protected AbstractProgram doLexingAndParsing(String sourceName, PrintStream err)
            throws DecacFatalError, DecacInternalError {
        DecaLexer lex;
        try {
            lex = new DecaLexer(CharStreams.fromFileName(sourceName));
        } catch (IOException ex) {
            throw new DecacFatalError("Failed to open input file: " + ex.getLocalizedMessage());
        }
        lex.setDecacCompiler(this);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(this);
        return parser.parseProgramAndManageErrors(err);
    }

}
