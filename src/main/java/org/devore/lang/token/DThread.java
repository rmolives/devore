package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程
 */
public class DThread extends DToken {
    private final Thread thread;
    private final AtomicReference<DToken> result;
    private final AtomicReference<Throwable> error;

    private DThread(Thread thread, AtomicReference<DToken> result, AtomicReference<Throwable> error) {
        this.thread = thread;
        this.result = result;
        this.error = error;
    }

    public static DThread valueOf(Thread thread) {
        return new DThread(thread, new AtomicReference<>(DWord.NIL), new AtomicReference<>());
    }

    public static DThread start(String name, ThreadBody body) {
        AtomicReference<DToken> result = new AtomicReference<>(DWord.NIL);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                result.set(body.run());
            } catch (Throwable e) {
                error.set(e);
            }
        }, name);
        DThread dThread = new DThread(thread, result, error);
        thread.start();
        return dThread;
    }

    public Thread toThread() {
        return this.thread;
    }

    public DToken result() {
        Throwable throwable = this.error.get();
        if (throwable instanceof DevoreRuntimeException)
            throw (DevoreRuntimeException) throwable;
        if (throwable != null)
            throw new DevoreRuntimeException(throwable.getMessage() == null
                    ? throwable.getClass().getSimpleName()
                    : throwable.getMessage());
        return this.result.get();
    }

    @Override
    public String type() {
        return "thread";
    }

    @Override
    protected String str() {
        return "<thread>";
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DThread && this.thread == ((DThread) t).thread ? 0 : -1;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.thread.hashCode();
        return result;
    }

    public interface ThreadBody {
        DToken run();
    }
}
