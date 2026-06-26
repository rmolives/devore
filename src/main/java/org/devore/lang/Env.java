package org.devore.lang;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.module.*;
import org.devore.lang.token.DMacro;
import org.devore.lang.token.DProcedure;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;
import org.devore.parser.Ast;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Env {
    // 默认导入
    public static final List<String> defaultModules = new ArrayList<>(Arrays.asList("core"));

    public final Map<String, DToken> table;         // 环境表
    public Env father;                              // 父环境
    public final IOConfig io;                       // IO表
    public final Map<String, Env> importEnvs;       // 导入的环境

    // 模块表
    public final Map<String, DModule> modules = Stream.of(
            new AbstractMap.SimpleEntry<>("binary", new BinaryModule()),
            new AbstractMap.SimpleEntry<>("core", new CoreModule()),
            new AbstractMap.SimpleEntry<>("file", new FileModule()),
            new AbstractMap.SimpleEntry<>("http", new HttpModule()),
            new AbstractMap.SimpleEntry<>("json", new JsonModule()),
            new AbstractMap.SimpleEntry<>("thread", new ThreadModule()),
            new AbstractMap.SimpleEntry<>("hash", new HashModule())
    ).collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
    ));

    /**
     * 创建环境
     *
     * @param table  环境表
     * @param father 父环境
     * @param io     IO表
     */
    public Env(Map<String, DToken> table, Env father, Map<String, Env> importEnvs, IOConfig io, boolean load) {
        this.table = table;
        this.father = father;
        this.io = io;
        this.importEnvs = importEnvs;
        if (load)
            defaultModules.forEach(this::loadModule);
    }

    /**
     * 加载环境
     *
     * @param name 名字
     */
    public void loadModule(String name) {
        if (!this.modules.containsKey(name))
            throw new DevoreRuntimeException("模块 " + name + " 不存在.");
        loadModule(this.modules.get(name));
    }

    /**
     * 加载环境
     *
     * @param module 模块
     */
    public void loadModule(DModule module) {
        module.init(this);
    }

    /**
     * 添加模块
     *
     * @param module 环境
     * @return 环境
     */
    public Env addModule(DModule module) {
        this.modules.put(module.name, module);
        return this;
    }

    /**
     * 添加import导入的环境
     *
     * @param key key
     * @param env 环境
     * @return 环境
     */
    public Env putImportEnv(String key, Env env) {
        if (this.importEnvs.containsKey(key))
            throw new DevoreRuntimeException("定义冲突: " + key);
        this.importEnvs.put(key, env);
        return this;
    }

    /**
     * 添加import导入的环境
     *
     * @param key key
     * @return 环境
     */
    public Env getImportEnv(String key) {
        Env temp = this;
        while (temp != null && !temp.importEnvs.containsKey(key))
            temp = temp.father;
        if (temp == null)
            throw new DevoreRuntimeException("未定义: " + key);
        return appendFather(temp.importEnvs.get(key), this);
    }

    private static Env appendFather(Env imported, Env current) {
        if (imported == null)
            return current;
        return new Env(imported.table, appendFather(imported.father, current),
                imported.importEnvs, imported.io, false);
    }

    /**
     * 创建环境
     *
     * @return 环境
     */
    public static Env newEnv() {
        return new Env(new HashMap<>(), null, new HashMap<>(), new IOConfig(), true);
    }

    /**
     * 创建环境
     *
     * @param father 父环境
     * @param io     IO表
     * @return 环境
     */
    public static Env newEnv(Env father, IOConfig io) {
        return new Env(new HashMap<>(), father, new HashMap<>(), io, true);
    }

    /**
     * 创建环境
     *
     * @param io IO表
     * @return 环境
     */
    public static Env newEnv(IOConfig io) {
        return new Env(new HashMap<>(), null, new HashMap<>(), io, true);
    }

    /**
     * 设置KY对
     *
     * @param key   key
     * @param value value
     * @return 环境
     */
    public Env put(String key, DToken value) {
        if (this.table.containsKey(key))
            throw new DevoreRuntimeException("定义冲突: " + key);
        this.table.put(key, value);
        return this;
    }

    /**
     * 添加宏
     *
     * @param key    key
     * @param params params
     * @param bodys  bodys
     * @return 环境
     */
    public Env addMacro(String key, List<String> params, List<Ast> bodys) {
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DMacro))
                throw new DevoreRuntimeException("定义冲突: " + key);
            this.table.put(key, ((DMacro) token).addMacro(key, DMacro.newMacro(key, params, bodys)));
            return this;
        }
        this.table.put(key, DMacro.newMacro(key, params, bodys));
        return this;
    }

    /**
     * 更改宏
     *
     * @param key    key
     * @param params params
     * @param bodys  bodys
     * @return 环境
     */
    public Env setMacro(String key, List<String> params, List<Ast> bodys) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        DMacro macro = DMacro.newMacro(key, params, bodys);
        DToken token = temp.table.get(key);
        if (token instanceof DMacro)
            macro = ((DMacro) token).setMacro(macro);
        temp.table.put(key, macro);
        return this;
    }

    /**
     * 添加Ast过程
     *
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return 环境
     */
    public Env addAstProcedure(String key, BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DProcedure))
                throw new DevoreRuntimeException("定义冲突: " + key);
            this.table.put(key, ((DProcedure) token).addProcedure(key, DProcedure.newProcedure(key, procedure, argc, vararg)));
            return this;
        }
        this.table.put(key, DProcedure.newProcedure(key, procedure, argc, vararg));
        return this;
    }

    /**
     * 添加普通过程
     *
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return 环境
     */
    public Env addTokenProcedure(String key, BiFunction<List<DToken>, Env, DToken> procedure, int argc, boolean vararg) {
        BiFunction<Ast, Env, DToken> df = (ast, env) -> {
            List<DToken> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).symbol = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).symbol);
            }
            return procedure.apply(args, env);
        };
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DProcedure))
                throw new DevoreRuntimeException("定义冲突: " + key);
            this.table.put(key, ((DProcedure) token).addProcedure(key, DProcedure.newProcedure(key, df, argc, vararg)));
            return this;
        }
        this.table.put(key, DProcedure.newProcedure(key, df, argc, vararg));
        return this;
    }

    /**
     * 更改Ast过程
     *
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return 环境
     */
    public Env setAstProcedure(String key, BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        DProcedure df = DProcedure.newProcedure(key, procedure, argc, vararg);
        DToken token = temp.table.get(key);
        if (token instanceof DProcedure)
            df = ((DProcedure) token).setProcedure(df);
        temp.table.put(key, df);
        return this;
    }

    /**
     * 更改普通过程
     *
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return 环境
     */
    public Env setTokenProcedure(String key, BiFunction<List<DToken>, Env, DToken> procedure, int argc, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        BiFunction<Ast, Env, DToken> df = (ast, env) -> {
            List<DToken> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).symbol = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).symbol);
            }
            return procedure.apply(args, env);
        };
        DProcedure newProcedure = DProcedure.newProcedure(key, df, argc, vararg);
        DToken token = temp.table.get(key);
        if (token instanceof DProcedure)
            newProcedure = ((DProcedure) token).setProcedure(newProcedure);
        temp.table.put(key, newProcedure);
        return this;
    }

    /**
     * 更改KY对
     *
     * @param key   key
     * @param value value
     * @return 环境
     */
    public Env set(String key, DToken value) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, value);
        return this;
    }

    /**
     * 查看是否包含特定key
     *
     * @param key key
     * @return 结果
     */
    public boolean contains(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        return temp.table.containsKey(key);
    }

    /**
     * 查看环境是否包含特定import key
     *
     * @param key key
     * @return 结果
     */
    public boolean containsImport(String key) {
        Env temp = this;
        while (temp.father != null && !temp.importEnvs.containsKey(key))
            temp = temp.father;
        return temp.importEnvs.containsKey(key);
    }

    /**
     * 获取key对应的value
     *
     * @param key key
     * @return value
     */
    public DToken get(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        if (!temp.table.containsKey(key))
            throw new DevoreRuntimeException("未定义: " + key);
        return temp.table.get(key);
    }

    /**
     * 删除KY对
     *
     * @param key key
     * @return 删除的value
     */
    public DToken remove(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        return temp.table.containsKey(key) ? temp.table.remove(key) : DWord.NIL;
    }

    /**
     * 清空环境
     */
    public void clear() {
        this.table.clear();
    }

    /**
     * 创建子环境
     *
     * @return 子环境
     */
    public Env createChild() {
        return newEnv(this, this.io);
    }
}
