package io.github.nhomble.regex.commands;

import io.github.nhomble.regex.matcher.RegexMatcher;
import io.github.nhomble.regex.parser.RegexParser;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

@ShellComponent
@ShellCommandGroup
public class RegexCommands {

    private final RegexParser parser;
    private RegexMatcher matcher;

    public RegexCommands(RegexParser parser) {
        this.parser = parser;
    }

    @ShellMethod("parse provided regex")
    public void regex(String regex) {
        matcher = parser.parse(regex);
    }

    @ShellCommandGroup
    @ShellComponent
    public class RegexMatchCommands {

        @ShellMethodAvailability
        public Availability hasParser() {
            return matcher != null ? Availability.available() : Availability.unavailable("provide a regex!");
        }

        @ShellMethod("check if provided string matches previously provided regex")
        public void check(String s) {
            boolean result = matcher.matches(s);
            if (result) {
                System.out.println("MATCHES");
            } else {
                System.out.println("DOES NOT MATCH");
            }
        }
    }

}
