package org.devore.lang.token;

import java.util.Objects;

/**
 * Java对象包装
 */
public class DJavaObject extends DToken {
    private final Object value;

    private DJavaObject(Object value) {
        this.value = value;
    }

    public static DJavaObject valueOf(Object value) {
        return new DJavaObject(value);
    }

    public Object value() {
        return this.value;
    }

    @Override
    public String type() {
        return "java-object";
    }

    @Override
    protected String str() {
        if (this.value == null)
            return "<java-object:null>";
        if (this.value instanceof Class<?>)
            return "<java-class:" + ((Class<?>) this.value).getName() + ">";
        return "<java-object:" + this.value.getClass().getName() + ":" + this.value + ">";
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DJavaObject && Objects.equals(this.value, ((DJavaObject) t).value) ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.value);
    }
}
