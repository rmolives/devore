package org.wumoe.devore.lang;

import org.wumoe.devore.lang.token.DFunction;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.parser.AstNode;

public class Evaluator {
    public static Token eval(Env env, AstNode ast) {
        while (true) {
            if (DType.isOp(ast.op) && env.contains(ast.op.str()))
                ast.op = env.get(ast.op.str());
            else
                break;
        }
        if (ast.isEmpty() && ast.type != AstNode.AstType.FUNCTION)
            return ast.op;
        if (DType.isFunction(ast.op)) {
            ast.op = ((DFunction) ast.op).call(ast, env);
            ast.clear();
        }
        return ast.op;
    }
}
