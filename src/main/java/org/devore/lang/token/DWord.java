package org.devore.lang.token;

/**
 * 关键字
 */
public class DWord extends Token {
    public static final DWord LB = DWord.valueOf(WordTags.LB);     // (
    public static final DWord RB = DWord.valueOf(WordTags.RB);     // )
    public static final DWord NIL = DWord.valueOf(WordTags.NIL);   // nil
    public final WordTags tag;

    protected DWord(WordTags tag) {
        this.tag = tag;
    }

    public static DWord valueOf(WordTags tag) {
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
            case LB -> result = "(";
            case RB -> result = ")";
        }
        return result;
    }

    @Override
    public Token copy() {
        return DWord.valueOf(tag);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DWord w && w.tag.equals(this.tag) ? 0 : -1;
    }

    public enum WordTags {
        LB, RB, NIL
    }
}
