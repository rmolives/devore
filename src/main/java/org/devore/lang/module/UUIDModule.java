package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;

import java.util.UUID;

public class UUIDModule extends DModule {
    public UUIDModule() {
        super("uuid");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("uuid", (args, env) ->
                DString.valueOf(UUID.randomUUID().toString()), 0, false);
        dEnv.addTokenProcedure("uuid-simple", (args, env) ->
                DString.valueOf(UUID.randomUUID().toString().replace("-", "")), 0, false);
        dEnv.addTokenProcedure("uuid-parse", (args, env) ->
                DString.valueOf(uuidArg(args.get(0)).toString()), 1, false);
        dEnv.addTokenProcedure("uuid-simple", (args, env) ->
                DString.valueOf(uuidArg(args.get(0)).toString().replace("-", "")), 1, false);
    }

    private static UUID uuidArg(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        String value = token.toString();
        if (value.matches("[0-9a-fA-F]{32}"))
            value = value.substring(0, 8) + "-"
                    + value.substring(8, 12) + "-"
                    + value.substring(12, 16) + "-"
                    + value.substring(16, 20) + "-"
                    + value.substring(20);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new DevoreRuntimeException("UUID格式错误: " + token);
        }
    }
}
