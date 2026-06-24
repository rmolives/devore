package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.Evaluator;
import org.devore.lang.token.*;
import org.devore.parser.Ast;
import org.devore.utils.DNumberUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 线程和锁
 */
public class ThreadModule extends Module {
    public ThreadModule() {
        super("thread");
    }

    @Override
    public void init(Env dEnv) {
        initThreadProcedures(dEnv);
        initLockProcedures(dEnv);
        initPredicateProcedures(dEnv);
    }

    private void initThreadProcedures(Env dEnv) {
        dEnv.addAstProcedure("thread", (ast, env) -> {
            Env threadEnv = env.createChild();
            List<Ast> bodies = ast.children.stream()
                    .map(Ast::copy)
                    .collect(Collectors.toList());
            return DThread.start("devore-thread", () -> bodies.stream()
                    .map(node -> Evaluator.eval(threadEnv, node.copy()))
                    .reduce((previous, current) -> current)
                    .orElse(DWord.NIL));
        }, 1, true);
        dEnv.addTokenProcedure("join", (args, env) -> {
            if (!(args.get(0) instanceof DThread))
                throw new DevoreCastException(args.get(0).type(), "thread");
            DThread thread = (DThread) args.get(0);
            try {
                thread.toThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DevoreRuntimeException("线程等待被中断.");
            }
            return thread.result();
        }, 1, false);
        dEnv.addTokenProcedure("join", (args, env) -> {
            if (!(args.get(0) instanceof DThread))
                throw new DevoreCastException(args.get(0).type(), "thread");
            DThread thread = (DThread) args.get(0);
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            try {
                thread.toThread().join(DNumberUtils.toLong((DInt) args.get(1)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DevoreRuntimeException("线程等待被中断.");
            }
            return thread.toThread().isAlive() ? DWord.NIL : thread.result();
        }, 2, false);
        dEnv.addTokenProcedure("thread-alive?", (args, env) -> {
            if (!(args.get(0) instanceof DThread))
                throw new DevoreCastException(args.get(0).type(), "thread");
            return DBool.valueOf(((DThread) args.get(0)).toThread().isAlive());
        }, 1, false);
        dEnv.addTokenProcedure("current-thread", (args, env) ->
                DThread.valueOf(Thread.currentThread()), 0, false);
    }

    private void initLockProcedures(Env dEnv) {
        dEnv.addTokenProcedure("lock", (args, env) -> DLock.valueOf(), 0, false);
        dEnv.addTokenProcedure("lock!", (args, env) -> {
            if (!(args.get(0) instanceof DLock))
                throw new DevoreCastException(args.get(0).type(), "lock");
            ((DLock) args.get(0)).toReentrantLock().lock();
            return DWord.NIL;
        }, 1, false);
        dEnv.addTokenProcedure("unlock!", (args, env) -> {
            if (!(args.get(0) instanceof DLock))
                throw new DevoreCastException(args.get(0).type(), "lock");
            ReentrantLock lock = ((DLock) args.get(0)).toReentrantLock();
            if (!lock.isHeldByCurrentThread())
                throw new DevoreRuntimeException("当前线程没有持有该锁.");
            lock.unlock();
            return DWord.NIL;
        }, 1, false);
        dEnv.addTokenProcedure("try-lock!", (args, env) -> {
            if (!(args.get(0) instanceof DLock))
                throw new DevoreCastException(args.get(0).type(), "lock");
            return DBool.valueOf(((DLock) args.get(0)).toReentrantLock().tryLock());
        }, 1, false);
        dEnv.addTokenProcedure("try-lock!", (args, env) -> {
            if (!(args.get(0) instanceof DLock))
                throw new DevoreCastException(args.get(0).type(), "lock");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            try {
                return DBool.valueOf(((DLock) args.get(0)).toReentrantLock()
                        .tryLock(((DInt) args.get(1)).toBigInteger().longValue(), TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DevoreRuntimeException("线程等待锁被中断.");
            }
        }, 2, false);
        dEnv.addTokenProcedure("locked?", (args, env) -> {
            if (!(args.get(0) instanceof DLock))
                throw new DevoreCastException(args.get(0).type(), "lock");
            return DBool.valueOf(((DLock) args.get(0)).toReentrantLock().isLocked());
        }, 1, false);
        dEnv.addTokenProcedure("held-by-current-thread?", (args, env) -> {
            if (!(args.get(0) instanceof DLock))
                throw new DevoreCastException(args.get(0).type(), "lock");
            return DBool.valueOf(((DLock) args.get(0)).toReentrantLock().isHeldByCurrentThread());
        }, 1, false);
        dEnv.addAstProcedure("with-lock", (ast, env) -> {
            DToken token = Evaluator.eval(env, ast.get(0).copy());
            if (!(token instanceof DLock))
                throw new DevoreCastException(token.type(), "lock");
            ReentrantLock lock = ((DLock) token).toReentrantLock();
            lock.lock();
            try {
                return ast.children.subList(1, ast.size()).stream()
                        .map(node -> Evaluator.eval(env, node.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
            } finally {
                lock.unlock();
            }
        }, 2, true);
    }

    private void initPredicateProcedures(Env dEnv) {
        dEnv.addTokenProcedure("thread?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DThread), 1, false);
        dEnv.addTokenProcedure("lock?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DLock), 1, false);
    }
}
