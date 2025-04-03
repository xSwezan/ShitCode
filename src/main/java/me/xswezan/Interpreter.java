package me.xswezan;

import java.util.Vector;

import me.xswezan.Environment.RuntimeFunction;
import me.xswezan.Environment.RuntimeNothing;
import me.xswezan.Environment.RuntimeValue;
import me.xswezan.Parser.Body;
import me.xswezan.Parser.CallExpression;
import me.xswezan.Parser.Expression;
import me.xswezan.Parser.FunctionDeclarationParameter;
import me.xswezan.Parser.FunctionDeclarationStatement;
import me.xswezan.Parser.IdentifierExpression;
import me.xswezan.Parser.IfStatement;
import me.xswezan.Parser.Node;
import me.xswezan.Parser.RepeatStatement;
import me.xswezan.Parser.ScopeStatement;
import me.xswezan.Parser.Statement;
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
        if (statement instanceof FunctionDeclarationStatement stmt) {
            EvaluateFunctionDeclarationStatement(stmt, environment);
        } else if (statement instanceof VariableDeclarationStatement stmt) {
            EvaluateVariableDeclarationStatement(stmt, environment);
        } else if (statement instanceof VariableAssignmentStatement stmt) {
            EvaluateVariableAssignmentStatement(stmt, environment);
        } else if (statement instanceof RepeatStatement stmt) {
            EvaluateRepeatStatement(stmt, environment);
        } else if (statement instanceof WhileStatement stmt) {
            EvaluateWhileStatement(stmt, environment);
        } else if (statement instanceof IfStatement stmt) {
            EvaluateIfStatement(stmt, environment);
        } else if (statement instanceof ScopeStatement stmt) {
            EvaluateScopeStatement(stmt, environment);
        } else {
            throw new RuntimeException("Statement " + statement + " isn't implemented for evaluation!");
        }
    }

    public static void EvaluateVariableDeclarationStatement(VariableDeclarationStatement statement, Environment environment) {
        assert environment.HasVariable(statement.name) : "Cannot create two variables of the same name!";
        environment.SetVariable(statement.name, new RuntimeNothing());
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
        assert environment.HasVariable(statement.name) : "No variable named '" + statement.name +"' found in scope!";

        RuntimeValue value = EvaluateExpression(statement.value, environment);
        environment.SetVariable(statement.name, value);
    }

    public static void EvaluateRepeatStatement(RepeatStatement statement, Environment environment) {

    }

    public static void EvaluateWhileStatement(WhileStatement statement, Environment environment) {

    }

    public static void EvaluateIfStatement(IfStatement statement, Environment environment) {

    }

    public static void EvaluateScopeStatement(ScopeStatement statement, Environment environment) {

    }

    /*-------------*\
    |  Expressions  |
    \*-------------*/

    public static RuntimeValue EvaluateExpression(Expression expression, Environment environment) {
        if (expression instanceof IdentifierExpression identifier) {
            EvaluateIdentifierExpression(identifier, environment);
        } else if (expression instanceof CallExpression call) {
            EvaluateCallExpression(call, environment);
        }

        return null;
    }

    public static RuntimeValue EvaluateIdentifierExpression(IdentifierExpression expression, Environment environment) {
        return environment.GetVariable(expression.symbol);
    }

    public static RuntimeValue EvaluateCallExpression(CallExpression expression, Environment environment) {
        Vector<RuntimeValue> arguments = new Vector<RuntimeValue>();
        for (Expression argument : expression.arguments) {
            arguments.add(EvaluateExpression(argument, environment));
        }


    }
}
