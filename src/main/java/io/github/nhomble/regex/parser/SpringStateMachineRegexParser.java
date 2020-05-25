package io.github.nhomble.regex.parser;

import io.github.nhomble.regex.matcher.RegexMatcher;
import io.github.nhomble.regex.matcher.SpringStateMachineMatcher;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SpringStateMachineRegexParser implements RegexParser {

    private String prettyState(int i) {
        return "S" + i;
    }

    /**
     * Build a {@link SpringStateMachineMatcher} from {@link DFA}
     *
     * @param regex
     * @return
     */
    public RegexMatcher parse(String regex) {
        DFA dfa = DFA.parse(regex);
        StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();
        int initial = dfa.getStart();
        Set<String> finalStates = new HashSet<>();
        Set<String> states = new HashSet<>();
        Map<String, Map<String, String>> transitions = new HashMap<>();
        dfa.accept((from, signal, to) -> {
            states.add(prettyState(from));
            states.add(prettyState(to));
            if (dfa.isFinal(from)) {
                finalStates.add(prettyState(from));
            }
            if (dfa.isFinal(to)) {
                finalStates.add(prettyState(to));
            }
            Map<String, String> t = transitions.getOrDefault(prettyState(from), new HashMap<>());
            t.put(signal, prettyState(to));
            transitions.put(prettyState(from), t);
        });

        try {
            builder.configureConfiguration().withConfiguration()
                    .autoStartup(false)
                    .beanFactory(null);
            builder.configureStates().withStates()
                    .initial(prettyState(initial))
                    .states(states);

            StateMachineTransitionConfigurer<String, String> transConfig = builder.configureTransitions();
            transitions.forEach((from, t) -> t.forEach((signal, to) -> {
                try {
                    transConfig.withExternal().source(from).target(to).event(signal).and();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }));

            StateMachine<String, String> machine = builder.build();
            machine.getExtendedState().getVariables().put(SpringStateMachineMatcher.FINAL_STATES, finalStates);
            return new SpringStateMachineMatcher<>(machine);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
