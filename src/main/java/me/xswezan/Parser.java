package me.xswezan;

import java.util.Vector;

import me.xswezan.Lexer.Token;
import me.xswezan.Lexer.TokenType;

public class Parser {
    static class Node {}

    /*-----------*\
    |  Statement  |
    \*-----------*/

    static class Statement extends Node {}

    static class Body {
        Vector<Node> nodes = new Vector<Node>();
    }

    static class VariableDeclarationStatement extends Statement {
        String type;
        String name;
    }

    static class FunctionDeclarationParameter {
        String type;
        String name;

        public String toString() { return name + ": " + type; }
    }
    static class FunctionDeclarationStatement extends VariableDeclarationStatement {
        String name;
        Body body;
        Vector<FunctionDeclarationParameter> parameters = new Vector<FunctionDeclarationParameter>();
    }

    static class VariableAssignmentStatement extends Statement {
        String name;
        Expression value;
    }

    static class RepeatStatement extends Statement {
        Expression repeatTimes;
        Body body;
    }

    static class WhileStatement extends Statement {
        Expression condition;
        Body body;
    }

    static class IfStatement extends Statement {
        Expression condition;
        Body body;
    }

    static class ScopeStatement extends Statement {
        Body body;
    }

    /*------------*\
    |  Expression  |
    \*------------*/

    enum OperatorType {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        MODULUS,
        EQUALS,
    }

    enum UnaryOperatorType {
        NOT,
    }

    static class Expression extends Node {}

    static class BinaryExpression extends Expression {
        Expression left;
        Expression right;
        OperatorType operator;

        public String toString() { return "BINEXPR(" + left + ", \033[31m" + operator + "\033[0m, " + right + ")"; }
    }

    static class UnaryExpression extends Expression {
        Expression expression;
        UnaryOperatorType operator;

        public String toString() { return "UNARYEXPR(\033[31m" + operator + "\033[0m, " + expression + ")"; }
    }

    static class NumericLiteral extends Expression {
        double value;

        public String toString() { return "NUMLTRL(\033[36m" + value + "\033[0m)"; }
    }

    static class StringLiteral extends Expression {
        String value;

        public String toString() { return "STRLTRL(\033[32m" + value + "\033[0m)"; }
    }

    static class IdentifierExpression extends Expression {
        String symbol;

        public String toString() { return "IDENTIF(" + symbol + ")"; }
    }

    static class CallExpression extends Expression {
        String functionName;
        Vector<Expression> arguments = new Vector<Expression>();
    }

    static class ReturnExpression extends Expression {
        Expression content;

        public String toString() { return "RETEXPR(" + content + ")"; }
    }

    /*--------*\
    |  Parser  |
    \*--------*/

    Vector<Token> tokens = new Vector<Token>();

