package ca.uwo.chaseTermination.primitives;

import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class ApplicablePair {
    public Rule rule;
    public Assignment assignment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicablePair that = (ApplicablePair) o;

        return (Objects.equals(assignment, that.assignment)) && (Objects.equals(rule, that.rule));
    }

    @Override
    public int hashCode() {
        int result = assignment != null ? assignment.hashCode() : 0;
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        return result;
    }
}
