package org.wumoe.devore.lang.token;

import org.wumoe.devore.lang.Env;
import org.wumoe.devore.parser.AstNode;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DThread extends Token {
    private final Runnable task;
    private final String str;
    public final UUID uuid;

    private Future<?> future;

    public DThread(DFunction func, Env env) {
        this.str = func.str();
        this.task = () -> func.call(AstNode.nullAst, env);
        this.uuid  = UUID.randomUUID();
    }

    public static DThread create(DFunction func, Env env) {
        return new DThread(func, env);
    }

    public boolean start() {
        if (this.future != null)
            return false;
        this.future = Executors.newSingleThreadExecutor().submit(task);
        return true;
    }

    public boolean isStart() {
        return future != null;
    }

    public boolean isDone() {
        if (future == null || future.isCancelled())
            return true;
        return future.isDone();
    }

    public boolean join() {
        if (future == null)
            return false;
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
        return true;
    }

    @Override
    public String type() {
        return "thread";
    }

    @Override
    public String str() {
        return "<thread>{" + str + "}";
    }

    @Override
    public Token copy() {
        return null;
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DThread thread && uuid == thread.uuid ? 0 : -1;
    }
}
