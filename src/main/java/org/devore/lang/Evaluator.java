package org.devore.lang;

import org.devore.lang.token.*;
import org.devore.parser.Ast;

import java.util.List;

/**
 * 求值器
 */
public class Evaluator {
    /**
     * 求值
     * @param env 环境
     * @param ast 语法树
     * @return 返回值
     */
    public static DToken eval(Env env, Ast ast) {
        while (true) {
            if (ast.symbol instanceof DSymbol && env.contains(ast.symbol.toString()))
                ast.symbol = env.get(ast.symbol.toString());
            else
                break;
        }
        if (ast.symbol instanceof DMacro) {
            DMacro macro = (DMacro) ast.symbol;
            List<Ast> bodys = macro.expand(ast.children);
            DToken result = DWord.NIL;
            for (Ast temp : bodys) result = eval(env, temp);
            return result;
        }
        if (ast.isEmpty() && ast.type != Ast.AstType.PROCEDURE)
            return ast.symbol;
        if (ast.symbol instanceof DProcedure) {
            DProcedure procedure = (DProcedure) ast.symbol;
            ast.symbol = procedure.call(ast, env);
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
