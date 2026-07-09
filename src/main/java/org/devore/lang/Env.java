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

/**
 * Devore运行环境
 */
public class Env {
    /**
     * 默认加载模块
     */
    public static final List<String> defaultModules = Collections.unmodifiableList(Arrays.asList("core"));

    /**
     * 当前环境符号表
     */
    public final Map<String, DToken> table;

    /**
     * 父环境
     */
    public Env father;

    /**
     * 当前环境直接设置的安全限制
     */
    public DSecurity security;

    /**
     * IO配置
     */
    public final IOConfig io;

    /**
     * 可加载模块表
     */
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
            new AbstractMap.SimpleEntry<>("math", new MathModule()),
            new AbstractMap.SimpleEntry<>("net", new NetModule()),
            new AbstractMap.SimpleEntry<>("os", new OSModule()),
            new AbstractMap.SimpleEntry<>("properties", new PropertiesModule()),
            new AbstractMap.SimpleEntry<>("regex", new RegexModule()),
            new AbstractMap.SimpleEntry<>("reflect", new ReflectModule()),
            new AbstractMap.SimpleEntry<>("security", new SecurityModule()),
            new AbstractMap.SimpleEntry<>("thread", new ThreadModule()),
            new AbstractMap.SimpleEntry<>("time", new TimeModule()),
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
     * @param table          环境符号表
     * @param father         父环境
     * @param io             IO配置
     * @param security       安全限制
     * @param load           是否加载默认模块
     */
    public Env(Map<String, DToken> table, Env father, IOConfig io,
               DSecurity security, boolean load) {
        this.table = concurrentMap(table);
        this.father = father;
        this.io = io;
        this.security = security;
        if (load)
            defaultModules.forEach(this::loadModule);
    }

    /**
     * 将普通Map转换为并发Map
     *
     * @param map 原Map
     * @param <K> key类型
     * @param <V> value类型
     * @return 并发Map
     */
    private static <K, V> Map<K, V> concurrentMap(Map<K, V> map) {
        if (map instanceof ConcurrentMap)
            return map;
        return new ConcurrentHashMap<>(map);
    }

    /**
     * 设置当前环境安全限制
     *
     * @param security 安全限制
     */
    public void setSecurity(DSecurity security) {
        this.security = security;
    }

    /**
     * 按名称加载模块
     *
     * @param name 模块名
     */
    public void loadModule(String name) {
        if (!this.modules.containsKey(name))
            throw new DevoreRuntimeException("模块 " + name + " 不存在.");
        loadModule(this.modules.get(name));
    }

    /**
     * 加载模块实例
     *
     * @param module 模块
     */
    public void loadModule(DModule module) {
        module.init(this);
    }

    /**
     * 添加模块
     *
     * @param module 模块
     */
    public synchronized void addModule(DModule module) {
        this.modules.put(module.name, module);
    }

    /**
     * 沿父环境查找包含指定符号的环境
     *
     * @param key 符号名
     * @return 包含符号的环境，不存在时返回null
     */
    private Env findTableEnv(String key) {
        Env temp = this;
        while (temp != null) {
            if (temp.table.containsKey(key))
                return temp;
            temp = temp.father;
        }
        return null;
    }

    /**
     * 获取根环境
     *
     * @return 根环境
     */
    private Env rootEnv() {
        Env temp = this;
        while (temp.father != null)
            temp = temp.father;
        return temp;
    }

    /**
     * 查找当前局部绑定可见的同名宏或过程
     *
     * @param key 符号名
     * @return 可见Token，不存在时返回null
     */
    private DToken findVisibleTokenForLocalDefinition(String key) {
        Env visibleEnv = this.father == null ? null : this.father.findTableEnv(key);
        if (visibleEnv != null)
            return visibleEnv.table.get(key);
        return null;
    }

    /**
     * 创建环境
     *
     * @return 环境
     */
    public static Env newEnv() {
        return new Env(new HashMap<>(), null, new IOConfig(), new DSecurity(new ArrayList<>()), true);
    }

    /**
     * 创建环境
     *
     * @param father 父环境
     * @param io     IO表
     * @return 环境
     */
    public static Env newEnv(Env father, IOConfig io) {
        return new Env(new HashMap<>(), father, io, new DSecurity(new ArrayList<>()), true);
    }

    /**
     * 创建环境
     *
     * @param io IO表
     * @return 环境
     */
    public static Env newEnv(IOConfig io) {
        return new Env(new HashMap<>(), null, io, new DSecurity(new ArrayList<>()), true);
    }

