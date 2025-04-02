package me.xswezan;

import java.util.HashMap;

public class Environment {
    /*----------------*\
    |  Runtime Values  |
    \*----------------*/

    public static class RuntimeValue {}

    //> RuntimeNumber
    public static class RuntimeNumber extends RuntimeValue {
        double value;

        RuntimeNumber(double value) {
            this.value = value;
        }
    }

    //> RuntimeString
    public static class RuntimeString extends RuntimeValue {
        String value;

        RuntimeString(String value) {
            this.value = value;
        }
    }

    //> RuntimeNativeFunction
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
