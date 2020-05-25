package io.github.nhomble.regex.parser;

import com.google.common.base.Preconditions;
import io.github.nhomble.regex.parser.exceptions.MissingSymbolException;
import io.github.nhomble.regex.parser.exceptions.RegexParseException;
import lombok.Getter;


import java.util.*;
import java.util.function.Predicate;

import static io.github.nhomble.regex.parser.ParseTree.Type.*;

/**
 * Creates another representation of the string regex into a tree for later transformations
 */
@Getter
public class ParseTree {

    private final ParseComponent component;

    ParseTree(ParseComponent component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return component.toString();
    }

    @Getter
    static class ParseComponent {
        private final ParseComponent left, right;
        private final Type type;
        private final String value;

        protected ParseComponent(ParseComponent left, ParseComponent right, Type type, String value) {
            this.left = left;
            this.right = right;
            this.type = type;
            this.value = value;
        }

        protected ParseComponent(String value) {
            this(null, null, CHAR, value);
        }

        protected ParseComponent(ParseComponent parseComponent, Type type) {
            this(parseComponent, null, type, null);
        }

        protected ParseComponent(ParseComponent left, ParseComponent right, Type type) {
            this(left, right, type, null);
        }

        @Override
        public String toString() {
            String c = type == CHAR ? value : type.character;
            return Optional.ofNullable(left).map(l -> "(" + l.toString() + ")").orElse("")
                    + c
                    + Optional.ofNullable(right).map(r -> "(" + r.toString() + ")").orElse("");
        }
    }

    enum Type {
        CHAR(null),
        STAR("*"),
        QUESTION("?"),
        CONCAT("."),
        OR("|");

        String character;

        Type(String character) {
            this.character = character;
        }
    }

    public static ParseTree parse(String regex) {
        Preconditions.checkNotNull(regex);
        List<String> characters = Arrays.asList(regex.split(""));
        characters = preprocess(characters);

        Queue<String> q = new LinkedList<>(characters);

        return new ParseTree(parseExpression(q));
    }

    private static ParseComponent parseCharacter(Queue<String> q) {
        String character = q.poll();
        if (character != null && Character.isLetterOrDigit(character.toCharArray()[0])) {
            return new ParseComponent(character);
        } else {
            throw new RegexParseException("Expected alphanum character, but got=" + character);
        }
    }

    private static ParseComponent parseAtom(Queue<String> q) {
        if ("(".equals(q.peek())) {
            q.poll();
            ParseComponent inner = parseExpression(q);
            if (!")".equals(q.poll())) {
                throw new MissingSymbolException(")");
            }
            return inner;
        } else {
            return parseCharacter(q);
        }
    }

    private static ParseComponent parseRepresentation(Queue<String> q) {
        ParseComponent atom = parseAtom(q);
        String peeked = q.peek();
        if ("*".equals(peeked)) {
            q.poll();
            return new ParseComponent(atom, STAR);
        } else if ("?".equals(peeked)) {
            q.poll();
            return new ParseComponent(atom, QUESTION);
        } else {
            return atom;
        }
    }

    private static ParseComponent parseConcatenation(Queue<String> q) {
        ParseComponent rep = parseRepresentation(q);
        if (".".equals(q.peek())) {
            q.poll();
            ParseComponent toConcat = parseConcatenation(q);
            return new ParseComponent(rep, toConcat, CONCAT);
        } else {
            return rep;
        }
    }

    private static ParseComponent parseExpression(Queue<String> q) {
        ParseComponent component = parseConcatenation(q);
        if ("|".equals(q.peek())) {
            q.poll();
            ParseComponent inner = parseExpression(q);
            return new ParseComponent(component, inner, OR);
        } else {
            return component;
        }
    }

    static List<String> preprocess(List<String> characters) {
        int curr = 0;
        int next = curr + 1;
        List<String> out = new ArrayList<>();
        while (next != characters.size()) {
            final char currentCharacter = characters.get(curr).toCharArray()[0];
            final char nextCharacter = characters.get(next).toCharArray()[0];
            out.add(characters.get(curr));

            Predicate<Character> isSymbolOrEnd = c -> Character.isLetterOrDigit(c) || c == ')' || c == '*' || c == '?';
            Predicate<Character> notOp = c -> c != ')' && c != '|' && c != '*' && c != '+' && c != '?';

            if (isSymbolOrEnd.test(currentCharacter) && notOp.test(nextCharacter)) {
                out.add(".");
            }

            curr++;
            next++;
        }
        out.add(characters.get(curr));
        return out;
    }
}
