package me.xswezan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import me.xswezan.Environment.RuntimeBundle;
import me.xswezan.Environment.RuntimeNativeFunction;
import me.xswezan.Environment.RuntimeNumber;
import me.xswezan.Environment.RuntimeString;
import me.xswezan.Environment.RuntimeValue;
import me.xswezan.Lexer.Token;
import me.xswezan.Parser.Body;

public class Main {
    // Send .shit file path as argument OR input with scanner
    public static void main(String[] mainArgs) throws FileNotFoundException, IOException {
        Scanner scanner = new Scanner(System.in);

        String fileName;
        if (mainArgs.length == 0) {
            System.out.println("No argument given! Please input path to .shit file!");
            fileName = scanner.nextLine();
            scanner.close();
            // throw new RuntimeException("No argument specified!");
        } else {
            fileName = mainArgs[0];
        }
        if (fileName == null) {
            scanner.close();
            throw new RuntimeException("File path needs to be the first argument!");
        }

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        // delete the last new line separator
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();

        String content = stringBuilder.toString();

        //> Run ShitCode
        Vector<Token> tokens = Lexer.Lexilize(content);
        Environment global = new Environment();

        global.SetVariable("takeInput", new RuntimeNativeFunction(args -> {
            return new RuntimeString(scanner.nextLine());
        }));

        global.SetVariable("toString", new RuntimeNativeFunction(args -> {
            return new RuntimeString(args[0].toString());
        }));

        global.SetVariable("toNumber", new RuntimeNativeFunction(args -> {
            if (args[0] instanceof RuntimeString string) {
                return new RuntimeNumber(Double.valueOf(string.value));
            }
            return null;
        }));

        global.SetVariable("talk", new RuntimeNativeFunction(args -> {
            for (int i = 0; i < args.length; ++i) {
                RuntimeValue value = args[i];
                System.out.print(value + "\t");
            }
            System.out.println("");
            return null;
        }));

        { // Math library
            RuntimeBundle mathLibrary = new RuntimeBundle();
            mathLibrary.environment.SetVariable("pi", new RuntimeNumber(3.14159265));
            mathLibrary.environment.SetVariable("tau", new RuntimeNumber(3.14159265 * 2));
            global.SetVariable("math", mathLibrary);
        }

        // final int ALIGN = 20;
        // for (Token token : tokens) {
        //     String raw = token.raw == null ? "" : token.raw;
        //     System.out.println("\u001B[30;40;1m " + raw.replace('\n', ' ') + " ".repeat(Math.max(0, ALIGN - raw.length())) + "\u001B[30;43;1m " + token.type + " \u001B[0m");
        // }

        Parser parser = new Parser();
        Body program = parser.Parse(tokens);

        Interpreter.EvaluateBody(program, global);
        scanner.close();
    }
}