    public Body Parse(Vector<Token> tokenList) {
        tokens = tokenList;

        Body program = new Body();

        while (!atIs(TokenType.END_OF_FILE)) {
            Statement statement = parseStatement();
            if (statement == null) continue;

            program.nodes.add(statement);
        }

        return program;
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

    private Statement parseStatement() {
        switch (at().type) {
            case TokenType.KEYWORD_SET: return parseVariableAssignment();
            case TokenType.KEYWORD_CREATE: return parseVariableDeclaration();
            case TokenType.KEYWORD_REPEAT: return parseRepeatStatement();
            case TokenType.KEYWORD_WHILE: return parseWhileStatement();
            case TokenType.KEYWORD_IF: return parseIfStatement();
            case TokenType.OPEN_SCOPE: return parseScopeStatement();
            case TokenType.NEW_LINE: eat(); break;
            default: throw new RuntimeException("Unexpected token: " + at().raw + " (" + at().type + ")");
        }

        return null;
    }

    private Statement parseVariableDeclaration() {
        eat(); // Eat create keyword

        Token variableType = expect(TokenType.IDENTIFIER, "Expected type identifier following 'create' keyword!");
        expect(TokenType.KEYWORD_CALLED, "Expected 'called' keyword following type identifier in variable declaration!");

        Token name = expect(TokenType.IDENTIFIER, "Expected variable name identifier following 'called' keyword in variable declaration!");

        if (variableType.raw.equals("function")) {
            FunctionDeclarationStatement statement = new FunctionDeclarationStatement();
            statement.name = name.raw;
            statement.type = variableType.raw;

            if (atIs(TokenType.KEYWORD_USING)) {
                eat(); // Eat using keyword

                { // Add first argument
                    Token argumentType = expect(TokenType.IDENTIFIER, "Expected type identifier for function argument!");
                    Token argumentName = expect(TokenType.IDENTIFIER, "Expected argument name identifier following type identifier in function declaration!");

                    FunctionDeclarationParameter argument = new FunctionDeclarationParameter();
                    argument.type = argumentType.raw;
                    argument.name = argumentName.raw;
                    statement.parameters.add(argument);
                }

                while (atIs(TokenType.COMMA)) {
                    eat(); // Eat comma

                    Token argumentType = expect(TokenType.IDENTIFIER, "Expected type identifier for function argument!");
                    Token argumentName = expect(TokenType.IDENTIFIER, "Expected argument name identifier following type identifier in function declaration!");

                    FunctionDeclarationParameter argument = new FunctionDeclarationParameter();
                    argument.type = argumentType.raw;
                    argument.name = argumentName.raw;
                    statement.parameters.add(argument);
                }
            }

            statement.body = parseBody();

            return statement;
        }

        VariableDeclarationStatement statement = new VariableDeclarationStatement();
        statement.name = name.raw;
        statement.type = variableType.raw;

        return statement;
    }

    private VariableAssignmentStatement parseVariableAssignment() {
        eat(); // Eat set keyword

        Token variableName = expect(TokenType.IDENTIFIER, "Expected variable name identifier following 'set' keyword!");
        expect(TokenType.KEYWORD_TO, "Expected 'to' keyword following variable name identifier in variable assignment!");

        Expression value = parseExpression();

        VariableAssignmentStatement statement = new VariableAssignmentStatement();
        statement.name = variableName.raw;
        statement.value = value;

        return statement;
    }

    private RepeatStatement parseRepeatStatement() {
        eat(); // Eat repeat keyword

        Expression repeatTimes = parseExpression();
        expect(TokenType.KEYWORD_TIMES, "Expected 'times' keyword following repeat number expression!");

        RepeatStatement statement = new RepeatStatement();
        statement.repeatTimes = repeatTimes;
        statement.body = parseBody();

        return statement;
    }

    private WhileStatement parseWhileStatement() {
        eat(); // Eat while keyword

        Expression condition = parseExpression();

        WhileStatement statement = new WhileStatement();
        statement.condition = condition;
        statement.body = parseBody();

        return null;
    }

    private CallStatement parseCallStatement() {
        eat(); // Eat call keyword

        Token token = expect(TokenType.IDENTIFIER, "Expected function name identifier following 'call' keyword!");

        CallStatement statement = new CallStatement();
        statement.functionName = token.raw;

        if (atIs(TokenType.KEYWORD_WITH)) {
            eat(); // Eat with keyword

            { // Add first argument
                Expression argument = parseExpression();
                statement.arguments.add(argument);
            }

            while (atIs(TokenType.COMMA)) {
                eat(); // Eat comma

                Expression argument = parseExpression();
                statement.arguments.add(argument);
            }
        }

        return statement;
    }

    private IfStatement parseIfStatement() {
        eat(); // Eat if keyword

        IfStatement statement = new IfStatement();
        statement.condition = parseExpression();
        statement.body = parseBody();

        return statement;
    }

    private ScopeStatement parseScopeStatement() {
        ScopeStatement statement = new ScopeStatement();
        statement.body = parseBody();

        return statement;
    }

    private Body parseBody() {
        Body body = new Body();

        expect(TokenType.OPEN_SCOPE, "Expected '{'!");

        while (!atIs(TokenType.END_OF_FILE) && !atIs(TokenType.CLOSE_SCOPE)) {
            body.nodes.add(parseStatement());
        }

        expect(TokenType.CLOSE_SCOPE, "Expected '}'!");

        return body;
    }

    private Expression parseExpression() {
        return parseUnaryExpression();
    }

    private Expression parseUnaryExpression() {
        if (atIs(TokenType.KEYWORD_NOT)) {
            eat(); // Eat operator

            UnaryExpression expression = new UnaryExpression();
            expression.expression = parseExpression();
            expression.operator = UnaryOperatorType.NOT;

            return expression;
        }

        return parseBooleanExpression();
    }

    // left equals right
    private Expression parseBooleanExpression() {
        Expression left = parseAdditiveExpression();

        while (atIs(TokenType.KEYWORD_OPERATOR_EQUALS)) {
            eat(); // Eat operator

            Expression right = parseAdditiveExpression();

            BinaryExpression expression = new BinaryExpression();
            expression.left = left;
            expression.right = right;
            expression.operator = OperatorType.EQUALS;
            left = expression;
        }

        return left;
    }

    // left [+, -] right
    private Expression parseAdditiveExpression() {
        Expression left = parseMultiplicativeExpression();

        while (atIs(TokenType.OPERATOR_ADD) || atIs(TokenType.OPERATOR_SUBTRACT)) {
            boolean isAdd = atIs(TokenType.OPERATOR_ADD);

            eat(); // Eat operator

            Expression right = parseMultiplicativeExpression();

            BinaryExpression expression = new BinaryExpression();
            expression.left = left;
            expression.right = right;
            expression.operator = isAdd ? OperatorType.ADD : OperatorType.SUBTRACT;
            left = expression;
        }

        return left;
    }

    // left [*, /, %] right
    private Expression parseMultiplicativeExpression() {
        Expression left = parsePrimaryExpression();

        while (atIs(TokenType.OPERATOR_MULTIPLY) || atIs(TokenType.OPERATOR_DIVIDE) || atIs(TokenType.OPERATOR_MODULUS)) {
            boolean isMultiply = atIs(TokenType.OPERATOR_MULTIPLY);
            boolean isDivide = atIs(TokenType.OPERATOR_DIVIDE);

            eat(); // Eat operator

            Expression right = parsePrimaryExpression();

            BinaryExpression expression = new BinaryExpression();
            expression.left = left;
            expression.right = right;
            expression.operator = isMultiply ? OperatorType.MULTIPLY : (isDivide ? OperatorType.DIVIDE : OperatorType.MODULUS);
            left = expression;
        }

        return left;
    }

    private Expression parsePrimaryExpression() {
        switch (at().type) {
            case TokenType.IDENTIFIER: {
                IdentifierExpression identifier = new IdentifierExpression();
                identifier.symbol = eat().raw;
                return identifier;
            }

            case TokenType.KEYWORD_RETURN: {
                eat(); // Eat return
                ReturnExpression expression = new ReturnExpression();
                if (!atIs(TokenType.CLOSE_SCOPE)) {
                    expression.content = parseExpression();
                }
                return expression;
            }

            case TokenType.NUMBER: {
                NumericLiteral value = new NumericLiteral();
                value.value = Double.parseDouble(eat().raw);
                return value;
            }

            case TokenType.STRING: {
                StringLiteral value = new StringLiteral();
                value.value = eat().raw;
                return value;
            }

            case TokenType.OPEN_PAREN: {
                eat(); // Eat open paren
                Expression value = parseExpression();
                expect(TokenType.CLOSE_PAREN, "Expected closing parenthesis following parenthesized expression!");
                return value;
            }
            default: throw new RuntimeException("Unexpected token found during parsing! " + at().type);
        }
    }
}
