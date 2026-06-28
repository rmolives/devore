package org.devore.lang;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.module.*;
import org.devore.lang.token.DMacro;
import org.devore.lang.token.DProcedure;
import org.devore.lang.token.DToken;
import org.devore.parser.Ast;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Env {
    // 默认导入
    public static final List<String> defaultModules = Collections.unmodifiableList(Arrays.asList("core"));

    public final Map<String, DToken> table;             // 环境表
    public Env father;                                  // 父环境
    public final IOConfig io;                           // IO表
    public final Map<String, Env> importEnvTable;       // 导入的环境

    // 模块表
    public final Map<String, DModule> modules = Stream.of(
            new AbstractMap.SimpleEntry<>("binary", new BinaryModule()),
            new AbstractMap.SimpleEntry<>("base64", new Base64Module()),
            new AbstractMap.SimpleEntry<>("core", new CoreModule()),
            new AbstractMap.SimpleEntry<>("csv", new CsvModule()),
            new AbstractMap.SimpleEntry<>("file", new FileModule()),
            new AbstractMap.SimpleEntry<>("crypto", new CryptoModule()),
            new AbstractMap.SimpleEntry<>("html", new HtmlModule()),
            new AbstractMap.SimpleEntry<>("http", new HttpModule()),
            new AbstractMap.SimpleEntry<>("json", new JsonModule()),
            new AbstractMap.SimpleEntry<>("net", new NetModule()),
            new AbstractMap.SimpleEntry<>("os", new OSModule()),
            new AbstractMap.SimpleEntry<>("properties", new PropertiesModule()),
            new AbstractMap.SimpleEntry<>("regex", new RegexModule()),
            new AbstractMap.SimpleEntry<>("reflect", new ReflectModule()),
            new AbstractMap.SimpleEntry<>("sign", new SignModule()),
            new AbstractMap.SimpleEntry<>("thread", new ThreadModule()),
            new AbstractMap.SimpleEntry<>("tcp", new TCPModule()),
            new AbstractMap.SimpleEntry<>("udp", new UDPModule()),
            new AbstractMap.SimpleEntry<>("uuid", new UUIDModule()),
            new AbstractMap.SimpleEntry<>("xml", new XmlModule()),
            new AbstractMap.SimpleEntry<>("hash", new HashModule()),
            new AbstractMap.SimpleEntry<>("zip", new ZipModule())
    ).collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (left, right) -> right,
            ConcurrentHashMap::new
    ));

    /**
     * 创建环境
     *
     * @param table  环境表
     * @param father 父环境
     * @param io     IO表
     */
    public Env(Map<String, DToken> table, Env father, Map<String, Env> importEnvTable, IOConfig io, boolean load) {
        this.table = concurrentMap(table);
        this.father = father;
        this.io = io;
        this.importEnvTable = concurrentMap(importEnvTable);
        if (load)
            defaultModules.forEach(this::loadModule);
    }

    private static <K, V> Map<K, V> concurrentMap(Map<K, V> map) {
        if (map instanceof ConcurrentMap)
            return map;
        return new ConcurrentHashMap<>(map);
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
     */
    public synchronized void addModule(DModule module) {
        this.modules.put(module.name, module);
    }

    /**
     * 添加import导入的环境
     *
     * @param key key
     * @param env 环境
     */
    public synchronized void putImportEnv(String key, Env env) {
        if (this.table.containsKey(key) || this.importEnvTable.containsKey(key))
            throw new DevoreRuntimeException("定义冲突: " + key);
        this.importEnvTable.put(key, env);
    }

    /**
     * 添加import导入的环境
     *
     * @param key key
     * @return 环境
     */
    public Env getImportEnv(String key) {
        Env temp = this;
        while (temp != null && !temp.importEnvTable.containsKey(key))
            temp = temp.father;
        if (temp == null)
            throw new DevoreRuntimeException("未定义: " + key);
        return appendFather(temp.importEnvTable.get(key), this);
    }

    private static Env appendFather(Env imported, Env current) {
        if (imported == null)
            return current;
        return new Env(imported.table, appendFather(imported.father, current),
                imported.importEnvTable, imported.io, false);
    }

    private Env findTableEnv(String key) {
        Env temp = this;
        while (temp != null) {
            if (temp.table.containsKey(key))
                return temp;
            temp = temp.father;
        }
        return null;
    }

    private Env findImportEnv(String key) {
        Env temp = this;
        while (temp != null) {
            if (temp.importEnvTable.containsKey(key))
                return temp;
            temp = temp.father;
        }
        return null;
    }

    private Env rootEnv() {
        Env temp = this;
        while (temp.father != null)
            temp = temp.father;
        return temp;
    }

    private DToken findVisibleTokenForLocalDefinition(String key) {
        Env visibleEnv = this.father == null ? null : this.father.findTableEnv(key);
        if (visibleEnv != null)
            return visibleEnv.table.get(key);
        Env importEnv = findImportEnv(key);
        if (importEnv != null)
            return importEnv.importEnvTable.get(key).get(key);
        return null;
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
     */
    public synchronized void put(String key, DToken value) {
        if (this.table.containsKey(key) || this.importEnvTable.containsKey(key))
            throw new DevoreRuntimeException("定义冲突: " + key);
        this.table.put(key, value);
    }

    /**
     * 添加宏
     *
     * @param key    key
     * @param params params
     * @param bodys  bodys
     */
    public synchronized void addMacro(String key, List<String> params, List<Ast> bodys) {
        DMacro newMacro = DMacro.newMacro(key, params, bodys);
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DMacro))
                throw new DevoreRuntimeException("定义冲突: " + key);
            this.table.put(key, ((DMacro) token).addMacro(key, newMacro));
            return;
        }
        DToken visibleToken = findVisibleTokenForLocalDefinition(key);
        if (visibleToken instanceof DMacro) {
            this.table.put(key, ((DMacro) visibleToken).copy().setMacro(newMacro));
            return;
        }
        this.table.put(key, newMacro);
    }

    /**
     * 更改宏
     *
     * @param key    key
     * @param params params
     * @param bodys  bodys
     */
    public synchronized void setMacro(String key, List<String> params, List<Ast> bodys) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        DMacro macro = DMacro.newMacro(key, params, bodys);
        DToken token = temp.table.get(key);
        if (token instanceof DMacro)
            macro = ((DMacro) token).setMacro(macro);
        temp.table.put(key, macro);
    }

    /**
     * 添加Ast过程
     *
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     */
    public synchronized void addAstProcedure(String key, BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        DProcedure newProcedure = DProcedure.newProcedure(key, procedure, argc, vararg);
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DProcedure))
                throw new DevoreRuntimeException("定义冲突: " + key);
            this.table.put(key, ((DProcedure) token).addProcedure(key, newProcedure));
            return;
        }
        DToken visibleToken = findVisibleTokenForLocalDefinition(key);
        if (visibleToken instanceof DProcedure) {
            this.table.put(key, ((DProcedure) visibleToken).copy().setProcedure(newProcedure));
            return;
        }
        this.table.put(key, newProcedure);
    }

    /**
     * 添加普通过程
     *
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     */
    public synchronized void addTokenProcedure(String key, BiFunction<List<DToken>, Env, DToken> procedure, int argc, boolean vararg) {
        BiFunction<Ast, Env, DToken> df = (ast, env) -> {
            List<DToken> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).symbol = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).symbol);
            }
            return procedure.apply(args, env);
        };
        DProcedure newProcedure = DProcedure.newProcedure(key, df, argc, vararg);
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DProcedure))
                throw new DevoreRuntimeException("定义冲突: " + key);
            this.table.put(key, ((DProcedure) token).addProcedure(key, newProcedure));
            return;
        }
        DToken visibleToken = findVisibleTokenForLocalDefinition(key);
        if (visibleToken instanceof DProcedure) {
            this.table.put(key, ((DProcedure) visibleToken).copy().setProcedure(newProcedure));
            return;
        }
        this.table.put(key, newProcedure);
    }

    /**
     * 更改Ast过程
     *
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     */
    public synchronized void setAstProcedure(String key, BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        DProcedure df = DProcedure.newProcedure(key, procedure, argc, vararg);
        DToken token = temp.table.get(key);
        if (token instanceof DProcedure)
            df = ((DProcedure) token).setProcedure(df);
        temp.table.put(key, df);
    }

    /**
     * 更改普通过程
     *
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     */
    public synchronized void setTokenProcedure(String key, BiFunction<List<DToken>, Env, DToken> procedure, int argc, boolean vararg) {
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
    }

    /**
     * 更改KY对
     *
     * @param key   key
     * @param value value
     */
    public void set(String key, DToken value) {
        Env tableEnv = findTableEnv(key);
        if (tableEnv != null) {
            tableEnv.table.put(key, value);
            return;
        }
        rootEnv().table.put(key, value);
    }

    /**
     * 查看是否包含特定key
     *
     * @param key key
     * @return 结果
     */
    public boolean contains(String key) {
        return findTableEnv(key) != null || findImportEnv(key) != null;
    }

    /**
     * 查看环境是否包含特定import key
     *
     * @param key key
     * @return 结果
     */
    public boolean containsImport(String key) {
        return findTableEnv(key) == null && findImportEnv(key) != null;
    }

    /**
     * 获取key对应的value
     *
     * @param key key
     * @return value
     */
    public DToken get(String key) {
        Env tableEnv = findTableEnv(key);
        if (tableEnv != null)
            return tableEnv.table.get(key);
        Env importEnv = findImportEnv(key);
        if (importEnv != null)
            return importEnv.importEnvTable.get(key).get(key);
        throw new DevoreRuntimeException("未定义: " + key);
    }

    /**
     * 删除KY对
     *
     * @param key key
     */
    public void remove(String key) {
        Env tableEnv = findTableEnv(key);
        if (tableEnv != null) {
            tableEnv.table.remove(key);
            return;
        }
        Env importEnv = findImportEnv(key);
        if (importEnv != null)
            importEnv.importEnvTable.remove(key);
    }

    /**
     * 清空环境
     */
    public synchronized void clear() {
        this.table.clear();
        this.importEnvTable.clear();
    }

    /**
     * 获取环境所有key
     *
     * @return 所有key
     */
    public Set<String> keySet() {
        Set<String> keys = new LinkedHashSet<>();
        Env temp = this;
        while (temp != null) {
            keys.addAll(temp.table.keySet());
            keys.addAll(temp.importEnvTable.keySet());
            temp = temp.father;
        }
        return keys;
    }

    /**
     * 创建子环境
     *
     * @return 子环境
     */
    public Env createChild() {
        return new Env(new HashMap<>(), this, new HashMap<>(), this.io, false);
    }
}
