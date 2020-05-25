package io.github.nhomble.regex.parser;

import io.github.nhomble.regex.matcher.RegexMatcher;

public interface RegexParser {

    RegexMatcher parse(String regex);
}
