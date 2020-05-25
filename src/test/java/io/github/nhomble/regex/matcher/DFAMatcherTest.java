package io.github.nhomble.regex.matcher;

import io.github.nhomble.regex.parser.DFARegexParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DFAMatcherTest {

    private DFARegexParser parser = new DFARegexParser();

    @Test
    void and() {
        RegexMatcher matcher = parser.parse("ab");
        assertTrue(matcher.matches("ab"));
        assertFalse(matcher.matches("a"));
        assertFalse(matcher.matches("c"));
    }

    @Test
    void or() {
        RegexMatcher matcher = parser.parse("a|b");
        assertTrue(matcher.matches("a"));
        assertTrue(matcher.matches("b"));
        assertFalse(matcher.matches("ab"));
        assertFalse(matcher.matches("c"));
    }

    @Test
    void star(){
        RegexMatcher matcher = parser.parse("a*");
        assertTrue(matcher.matches("a"));
        assertTrue(matcher.matches("aa"));
        assertTrue(matcher.matches(""));
        assertFalse(matcher.matches("c"));
        assertFalse(matcher.matches("ac"));
    }

    @Test
    void question(){
        RegexMatcher matcher = parser.parse("a?");
        assertTrue(matcher.matches("a"));
        assertTrue(matcher.matches(""));
        assertFalse(matcher.matches("aa"));
    }
}
