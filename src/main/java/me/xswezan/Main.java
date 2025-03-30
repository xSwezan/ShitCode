package me.xswezan;

import java.util.Vector;

import me.xswezan.Lexer.Token;

public class Main {
    public static void main(String[] args) {
        Vector<Token> tokens = Lexer.Lexilize("""
            create number called testNumber
            set testNumber to 50.025

            call talk with create function using name, dog

            create function called greet {
                call talk with "Hello, world!"
            }
        """);

        final int ALIGN = 20;
        for (Token token : tokens) {
            System.out.println("\u001B[30;40;1m " + token.raw.replace('\n', ' ') + " ".repeat(Math.max(0, ALIGN - token.raw.length())) + "\u001B[30;43;1m " + token.type + " \u001B[0m");
        }
    }
}