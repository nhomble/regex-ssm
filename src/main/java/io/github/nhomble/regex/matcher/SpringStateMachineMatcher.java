package io.github.nhomble.regex.matcher;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.Set;

@Slf4j
public class SpringStateMachineMatcher<S> implements RegexMatcher {

    public static final String FINAL_STATES = "_regexFinalStates";

    private final StateMachine<S, String> fsm;

    public SpringStateMachineMatcher(StateMachine<S, String> fsm) {
        Preconditions.checkArgument(fsm.getExtendedState().getVariables().containsKey(FINAL_STATES));
        this.fsm = fsm;
        this.fsm.addStateListener(new LoggingSMListener<>());
    }

    public boolean matches(String input) {
        Preconditions.checkNotNull(input);
        fsm.start();
        for (String s : input.split("")) {
            if (s.length() > 0) {
                if (!fsm.sendEvent(s)) {
                    fsm.stop();
                    return false;
                }
            }
        }
        fsm.stop();
        return fsm.getExtendedState().get(FINAL_STATES, Set.class).contains(fsm.getState().getId());
    }

    static class LoggingSMListener<S> extends StateMachineListenerAdapter<S, String> {
        @Override
        public void stateChanged(State<S, String> from, State<S, String> to) {
            log.info("State transition has happened! to={}", to.getId());
        }

        @Override
        public void eventNotAccepted(Message<String> event) {
            log.info("Event was not accepted! event={}", event.getPayload());
        }
    }
}
