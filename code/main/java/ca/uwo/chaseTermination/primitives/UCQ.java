package ca.uwo.chaseTermination.primitives;

import java.util.HashSet;
import java.util.Set;

public class UCQ extends Query<Set<Conjunct>> {
    public UCQ() {
        body = new HashSet<>();
    }

    @Override
    public String toString() {
        StringBuilder bodystr = new StringBuilder();
        body.forEach(conjunct -> bodystr.append("[").append(conjunct).append("]\n"));
        return head + "?-" + bodystr;
    }
}
