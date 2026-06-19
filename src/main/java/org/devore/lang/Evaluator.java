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
                node.symbol = eval(env, (Ast) node.symbol);
            while (true) {
                if (node.symbol instanceof DSymbol && env.contains(node.symbol.toString()))
                    node.symbol = env.get(node.symbol.toString());
                else
                    break;
            }
            if (node.symbol instanceof DMacro) {
                DMacro macro = (DMacro) node.symbol;
                List<Ast> bodys = macro.expand(node.children);
                DToken result = DWord.NIL;
                for (Ast temp : bodys)
                    result = eval(env, temp);
                return result;
            }
            if (node.isEmpty() && node.type != Ast.Type.PROCEDURE)
                return node.symbol;
            if (node.symbol instanceof DProcedure) {
                DProcedure procedure = (DProcedure) node.symbol;
                node.symbol = procedure.call(node, env);
                node.clear();
            }
            if (node.symbol instanceof DSymbol)
                while (true) {
                    if (node.symbol instanceof DSymbol && env.contains(node.symbol.toString()))
                        node.symbol = env.get(node.symbol.toString());
                    else
                        break;
                }
            return node.symbol;
        } catch (StackOverflowError e) {
            throw new DevoreRuntimeException("栈溢出，可能存在无限递归或递归宏展开.", expression, index);
        } catch (DevoreRuntimeException e) {
            e.setExpressionIfAbsent(expression, index);
            e.addFrameIfAbsent(expression, index);
            throw e;
        }
    }
}
