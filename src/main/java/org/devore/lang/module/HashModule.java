package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.lang.Env;
import org.devore.lang.token.DList;
import org.devore.lang.token.DString;
import org.devore.utils.DByteUtils;
import org.devore.utils.HashUtils;

public class HashModule extends Module {
    public HashModule() {
        super("hash");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("md5", ((args, env) -> {
            if (args.get(0) instanceof DString)
                return DString.valueOf(HashUtils.hash(args.toString(), "MD5"));
            else if (args.get(0) instanceof DList)
                return DString.valueOf(HashUtils.hash(DByteUtils.toBytes((DList) args.get(0)), "MD5"));
            throw new DevoreCastException(args.get(0).type(), "string|list");
        }), 1, false);
        dEnv.addTokenProcedure("sha1", ((args, env) -> {
            if (args.get(0) instanceof DString)
                return DString.valueOf(HashUtils.hash(args.toString(), "SHA-1"));
            else if (args.get(0) instanceof DList)
                return DString.valueOf(HashUtils.hash(DByteUtils.toBytes((DList) args.get(0)), "SHA-1"));
            throw new DevoreCastException(args.get(0).type(), "string|list");
        }), 1, false);
        dEnv.addTokenProcedure("sha256", ((args, env) -> {
            if (args.get(0) instanceof DString)
                return DString.valueOf(HashUtils.hash(args.toString(), "SHA-256"));
            else if (args.get(0) instanceof DList)
                return DString.valueOf(HashUtils.hash(DByteUtils.toBytes((DList) args.get(0)), "SHA-256"));
            throw new DevoreCastException(args.get(0).type(), "string|list");
        }), 1, false);
        dEnv.addTokenProcedure("sha512", ((args, env) -> {
            if (args.get(0) instanceof DString)
                return DString.valueOf(HashUtils.hash(args.toString(), "SHA-512"));
            else if (args.get(0) instanceof DList)
                return DString.valueOf(HashUtils.hash(DByteUtils.toBytes((DList) args.get(0)), "SHA-512"));
            throw new DevoreCastException(args.get(0).type(), "string|list");
        }), 1, false);
    }
}
