package org.wumoe.devore.api.scripting;

import org.wumoe.devore.Devore;
import org.wumoe.devore.exception.DevoreScriptException;
import org.wumoe.devore.lang.Env;

import javax.script.*;
import java.io.*;

public class DevoreScriptEngine implements ScriptEngine {
    private DevoreScriptContext context = new DevoreScriptContext(new DevoreScriptBindings(Env.newEnv()), new InputStreamReader(System.in), new OutputStreamWriter(System.out), new OutputStreamWriter(System.err));

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return Devore.call(((DevoreScriptBindings) context.getBindings(0)).env, script);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return Devore.call(((DevoreScriptBindings) context.getBindings(0)).env, readAllString(reader));
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return Devore.call(((DevoreScriptBindings) context.getBindings(0)).env, script);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return Devore.call(((DevoreScriptBindings) context.getBindings(0)).env, readAllString(reader));
    }

    @Override
    public Object eval(String script, Bindings n) throws ScriptException {
        if (n instanceof DevoreScriptBindings d)
            return Devore.call(d.env, script);
        else
            throw new DevoreScriptException("bindings并不是DevoreScriptBindings.");
    }

    @Override
    public Object eval(Reader reader, Bindings n) throws ScriptException {
        if (n instanceof DevoreScriptBindings d)
            return Devore.call(d.env, readAllString(reader));
        else
            throw new DevoreScriptException("bindings并不是DevoreScriptBindings.");
    }

    @Override
    public void put(String key, Object value) {
        context.bindings.put(key, value);
    }

    @Override
    public Object get(String key) {
        return context.bindings.get(key);
    }

    @Override
    public Bindings getBindings(int scope) {
        return context.bindings;
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        if (bindings instanceof DevoreScriptBindings d)
            context.bindings = d;
        else
            throw new DevoreScriptException("bindings并不是DevoreScriptBindings.");
    }

    @Override
    public Bindings createBindings() {
        return new DevoreScriptBindings(Env.newEnv());
    }

    @Override
    public ScriptContext getContext() {
        return context;
    }

    @Override
    public void setContext(ScriptContext context) {
        if (context instanceof DevoreScriptContext d)
            this.context = d;
        else
            throw new DevoreScriptException("context并不是DevoreScriptContext.");
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new DevoreScriptEngineFactory();
    }

    private static String readAllString(Reader reader) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[1024];
        int numCharsRead;
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            while ((numCharsRead = bufferedReader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, numCharsRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }
}
