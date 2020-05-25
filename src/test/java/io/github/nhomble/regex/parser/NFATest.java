package io.github.nhomble.regex.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NFATest {

    @Test
    void primitives() {
        NFA n = NFA.parseTree("ab");
        assertEquals("" +
                "From=0 to=1 by signal=a\n" +
                "From=1 to=2 by signal=b\n", n.toString());
    }

    @Test
    void or() {
        NFA n = NFA.parseTree("a|b");
        assertEquals("" +
                "From=0 to=1 by signal=EPS\n" +
                "From=0 to=3 by signal=EPS\n" +
                "From=1 to=2 by signal=a\n" +
                "From=2 to=4 by signal=EPS\n" +
                "From=3 to=4 by signal=b\n" +
                "From=4 to=4 by signal=EPS\n", n.toString());
    }

    @Test
    void star() {
        NFA n = NFA.parseTree("a*");
        assertEquals("" +
                "From=0 to=1 by signal=EPS\n" +
                "From=0 to=2 by signal=EPS\n" +
                "From=1 to=2 by signal=a\n" +
                "From=2 to=1 by signal=EPS\n" +
                "From=2 to=2 by signal=EPS\n", n.toString());
    }

    @Test
    void question(){
        NFA n = NFA.parseTree("a?");
        assertEquals("" +
                "From=0 to=1 by signal=EPS\n" +
                "From=0 to=3 by signal=EPS\n" +
                "From=1 to=2 by signal=a\n" +
                "From=2 to=4 by signal=EPS\n" +
                "From=3 to=4 by signal=EPS\n" +
                "From=4 to=4 by signal=EPS\n", n.toString());
    }
}
