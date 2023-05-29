package ugnopus.interpreter.visitors;

import ugnopus.interpreter.generated.UgnopusBaseVisitor;
import ugnopus.interpreter.generated.UgnopusParser;

public class IfStatementVisitor extends UgnopusBaseVisitor<Object>
{
    private final InterpreterVisitor parent;
    public IfStatementVisitor(InterpreterVisitor parent)
    {
        this.parent = parent;
    }

    @Override
    public Object visitIfStatement(UgnopusParser.IfStatementContext ctx)
    {
        var blocksSize = ctx.block().size();
        Object ifCondition;
        boolean execIf = false;

        Object elseIfCondition;
        boolean execElseIf = false;


        // get if expression and values
        if (ctx.expression(0).children.size() == 3)
        {
            execIf = (boolean) parent.visit(ctx.expression(0));
        }
        else if (ctx.expression(0).children.size() == 1)
        {
            ifCondition = parent.visit(ctx.expression(0));
            execIf = parent.resolveCondition(ifCondition);
        }

        // if else if and else blocks are decleared
        if(blocksSize == 3)
        {
            // get else if expression and values
            if (ctx.expression(1).children.size() == 3)
            {
                execElseIf =  (boolean) parent.visit(ctx.expression(1));
            }
            else if (ctx.expression(1).children.size() == 1)
            {
                elseIfCondition = parent.visit(ctx.expression(1));
                execElseIf = parent.resolveCondition(elseIfCondition);
            }
            //logic
            if (execIf)
            {
                parent.visit(ctx.block(0));

            }
            else if(execElseIf)
            {
                parent.visit(ctx.block(1));

            }
            else
            {
                parent.visit(ctx.block(2));
            }
        }
        // if and else blocks are decleared
        else if (blocksSize == 2)
        {
            if (execIf)
            {
                parent.visit(ctx.block(0));
            }
            else
            {
                parent.visit(ctx.block(1));
            }
        }
        // if block is decleared
        else
        {
            if (execIf)
            {
                parent.visit(ctx.block(0));
            }
        }
        return null;
    }
}
