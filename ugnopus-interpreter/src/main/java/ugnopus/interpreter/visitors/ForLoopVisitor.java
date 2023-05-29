package ugnopus.interpreter.visitors;

import ugnopus.interpreter.generated.UgnopusBaseVisitor;
import ugnopus.interpreter.generated.UgnopusParser;

import java.util.UUID;

public class ForLoopVisitor extends UgnopusBaseVisitor<Object> {

    private final InterpreterVisitor parent;

    public ForLoopVisitor(InterpreterVisitor parent)
    {
            this.parent = parent;
    }

    @Override
    public Object visitForLoop(UgnopusParser.ForLoopContext ctx)
    {
        var varDec = ctx.variableDeclaration();
        UgnopusParser.AssignmentContext increment;
        if (varDec == null)
        {
            // Get initialization
            var assigments = ctx.assignment();
            var assigment = assigments.get(0);
            increment = assigments.get(1);
            parent.visitAssignment(assigment);
        }
        else {
            // Get initialization
            var init = ctx.variableDeclaration();
            parent.visitVariableDeclaration(init);
            //get increment
           increment = ctx.assignment().get(0);
        }

        //get expresion value
        var expression = (boolean) parent.visit(ctx.expression());

        //logic
        while (expression) {


            var oldScopeId = parent.currentScopeId;
            parent.currentScopeId = UUID.randomUUID();
            parent.scopesTable.put(parent.currentScopeId, oldScopeId);

            parent.visit(ctx.block());

            parent.currentScopeId = parent.scopesTable.getParent(parent.currentScopeId);

            parent.visitAssignment(increment);
            expression = (boolean) parent.visit(ctx.expression());
        }

        return null;
    }


}
