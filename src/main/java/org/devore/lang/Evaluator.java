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
            if (node.symbol instanceof DMacro && node.type == Ast.Type.PROCEDURE) {
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
                    throw new DevoreRuntimeException("找不到匹配的过程: " + node.symbol);
            }
            if (node.symbol instanceof DProcedure) {
                DProcedure procedure = (DProcedure) node.symbol;
                node.symbol = procedure.call(node, env);
                node.clear();
            }
            return resolveSymbol(env, node.symbol);
        } catch (StackOverflowError e) {
            throw new DevoreRuntimeException("栈溢出，可能存在无限递归或递归宏展开.",
                    expression, index, node.source, node.code);
        } catch (DevoreRuntimeException e) {
            e.setExpressionIfAbsent(expression, index, node.source, node.code);
            e.addFrameIfAbsent(expression, index, node.source, node.code);
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
        DToken temp = token;
        while (temp instanceof DSymbol && env.contains(temp.toString()))
            temp = env.get(temp.toString());
        if (temp instanceof DSymbol)
            throw new DevoreRuntimeException("找不到匹配的绑定: " + temp);
        return temp;
    }
}
