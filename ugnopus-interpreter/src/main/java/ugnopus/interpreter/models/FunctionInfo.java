package ugnopus.interpreter.models;

import ugnopus.interpreter.generated.UgnopusParser;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FunctionInfo {

    private final String FunctionName;
    private String FunctionType;
    private final List<String> FunctionParamTypes;
    private List<String> FunctionParamNames;
    private UUID OriginalScope;

    private UgnopusParser.FunctionContext functionContext;
    private Object Result;

    public FunctionInfo(
            String functionName,
            String functionType,
            List<String> functionParamTypes,
            List<String> functionParamNames,
            UUID originalScope,
            UgnopusParser.FunctionContext functionContext)
    {
        this.FunctionName = functionName;
        this.FunctionType = functionType;
        this.OriginalScope = originalScope;
        this.functionContext = functionContext;
        this.FunctionParamTypes = new LinkedList<>();
        this.FunctionParamTypes.addAll(functionParamTypes);
        this.FunctionParamNames = new LinkedList<>();
        this.FunctionParamNames.addAll(functionParamNames);
    }

    public FunctionInfo(String functionName, String functionType)
    {
        this.FunctionName = functionName;
        this.FunctionType = functionType;

        this.FunctionParamTypes = new LinkedList<>();
        this.FunctionParamNames = new LinkedList<>();
    }

    public Object getResult() {return this.Result;}

    public void updateContext(UgnopusParser.FunctionContext c) {this.functionContext = c;}
    public void updateParamNames(List<String> c) {this.FunctionParamNames = c;}
    public UUID getOriginalScope() {return this.OriginalScope;}
    public UgnopusParser.FunctionContext getContext() {return this.functionContext;}

    public void updateResult(Object o) { this.Result = o;}

    public String getName() {return  this.FunctionName;}
    public String getType()
    {
        return this.FunctionType;
    }

    public List<String> getParamTypes()
    {
        return this.FunctionParamTypes;
    }
    public List<String> getParamNames()
    {
        return this.FunctionParamNames;
    }
    public void updateReturnType(String type) {this.FunctionType = type;}
    public String getReturnType() {return this.FunctionType;}
    public void addParam(String param) { this.FunctionParamTypes.add(param); }

    public boolean equals(FunctionInfo o) {
        if(this.FunctionName.equals(o.FunctionName) && this.FunctionParamTypes.size() == o.FunctionParamTypes.size())
        {
            for(int i = 0; i < this.FunctionParamTypes.size(); i++)
            {
                if(!Objects.equals(this.FunctionParamTypes.get(i), o.FunctionParamTypes.get(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
