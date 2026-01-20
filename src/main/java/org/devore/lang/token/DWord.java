package org.devore.lang.token;

/**
 * 关键字
 */
public class DWord extends DToken {
    public static final DWord LB = DWord.valueOf(WordTags.LB);     // (
    public static final DWord RB = DWord.valueOf(WordTags.RB);     // )
    public static final DWord NIL = DWord.valueOf(WordTags.NIL);   // nil
    private final WordTags tag;

    private DWord(WordTags tag) {
        this.tag = tag;
    }

    private static DWord valueOf(WordTags tag) {
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
    public DToken copy() {
        return DWord.valueOf(this.tag);
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

    private enum WordTags {
        LB, RB, NIL
    }
}
