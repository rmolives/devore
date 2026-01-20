package org.devore.lang;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.DMacro;
import org.devore.lang.token.DProcedure;
import org.devore.lang.token.DWord;
import org.devore.lang.token.DToken;
import org.devore.parser.Ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class Env {
    public final Map<String, DToken> table;  // 环境表
    public final Env father;                // 父环境
    public final IOConfig io;               // IO表

    /**
     * 创建环境
     * @param table  环境表
     * @param father 父环境
     * @param io     IO表
     */
    public Env(Map<String, DToken> table, Env father, IOConfig io) {
        this.table = table;
        this.father = father;
        this.io = io;
        Core.init(this);
    }

    /**
     * 创建环境
     * @return 环境
     */
    public static Env newEnv() {
        return new Env(new HashMap<>(), null, new IOConfig());
    }

    /**
     * 创建环境
     * @param father 父环境
     * @param io     IO表
     * @return 环境
     */
    public static Env newEnv(Env father, IOConfig io) {
        return new Env(new HashMap<>(), father, io);
    }

    /**
     * 创建环境
     * @param io IO表
     * @return 环境
     */
    public static Env newEnv(IOConfig io) {
        return new Env(new HashMap<>(), null, io);
    }

    /**
     * 设置KY对
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
     * @param key   key
     * @param macro 宏
     * @return 环境
     */
    public Env addMacro(String key, DMacro macro) {
        if (this.table.containsKey(key)) {
            this.table.put(key, ((DMacro) this.table.get(key)).addMacro(macro));
            return this;
        }
        this.table.put(key, macro);
        return this;
    }

    /**
     * 设置Ast过程
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return 环境
     */
    public Env addAstProcedure(String key, BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        if (this.table.containsKey(key)) {
            this.table.put(key, ((DProcedure) this.table.get(key)).addProcedure(DProcedure.newProcedure(procedure, argc, vararg)));
            return this;
        }
        this.table.put(key, DProcedure.newProcedure(procedure, argc, vararg));
        return this;
    }

    /**
     * 添加普通过程
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
            this.table.put(key, ((DProcedure) this.table.get(key)).addProcedure(DProcedure.newProcedure(df, argc, vararg)));
            return this;
        }
        this.table.put(key, DProcedure.newProcedure(df, argc, vararg));
        return this;
    }

    /**
     * 更改Ast过程
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return 环境
     */
    public Env setAstProcedure(String key, BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key)) temp = temp.father;
        temp.table.put(key, DProcedure.newProcedure(procedure, argc, vararg));
        return this;
    }

    /**
     * 更改普通过程
     * @param key       key
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return 环境
     */
    public Env setTokenProcedure(String key, BiFunction<List<DToken>, Env, DToken> procedure, int argc, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key)) temp = temp.father;
        BiFunction<Ast, Env, DToken> df = (ast, env) -> {
            List<DToken> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).symbol = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).symbol);
            }
            return procedure.apply(args, env);
        };
        temp.table.put(key, DProcedure.newProcedure(df, argc, vararg));
        return this;
    }

    /**
     * 更改KY对
     * @param key   key
     * @param value value
     * @return 环境
     */
    public Env set(String key, DToken value) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key)) temp = temp.father;
        temp.table.put(key, value);
        return this;
    }

    /**
     * 查看是否包含特定key
     * @param key key
     * @return 结果
     */
    public boolean contains(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key)) temp = temp.father;
        return temp.table.containsKey(key);
    }

    /**
     * 获取key对应的value
     * @param key key
     * @return value
     */
    public DToken get(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key)) temp = temp.father;
        return temp.contains(key) ? temp.table.get(key) : DWord.NIL;
    }

    /**
     * 删除KY对
     * @param key key
     * @return 删除的value
     */
    public DToken remove(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key)) temp = temp.father;
        return temp.contains(key) ? temp.table.remove(key) : DWord.NIL;
    }

    /**
     * 清空环境
     */
    public void clear() {
        this.table.clear();
    }

    /**
     * 创建子环境
     * @return 子环境
     */
    public Env createChild() {
        return newEnv(this, this.io);
    }
}
