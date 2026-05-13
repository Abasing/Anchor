package dev.anchor.gui;

public final class AnchorGui {

    private AnchorGui() {
    }

    public static GuiBuilder builder() {
        return new GuiBuilder();
    }
}
