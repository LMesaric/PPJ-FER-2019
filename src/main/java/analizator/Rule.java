package analizator;

import java.util.List;

public class Rule {

    private String state;
    private Enka enka;
    private List<String> actions;

    public Rule(String state, Enka enka, List<String> actions) {
        this.state = state;
        this.enka = enka;
        this.actions = actions;
    }

    public String getState() {
        return state;
    }

    public Enka getEnka() {
        return enka;
    }

    public List<String> getActions() {
        return actions;
    }
}
