package me.xswezan;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Lexer.Lexilize("""
                create number called i
                set i to 50
        """);
    }
}