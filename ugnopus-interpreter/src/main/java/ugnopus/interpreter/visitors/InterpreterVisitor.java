package ugnopus.interpreter.visitors;

import ugnopus.interpreter.generated.*;
import ugnopus.interpreter.models.Bulynas;
import ugnopus.interpreter.models.FunctionInfo;
import ugnopus.interpreter.models.TypeValue;
import ugnopus.interpreter.models.VariableScope;
import ugnopus.interpreter.tables.ScopesTable;
import ugnopus.interpreter.tables.SymbolTable;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.UUID;


public class InterpreterVisitor extends UgnopusBaseVisitor<Object> {

    private final StringBuilder SYSTEM_OUT = new StringBuilder();
    public final SymbolTable symbolTable;
    public final ScopesTable scopesTable;
    private final IfStatementVisitor ifStatementVisitor;
    private final ForLoopVisitor forLoopVisitor;
    private  final WhileLoopVisitor whileLoopVisitor;
    public UUID currentScopeId;

    private final ArrayList<FunctionInfo> functionList;
    private static final int INTEGER_INTEGER = 1;
    private static final int DOUBLE_DOUBLE = 2;
    private static final int INTEGER_DOUBLE = 3;
    private static final int DOUBLE_INTEGER = 4;
    private static final int STRING_STRING = 5;
    private static final int INTEGER_STRING = 6;
    private static final int STRING_INTEGER = 7;


    public InterpreterVisitor(SymbolTable symbolTable, ScopesTable scopesTable, UUID currentScopeId, ArrayList<FunctionInfo> functionList) {
        this.symbolTable = symbolTable;
        this.scopesTable = scopesTable;
        this.currentScopeId = currentScopeId;
        this.ifStatementVisitor = new IfStatementVisitor(this);
        this.forLoopVisitor = new ForLoopVisitor(this);
        this.whileLoopVisitor = new WhileLoopVisitor(this);
        this.functionList = functionList;
    }

    private int getType(Object val1, Object val2) {
        if (val1 instanceof Integer && val2 instanceof Integer) {
            return INTEGER_INTEGER;
        } else if (val1 instanceof Double && val2 instanceof Double) {
            return DOUBLE_DOUBLE;
        } else if (val1 instanceof Integer && val2 instanceof Double) {
            return INTEGER_DOUBLE;
        } else if (val1 instanceof Double && val2 instanceof Integer) {
            return DOUBLE_INTEGER;
        } else if (val1 instanceof String && val2 instanceof String) {
            return STRING_STRING;
        } else if (val1 instanceof Integer && val2 instanceof String) {
            return INTEGER_STRING;
        } else if (val1 instanceof String && val2 instanceof Integer) {
            return STRING_INTEGER;
        }
        else {
            return -1;
        }
    }

    @Override
    public Object visitProgram(UgnopusParser.ProgramContext ctx) {
        super.visitProgram(ctx);
        return SYSTEM_OUT.toString();
    }

    @Override
    public Object visitVariableDeclaration(UgnopusParser.VariableDeclarationContext ctx) {
        var varType = ctx.getChild(0).toString();
        String varName = ctx.ID().getText();
        VariableScope vs = new VariableScope(varName, currentScopeId);
        Object value = null;
        if (ctx.expression() != null) {
            var oldScopeId = currentScopeId;
            currentScopeId = UUID.randomUUID();
            scopesTable.put(currentScopeId, oldScopeId);

            value = visit(ctx.expression());

            currentScopeId = scopesTable.getParent(currentScopeId);
        }
        else {
            var tv = new TypeValue(varType, value);
            if (!this.symbolTable.contains(vs, scopesTable)) {
                this.symbolTable.put(vs, tv);
                return null;
            } else {
                throw new RuntimeException("Variable already exists.");
            }
        }
        if(isParsable(varType, value)) {
            var tv = new TypeValue(varType, value);
            if (!this.symbolTable.contains(vs, scopesTable)) {
                this.symbolTable.put(vs, tv);
            } else {
                throw new RuntimeException("Variable already exists.");
            }
        }
        else
        {
            throw new RuntimeException("Value does not match variable type");
        }
        return null;
    }

