package ugnopus.interpreter.tables;

import org.antlr.v4.runtime.misc.ObjectEqualityComparator;
import ugnopus.interpreter.models.TypeValue;
import ugnopus.interpreter.models.VariableScope;

import javax.swing.border.SoftBevelBorder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SymbolTable {
    private final Map<VariableScope, TypeValue> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public void put(VariableScope vs, TypeValue value) {
        table.put(vs, value);
    }

    public TypeValue get(VariableScope vs, ScopesTable scopesTable) {
        var parentId = vs.getScopeId();
        TypeValue value = null;

        while (parentId != null) {
            var updatedVs = new VariableScope(vs.getVariableName(), parentId);

            value = table.get(updatedVs);
            if (value != null) {
                break;
            }

            parentId = scopesTable.getParent(parentId);

        }

        return value;
    }

    public UUID getVariableScope(VariableScope vs, ScopesTable scopesTable) {
        var parentId = vs.getScopeId();
        TypeValue value = null;

        while (parentId != null) {
            var updatedVs = new VariableScope(vs.getVariableName(), parentId);

            value = table.get(updatedVs);
            if (value != null) {
                break;
            }

            parentId = scopesTable.getParent(parentId);

        }

        return parentId;
    }

    public boolean contains(VariableScope vs, ScopesTable scopesTable) {
        return get(vs, scopesTable) != null;
    }

    public void updateScope(VariableScope vs, ScopesTable scopesTable, UUID updateToSCopeID)
    {
        VariableScope[] VS = table.keySet().toArray(VariableScope[]::new);
        ArrayList<VariableScope> currentValues = new ArrayList<>();
        ArrayList<VariableScope> valuesToChange = new ArrayList<>();

        UUID tevas = scopesTable.getParent(updateToSCopeID);


        for(VariableScope obj : VS )
        {
            if(obj.getScopeId().equals(updateToSCopeID))
                currentValues.add(obj);
        }

        for(VariableScope obj : VS )
        {
            if(obj.getScopeId().equals(tevas))
                valuesToChange.add(obj);
        }

        for(VariableScope changeTo : currentValues)
        {
            for(VariableScope toChange : valuesToChange)
            {
                if(changeTo.getVariableName().equals(toChange.getVariableName()))
                {
                    TypeValue toUpdate = table.get(toChange);
                    toUpdate.updateValue(table.get(changeTo).getValue());
                }
            }
        }
    }
}
