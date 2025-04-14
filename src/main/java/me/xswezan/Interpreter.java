package me.xswezan;

import java.util.ArrayList;

import me.xswezan.Environment.RuntimeBoolean;
import me.xswezan.Environment.RuntimeBundle;
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
import me.xswezan.Parser.MemberExpression;
import me.xswezan.Parser.Node;
import me.xswezan.Parser.NumericLiteral;
import me.xswezan.Parser.BinaryOperatorType;
import me.xswezan.Parser.RepeatStatement;
import me.xswezan.Parser.ReturnStatement;
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
            if (node == null) continue;
            if (environment.HasReturned()) break;

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
        else if (statement instanceof ReturnStatement stmt) { EvaluateReturnStatement(stmt, environment); }
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
            case "bundle": value = new RuntimeBundle(); break;
            default: throw new RuntimeException("You idiot, you cannot create a variable of type '" + statement.type + "'!");
        }

        if (statement.inBundle != null) {
            RuntimeValue bundleValue = EvaluateExpression(statement.inBundle, environment);
            if (bundleValue instanceof RuntimeBundle bundle) {
                bundle.environment.SetVariable(statement.name, value);
            } else {
                throw new RuntimeException("Expected valid bundle following 'in' keyword in variable declaration statement! Got " + bundleValue + "!");
            }
        } else {
            environment.SetVariable(statement.name, value);
        }
    }

    public static void EvaluateFunctionDeclarationStatement(FunctionDeclarationStatement statement, Environment environment) {
        RuntimeFunction function = new RuntimeFunction();
        function.parameters = statement.parameters;
        function.declarationEnvironment = environment;
        function.body = statement.body;

        if (!statement.name.isEmpty()) {
            if (statement.inBundle != null) {
                RuntimeValue bundleValue = EvaluateExpression(statement.inBundle, environment);
                if (bundleValue instanceof RuntimeBundle bundle) {
                    bundle.environment.SetVariable(statement.name, function);
                } else {
                    throw new RuntimeException("Expected valid bundle following 'in' keyword in variable declaration statement! Got " + bundleValue + "!");
                }
            } else {
                environment.SetVariable(statement.name, function);
            }
        }
    }

    //-! MAKE THIS SHIT TYPE CHECKED
    //-! MAKE THIS SHIT TYPE CHECKED
    //-! MAKE THIS SHIT TYPE CHECKED
    //-! MAKE THIS SHIT TYPE CHECKED
    //-! MAKE THIS SHIT TYPE CHECKED
    public static void EvaluateVariableAssignmentStatement(VariableAssignmentStatement statement, Environment environment) {
        RuntimeValue value = EvaluateExpression(statement.value, environment);

        if (statement.name instanceof IdentifierExpression name) {
            if (!environment.HasVariable(name.symbol)) throw new RuntimeException("No variable named '" + name.symbol +"' found in scope!");

            Environment container = environment.GetVariableContainer(name.symbol);
            if (container == null) throw new RuntimeException("Couldn't find variable with name '" + name.symbol + "'!");

            container.SetVariable(name.symbol, value);
        } else if (statement.name instanceof MemberExpression expression) {
            RuntimeValue object = EvaluateExpression(expression.object, environment);
            if (object instanceof RuntimeBundle bundle && expression.member instanceof IdentifierExpression member) {
                if (!bundle.environment.HasVariable(member.symbol)) throw new RuntimeException("No variable named '" + member.symbol +"' found in bundle!");

                bundle.environment.SetVariable(member.symbol, value);
            }
        }
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
        if (condition instanceof RuntimeBoolean bool) {
            if (bool.value == false) return;

            Environment scope = new Environment();
            scope.parent = environment;

            EvaluateBody(statement.body, scope);
        } else {
            throw new RuntimeException("Expected while condition expression to be a boolean!");
        }
    }

    public static void EvaluateReturnStatement(ReturnStatement statement, Environment environment) {
        environment.SetReturnValue(EvaluateExpression(statement.content, environment));
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
        else if (expression instanceof MemberExpression member) { return EvaluateMemberExpression(member, environment); }
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

    public static RuntimeValue EvaluateMemberExpression(MemberExpression expression, Environment environment) {
        RuntimeValue object = EvaluateExpression(expression.object, environment);
        if (object instanceof RuntimeBundle bundle) {
            if (expression.member instanceof IdentifierExpression member) {
                return bundle.environment.GetVariable(member.symbol);
            } else {
                throw new RuntimeException("Expected member of member expression to be an identifier! Got " + expression.member + "!");
            }
        } else {
            throw new RuntimeException("Expected object of member expression to be a bundle! Got " + expression.object + "!");
        }
    }

    public static RuntimeValue EvaluateCallExpression(CallExpression expression, Environment environment) {
        ArrayList<RuntimeValue> arguments = new ArrayList<RuntimeValue>();
        for (Expression argument : expression.arguments) {
            arguments.add(EvaluateExpression(argument, environment));
        }

        RuntimeValue function = null;
        if (expression.functionName instanceof IdentifierExpression name) {
            Environment container = environment.GetVariableContainer(name.symbol);
            if (container == null) throw new RuntimeException("Couldn't find function with name '" + name.symbol + "'!");

            function = container.GetVariable(name.symbol);
        } else if (expression.functionName instanceof MemberExpression memberExpression) {
            RuntimeValue object = EvaluateExpression(memberExpression.object, environment);
            if (object instanceof RuntimeBundle bundle && memberExpression.member instanceof IdentifierExpression member) {
                function = bundle.environment.GetVariable(member.symbol);
            }
        }

        if (function == null) throw new RuntimeException("No function with the name '" + expression.functionName + "' was found!");

        if (function instanceof RuntimeNativeFunction nativeFunction) {
            RuntimeValue[] args = new RuntimeValue[arguments.size()];
            arguments.toArray(args);
            return nativeFunction.call(args);
        } else if (function instanceof RuntimeFunction func) {
            Environment scope = new Environment();
            scope.returnable = true;
            scope.parent = environment;

            for (int i = 0; i < arguments.size(); ++i) {
                FunctionDeclarationParameter param = func.parameters.get(i);
                RuntimeValue value = arguments.get(i);

                scope.SetVariable(param.name, value);
            }

            EvaluateBody(func.body, scope);
            return scope.GetReturnValue();
        } else {
            throw new RuntimeException("Can't call a non-function variable!");
        }
    }
}
