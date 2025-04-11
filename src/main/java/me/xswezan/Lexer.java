package me.xswezan;

import java.util.Vector;

public class Lexer {
    enum TokenType {
        IDENTIFIER,
        NUMBER,
        STRING,

        COMMA, // ,

        OPEN_PAREN, // (
        CLOSE_PAREN, // )

        OPEN_SCOPE, // {
        CLOSE_SCOPE, // }

        // String concatenation
        KEYWORD_ADD, //-! Maybe change to PLUS (same as + operator)

        // Loops
        KEYWORD_WHILE,
        KEYWORD_REPEAT,
        KEYWORD_TIMES,

        // Function
        KEYWORD_CALL,
        KEYWORD_WITH,
        KEYWORD_RETURN,

        // Variable
        KEYWORD_TO,
        KEYWORD_SET,
        KEYWORD_USING,
        KEYWORD_CREATE,
        KEYWORD_CALLED,
        KEYWORD_INCREASE,
        KEYWORD_DECREASE,

        // Binary
        KEYWORD_IF,
        KEYWORD_NOT,
        KEYWORD_AND,
        KEYWORD_OR,
        KEYWORD_OPERATOR_EQUALS,

        OPERATOR_ADD,
        OPERATOR_SUBTRACT,
        OPERATOR_DIVIDE,
        OPERATOR_MULTIPLY,
        OPERATOR_MODULUS,

        NEW_LINE,
        END_OF_FILE,
    }

    public static class Token {
        String raw;
        TokenType type;
    }

    public static Vector<Token> Lexilize(String code) {
        Vector<Token> tokens = new Vector<Token>();

        int numChars = code.length();
        for (int i = 0; i < numChars; ++i) {
            char c = code.charAt(i);
            String s = Character.toString(c);

            if (c == '(') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.OPEN_PAREN;
                tokens.add(token);
            } else if (c == ')') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.CLOSE_PAREN;
                tokens.add(token);
            } else if (c == '{') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.OPEN_SCOPE;
                tokens.add(token);
            } else if (c == '}') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.CLOSE_SCOPE;
                tokens.add(token);
            } else if (c == ',') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.COMMA;
                tokens.add(token);
            } else if (c == '+') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.OPERATOR_ADD;
                tokens.add(token);
            } else if (c == '-') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.OPERATOR_SUBTRACT;
                tokens.add(token);
            } else if (c == '*') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.OPERATOR_MULTIPLY;
                tokens.add(token);
            } else if (c == '%') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.OPERATOR_MULTIPLY;
                tokens.add(token);
            } else if (c == '/') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.OPERATOR_DIVIDE;
                tokens.add(token);
            } else if (c == '\n') {
                Token token = new Token();
                token.raw = s;
                token.type = TokenType.NEW_LINE;
                tokens.add(token);
            } else if (c == '"') {
                String result = "";
                boolean closed = false;

                while (i < numChars) {
                    char thisChar = code.charAt(i + 1);
                    if (thisChar == '\n') break;
                    if (thisChar == '"') {
                        closed = true;
                        ++i;
                        break;
                    }

                    result += thisChar;
                    ++i;
                }

                if (!closed) {
                    throw new RuntimeException("Unclosed string!");
                }

                Token token = new Token();
                token.raw = result;
                token.type = TokenType.STRING;
                tokens.add(token);
            } else if (Character.isLetter(c)) {
                String result = Character.toString(c);

                while (i < numChars) {
                    char thisChar = code.charAt(i + 1);
                    if (!Character.isLetterOrDigit(thisChar)) break;

                    result += thisChar;
                    ++i;
                }

                Token token = new Token();
                token.raw = result;
                token.type = TokenType.IDENTIFIER;
                processIdentifier(token);
                tokens.add(token);
            } else if (Character.isDigit(c)) {
                String result = Character.toString(c);
                boolean hasDecimal = false;

                while (i < numChars) {
                    char thisChar = code.charAt(i + 1);
                    if (thisChar == '.') {
                        if (hasDecimal) {
                            throw new RuntimeException("Malformed number! (Two decimal signs found)");
                        }
                        hasDecimal = true;
                    } else if (!Character.isDigit(thisChar)) {
                        break;
                    }

                    result += thisChar;
                    ++i;
                }

                Token token = new Token();
                token.raw = result;
                token.type = TokenType.NUMBER;
                tokens.add(token);
            }
        }

        { // New line
            Token token = new Token();
            token.raw = "\n";
            token.type = TokenType.NEW_LINE;
            tokens.add(token);
        }

        { // End of file
            Token token = new Token();
            token.raw = "\u001a";
            token.type = TokenType.END_OF_FILE;
            tokens.add(token);
        }

        return tokens;
    }

    private static void processIdentifier(Token token) {
        switch (token.raw) {
            case "while": token.type = TokenType.KEYWORD_WHILE; break;
            case "repeat": token.type = TokenType.KEYWORD_REPEAT; break;
            case "times": token.type = TokenType.KEYWORD_TIMES; break;

            case "call": token.type = TokenType.KEYWORD_CALL; break;
            case "with": token.type = TokenType.KEYWORD_WITH; break;
            case "return": token.type = TokenType.KEYWORD_RETURN; break;

            case "to": token.type = TokenType.KEYWORD_TO; break;
            case "set": token.type = TokenType.KEYWORD_SET; break;
            case "using": token.type = TokenType.KEYWORD_USING; break;
            case "create": token.type = TokenType.KEYWORD_CREATE; break;
            case "called": token.type = TokenType.KEYWORD_CALLED; break;
            case "increase": token.type = TokenType.KEYWORD_INCREASE; break;
            case "decrease": token.type = TokenType.KEYWORD_DECREASE; break;

            case "if": token.type = TokenType.KEYWORD_IF; break;
            case "not": token.type = TokenType.KEYWORD_NOT; break;
            case "and": token.type = TokenType.KEYWORD_AND; break;
            case "or": token.type = TokenType.KEYWORD_OR; break;
            case "equals": token.type = TokenType.KEYWORD_OPERATOR_EQUALS; break;
        }
    }
}

