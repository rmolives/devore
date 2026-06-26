package org.devore.lang.token;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class DExport extends DToken {
    public final List<String> keys;

    public DExport(List<String> keys) {
        this.keys = keys;
    }

    public static DExport valueOf(List<String> keys) {
        return new DExport(keys);
    }

    @Override
    public String type() {
        return "export";
    }

    @Override
    protected String str() {
        return "<export>";
    }

    public int size() {
        return this.keys.size();
    }

    @Override
    public int compareTo(DToken t) {
        if (!(t instanceof DExport))
            return -1;
        DExport other = (DExport) t;
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
