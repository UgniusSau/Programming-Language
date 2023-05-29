package ugnopus.interpreter.visitors;

import ugnopus.interpreter.generated.UgnopusBaseVisitor;
import ugnopus.interpreter.generated.UgnopusParser;

import java.util.UUID;

public class WhileLoopVisitor  extends UgnopusBaseVisitor<Object> {
    private final InterpreterVisitor parent;

    public WhileLoopVisitor(InterpreterVisitor parent)
    {
        this.parent = parent;
    }

    @Override
    public Object visitWhileLoop(UgnopusParser.WhileLoopContext ctx)
    {
        Object condition;
        boolean execWhile = false;

        if (ctx.expression().children.size() == 3)
        {
            execWhile = (boolean) parent.visit(ctx.expression());
            while(execWhile) {
                var oldScopeId = parent.currentScopeId;
                parent.currentScopeId = UUID.randomUUID();
                parent.scopesTable.put(parent.currentScopeId, oldScopeId);

                parent.visit(ctx.block());

                parent.currentScopeId = parent.scopesTable.getParent(parent.currentScopeId);
                //check condition
                execWhile = (boolean) parent.visit(ctx.expression());
            }
        }
        else if (ctx.expression().children.size() == 1)
        {
            condition = parent.visit(ctx.expression());
            execWhile = parent.resolveCondition(condition);

            while(execWhile)
            {
                var oldScopeId = parent.currentScopeId;
                parent.currentScopeId = UUID.randomUUID();
                parent.scopesTable.put(parent.currentScopeId, oldScopeId);

                parent.visit(ctx.block());

                parent.currentScopeId = parent.scopesTable.getParent(parent.currentScopeId);

                condition = parent.visit(ctx.expression());
                execWhile = parent.resolveCondition(condition);
            }
        }

        return null;
    }
}
