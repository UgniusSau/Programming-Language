package ugnopus.interpreter.tables;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScopesTable {
    private final Map<UUID, UUID> table;

    public ScopesTable() {
        table = new HashMap<>();
    }

    public void put(UUID scopeId, UUID parentScopeId) {
        table.put(scopeId, parentScopeId);
    }

    public UUID getParent(UUID scopeId) {
        return table.get(scopeId);
    }

    public boolean contains(UUID scopeId) {
        return table.containsKey(scopeId);
    }
}
