package dev.anchor.core;

public interface AnchorModule {

    String getModuleName();

    void enable();

    void disable();
}
