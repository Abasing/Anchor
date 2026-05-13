package dev.anchor.core;

public class AnchorException extends RuntimeException {

    public AnchorException(String message) {
        super(message);
    }

    public AnchorException(String message, Throwable cause) {
        super(message, cause);
    }
}
