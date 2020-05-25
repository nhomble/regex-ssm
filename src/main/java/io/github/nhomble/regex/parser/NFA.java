package io.github.nhomble.regex.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Thompson construction from ParseTree
 */
public class NFA {

    private static final String EPS = null;
    private static final String NONE = "";

    private int initialState;
    private int finalState;
    private String[][] table;
    private final Set<String> legalSignals;

    NFA(int initialState, int finalState) {
        this.initialState = initialState;
        this.finalState = finalState;
        table = new String[2][];
        legalSignals = new HashSet<>();

        // init table for basic
        for (int i = 0; i < 2; i++) {
            table[i] = new String[]{NONE, NONE};
        }
    }

    NFA(NFA other) {
        this.initialState = other.initialState;
        this.finalState = other.finalState;
        this.table = other.table;
        this.legalSignals = other.legalSignals;
    }

    private int size() {
        return table.length;
    }

    /**
     * add to our table of transitions
     *
     * @param fromState
     * @param toState
     * @param signal
     * @return
     */
    private NFA addTransition(int fromState, int toState, String signal) {
        table[fromState][toState] = signal;

        if (signal != null && signal.length() > 0) {
            legalSignals.add(signal);
        }
        return this;
    }

    /**
     * make room in the table for new states - this happens as we combine primitive NFAs
     *
     * @param shift
     * @return
     */
    private NFA expand(int shift) {
        if (shift < 0) {
            return this;
        }
        int newSize = size() + shift;
        String[][] largerTable = new String[newSize][];
        for (int i = 0; i < newSize; i++) {
            String[] empty = new String[newSize];
            for (int j = 0; j < newSize; j++) {
                empty[j] = NONE;
            }
            largerTable[i] = empty;
        }

        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                largerTable[i + shift][j + shift] = table[i][j];
            }
        }
        table = largerTable;
        initialState += shift;
        finalState += shift;
        return this;
    }

    private NFA appendEmptyState() {
        int newSize = size() + 1;
        String[][] newTable = new String[newSize][newSize];
        for (int i = 0; i < newTable.length; i++) {
            for (int j = 0; j < newTable[i].length; j++) {
                if (i == table.length || j == table.length) {
                    newTable[i][j] = NONE;
                } else {
                    newTable[i][j] = table[i][j];
                }
            }
        }
        return this;
    }

    private NFA adopt(NFA other) {
        for (int i = 0; i < other.table.length; i++) {
            System.arraycopy(other.table[i], 0, this.table[i], 0, other.table[i].length);
        }
        legalSignals.addAll(other.legalSignals);
        return this;
    }

    private NFA setInitial(int state) {
        this.initialState = state;
        return this;
    }

    private NFA setFinal(int state) {
        this.finalState = state;
        return this;
    }

    public Set<String> getSignals() {
        return ImmutableSet.copyOf(legalSignals);
    }

    public Set<Integer> reachable(Integer from, String with) {
        Set<Integer> ret = new HashSet<>();
        for (int i = 0; i < table[from].length; i++) {
            if (with == EPS) {
                if (with == table[from][i]) {
                    ret.add(i);
                }
            } else {
                if (with.equals(table[from][i])) {
                    ret.add(i);
                }
            }
        }
        return ret;
    }

    public Set<Integer> reachable(Set<Integer> states, String with) {
        return states.stream()
                .flatMap(i -> reachable(i, with).stream())
                .collect(Collectors.toSet());
    }

    public Set<Integer> epsReachable(int from) {
        return reachable(from, EPS);
    }

    public int getInitialState() {
        return initialState;
    }

    public int getFinalState() {
        return finalState;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int from = 0; from < size(); from++) {
            for (int to = 0; to < size(); to++) {
                if (!NONE.equals(table[from][to])) {
                    builder
                            .append("From=").append(from)
                            .append(" to=").append(to)
                            .append(" by signal=").append(table[from][to] == EPS ? "EPS" : table[from][to])
                            .append("\n");
                }
            }
        }
        return builder.toString();
    }

    /**
     * convenience wrapper around a string that uses the intermediate {@link ParseTree} representation to do the parsing
     * into {@link NFA}
     *
     * @param regex
     * @return
     */
    public static NFA parseTree(String regex) {
        ParseTree tree = ParseTree.parse(regex);
        return parseTree(tree.getComponent());
    }

    /**
     * Check if given signal is an eps signal which allows us to hide the eps implementation outside of this parser
     *
     * @param s
     * @return
     */
    public static boolean isEps(String s) {
        return EPS == s;
    }

    private static NFA parseTree(ParseTree.ParseComponent parseComponent) {
        switch (parseComponent.getType()) {
            case CHAR:
                return parseChar(parseComponent.getValue());
            case CONCAT:
                return parseConcat(parseTree(parseComponent.getLeft()), parseTree(parseComponent.getRight()));
            case OR:
                return parseOr(parseTree(parseComponent.getLeft()), parseTree(parseComponent.getRight()));
            case QUESTION:
                return parseOr(parseTree(parseComponent.getLeft()), parseChar(EPS));
            case STAR:
                return parseStar(parseTree(parseComponent.getLeft()));
            default:
                throw new IllegalStateException();
        }
    }

    private static NFA parseChar(String data) {
        return new NFA(0, 1)
                .addTransition(0, 1, data);
    }

    private static NFA parseConcat(NFA left, NFA right) {
        right.expand(left.size() - 1);
        return new NFA(right)
                .adopt(left)
                .setInitial(left.initialState);
    }

    private static NFA parseStar(NFA single) {
        return single.expand(1)
                .appendEmptyState()
                .addTransition(single.finalState, single.initialState, EPS)
                .addTransition(0, single.initialState, EPS)
                .addTransition(single.finalState, single.size() - 1, EPS)
                .addTransition(0, single.size() - 1, EPS)
                .setInitial(0)
                .setFinal(single.size() - 1);
    }

    private static NFA parseOr(NFA left, NFA right) {
        left.expand(1); // room for initial state
        right.expand(left.size());
        NFA toRet = new NFA(right)
                .adopt(left)
                .addTransition(0, left.initialState, EPS)
                .addTransition(0, right.initialState, EPS)
                .setInitial(0)
                .appendEmptyState();

        return toRet
                .setFinal(toRet.size() - 1)
                .addTransition(left.finalState, toRet.finalState, EPS)
                .addTransition(right.finalState, toRet.finalState, EPS);
    }
}
