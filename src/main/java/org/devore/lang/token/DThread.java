package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程
 */
public class DThread extends DToken {
    /**
     * Java线程实例
     */
    private final Thread thread;

    /**
     * 线程执行结果
     */
    private final AtomicReference<DToken> result;

    /**
     * 线程执行异常
     */
    private final AtomicReference<Throwable> error;

    /**
     * 创建线程Token
     *
     * @param thread Java线程实例
     * @param result 线程执行结果引用
     * @param error  线程执行异常引用
     */
    private DThread(Thread thread, AtomicReference<DToken> result, AtomicReference<Throwable> error) {
        this.thread = thread;
        this.result = result;
        this.error = error;
    }

    /**
     * 将Java线程包装为线程Token
     *
     * @param thread Java线程实例
     * @return 线程Token
     */
    public static DThread valueOf(Thread thread) {
        return new DThread(thread, new AtomicReference<>(DWord.NIL), new AtomicReference<>());
    }

    /**
     * 创建带执行体的线程Token
     *
     * @param name 线程名
     * @param body 线程执行体
     * @return 线程Token
     */
    public static DThread create(String name, ThreadBody body) {
        AtomicReference<DToken> result = new AtomicReference<>(DWord.NIL);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                result.set(body.run());
            } catch (Throwable e) {
                error.set(e);
            }
        }, name);
        return new DThread(thread, result, error);
    }

    /**
     * 启动线程
     */
    public void start() {
        try {
            this.thread.start();
        } catch (IllegalThreadStateException e) {
            throw new DevoreRuntimeException("线程已经启动.");
        }
    }

    /**
     * 获取Java线程实例
     *
     * @return Java线程实例
     */
    public Thread toThread() {
        return this.thread;
    }

    /**
     * 获取线程执行结果，若线程执行失败则抛出对应运行时错误
     *
     * @return 线程执行结果
     */
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
        return Objects.hash(this.type(), this.thread);
    }

    /**
     * 线程执行体
     */
    public interface ThreadBody {
        /**
         * 执行线程逻辑
         *
         * @return 线程执行结果
         */
        DToken run();
    }
}
