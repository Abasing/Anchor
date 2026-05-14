package me.zamin.anchor.api.gui;

import me.zamin.anchor.api.AnchorService;

/**
 * Factory for building safe inventory GUIs through Anchor.
 */
public interface GuiFactory extends AnchorService {

    /**
     * Creates a new GUI builder.
     *
     * @return non-null builder
     */
    GuiBuilder builder();
}
