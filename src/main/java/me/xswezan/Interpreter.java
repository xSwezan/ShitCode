package me.xswezan;

import me.xswezan.Parser.Body;
import me.xswezan.Parser.Statement;
import me.xswezan.Parser.VariableAssignmentStatement;
import me.xswezan.Parser.VariableDeclarationStatement;

public class Interpreter {
    public void Evaluate(Body program, Environment globalEnvironment) {
        for (Statement statement : program.statements) {
            EvaluateStatement(statement);
        }
    }

    public void EvaluateStatement(Statement statement) {
        if (statement instanceof VariableDeclarationStatement) {

        } else if (statement instanceof VariableAssignmentStatement) {

        }
    }
}
