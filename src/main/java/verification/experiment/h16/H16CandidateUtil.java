package verification.experiment.h16;

import ta.Clock;
import ta.TaLocation;
import ta.TaTransition;
import ta.TimeGuard;
import ta.ota.DOTA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class H16CandidateUtil {
    private H16CandidateUtil() {
    }

    public static DOTA buildA0(Set<String> sigma) {
        Clock clock = new Clock("c");
        Set<String> alphabet = new HashSet<>(sigma);
        TaLocation q0 = new TaLocation("0", "0", true, true);
        List<TaLocation> locations = new ArrayList<>();
        locations.add(q0);
        List<TaTransition> transitions = new ArrayList<>();
        transitions.add(buildTransition(q0, q0, "a?", "[0,+)", true, clock));
        transitions.add(buildTransition(q0, q0, "b!", "[0,+)", false, clock));
        return new DOTA("probe_a0", alphabet, locations, transitions, clock);
    }

    public static DOTA buildATight(Set<String> sigma) {
        Clock clock = new Clock("c");
        Set<String> alphabet = new HashSet<>(sigma);
        TaLocation q0 = new TaLocation("0", "0", true, true);
        List<TaLocation> locations = new ArrayList<>();
        locations.add(q0);
        List<TaTransition> transitions = new ArrayList<>();
        transitions.add(buildTransition(q0, q0, "a?", "[0,+)", true, clock));
        transitions.add(buildTransition(q0, q0, "b!", "[0,2]", false, clock));
        return new DOTA("probe_a_tight", alphabet, locations, transitions, clock);
    }

    private static TaTransition buildTransition(TaLocation source, TaLocation target, String action,
                                                String guardText, boolean reset, Clock clock) {
        Map<Clock, TimeGuard> guardMap = new HashMap<>();
        guardMap.put(clock, new TimeGuard(guardText));
        Set<Clock> resetSet = new HashSet<>();
        if (reset) {
            resetSet.add(clock);
        }
        return new TaTransition(source, target, action, guardMap, resetSet);
    }
}