    /**
     * 设置当前环境键值对
     *
     * @param key   符号名
     * @param value Token值
     */
    public synchronized void put(String key, DToken value) {
        if (this.table.containsKey(key))
            throw new DevoreRuntimeException("绑定冲突: " + key);
        this.table.put(key, value);
    }

    /**
     * 添加宏
     *
     * @param key   宏名
     * @param macro 宏
     */
    public synchronized void addMacro(String key, DMacro macro) {
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DMacro))
                throw new DevoreRuntimeException("绑定冲突: " + key);
            this.table.put(key, ((DMacro) token).addMacro(key, macro));
            return;
        }
        DToken visibleToken = findVisibleTokenForLocalDefinition(key);
        if (visibleToken instanceof DMacro) {
            this.table.put(key, ((DMacro) visibleToken).copy().setMacro(macro));
            return;
        }
        this.table.put(key, macro);
    }

    /**
     * 添加宏
     *
     * @param key    宏名
     * @param params 参数名列表
     * @param bodys  宏体列表
     */
    public synchronized void addMacro(String key, List<String> params, List<Ast> bodys) {
        DMacro newMacro = DMacro.newMacro(key, params, bodys);
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DMacro))
                throw new DevoreRuntimeException("绑定冲突: " + key);
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
     * @param key    宏名
     * @param params 参数名列表
     * @param bodys  宏体列表
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
     * @param key       过程名
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     */
    public synchronized void addAstProcedure(String key, BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        DProcedure newProcedure = DProcedure.newProcedure(key, procedure, argc, vararg);
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DProcedure))
                throw new DevoreRuntimeException("绑定冲突: " + key);
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
     * @param key       过程名
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     */
    public synchronized void addTokenProcedure(String key, BiFunction<List<DToken>, Env, DToken> procedure, int argc, boolean vararg) {
        BiFunction<Ast, Env, DToken> df = (ast, env) -> procedure.apply(ast.children.stream()
                .map(token -> Evaluator.eval(env, token.copy()))
                .collect(Collectors.toList()), env);
        DProcedure newProcedure = DProcedure.newProcedure(key, df, argc, vararg);
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DProcedure))
                throw new DevoreRuntimeException("绑定冲突: " + key);
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
     * 添加过程
     *
     * @param key       过程名
     * @param procedure 过程
     */
    public synchronized void addProcedure(String key, DProcedure procedure) {
        if (this.table.containsKey(key)) {
            DToken token = this.table.get(key);
            if (!(token instanceof DProcedure))
                throw new DevoreRuntimeException("绑定冲突: " + key);
            this.table.put(key, ((DProcedure) token).addProcedure(key, procedure));
            return;
        }
        DToken visibleToken = findVisibleTokenForLocalDefinition(key);
        if (visibleToken instanceof DProcedure) {
            this.table.put(key, ((DProcedure) visibleToken).copy().setProcedure(procedure));
            return;
        }
        this.table.put(key, procedure);
    }

    /**
     * 更改Ast过程
     *
     * @param key       过程名
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
     * @param key       过程名
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     */
    public synchronized void setTokenProcedure(String key, BiFunction<List<DToken>, Env, DToken> procedure, int argc, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        BiFunction<Ast, Env, DToken> df = (ast, env) -> procedure.apply(ast.children.stream()
                .map(token -> Evaluator.eval(env, token.copy()))
                .collect(Collectors.toList()), env);
        DProcedure newProcedure = DProcedure.newProcedure(key, df, argc, vararg);
        DToken token = temp.table.get(key);
        if (token instanceof DProcedure)
            newProcedure = ((DProcedure) token).setProcedure(newProcedure);
        temp.table.put(key, newProcedure);
    }

    /**
     * 更改键值对，不存在时写入根环境
     *
     * @param key   符号名
     * @param value Token值
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
     * @param key 符号名
     * @return 是否包含指定符号
     */
    public boolean contains(String key) {
        return findTableEnv(key) != null;
    }

    /**
     * 获取key对应的value
     *
     * @param key 符号名
     * @return Token值
     */
    public DToken get(String key) {
        Env tableEnv = findTableEnv(key);
        if (tableEnv != null)
            return tableEnv.table.get(key);
        throw new DevoreRuntimeException("未绑定: " + key);
    }

    /**
     * 删除键值对或导入环境
     *
     * @param key 符号名
     */
    public void remove(String key) {
        Env tableEnv = findTableEnv(key);
        if (tableEnv != null)
            tableEnv.table.remove(key);
    }

    /**
     * 清空环境
     */
    public synchronized void clear() {
        this.table.clear();
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
        return new Env(new HashMap<>(), this, this.io, new DSecurity(new ArrayList<>()), false);
    }
}
