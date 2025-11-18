package org.devore.module;

import org.devore.exception.DevoreCastException;
import org.devore.lang.Env;
import org.devore.lang.token.DInt;
import org.devore.lang.token.DList;
import org.devore.lang.token.DTable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 表相关模块
 */
public class TableModule extends Module {
    @Override
    public void init(Env dEnv) {
        dEnv.addTokenFunction("table", ((args, env) -> DTable.valueOf(new HashMap<>())), 0, false);
        dEnv.addTokenFunction("table-get", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return table.get(args.get(1));
        }), 2, false);
        dEnv.addTokenFunction("table-contains-key", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return table.containsKey(args.get(1));
        }), 2, false);
        dEnv.addTokenFunction("table-contains-value", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return table.containsValue(args.get(1));
        }), 2, false);
        dEnv.addTokenFunction("table-size", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return DInt.valueOf(table.size());
        }), 1, false);
        dEnv.addTokenFunction("table-put", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return table.put(args.get(1), args.get(2), false);
        }), 3, false);
        dEnv.addTokenFunction("table-put!", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return table.put(args.get(1), args.get(2), true);
        }), 3, false);
        dEnv.addTokenFunction("table-remove", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return table.remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenFunction("table-remove!", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return table.remove(args.get(1), true);
        }), 2, false);
        dEnv.addTokenFunction("table-keys", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable table))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return DList.valueOf(new ArrayList<>(table.keys()));
        }), 1, false);
    }
}
