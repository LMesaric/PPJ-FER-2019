package analizator;

import java.util.List;

public class Rule {

    private Enka enka;
    private List<String> actions;

    public Rule(Enka enka, List<String> actions) {
        this.enka = enka;
        this.actions = actions;
    }

    public Enka getEnka() {
        return enka;
    }

    public List<String> getActions() {
        return actions;
    }
}
