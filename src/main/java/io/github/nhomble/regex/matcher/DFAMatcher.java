package io.github.nhomble.regex.matcher;

import com.google.common.base.Preconditions;
import io.github.nhomble.regex.parser.DFA;

public class DFAMatcher implements RegexMatcher {

    private final DFA dfa;

    public DFAMatcher(DFA dfa) {
        this.dfa = dfa;
    }

    @Override
    public boolean matches(String input) {
        Preconditions.checkNotNull(input);
        int currState = dfa.getStart();

        for (String s : input.split("")) {
            if (s.length() > 0) {
                currState = dfa.next(currState, s);
            }
        }

        return dfa.isFinal(currState);
    }
}
