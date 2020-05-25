package io.github.nhomble.regex.parser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.*;

public class DFA {

    private static final int OFF_STATE = -1;

    private final Map<Pair, Integer> transitions;
    private final int start;
    private final Set<Integer> finalStates;

    DFA(Map<Pair, Integer> transitions, int start, Set<Integer> finalStates) {
        this.transitions = transitions;
        this.start = start;
        this.finalStates = finalStates;
    }

    public int getStart() {
        return start;
    }

    public void accept(DFAVisitor visitor) {
        transitions.forEach((pair, to) -> {
            visitor.visit(pair.state, pair.signal, to);
        });
    }

    public int next(int state, String input) {
        Pair pair = new Pair(state, input);
        return transitions.getOrDefault(pair, OFF_STATE);
    }

    public boolean isFinal(int state) {
        return finalStates.contains(state);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("start=").append(start).append("\n");
        builder.append("end={").append(finalStates).append("}\n");
        transitions.forEach((p, to) -> {
            builder.append("from={").append(p).append("} to=").append(to).append("\n");
        });
        return builder.toString();
    }

    public interface DFAVisitor {
        void visit(int from, String signal, int to);
    }

    /**
     * Basically a graph traversal on the NFA where the edges are just epsilon transitions
     *
     * @param nfa
     * @param states
     * @return
     */
    private static DFAState epsClosure(NFA nfa, Set<Integer> states) {
        Stack<Integer> stack = new Stack<>();
        stack.addAll(states);

        Set<Integer> closure = new HashSet<>(states);

        while (!stack.isEmpty()) {
            Integer curr = stack.pop();
            Set<Integer> reachable = nfa.epsReachable(curr);
            for (Integer reach : reachable) {
                if (!closure.contains(reach)) {
                    stack.push(reach);
                    closure.add(reach);
                }
            }
        }
        return new DFAState(closure);
    }

    /**
     * For clarity, we wrap a set of NFA states into a single DFA state object.
     */
    static class DFAState {
        private final Set<Integer> nfaStates;

        DFAState(Set<Integer> nfaStates) {
            this.nfaStates = ImmutableSet.copyOf(nfaStates);
        }

        public Set<Integer> getNfaStates() {
            return nfaStates;
        }

        public boolean contains(int state) {
            return nfaStates.contains(state);
        }

        @Override
        public int hashCode() {
            return nfaStates.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof DFAState)) {
                return false;
            }
            return Optional.ofNullable(nfaStates).orElse(new HashSet<>()).equals(((DFAState) other).nfaStates);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(DFAState.class)
                    .add("states", nfaStates)
                    .toString();
        }
    }

    static class Pair {
        private final int state;
        private final String signal;

        Pair(int state, String signal) {
            this.state = state;
            this.signal = signal;
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, signal);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pair)) {
                return false;
            }
            Pair other = (Pair) o;
            return state == other.state && signal.equals(other.signal);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Pair.class)
                    .add("state", state)
                    .add("signal", signal)
                    .toString();
        }
    }

    public static DFA parse(String regex) {
        Preconditions.checkArgument(regex != null && regex.length() > 0);
        NFA nfa = NFA.parseTree(regex);

        Map<Pair, Integer> transitions = new HashMap<>();
        int stateCounter = 0;
        Map<DFAState, Integer> dfaMap = new HashMap<>();
        int dfaStart;
        Set<Integer> finalStates = new HashSet<>();
        Set<DFAState> seen = new HashSet<>();
        Queue<DFAState> unseen = new LinkedList<>();

        DFAState initial = epsClosure(nfa, Collections.singleton(nfa.getInitialState()));
        unseen.add(initial);
        dfaStart = stateCounter++;
        dfaMap.put(initial, dfaStart);

        while (!unseen.isEmpty()) {
            DFAState curr = unseen.poll();
            seen.add(curr);

            if (curr.contains(nfa.getFinalState())) {
                finalStates.add(dfaMap.get(curr));
            }

            for (String input : nfa.getSignals()) {
                DFAState next = epsClosure(nfa, nfa.reachable(curr.getNfaStates(), input));
                if (!seen.contains(next) && !unseen.contains(next)) {
                    unseen.add(next);
                    dfaMap.put(next, stateCounter++);
                }

                transitions.put(new Pair(dfaMap.get(curr), input), dfaMap.get(next));
            }
        }
        return new DFA(transitions, dfaStart, finalStates);
    }
}
