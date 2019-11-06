package lab1.analizator;

import java.util.List;

class Rule {

    private Enka enka;

    private String tokenName;
    private boolean goToNewLine = false;
    private String nextLexerState;
    private Integer takeOnlyNChars;

    Rule(Enka enka, List<String> actions) {
        this.enka = enka;
        parseActions(actions);
    }

    private void parseActions(List<String> actions) {
        tokenName = (actions.get(0).equals("-")) ? null : actions.get(0);

        for (int i = 1; i < actions.size(); i++) {
            String[] parts = actions.get(i).split("\\s");
            switch (parts[0]) {
                case "UDJI_U_STANJE":
                    nextLexerState = parts[1];
                    break;
                case "NOVI_REDAK":
                    goToNewLine = true;
                    break;
                case "VRATI_SE":
                    takeOnlyNChars = Integer.parseInt(parts[1].trim());
                    break;
                default:
                    break;
            }
        }
    }


    Enka getEnka() {
        return enka;
    }

    String getTokenName() {
        return tokenName;
    }

    boolean isGoToNewLine() {
        return goToNewLine;
    }

    String getNextLexerState() {
        return nextLexerState;
    }

    Integer getTakeOnlyNChars() {
        return takeOnlyNChars;
    }

}
