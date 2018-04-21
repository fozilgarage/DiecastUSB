package diecast.fozil.com.diecast;

/**
 * Created by eduardo.benitez on 18/12/2017.
 */

public enum TextsDefault {

    UNKNOWN("Desconocido");

    private String textToShow;

    private TextsDefault(final String textToShow) {
        this.textToShow = textToShow;
    }

    public String getText() {
        return textToShow;
    }
}
