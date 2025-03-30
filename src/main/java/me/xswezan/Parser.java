package me.xswezan;

import java.util.Vector;

import me.xswezan.Lexer.Token;
import me.xswezan.Lexer.TokenType;

public class Parser {
    /*-----------*\
    |  Statement  |
    \*-----------*/

    enum StatementKind {
        VARIABLE_DECLARATION,
        IF_STATEMENT,
    }

    static class Statement {

    }

    /*------------*\
    |  Expression  |
    \*------------*/

    enum ExpressionKind {
        BINARY_EXPRESSION,
        CALL_EXPRESSION,
    }

    static class Expression {

    }

    /*--------*\
    |  Parser  |
    \*--------*/

    Vector<Token> tokens = new Vector<Token>();

    public void Parse(Vector<Token> tokenList) {
        tokens = tokenList;

        while (!atIs(TokenType.END_OF_FILE)) {

        }
    }

    /*---------*\
    |  Private  |
    \*---------*/

    private Token expect(TokenType type, String message) {
        Token atToken = eat();
        if (atToken == null) throw new RuntimeException(message + " [No token found!]");
        if (atToken.type != type) throw new RuntimeException(message + " [Expected " + type + " but found " + atToken.type + "!]");

        return atToken;
    }

    private boolean atIs(TokenType type) {
        return at().type == type;
    }

    private Token at() {
        return tokens.firstElement();
    }

    private Token eat() {
        return tokens.removeFirst();
    }

    /*---------*\
    |  Parsing  |
    \*---------*/

    private void parseStatement() {
        if (atIs(TokenType.KEYWORD_CREATE)) {
            parseVariableDeclaration();
        } else if (atIs(TokenType.KEYWORD_REPEAT)) {
            parseRepeatStatement();
        } else if (atIs(TokenType.KEYWORD_WHILE)) {
            parseWhileStatement();
        } else if (atIs(TokenType.KEYWORD_CALL)) {
            parseCallStatement();
        } else if (atIs(TokenType.KEYWORD_IF)) {
            parseIfStatement();
        } else {
            parseExpression();
        }
    }

    private void parseVariableDeclaration() {
        eat(); // Eat create keyword

        Token variableType = expect(TokenType.IDENTIFIER, "Expected type identifier following 'create' keyword!");
        expect(TokenType.KEYWORD_CALLED, "Expected 'called' keyword following type identifier in variable declaration!");
    }

    private void parseRepeatStatement() {
        eat(); // Eat repeat keyword
    }

    private void parseWhileStatement() {
        eat(); // Eat while keyword
    }

    private void parseCallStatement() {
        eat(); // Eat call keyword
    }

    private void parseIfStatement() {
        eat(); // Eat if keyword
    }


    private void parseExpression() {

    }
}
