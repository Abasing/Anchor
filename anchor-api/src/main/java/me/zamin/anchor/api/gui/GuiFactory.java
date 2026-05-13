package me.zamin.anchor.api.gui;

import me.zamin.anchor.api.AnchorService;

public interface GuiFactory extends AnchorService {

    GuiBuilder builder();
}
