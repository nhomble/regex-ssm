package io.github.nhomble.regex.matcher;

import io.github.nhomble.regex.parser.SpringStateMachineRegexParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpringStateMachineMatcherTest {

    private SpringStateMachineRegexParser parser;

    @BeforeEach
    void setup() {
        parser = new SpringStateMachineRegexParser();
    }

    @Test
    void and() {
        RegexMatcher machineMatcher = parser.parse("ab");
        assertTrue(machineMatcher.matches("ab"));
        assertFalse(machineMatcher.matches("a"));
        assertFalse(machineMatcher.matches("ba"));
    }

    @Test
    void star() {
        RegexMatcher matcher = parser.parse("ab*");
        assertTrue(matcher.matches("ab"));
        assertTrue(matcher.matches("a"));
        assertTrue(matcher.matches("abb"));
        assertFalse(matcher.matches("ac"));
    }

    @Test
    void question() {
        RegexMatcher matcher = parser.parse("ab?");
        assertTrue(matcher.matches("a"));
        assertTrue(matcher.matches("ab"));
        assertFalse(matcher.matches("abb"));
    }

    @Test
    void or(){
        RegexMatcher matcher = parser.parse("A|B");
        assertTrue(matcher.matches("A"));
        assertTrue(matcher.matches("B"));
        assertFalse(matcher.matches("AB"));
    }

    @Test
    void uat(){
        RegexMatcher matcher = parser.parse("(a|b)*c?");
        assertTrue(matcher.matches("aaaaaa"));
        assertTrue(matcher.matches("bbbbbb"));
        assertTrue(matcher.matches("aabbbbc"));
        assertFalse(matcher.matches("cc"));
    }
}
