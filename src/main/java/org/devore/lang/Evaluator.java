package org.devore.lang;

import org.devore.lang.token.DMacro;
import org.devore.lang.token.DProcedure;
import org.devore.lang.token.DSymbol;
import org.devore.lang.token.Token;
import org.devore.parser.AstNode;

import java.util.List;

/**
 * 求值器
 */
public class Evaluator {
    /**
     * 求值
     *
     * @param env 环境
     * @param ast 语法树
     * @return 返回值
     */
    public static Token eval(Env env, AstNode ast) {
        while (true) {
            if (ast.symbol instanceof DSymbol && env.contains(ast.symbol.toString()))
                ast.symbol = env.get(ast.symbol.toString());
            else
                break;
        }
        if (ast.symbol instanceof DMacro) {
            List<AstNode> bodys = ((DMacro) ast.symbol).expand(ast.children);
            ast.symbol = DSymbol.valueOf("begin");
            ast.children = bodys;
            return eval(env, ast);
        }
        if (ast.isEmpty() && ast.type != AstNode.AstType.PROCEDURE)
            return ast.symbol;
        if (ast.symbol instanceof DProcedure) {
            ast.symbol = ((DProcedure) ast.symbol).call(ast, env);
            ast.clear();
        }
        if (ast.symbol instanceof DSymbol)
            while (true) {
                if (ast.symbol instanceof DSymbol && env.contains(ast.symbol.toString()))
                    ast.symbol = env.get(ast.symbol.toString());
                else
                    break;
            }
        return ast.symbol;
    }
}
