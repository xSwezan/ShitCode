package me.xswezan;

import java.util.ArrayList;
import java.util.Vector;
import java.util.function.BinaryOperator;

import me.xswezan.Environment.RuntimeBoolean;
import me.xswezan.Environment.RuntimeFunction;
import me.xswezan.Environment.RuntimeNativeFunction;
import me.xswezan.Environment.RuntimeNothing;
import me.xswezan.Environment.RuntimeNumber;
import me.xswezan.Environment.RuntimeString;
import me.xswezan.Environment.RuntimeValue;
import me.xswezan.Parser.BinaryExpression;
import me.xswezan.Parser.Body;
import me.xswezan.Parser.BooleanLiteral;
import me.xswezan.Parser.CallExpression;
import me.xswezan.Parser.Expression;
import me.xswezan.Parser.FunctionDeclarationParameter;
import me.xswezan.Parser.FunctionDeclarationStatement;
import me.xswezan.Parser.IdentifierExpression;
import me.xswezan.Parser.IfStatement;
import me.xswezan.Parser.Node;
import me.xswezan.Parser.NumericLiteral;
import me.xswezan.Parser.BinaryOperatorType;
import me.xswezan.Parser.RepeatStatement;
import me.xswezan.Parser.ScopeStatement;
import me.xswezan.Parser.Statement;
import me.xswezan.Parser.StringLiteral;
import me.xswezan.Parser.UnaryExpression;
import me.xswezan.Parser.UnaryOperatorType;
import me.xswezan.Parser.VariableAssignmentStatement;
import me.xswezan.Parser.VariableDeclarationStatement;
import me.xswezan.Parser.WhileStatement;

public class Interpreter {
    public static void EvaluateBody(Body program, Environment environment) {
        for (Node node : program.nodes) {
            if (node instanceof Statement statement) {
                EvaluateStatement(statement, environment);
            } else if (node instanceof Expression expression) {
                EvaluateExpression(expression, environment);
            }
        }
    }

    /*------------*\
    |  Statements  |
    \*------------*/

    public static void EvaluateStatement(Statement statement, Environment environment) {
        if (statement instanceof FunctionDeclarationStatement stmt) { EvaluateFunctionDeclarationStatement(stmt, environment); }
        else if (statement instanceof VariableDeclarationStatement stmt) { EvaluateVariableDeclarationStatement(stmt, environment); }
        else if (statement instanceof VariableAssignmentStatement stmt) { EvaluateVariableAssignmentStatement(stmt, environment); }
        else if (statement instanceof RepeatStatement stmt) { EvaluateRepeatStatement(stmt, environment); }
        else if (statement instanceof WhileStatement stmt) { EvaluateWhileStatement(stmt, environment); }
        else if (statement instanceof IfStatement stmt) { EvaluateIfStatement(stmt, environment); }
        else if (statement instanceof ScopeStatement stmt) { EvaluateScopeStatement(stmt, environment); }
        else { throw new RuntimeException("Statement " + statement + " isn't implemented for evaluation!"); }
    }

    public static void EvaluateVariableDeclarationStatement(VariableDeclarationStatement statement, Environment environment) {
        assert environment.HasVariable(statement.name) : "Cannot create two variables of the same name!";

        RuntimeValue value;
        switch (statement.type) {
            case "string": value = new RuntimeString(""); break;
            case "number": value = new RuntimeNumber(0.0); break;
            case "boolean": value = new RuntimeBoolean(true); break;
            case "function": value = new RuntimeNothing(); break;
            default: throw new RuntimeException("You idiot, you cannot create a variable of type '" + statement.type + "'!");
        }

        environment.SetVariable(statement.name, value);
    }

    public static void EvaluateFunctionDeclarationStatement(FunctionDeclarationStatement statement, Environment environment) {
        RuntimeFunction function = new RuntimeFunction();
        function.declarationEnvironment = environment;
        function.parameters = statement.parameters;
        function.body = statement.body;

        if (!statement.name.isEmpty()) {
            environment.SetVariable(statement.name, function);
        }
    }

