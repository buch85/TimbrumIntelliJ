package it.buch85.timbrum;

/**
 * Created by Marco on 19/08/2014.
 */
public enum VersoTimbratura {
    ENTRATA("E"), USCITA("U");
    private String code;

    VersoTimbratura(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