    @Override
    public Object visitAssignment(UgnopusParser.AssignmentContext ctx) {
        String varName = ctx.ID().getText();
        VariableScope vs = new VariableScope(varName, currentScopeId);
        var currentTv = this.symbolTable.get(vs, scopesTable);

        Object value = visit(ctx.expression());
        if (currentTv != null && isParsable(currentTv.getType(), value))
        {
            var updatedTv = new TypeValue(currentTv.getType(), value);
            var variableScopeId = this.symbolTable.getVariableScope(vs, scopesTable);
            var newVs = new VariableScope(varName, variableScopeId);
            this.symbolTable.put(newVs, updatedTv);
        } else {
            throw new RuntimeException("Undeclared variable.");
        }
        return null;
    }

    @Override
    public Object visitIntExpression(UgnopusParser.IntExpressionContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Object visitStringExpression(UgnopusParser.StringExpressionContext ctx) {
        return ctx.STRING().getText();
    }

    @Override
    public Object visitBoolExpression(UgnopusParser.BoolExpressionContext ctx) {
        var boolValue = ctx.BOOL().getText();
        return new Bulynas(boolValue);
    }

    @Override
    public Object visitCharExpression(UgnopusParser.CharExpressionContext ctx) {
        return ctx.CHAR().getText().charAt(1);
    }

    @Override
    public Object visitDoubleExpression(UgnopusParser.DoubleExpressionContext ctx) {
        return Double.parseDouble(ctx.DOUBLE().getText());
    }

    @Override
    public Object visitIdExpression(UgnopusParser.IdExpressionContext ctx) {
        String varName = ctx.ID().getText();
        VariableScope vs = new VariableScope(varName, currentScopeId);
        return this.symbolTable.get(vs, scopesTable).getValue();
    }

    @Override
    public Object visitPrintStatement(UgnopusParser.PrintStatementContext ctx) {
        String text = visit(ctx.expression()).toString();
        System.out.print(text + "\n"); // Print immediately
        return null;
    }

    private Scanner scanner = new Scanner(System.in);

    @Override
    public Object visitReadConsoleStatment(UgnopusParser.ReadConsoleStatmentContext ctx) {
        System.out.print("Enter a value: ");
        String input = scanner.nextLine();
        return input;
    }

    @Override
    public Object visitReadConsoleExpression(UgnopusParser.ReadConsoleExpressionContext ctx) {
        var input = visitReadConsoleStatment(ctx.readConsoleStatment());
        var type = ctx.parent.getChild(0).getText();
        if(input.equals("tru"))
        {
            input = "Tru";
        }
        if(input.equals("fols"))
        {
            input = "Fols";
        }
        var cehckParsing = isParsable(type ,input);
        if(cehckParsing)
        {
            return Parse(type, input.toString());
        }
        return null;
    }

    @Override
    public Object visitFileOutputStatement(UgnopusParser.FileOutputStatementContext ctx) {
        var filePath = ctx.expression().getText();
        if(filePath.contains("."))
        {
            filePath = filePath.substring(1, filePath.length() - 1);
        }
        else
        {
            filePath = visit(ctx.expression()).toString();
            filePath = filePath.substring(1, filePath.length() - 1);
        }
        var varName = ctx.ID().getText();
        VariableScope vs = new VariableScope(varName, currentScopeId);
        var varValue = this.symbolTable.get(vs, scopesTable).getValue().toString();
        if (varValue.contains("\"")) {
            varValue = varValue.substring(1, varValue.length() - 1);
        }
        File file = new File(filePath);

        try {
            if (file.createNewFile()) {
                System.out.println("File created successfully.");
                FileWriter writer = new FileWriter(file);
                writer.write(varValue);
                writer.close();
            } else {
                System.out.println("File already exists.");
                FileWriter writer = new FileWriter(file, true);
                writer.write("\n" + varValue);
                writer.close();

                System.out.println("New output been appended to the file.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Object visitFileInputExpression(UgnopusParser.FileInputExpressionContext ctx) {
        var filePath = visit(ctx.fileInputStatement()).toString();
        filePath = filePath.substring(1, filePath.length() - 1);
        File file = new File(filePath);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder content = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            reader.close();

            String varValue = content.toString();
            var type = ctx.parent.getChild(0).getText();
            if(type.equals("Bulynas") && varValue.equals("tru"))
            {
                varValue = "Tru";
            }
            if(type.equals("Bulynas") && varValue.equals("fols"))
            {
                varValue = "Fols";
            }
            if(isParsable(type ,varValue))
            {
                System.out.println("The value from the file: " + varValue);
                return varValue;
            }
            else
            {
                throw new RuntimeException("Value does not match variable type");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Object visitFileInputStatement(UgnopusParser.FileInputStatementContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitParenthesesExpression(UgnopusParser.ParenthesesExpressionContext ctx) {
        return visit(ctx.expression());
    }


    @Override
    public Object visitAddOpExpression(UgnopusParser.AddOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));

        Object val2 = visit(ctx.expression(1));

        return switch (ctx.addOp().getText()) {
            case "+" -> switch (getType(val1, val2)) {
                case INTEGER_INTEGER -> (Integer) val1 + (Integer) val2;
                case DOUBLE_DOUBLE ->  (Double) val1 + (Double) val2;
                case INTEGER_DOUBLE -> (Integer) val1 + (Double) val2;
                case DOUBLE_INTEGER -> (Double) val1 + (Integer) val2;
                case STRING_STRING -> "\"" + val1.toString().replace("\"", "") + val2.toString().replace("\"", "") + "\"";
                case INTEGER_STRING -> val1.toString() + val2.toString().replace("\"", "");
                case STRING_INTEGER -> val1.toString().replace("\"", "") + val2.toString();
                default -> null;
            };
            case "-" -> switch (getType(val1, val2)) {
                case INTEGER_INTEGER -> (Integer) val1 - (Integer) val2;
                case DOUBLE_DOUBLE -> (Double) val1 - (Double) val2;
                case INTEGER_DOUBLE -> (Integer) val1 - (Double) val2;
                case DOUBLE_INTEGER -> (Double) val1 - (Integer) val2;
                default -> null;
            };
            default -> null;
        };

    }

    @Override
    public Object visitMultiOpExpression(UgnopusParser.MultiOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));

        return switch (ctx.multiOp().getText()) {
            case "*" -> switch (getType(val1, val2)) {
                case INTEGER_INTEGER -> (Integer) val1 * (Integer) val2;
                case DOUBLE_DOUBLE -> (Double) val1 * (Double) val2;
                case INTEGER_DOUBLE -> (Integer) val1 * (Double) val2;
                case DOUBLE_INTEGER -> (Double) val1 * (Integer) val2;
                default -> null;
            };
            case "/" -> switch (getType(val1, val2)) {
                case INTEGER_INTEGER -> (Integer) val1 / (Integer) val2;
                case DOUBLE_DOUBLE -> (Double) val1 / (Double) val2;
                case INTEGER_DOUBLE -> (Integer) val1 / (Double) val2;
                case DOUBLE_INTEGER -> (Double) val1 / (Integer) val2;
                default -> null;
            };
            case "%" -> switch (getType(val1, val2)) {
                case INTEGER_INTEGER -> (Integer) val1 % (Integer) val2;
                case DOUBLE_DOUBLE -> (Double) val1 % (Double) val2;
                case INTEGER_DOUBLE -> (Integer) val1 % (Double) val2;
                case DOUBLE_INTEGER -> (Double) val1 % (Integer) val2;
                default -> null;
            };
            default -> null;
        };
    }

    @Override
    public Object visitIfStatement(UgnopusParser.IfStatementContext ctx) {
        var oldScopeId = currentScopeId;
        currentScopeId = UUID.randomUUID();
        scopesTable.put(currentScopeId, oldScopeId);

        this.ifStatementVisitor.visitIfStatement(ctx);

        currentScopeId = scopesTable.getParent(currentScopeId);

        return null;
    }

    @Override
    public Object visitForLoop(UgnopusParser.ForLoopContext ctx) {
        var oldScopeId = currentScopeId;
        currentScopeId = UUID.randomUUID();
        scopesTable.put(currentScopeId, oldScopeId);

        this.forLoopVisitor.visitForLoop(ctx);

        currentScopeId = scopesTable.getParent(currentScopeId);

        return null;
    }

    @Override
    public Object visitBlock(UgnopusParser.BlockContext ctx) {
        for (var statement : ctx.statement())
        {
            visit(statement);
        }
        return null;
    }

    @Override
    public Object visitWhileLoop(UgnopusParser.WhileLoopContext ctx) {
        var oldScopeId = currentScopeId;
        currentScopeId = UUID.randomUUID();
        scopesTable.put(currentScopeId, oldScopeId);

        this.whileLoopVisitor.visitWhileLoop(ctx);

        currentScopeId = scopesTable.getParent(currentScopeId);

        return null;
    }
















    // viska gauna
    @Override
    public Object visitFunction(UgnopusParser.FunctionContext ctx) {
        var names = ctx.ID().toArray();
        var types = ctx.TYPE().toArray();
        ArrayList<String> paramTypes = new ArrayList<>();
        ArrayList<String> paramNames = new ArrayList<>();
        for(int i = 0; i < types.length-1; i++)
        {
            paramNames.add(names[i].toString());
            paramTypes.add(types[i].toString());
        }
        FunctionInfo tempFunction =
                new FunctionInfo(
                        names[names.length-1].toString(),
                        types[types.length-1].toString(),
                        paramTypes,
                        paramNames,
                        currentScopeId,
                        ctx);

        for(var function : functionList) {
            if (function.equals(tempFunction)) {
                throw new IllegalStateException("Duplicate funkcijus was found");
            }
        }

        functionList.add(tempFunction);
        return null;
    }


    // function body  tarp {}
    Object functionResult = null;
    @Override
    public Object visitFunctionBlock(UgnopusParser.FunctionBlockContext ctx) {


        Object result = null;
        for (var statemnt : ctx.statement())
        {
            result = visit(statemnt);
            if(functionResult != null)
            {
                return functionResult;
            }
        }

        return null;
    }

    @Override
    public Object visitReturnStatment(UgnopusParser.ReturnStatmentContext ctx) {
        functionResult = visit(ctx.expression());
        return null;
    }


    @Override
    public Object visitFunctionResult(UgnopusParser.FunctionResultContext ctx) {
        var x = 0;
        return super.visitFunctionResult(ctx);
    }



    @Override
    public Object visitFunctionResultExpression(UgnopusParser.FunctionResultExpressionContext ctx) {
        var fName = ctx.functionResult().ID(0).getText();
        ArrayList<String> paramTypes = new ArrayList<>();
        ArrayList<Object> paramValues = new ArrayList<>();

        for(int i = 1; i < ctx.functionResult().ID().size(); i++)
        {
            var varName = ctx.functionResult().ID(i).getText();
            var varType = symbolTable.get(new VariableScope(varName, currentScopeId),scopesTable).getType();
            var varValue = symbolTable.get(new VariableScope(varName,currentScopeId),scopesTable).getValue();
            paramTypes.add(varType);
            paramValues.add(varValue);
        }
        var fInfo = new FunctionInfo(fName,null,paramTypes,new LinkedList<>(), currentScopeId, null);
        for(int i = 0; i < functionList.size(); i++)
        {
            if(functionList.get(i).equals(fInfo))
            {
                fInfo.updateContext(functionList.get(i).getContext());
                fInfo.updateParamNames(functionList.get(i).getParamNames());
                fInfo.updateReturnType(functionList.get(i).getReturnType());
            }
        }
        var fBlock = fInfo.getContext().functionBlock();
        currentScopeId = scopesTable.getParent(currentScopeId);
        var oldScopeId = currentScopeId;
        currentScopeId = UUID.randomUUID();
        scopesTable.put(currentScopeId, oldScopeId);

        for (int i = 0; i < paramValues.size(); i++)
        {
            symbolTable.put(
                    new VariableScope
                            (
                                    fInfo.getParamNames().get(i),
                                    currentScopeId
                            ),
                    new TypeValue
                            (
                                    paramTypes.get(i),
                                    paramValues.get(i)
                            ));
        }

        Object result = visit(fBlock);
        currentScopeId = fInfo.getOriginalScope();
        functionResult = null;
        return Parse(fInfo.getReturnType(), result);
    }



    @Override
    public Object visitUseFunction(UgnopusParser.UseFunctionContext ctx) {
        var fName = ctx.functionResult().ID(0).getText();
        ArrayList<String> paramTypes = new ArrayList<>();
        ArrayList<Object> paramValues = new ArrayList<>();

        for(int i = 1; i < ctx.functionResult().ID().size(); i++)
        {
            var varName = ctx.functionResult().ID(i).getText();
            var varType = symbolTable.get(new VariableScope(varName, currentScopeId),scopesTable).getType();
            var varValue = symbolTable.get(new VariableScope(varName,currentScopeId),scopesTable).getValue();
            paramTypes.add(varType);
            paramValues.add(varValue);
        }
        var fInfo = new FunctionInfo(fName,null,paramTypes,new LinkedList<>(), currentScopeId, null);
        for(int i = 0; i < functionList.size(); i++)
        {
            if(functionList.get(i).equals(fInfo))
            {
                fInfo.updateContext(functionList.get(i).getContext());
                fInfo.updateParamNames(functionList.get(i).getParamNames());
                fInfo.updateReturnType(functionList.get(i).getReturnType());
            }
        }
        var fBlock = fInfo.getContext().functionBlock();
        currentScopeId = scopesTable.getParent(currentScopeId);
        var oldScopeId = currentScopeId;
        currentScopeId = UUID.randomUUID();
        scopesTable.put(currentScopeId, oldScopeId);

        for (int i = 0; i < paramValues.size(); i++)
        {
            symbolTable.put(
                    new VariableScope
                            (
                                    fInfo.getParamNames().get(i),
                                    currentScopeId
                            ),
                    new TypeValue
                            (
                                    paramTypes.get(i),
                                    paramValues.get(i)
                            ));
        }

        Object result = visit(fBlock);
        currentScopeId = fInfo.getOriginalScope();
        functionResult = null;
        return Parse(fInfo.getReturnType(), result);
    }



















    private static boolean isParsable(String type, Object value)
    {
        try
        {
            switch (type) {
                case "Intas":
                    Integer.parseInt(value.toString());
                    return true;
                case "Bulynas":
                    Bulynas.parseBoolean(value.toString());
                    return true;
                case "Charas":
                    if (value.toString().length() == 1)
                        return true;
                    throw new IllegalArgumentException();
                case "Doublas":
                    Double.parseDouble(value.toString());
                    return true;
                case "Stringas":
                    return value.equals(value.toString());
                default:
                    return false;
            }
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    private static Object Parse(String type, Object value)
    {
        try
        {
            switch (type) {
                case "Intas":
                    return Integer.parseInt(value.toString());
                case "Bulynas":
                    return Bulynas.parseBoolean(value.toString());
                case "Charas":
                    if (value.toString().length() == 1)
                        return value.toString();
                    throw new IllegalArgumentException();
                case "Doublas":
                    return Double.parseDouble(value.toString());
                case "Stringas":
                    return value.toString();
                default:
                    return null;
            }
        }
        catch (Exception ex)
        {
            return null;
        }
    }


    @Override
    public Object visitRelationOp(UgnopusParser.RelationOpContext ctx) {
        return ctx.getText();
    }

    @Override
    public Object visitRelationOpExpression(UgnopusParser.RelationOpExpressionContext ctx) {
        Object Left = visit(ctx.expression(0));
        Object Right = visit(ctx.expression(1));
        String relOp = ctx.relationOp().getText();
        return resolveCondition(Left, Right, relOp);
    }


    public static boolean resolveCondition(Object left, Object right, String relOp) {
        return switch (relOp) {
            case "==" -> left == right;
            case "!=" -> left != right;
            case "<=" -> {
                if (left instanceof Comparable && right instanceof Comparable) {
                    yield ((Comparable) left).compareTo(right) <= 0;
                } else {
                    throw new RuntimeException("Unsupported operand types for operator '<='.");
                }
            }
            case ">=" -> {
                if (left instanceof Comparable && right instanceof Comparable) {
                    yield ((Comparable) left).compareTo(right) >= 0;
                } else {
                    throw new RuntimeException("Unsupported operand types for operator '>='.");
                }
            }
            case "<" -> {
                if (left instanceof Comparable && right instanceof Comparable) {
                    yield ((Comparable) left).compareTo(right) < 0;
                } else {
                    throw new RuntimeException("Unsupported operand types for operator '<'.");
                }
            }
            case ">" -> {
                if (left instanceof Comparable && right instanceof Comparable) {
                    yield ((Comparable) left).compareTo(right) > 0;
                } else {
                    throw new RuntimeException("Unsupported operand types for operator '>'.");
                }
            }
            default -> throw new RuntimeException("Unsupported operator.");
        };
    }

    public static boolean resolveCondition(Object condition)
    {
        return  condition.toString().equals("Tru");
    }

}
