package org.wumoe.devore.api.scripting;

import org.wumoe.devore.Devore;
import org.wumoe.devore.exception.DevoreScriptException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.ArrayList;
import java.util.List;

public class DevoreScriptEngineFactory implements ScriptEngineFactory {
    @Override
    public String getEngineName() {
        return "Devore";
    }

    @Override
    public String getEngineVersion() {
        return Devore.VERSION;
    }

    @Override
    public List<String> getExtensions() {
        return new ArrayList<>() {
            {
                add("devore");
            }
        };
    }

    @Override
    public List<String> getMimeTypes() {
        return new ArrayList<>() {
            {
                add("application/devore");
                add("text/devore");
            }
        };
    }

    @Override
    public List<String> getNames() {
        return new ArrayList<>() {
            {
                add("devore");
                add("Devore");
                add("DevoreScript");
            }
        };
    }

    @Override
    public String getLanguageName() {
        return "Devore";
    }

    @Override
    public String getLanguageVersion() {
        return Devore.VERSION;
    }

    @Override
    public Object getParameter(String key) {
        Object result = null;
        switch (key) {
            case "javax.script.engine_version", "javax.script.language_version" ->
                    result = Devore.VERSION;
            case "javax.script.engine", "javax.script.language", "javax.script.name" ->
                    result = "devore";
        }
        return result;
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        throw new DevoreScriptException("不支持的操作.");
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "(print " + toDisplay + ")";
    }

    @Override
    public String getProgram(String... statements) {
        return String.join("", statements);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new DevoreScriptEngine();
    }
}
