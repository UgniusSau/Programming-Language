package ugnopus.interpreter;


import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import ugnopus.interpreter.generated.*;
import ugnopus.interpreter.models.FunctionInfo;
import ugnopus.interpreter.tables.ScopesTable;
import ugnopus.interpreter.tables.SymbolTable;
import ugnopus.interpreter.visitors.InterpreterVisitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class UgnopusInterpreter {
    public static void main(String[] args) {
        // Initialize variables to hold parsed arguments
        String filename = null;
        boolean isInteractiveMode = false;

        // Loop through program arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f" -> {
                    // If the -f flag is provided, check if there is a filename argument after it
                    if (i + 1 < args.length) {
                        // If there is a filename argument, store it and skip the next iteration of the loop
                        filename = args[i + 1];
                        i++;
                    } else {
                        // If there is no filename argument, print an error message and the help information and exit the program
                        System.err.println("Error: Missing filename argument for -f flag.");
                        printHelp();
                        System.exit(1);
                    }
                }
                case "-i" ->
                    // If the -i flag is provided, enable interactive mode
                        isInteractiveMode = true;
                case "-h" -> {
                    // If the -h flag is provided, print the help information and exit the program
                    printHelp();
                    System.exit(0);
                }
                default -> {
                    // If an invalid argument is provided, print an error message and the help information and exit the program
                    System.err.println("Error: Invalid argument: " + args[i]);
                    printHelp();
                    System.exit(1);
                }
            }
        }

        try {
            if (isInteractiveMode) {
                processInteractiveInput();
            } else {
                processFile(filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java ArgumentParser [-f filename] [-i] [-h]");
        System.out.println("-f filename\tPass a file as an argument");
        System.out.println("-i\t\tEnable interactive mode");
        System.out.println("-h\t\tDisplay help information");
    }

    private static void processInteractiveInput() throws IOException {
        SymbolTable symbolTable = new SymbolTable();
        ScopesTable scopesTable = new ScopesTable();
        ArrayList<FunctionInfo> functionList = new ArrayList<FunctionInfo>();
        UUID currentScopeId = UUID.randomUUID();
        scopesTable.put(currentScopeId, null);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (Objects.equals(line, "exit")) {
                break;
            }
            input += line + "\n";
            try {
                String output = executeCode(symbolTable, CharStreams.fromString(input), scopesTable, currentScopeId, functionList);
                if (output != null) {
                    input = "";
                    if (!output.equals("")) {
                        System.out.println(output);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("<ERROR> " + e.getMessage());
                input = "";
            }
        }
    }

    public static void processFile(String filename) {
        SymbolTable symbolTable = new SymbolTable();
        ScopesTable scopesTable = new ScopesTable();
        ArrayList<FunctionInfo> functionList = new ArrayList<FunctionInfo>();
        UUID currentScopeId = UUID.randomUUID();
        scopesTable.put(currentScopeId, null);

        try {
            String output = executeCode(symbolTable, CharStreams.fromFileName(filename), scopesTable, currentScopeId, functionList);
            System.out.println("<PROGRAM OUTPUT>");
            System.out.println(output);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("<ERROR> " + e.getMessage());
        }
    }

    private static String executeCode(SymbolTable symbolTable, CharStream input, ScopesTable scopesTable, UUID currentScopeId, ArrayList<FunctionInfo> functionList) {
        UgnopusLexer lexer = new UgnopusLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        UgnopusParser parser = new UgnopusParser(tokens);
        parser.removeErrorListeners();
        UgnopusErrorListener errorListener = new UgnopusErrorListener();
        parser.addErrorListener(errorListener);

        ParseTree tree = parser.program();

        if (errorListener.isHasSyntaxError()) {
            throw new ParseCancellationException(errorListener.getErrorMsg());
        }
        if (errorListener.isPartialTree()) {
            return null;
        }

        InterpreterVisitor interpreter = new InterpreterVisitor(symbolTable, scopesTable, currentScopeId, functionList);
        return (String) interpreter.visit(tree);
    }
}