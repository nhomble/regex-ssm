package io.github.nhomble.regex.parser;

import com.google.common.base.Preconditions;
import io.github.nhomble.regex.matcher.DFAMatcher;
import io.github.nhomble.regex.matcher.RegexMatcher;

public class DFARegexParser implements RegexParser {
    @Override
    public RegexMatcher parse(String regex) {
        Preconditions.checkArgument(regex != null && regex.length() > 0);
        return new DFAMatcher(DFA.parse(regex));
    }
}