    public static void EvaluateVariableAssignmentStatement(VariableAssignmentStatement statement, Environment environment) {
        //-! MAKE THIS SHIT TYPE CHECKED
        //-! MAKE THIS SHIT TYPE CHECKED
        //-! MAKE THIS SHIT TYPE CHECKED
        //-! MAKE THIS SHIT TYPE CHECKED
        //-! MAKE THIS SHIT TYPE CHECKED
        if (!environment.HasVariable(statement.name)) {
            throw new RuntimeException("No variable named '" + statement.name +"' found in scope!");
        }

        Environment container = environment.GetVariableContainer(statement.name);

        RuntimeValue value = EvaluateExpression(statement.value, environment);
        container.SetVariable(statement.name, value);
    }

    public static void EvaluateRepeatStatement(RepeatStatement statement, Environment environment) {
        RuntimeValue rawRepeatTimes = EvaluateExpression(statement.repeatTimes, environment);
        if (rawRepeatTimes instanceof RuntimeNumber repeatTimes) {
            for (int i = 0; i < (int)repeatTimes.value; ++i) {
                Environment scope = new Environment();
                scope.parent = environment;

                EvaluateBody(statement.body, scope);
            }
        } else {
            throw new RuntimeException("Repeat times has to be of type number!");
        }
    }

    public static void EvaluateWhileStatement(WhileStatement statement, Environment environment) {
        while (true) {
            RuntimeValue condition = EvaluateExpression(statement.condition, environment);
            if (condition instanceof RuntimeBoolean bool) {
                if (bool.value == false) break;
            } else {
                throw new RuntimeException("Expected while condition expression to be a boolean!");
            }

            Environment scope = new Environment();
            scope.parent = environment;

            EvaluateBody(statement.body, scope);
        }
    }

    public static void EvaluateIfStatement(IfStatement statement, Environment environment) {
        RuntimeValue condition = EvaluateExpression(statement.condition, environment);
        assert condition instanceof RuntimeBoolean : "Expected while condition expression to be a boolean!";

        Environment scope = new Environment();
        scope.parent = environment;

        EvaluateBody(statement.body, scope);
    }

    public static void EvaluateScopeStatement(ScopeStatement statement, Environment environment) {
        Environment scope = new Environment();
        scope.parent = environment;

        EvaluateBody(statement.body, scope);
    }

    /*-------------*\
    |  Expressions  |
    \*-------------*/

    public static RuntimeValue EvaluateExpression(Expression expression, Environment environment) {
        if (expression instanceof IdentifierExpression identifier) { return EvaluateIdentifierExpression(identifier, environment); }
        else if (expression instanceof CallExpression call) { return EvaluateCallExpression(call, environment); }
        else if (expression instanceof UnaryExpression unary) { return EvaluateUnaryExpression(unary, environment); }
        else if (expression instanceof BinaryExpression binary) { return EvaluateBinaryExpression(binary, environment); }
        else if (expression instanceof NumericLiteral literal) { return new RuntimeNumber(literal.value); }
        else if (expression instanceof StringLiteral literal) { return new RuntimeString(literal.value); }
        else if (expression instanceof BooleanLiteral literal) { return new RuntimeBoolean(literal.value); }

        throw new RuntimeException("Couldn't evaluate expression: " + expression + "!");
    }

    public static RuntimeValue EvaluateIdentifierExpression(IdentifierExpression expression, Environment environment) {
        return environment.GetVariable(expression.symbol);
    }

    public static RuntimeValue EvaluateUnaryExpression(UnaryExpression expression, Environment environment) {
        RuntimeValue value = EvaluateExpression(expression.expression, environment);

        if (value instanceof RuntimeNothing) {
            throw new RuntimeException("Failed to perform unary expression on value of nothing!");
        }

        switch (expression.operator) {
            case UnaryOperatorType.NOT: {
                if (value instanceof RuntimeBoolean bool) {
                    return new RuntimeBoolean(!bool.value);
                }
            }
        }

        throw new RuntimeException("Couldn't evaluate " + expression.operator + " unary expression on value: " + value + "!");
    }

