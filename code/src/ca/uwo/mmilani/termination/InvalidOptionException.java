package ca.uwo.mmilani.termination;

public class InvalidOptionException extends Throwable {
    public String option;
    public InvalidOptionException(String option) {
        this.option = option;
    }
}
