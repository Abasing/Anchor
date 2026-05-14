package me.zamin.anchor.api.gui;

import java.util.UUID;

/**
 * Represents a live GUI session opened through Anchor.
 */
public interface GuiSession {

    /**
     * Returns the viewer UUID for this session.
     *
     * @return non-null viewer UUID
     */
    UUID viewerId();
}
