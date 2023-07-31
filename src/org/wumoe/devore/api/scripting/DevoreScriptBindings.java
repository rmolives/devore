package org.wumoe.devore.api.scripting;

import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.token.*;

import javax.script.Bindings;
import java.util.*;

public class DevoreScriptBindings implements Bindings {
    public Env env;

    public DevoreScriptBindings(Env env) {
        this.env = env;
    }

    @Override
    public Object put(String name, Object value) {
        Token result = env.get(name);
        env.put(name, Token.cast(value));
        return result;
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        for (Entry<? extends String, ?> entry : toMerge.entrySet())
            put(entry.getKey(), entry.getValue());
    }

    @Override
    public void clear() {
        env.clear();
    }

    @Override
    public Set<String> keySet() {
        return env.table.keySet();
    }

    @Override
    public Collection<Object> values() {
        return Collections.singleton(env.table.values());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> result = new HashSet<>();
        for (Entry<String, Token> entry : env.table.entrySet()) {
            Entry<String, Object> newEntry = new Entry<>() {
                @Override
                public String getKey() {
                    return entry.getKey();
                }

                @Override
                public Object getValue() {
                    return entry.getValue();
                }

                @Override
                public Object setValue(Object value) {
                    throw new UnsupportedOperationException("setValue is not supported.");
                }
            };
            result.add(newEntry);
        }
        return result;
    }

    @Override
    public int size() {
        return env.table.size();
    }

    @Override
    public boolean isEmpty() {
        return env.table.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return env.table.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return env.table.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return env.get(key.toString());
    }

    @Override
    public Object remove(Object key) {
        return env.remove(key.toString());
    }
}
