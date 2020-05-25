package io.github.nhomble.regex.parser;

import io.github.nhomble.regex.parser.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseTreeTest {

    @ParameterizedTest
    @CsvSource(value = {
            "'',''",
            "ab,a.b",
            "a|b,a|b",
            "(a|ab)*c+,(a|a.b)*.c+"
    })
    void preprocess(String in, String out) {
        assertEquals(out, String.join("", ParseTree.preprocess(Arrays.asList(in.split("")))));
    }

    @Nested
    class TreeParse {
        @Test
        void simpleConcat() {
            ParseTree tree = ParseTree.parse("abc");
            Assertions.assertEquals("a", tree.getComponent().getLeft().getValue());
            Assertions.assertEquals("b", tree.getComponent().getRight().getLeft().getValue());
            Assertions.assertEquals("c", tree.getComponent().getRight().getRight().getValue());
        }

        @Test
        void simpleOr() {
            ParseTree tree = ParseTree.parse("a|bc");
            Assertions.assertEquals("a", tree.getComponent().getLeft().getValue());
            Assertions.assertEquals("b", tree.getComponent().getRight().getLeft().getValue());
            Assertions.assertEquals("c", tree.getComponent().getRight().getRight().getValue());
        }

        @Test
        void uat() {
            ParseTree tree = ParseTree.parse("(a|b)*abb");
            Assertions.assertEquals("(((a)|(b))*).((a).((b).(b)))", tree.toString());
        }
    }
}
