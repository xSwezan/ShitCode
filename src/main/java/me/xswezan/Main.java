package me.xswezan;

import java.util.Vector;

import me.xswezan.Environment.RuntimeNativeFunction;
import me.xswezan.Environment.RuntimeValue;
import me.xswezan.Lexer.Token;
import me.xswezan.Parser.Body;

public class Main {
    public static void main(String[] _args) {
        Vector<Token> tokens = Lexer.Lexilize("""
create number called i
set i to 50

create boolean called ok
set ok to not not 1 equals 2

create function called hello using string name, number age {
    call talk with "Hello " + name + "! You are " + age + " years old!"
}

if i equals 50 {
    call hello with "Eddie", 17
}

create number called i
repeat 5 times {
    set i to i + 1
    call talk with "Test ", i
}

create number called counter
while not counter equals 50 {
    set counter to counter + 1
    call talk with "Counting..."
}

create number called x
create number called y

set x to 10
set y to 20
        """);

        Environment global = new Environment();

        global.SetVariable("talk", new RuntimeNativeFunction(args -> {
            return null;
        }));

        final int ALIGN = 20;
        for (Token token : tokens) {
            String raw = token.raw == null ? "" : token.raw;
            System.out.println("\u001B[30;40;1m " + raw.replace('\n', ' ') + " ".repeat(Math.max(0, ALIGN - raw.length())) + "\u001B[30;43;1m " + token.type + " \u001B[0m");
        }

        Parser parser = new Parser();
        Body program = parser.Parse(tokens);

        Interpreter interpreter = new Interpreter();
        interpreter.Evaluate(program);
    }
}