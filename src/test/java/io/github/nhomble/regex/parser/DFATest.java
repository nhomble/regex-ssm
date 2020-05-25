package io.github.nhomble.regex.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DFATest {

    @Test
    void and() {
        DFA dfa = DFA.parse("ab");
        assertEquals("" +
                "start=0\n" +
                "end={[3]}\n" +
                "from={Pair{state=2, signal=a}} to=2\n" +
                "from={Pair{state=3, signal=b}} to=2\n" +
                "from={Pair{state=1, signal=a}} to=2\n" +
                "from={Pair{state=2, signal=b}} to=2\n" +
                "from={Pair{state=0, signal=a}} to=1\n" +
                "from={Pair{state=1, signal=b}} to=3\n" +
                "from={Pair{state=0, signal=b}} to=2\n" +
                "from={Pair{state=3, signal=a}} to=2\n", dfa.toString());
    }
}