    public static RuntimeValue EvaluateBinaryExpression(BinaryExpression expression, Environment environment) {
        RuntimeValue left = EvaluateExpression(expression.left, environment);
        RuntimeValue right = EvaluateExpression(expression.right, environment);

        if (left instanceof RuntimeNothing || right instanceof RuntimeNothing) {
            throw new RuntimeException("Failed to perform binary expression on value of nothing!");
        }

        switch (expression.operator) {
            case BinaryOperatorType.ADD: {
                //> number + number
                if (left instanceof RuntimeNumber leftNumber && right instanceof RuntimeNumber rightNumber) {
                    return new RuntimeNumber(leftNumber.value + rightNumber.value);
                }

                //> String concatenation
                if (left instanceof RuntimeString leftString) {
                    return new RuntimeString(leftString.value + right.toString());
                }
            }
            case BinaryOperatorType.SUBTRACT: {
                //> number - number
                if (left instanceof RuntimeNumber leftNumber && right instanceof RuntimeNumber rightNumber) {
                    return new RuntimeNumber(leftNumber.value - rightNumber.value);
                }
            }
            case BinaryOperatorType.MULTIPLY: {
                //> number * number
                if (left instanceof RuntimeNumber leftNumber && right instanceof RuntimeNumber rightNumber) {
                    return new RuntimeNumber(leftNumber.value * rightNumber.value);
                }
            }
            case BinaryOperatorType.DIVIDE: {
                //> number / number
                if (left instanceof RuntimeNumber leftNumber && right instanceof RuntimeNumber rightNumber) {
                    return new RuntimeNumber(leftNumber.value / rightNumber.value);
                }
            }
            case BinaryOperatorType.MODULUS: {
                //> number % number
                if (left instanceof RuntimeNumber leftNumber && right instanceof RuntimeNumber rightNumber) {
                    return new RuntimeNumber(leftNumber.value % rightNumber.value);
                }
            }
            case BinaryOperatorType.BOOLEAN_EQUALS: {
                if (left == null || right == null) {
                    return new RuntimeBoolean(left == right);
                }

                if (left.getClass() != right.getClass()) {
                    return new RuntimeBoolean(false);
                }

                //> number == number
                if (left instanceof RuntimeNumber leftNumber && right instanceof RuntimeNumber rightNumber) {
                    return new RuntimeBoolean(leftNumber.value == rightNumber.value);
                }

                //> string == string
                if (left instanceof RuntimeString leftString && right instanceof RuntimeString rightString) {
                    return new RuntimeBoolean(leftString.value.equals(rightString.value));
                }
            }
            case BinaryOperatorType.BOOLEAN_AND: {
                //> bool && bool
                if (left instanceof RuntimeBoolean leftBoolean && right instanceof RuntimeBoolean rightBoolean) {
                    return new RuntimeBoolean(leftBoolean.value && rightBoolean.value);
                }
            }
            case BinaryOperatorType.BOOLEAN_OR: {
                //> bool || bool
                if (left instanceof RuntimeBoolean leftBoolean && right instanceof RuntimeBoolean rightBoolean) {
                    return new RuntimeBoolean(leftBoolean.value || rightBoolean.value);
                }
            }
        }

        throw new RuntimeException("Couldn't evaluate " + expression.operator + " binary expression on values: " + left + " and " + right + "!");
    }

    public static RuntimeValue EvaluateCallExpression(CallExpression expression, Environment environment) {
        ArrayList<RuntimeValue> arguments = new ArrayList<RuntimeValue>();
        for (Expression argument : expression.arguments) {
            arguments.add(EvaluateExpression(argument, environment));
        }

        RuntimeValue function = environment.GetVariable(expression.functionName);
        assert function != null : "No function with the name '" + expression.functionName + "' was found!";
        assert function instanceof RuntimeFunction || function instanceof RuntimeNativeFunction : "Can't call a non-function variable!";

        if (function instanceof RuntimeNativeFunction nativeFunction) {
            RuntimeValue[] args = new RuntimeValue[arguments.size()];
            arguments.toArray(args);
            return nativeFunction.call(args);
        } else if (function instanceof RuntimeFunction func) {
            Environment scope = new Environment();
            scope.parent = environment;

            for (int i = 0; i < arguments.size(); ++i) {
                FunctionDeclarationParameter param = func.parameters.get(i);
                RuntimeValue value = arguments.get(i);

                scope.SetVariable(param.name, value);
            }

            EvaluateBody(func.body, scope);
        }

        return null;
    }
}
