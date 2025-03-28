package me.xswezan;

public class Lexer {
    enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,
        COLON,
    }

    public class Token {
        String raw;
        TokenType type;
    }

    public static void Lexilize(String code) {
        for (int i = 0; i < code.length(); ++i) {
            char c = code.charAt(i);
            System.out.println(c);
        }
    }
}

