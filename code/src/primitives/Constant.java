package primitives;

public class Constant extends Term {

    public Constant(String label) {
        this.label = label;
    }

    @Override
//    public String toString() {
//        return "\"" + label + "\"";
//    }
    public String toString() {
        return label;
    }

}
