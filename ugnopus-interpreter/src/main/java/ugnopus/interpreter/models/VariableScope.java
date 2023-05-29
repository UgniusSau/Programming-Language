package ugnopus.interpreter.models;

import java.util.Objects;
import java.util.UUID;

public class VariableScope {
    private final String variableName;
    private final UUID scopeId;

    public VariableScope(String variableName, UUID scopeId) {
        this.variableName = variableName;
        this.scopeId = scopeId;
    }

    public String getVariableName() {
        return variableName;
    }


    public UUID getScopeId() {
        return scopeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableScope that)) return false;
        return Objects.equals(getVariableName(), that.getVariableName()) &&
                Objects.equals(getScopeId(), that.getScopeId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVariableName(), getScopeId());
    }
}
