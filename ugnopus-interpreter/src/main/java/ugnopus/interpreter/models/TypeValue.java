package ugnopus.interpreter.models;

import java.util.Objects;

public class TypeValue {
    private final String VariableType;
    private Object VariableValue;

    public TypeValue(String variableType, Object variableValue)
    {
        this.VariableType = variableType;
        this.VariableValue = variableValue;
    }

    public String getType()
    {
        return this.VariableType;
    }

    public Object getValue()
    {
        return this.VariableValue;
    }

    public void updateValue(Object value) {this.VariableValue = value;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeValue that)) return false;
        return Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
