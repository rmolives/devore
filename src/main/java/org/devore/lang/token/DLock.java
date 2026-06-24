package org.devore.lang.token;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 可重入锁
 */
public class DLock extends DToken {
    private final ReentrantLock lock;

    private DLock(ReentrantLock lock) {
        this.lock = lock;
    }

    public static DLock valueOf() {
        return new DLock(new ReentrantLock());
    }

    public ReentrantLock toReentrantLock() {
        return this.lock;
    }

    @Override
    public String type() {
        return "lock";
    }

    @Override
    protected String str() {
        return "<lock>";
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DLock && this.lock == ((DLock) t).lock ? 0 : -1;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.lock.hashCode();
        return result;
    }
}
