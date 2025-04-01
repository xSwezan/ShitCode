package me.xswezan;

import java.util.Vector;

import me.xswezan.Lexer.Token;
import me.xswezan.Lexer.TokenType;

public class Parser {
    /*-----------*\
    |  Statement  |
    \*-----------*/

    enum StatementKind {
        FUNCTION_DECLARATION,
        VARIABLE_DECLARATION,
        VARIABLE_ASSIGNMENT,
        REPEAT_STATEMENT,
        WHILE_STATEMENT,
        SCOPE_STATEMENT,
        CALL_STATEMENT,
        IF_STATEMENT,
    }

    static class Statement {
        StatementKind kind;
    }

    static class Body {
        Vector<Statement> statements = new Vector<Statement>();
    }

    static class VariableDeclarationStatement extends Statement {
        String type;
        String name;
    }

    static class FunctionDeclarationArgument {
        String type;
        String name;
    }
    static class FunctionDeclarationStatement extends VariableDeclarationStatement {
        String name;
        Body body;
        Vector<FunctionDeclarationArgument> arguments = new Vector<FunctionDeclarationArgument>();
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

    static class CallStatement extends Statement {
        String functionName;
        Vector<Expression> arguments = new Vector<Expression>();
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

    enum ExpressionKind {
        BINARY_EXPRESSION,
        CALL_EXPRESSION,
    }

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

    static class Expression {

    }

    static class BinaryExpression extends Expression {
        Expression left;
        Expression right;
        OperatorType operator;
    }

    static class UnaryExpression extends Expression {
        Expression expression;
        OperatorType operator;
    }

    static class NumericLiteral extends Expression {
        double value;
    }

    static class StringLiteral extends Expression {
        String value;
    }

    static class Identifier extends Expression {
        String symbol;
    }

    static class ReturnExpression extends Expression {
        Expression content;
    }

    /*--------*\
    |  Parser  |
    \*--------*/

    Vector<Token> tokens = new Vector<Token>();

    public void Parse(Vector<Token> tokenList) {
        tokens = tokenList;

        Body program = new Body();

        while (!atIs(TokenType.END_OF_FILE)) {
            Statement statement = parseStatement();
            if (statement == null) continue;

            program.statements.add(statement);
        }

        for (Statement statement : program.statements) {
            if (statement instanceof FunctionDeclarationStatement stmt) { System.out.println("FUNDECL: " + stmt.name + "(" + stmt.arguments + ")"); }
            else if (statement instanceof VariableDeclarationStatement stmt) { System.out.println("VARDECL: " + stmt.name + ": " + stmt.type); }
            else if (statement instanceof VariableAssignmentStatement stmt) { System.out.println("VARASGN: " + stmt.name + ": " + stmt.value); }
            else if (statement instanceof RepeatStatement stmt) { System.out.println("REPSTMT: " + stmt.repeatTimes); }
            else if (statement instanceof WhileStatement stmt) { System.out.println("WHLSTMT: " + stmt.condition); }
            else if (statement instanceof CallStatement stmt) { System.out.println("CALSTMT: " + stmt.functionName + "(" + stmt.arguments + ")"); }
            else if (statement instanceof IfStatement stmt) { System.out.println("IFSTMT: " + stmt.condition); }
            else if (statement instanceof ScopeStatement stmt) { System.out.println("SCPSTMT: " + stmt.body); }
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

    private Statement parseStatement() {
        switch (at().type) {
            case TokenType.KEYWORD_SET: return parseVariableAssignment();
            case TokenType.KEYWORD_CREATE: return parseVariableDeclaration();
            case TokenType.KEYWORD_REPEAT: return parseRepeatStatement();
            case TokenType.KEYWORD_WHILE: return parseWhileStatement();
            case TokenType.KEYWORD_CALL: return parseCallStatement();
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
            statement.kind = StatementKind.FUNCTION_DECLARATION;
            statement.name = name.raw;
            statement.type = variableType.raw;

            if (atIs(TokenType.KEYWORD_USING)) {
                eat(); // Eat using keyword

                { // Add first argument
                    Token argumentType = expect(TokenType.IDENTIFIER, "Expected type identifier for function argument!");
                    Token argumentName = expect(TokenType.IDENTIFIER, "Expected argument name identifier following type identifier in function declaration!");

                    FunctionDeclarationArgument argument = new FunctionDeclarationArgument();
                    argument.type = argumentType.raw;
                    argument.name = argumentName.raw;
                    statement.arguments.add(argument);
                }

                while (atIs(TokenType.COMMA)) {
                    eat(); // Eat comma

                    Token argumentType = expect(TokenType.IDENTIFIER, "Expected type identifier for function argument!");
                    Token argumentName = expect(TokenType.IDENTIFIER, "Expected argument name identifier following type identifier in function declaration!");

                    FunctionDeclarationArgument argument = new FunctionDeclarationArgument();
                    argument.type = argumentType.raw;
                    argument.name = argumentName.raw;
                    statement.arguments.add(argument);
                }
            }

            statement.body = parseBody();


            return statement;
        }

        VariableDeclarationStatement statement = new VariableDeclarationStatement();
        statement.kind = StatementKind.VARIABLE_DECLARATION;
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
        statement.kind = StatementKind.VARIABLE_ASSIGNMENT;
        statement.name = variableName.raw;
        statement.value = value;

        return statement;
    }

    private RepeatStatement parseRepeatStatement() {
        eat(); // Eat repeat keyword

        Expression repeatTimes = parseExpression();
        expect(TokenType.KEYWORD_TIMES, "Expected 'times' keyword following repeat number expression!");

        RepeatStatement statement = new RepeatStatement();
        statement.kind = StatementKind.REPEAT_STATEMENT;
        statement.repeatTimes = repeatTimes;
        statement.body = parseBody();

        return statement;
    }

    private WhileStatement parseWhileStatement() {
        eat(); // Eat while keyword

        Expression condition = parseExpression();

        WhileStatement statement = new WhileStatement();
        statement.kind = StatementKind.WHILE_STATEMENT;
        statement.condition = condition;
        statement.body = parseBody();

        return null;
    }

    private CallStatement parseCallStatement() {
        eat(); // Eat call keyword

        Token token = expect(TokenType.IDENTIFIER, "Expected function name identifier following 'call' keyword!");

        CallStatement statement = new CallStatement();
        statement.kind = StatementKind.CALL_STATEMENT;
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
        statement.kind = StatementKind.IF_STATEMENT;
        statement.condition = parseExpression();
        statement.body = parseBody();

        return statement;
    }

    private ScopeStatement parseScopeStatement() {
        ScopeStatement statement = new ScopeStatement();
        statement.kind = StatementKind.SCOPE_STATEMENT;
        statement.body = parseBody();

        return statement;
    }

    private Body parseBody() {
        Body body = new Body();

        expect(TokenType.OPEN_SCOPE, "Expected '{'!");

        while (!atIs(TokenType.END_OF_FILE) && !atIs(TokenType.CLOSE_SCOPE)) {
            body.statements.add(parseStatement());
        }

        expect(TokenType.CLOSE_SCOPE, "Expected '}'!");

        return body;
    }

    private Expression parseExpression() {
        return parseBooleanExpression();
    }

    private Expression parseUnaryExpression() {
        if (atIs(TokenType.KEYWORD_NOT)) {

        }

        return parseAdditiveExpression();
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
                Identifier identifier = new Identifier();
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
