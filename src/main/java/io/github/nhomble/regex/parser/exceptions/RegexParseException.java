package io.github.nhomble.regex.parser.exceptions;

public class RegexParseException extends RuntimeException {
    public RegexParseException(String msg) {
        super("Parsing exception msg='" + msg + "'");
    }
}
