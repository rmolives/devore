package org.wumoe.devore.api.scripting;

import org.wumoe.devore.exception.DevoreRuntimeException;
import org.wumoe.devore.exception.DevoreScriptException;
import org.wumoe.devore.lang.token.Token;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public class DevoreScriptContext implements ScriptContext {
    public DevoreScriptBindings bindings;
    private Reader reader;
    private Writer writer;
    private Writer errorWriter;

    public DevoreScriptContext(DevoreScriptBindings bindings, Reader reader, Writer writer, Writer errorWriter) {
        this.bindings = bindings;
        this.reader = reader;
        this.writer = writer;
        this.errorWriter = errorWriter;
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        if (bindings instanceof DevoreScriptBindings d)
            this.bindings = d;
        else {
            if (bindings == null)
                throw new DevoreRuntimeException("Bindings为null.");
            this.bindings = new DevoreScriptBindings(this.bindings.env);
            this.bindings.putAll(bindings);
        }
    }

    @Override
    public Bindings getBindings(int scope) {
        return bindings;
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        bindings.env.set(name, Token.cast(value));
    }

    @Override
    public Object getAttribute(String name, int scope) {
        return getAttribute(name);
    }

    @Override
    public Object removeAttribute(String name, int scope) {
        return bindings.remove(name);
    }

    @Override
    public Object getAttribute(String name) {
        return bindings.get(name);
    }

    @Override
    public int getAttributesScope(String name) {
        throw new DevoreScriptException("不支持的操作.");
    }

    @Override
    public Writer getWriter() {
        return writer;
    }

    @Override
    public Writer getErrorWriter() {
        return errorWriter;
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void setErrorWriter(Writer writer) {
        this.errorWriter = writer;
    }

    @Override
    public Reader getReader() {
        return reader;
    }

    @Override
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public List<Integer> getScopes() {
        throw new DevoreScriptException("不支持的操作.");
    }
}
