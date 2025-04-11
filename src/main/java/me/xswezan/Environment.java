package me.xswezan;

import java.util.HashMap;
import java.util.Vector;

import me.xswezan.Parser.Body;
import me.xswezan.Parser.FunctionDeclarationParameter;

public class Environment {
    /*----------------*\
    |  Runtime Values  |
    \*----------------*/

    public static class RuntimeValue {}

    public static class RuntimeNumber extends RuntimeValue {
        double value;

        RuntimeNumber(double value) {
            this.value = value;
        }

        public String toString() { return Double.toString(value); }
    }

    public static class RuntimeString extends RuntimeValue {
        String value;

        RuntimeString(String value) {
            this.value = value;
        }

        public String toString() { return value; }
    }

    public static class RuntimeBoolean extends RuntimeValue {
        boolean value;

        RuntimeBoolean(boolean value) {
            this.value = value;
        }

        public String toString() { return Boolean.toString(value); }
    }

    public static class RuntimeNothing extends RuntimeValue {
        public String toString() { return "<nothing>"; }
    }

    public static class RuntimeFunction extends RuntimeValue {
        Environment declarationEnvironment;
        Vector<FunctionDeclarationParameter> parameters;
        Body body;
    }

    @FunctionalInterface
    public interface NativeFunction {
        RuntimeValue call(RuntimeValue[] args);
    }
    public static class RuntimeNativeFunction extends RuntimeValue {
        private final NativeFunction implementation;

        public RuntimeNativeFunction(NativeFunction implementation) {
            this.implementation = implementation;
        }

        public RuntimeValue call(RuntimeValue[] args) {
            return implementation.call(args);
        }

        public String toString() { return "NativeFunction<" + implementation.hashCode() + ">"; }
    }

    /*-------------*\
    |  Environment  |
    \*-------------*/

    Environment parent = null;
    HashMap<String, RuntimeValue> variables = new HashMap<String, RuntimeValue>();

    public RuntimeValue GetVariable(String name) {
        Environment container = GetVariableContainer(name);
        if (container == null) return null;
        return container.variables.get(name);
    }

    public void SetVariable(String name, RuntimeValue value) {
        variables.put(name, value);
    }

    public Environment GetVariableContainer(String name) {
        if (variables.containsKey(name)) return this;
        if (parent == null) return null;
        return parent.GetVariableContainer(name);
    }

    public boolean HasVariable(String name) {
        return GetVariableContainer(name) != null;
    }
}
