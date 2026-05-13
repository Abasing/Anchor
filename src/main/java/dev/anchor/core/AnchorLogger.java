package dev.anchor.core;

public interface AnchorLogger {

    void info(String message);

    void warn(String message);

    void error(String message);

    void error(String message, Throwable throwable);

    void debug(String message);
}
