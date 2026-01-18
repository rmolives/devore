package org.devore.lang.token;

/**
 * 关键字
 */
public class DWord extends Token {
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
        switch (tag) {
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
    public Token copy() {
        return DWord.valueOf(tag);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DWord && ((DWord) t).tag.equals(this.tag) ? 0 : -1;
    }

    private enum WordTags {
        LB, RB, NIL
    }
}
