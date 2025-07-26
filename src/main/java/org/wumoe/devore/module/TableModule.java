package org.wumoe.devore.module;

import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.token.DInt;
import org.wumoe.devore.lang.token.DList;
import org.wumoe.devore.lang.token.DTable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 表相关模块
 */
public class TableModule extends Module {
    @Override
    public void init(Env dEnv) {
        dEnv.addTokenFunction("table-new", ((args, env) -> DTable.valueOf(new HashMap<>())), 0, false);
        dEnv.addTokenFunction("table-get", ((args, env) -> {
            if (!(args.get(0) instanceof DTable table))
                throw new DevoreCastException(args.get(0).type(), "table");
            return table.get(args.get(1));
        }), 2, false);
        dEnv.addTokenFunction("table-size", ((args, env) -> {
            if (!(args.get(0) instanceof DTable table))
                throw new DevoreCastException(args.get(0).type(), "table");
            return DInt.valueOf(table.size());
        }), 1, false);
        dEnv.addTokenFunction("table-put", ((args, env) -> {
            if (!(args.get(0) instanceof DTable table))
                throw new DevoreCastException(args.get(0).type(), "table");
            return table.put(args.get(1), args.get(2), false);
        }), 3, false);
        dEnv.addTokenFunction("table-put!", ((args, env) -> {
            if (!(args.get(0) instanceof DTable table))
                throw new DevoreCastException(args.get(0).type(), "table");
            return table.put(args.get(1), args.get(2), true);
        }), 3, false);
        dEnv.addTokenFunction("table-remove", ((args, env) -> {
            if (!(args.get(0) instanceof DTable table))
                throw new DevoreCastException(args.get(0).type(), "table");
            return table.remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenFunction("table-remove!", ((args, env) -> {
            if (!(args.get(0) instanceof DTable table))
                throw new DevoreCastException(args.get(0).type(), "table");
            return table.remove(args.get(1), true);
        }), 2, false);
        dEnv.addTokenFunction("table-keys", ((args, env) -> {
            if (!(args.get(0) instanceof DTable table))
                throw new DevoreCastException(args.get(0).type(), "table");
            return DList.valueOf(new ArrayList<>(table.keys()));
        }), 1, false);
    }
}
