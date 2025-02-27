package org.wumoe.devore.module;

import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.Evaluator;
import org.wumoe.devore.lang.token.*;
import org.wumoe.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

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
            if (!(args.getFirst() instanceof DThread))
                throw new DevoreCastException(args.getFirst().type(), "thread");
            ((DThread) args.getFirst()).start();
            return DWord.WORD_NIL;
        }), 1, false);
        dEnv.addTokenFunction("thread-start?", ((args, env) -> {
            if (!(args.getFirst() instanceof DThread))
                throw new DevoreCastException(args.getFirst().type(), "thread");
            return DBool.valueOf(((DThread) args.getFirst()).isStart());
        }), 1, false);
        dEnv.addTokenFunction("thread-done?", ((args, env) -> {
            if (!(args.getFirst() instanceof DThread))
                throw new DevoreCastException(args.getFirst().type(), "thread");
            return DBool.valueOf(((DThread) args.getFirst()).isDone());
        }), 1, false);
        dEnv.addTokenFunction("thread-join", ((args, env) -> {
            if (!(args.getFirst() instanceof DThread))
                throw new DevoreCastException(args.getFirst().type(), "thread");
            return DBool.valueOf(((DThread) args.getFirst()).join());
        }), 1, false);
    }

}
