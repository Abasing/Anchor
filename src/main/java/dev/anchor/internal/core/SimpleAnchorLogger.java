package dev.anchor.internal.core;

import dev.anchor.core.AnchorLogger;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleAnchorLogger implements AnchorLogger {

    private final Logger logger;
    private final boolean debugEnabled;

    public SimpleAnchorLogger(Logger logger, boolean debugEnabled) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.debugEnabled = debugEnabled;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    @Override
    public void debug(String message) {
        if (debugEnabled) {
            logger.info("[debug] " + message);
        }
    }
}
