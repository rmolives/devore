package org.devore.module;

import org.devore.exception.DevoreCastException;
import org.devore.lang.Env;
import org.devore.lang.Evaluator;
import org.devore.lang.token.*;
import org.wumoe.devore.lang.token.*;
import org.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 并发相关模块
 */
public class ThreadModule extends Module {
    @Override
    public void init(Env dEnv) {
        dEnv.addSymbolFunction("thread-new", ((ast, env) -> {
            List<AstNode> asts = new ArrayList<>();
            asts.add(ast.get(0).copy());
            BiFunction<AstNode, Env, Token> df = (inAst, inEnv) -> {
                for (int i = 0; i < inAst.size(); ++i)
                    inAst.get(i).op = Evaluator.eval(inEnv, inAst.get(i).copy());
                Env newInEnv = env.createChild();
                Token inResult = DWord.WORD_NIL;
                for (AstNode astNode : asts)
                    inResult = Evaluator.eval(newInEnv, astNode.copy());
                return inResult;
            };
            return DThread.create(DFunction.newFunction(df, 0, false), env);
        }), 1, false);
        dEnv.addTokenFunction("thread-start", ((args, env) -> {
            if (!(args.get(0) instanceof DThread))
                throw new DevoreCastException(args.get(0).type(), "thread");
            ((DThread) args.get(0)).start();
            return DWord.WORD_NIL;
        }), 1, false);
        dEnv.addTokenFunction("thread-start?", ((args, env) -> {
            if (!(args.get(0) instanceof DThread))
                throw new DevoreCastException(args.get(0).type(), "thread");
            return DBool.valueOf(((DThread) args.get(0)).isStart());
        }), 1, false);
        dEnv.addTokenFunction("thread-done?", ((args, env) -> {
            if (!(args.get(0) instanceof DThread))
                throw new DevoreCastException(args.get(0).type(), "thread");
            return DBool.valueOf(((DThread) args.get(0)).isDone());
        }), 1, false);
        dEnv.addTokenFunction("thread-join", ((args, env) -> {
            if (!(args.get(0) instanceof DThread))
                throw new DevoreCastException(args.get(0).type(), "thread");
            return DBool.valueOf(((DThread) args.get(0)).join());
        }), 1, false);
    }

}
