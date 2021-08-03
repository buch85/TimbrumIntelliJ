package it.buch85.timbrum.request;

public class RequestException extends Exception {
    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestException(String message) {
        super(message);
    }
}
