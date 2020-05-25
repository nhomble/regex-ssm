package io.github.nhomble.regex.parser.exceptions;

public class MissingSymbolException extends RuntimeException {
    public MissingSymbolException(String symbol){
        super("Missing symbol='" + symbol + "'");
    }
}
