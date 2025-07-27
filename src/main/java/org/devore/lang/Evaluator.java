package org.devore.lang;

import org.devore.lang.token.DFunction;
import org.devore.lang.token.DOp;
import org.devore.lang.token.Token;
import org.devore.parser.AstNode;

/**
 * 求值器
 */
public class Evaluator {
    /**
     * 求值
     * @param env   环境
     * @param ast   语法树
     * @return      返回值
     */
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
