package org.wumoe.devore.lang;

import org.wumoe.devore.lang.token.DFunction;
import org.wumoe.devore.lang.token.DOp;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.parser.AstNode;

public class Evaluator {
    public static Token eval(Env env, AstNode ast) {
        while (true) {
            if (ast.op instanceof DOp && env.contains(ast.op.toString()))
                ast.op = env.get(ast.op.toString());
            else
                break;
        }
        if (ast.isEmpty() && ast.type != AstNode.AstType.FUNCTION)
            return ast.op;
        if (ast.op instanceof DFunction) {
            ast.op = ((DFunction) ast.op).call(ast, env);
            ast.clear();
        }
        return ast.op;
    }
}
