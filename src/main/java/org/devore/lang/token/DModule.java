package org.devore.lang.token;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class DModule extends DToken {
    public final List<String> keys;

    public DModule(List<String> keys) {
        this.keys = keys;
    }

    public static DModule valueOf(List<String> keys) {
        return new DModule(keys);
    }

    @Override
    public String type() {
        return "module";
    }

    @Override
    protected String str() {
        return "<module>";
    }

    public int size() {
        return this.keys.size();
    }

    @Override
    public int compareTo(DToken t) {
        if (!(t instanceof DModule))
            return -1;
        DModule other = (DModule) t;
        if (other.size() != this.size())
            return -1;
        return IntStream.range(0, this.size())
                .allMatch(i -> other.keys.get(i).equals(this.keys.get(i))) ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.keys);
    }
}
