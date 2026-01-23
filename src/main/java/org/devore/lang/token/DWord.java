package org.devore.lang.token;

/**
 * 关键字
 */
public class DWord extends DToken {
    public static final DWord LB = DWord.valueOf(Tags.LB);      // (
    public static final DWord RB = DWord.valueOf(Tags.RB);      // )
    public static final DWord NIL = DWord.valueOf(Tags.NIL);    // nil
    private final Tags tag;                                     // 关键字

    private DWord(Tags tag) {
        this.tag = tag;
    }

    private static DWord valueOf(Tags tag) {
        return new DWord(tag);
    }

    @Override
    public String type() {
        return "word";
    }

    @Override
    protected String str() {
        String result = "nil";
        switch (this.tag) {
            case LB:
                result = "(";
                break;
            case RB:
                result = ")";
                break;
        }
        return result;
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DWord && this.tag.equals(((DWord) t).tag) ? 0 : -1;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.tag.hashCode();
        return result;
    }

    private enum Tags {
        LB, RB, NIL
    }
}
