package org.devore.lang;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.*;
import org.devore.parser.Ast;

import java.util.List;

/**
 * 求值器
 */
public class Evaluator {
    /**
     * 求值
     *
     * @param env  环境
     * @param node 语法树
     * @return 返回值
     */
    public static DToken eval(Env env, Ast node) {
        String expression = node.toString();
        int index = node.index;
        try {
            if (node.symbol instanceof Ast)
                node.symbol = eval(env, ((Ast) node.symbol).copy());
            node.symbol = resolveSymbol(env, node.symbol);
            if (node.symbol instanceof DMacro) {
                DMacro macro = (DMacro) node.symbol;
                List<Ast> bodies = macro.expand(node.children);
                DToken result = DWord.NIL;
                for (Ast temp : bodies)
                    result = eval(env, temp);
                return result;
            }
            if (node.isEmpty() && node.type != Ast.Type.PROCEDURE)
                return node.symbol;
            if (node.type == Ast.Type.PROCEDURE) {
                if (node.isEmpty() && node.symbol == DWord.NIL)
                    return DWord.NIL;
                if (!(node.symbol instanceof DProcedure))
                    throw new DevoreRuntimeException("找不到匹配条件的过程: " + node.symbol);
            }
            if (node.symbol instanceof DProcedure) {
                DProcedure procedure = (DProcedure) node.symbol;
                node.symbol = procedure.call(node, env);
                node.clear();
            }
            return resolveSymbol(env, node.symbol);
        } catch (StackOverflowError e) {
            throw new DevoreRuntimeException("栈溢出，可能存在无限递归或递归宏展开.",
                    expression, index, node.source, node.toString());
        } catch (DevoreRuntimeException e) {
            e.setExpressionIfAbsent(expression, index, node.source, node.toString());
            e.addFrameIfAbsent(expression, index, node.source, node.toString());
            throw e;
        }
    }

    /**
     * 解析符号绑定，直到得到非符号值或环境中不存在该符号
     *
     * @param env   环境
     * @param token token
     * @return 解析结果
     */
    private static DToken resolveSymbol(Env env, DToken token) {
        while (token instanceof DSymbol && env.contains(token.toString()))
            token = env.get(token.toString());
        return token;
    }
}
