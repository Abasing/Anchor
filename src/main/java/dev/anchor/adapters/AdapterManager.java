package dev.anchor.adapters;

import java.util.Collection;

public interface AdapterManager {

    void register(Adapter adapter);

    void enableAll();

    void disableAll();

    Collection<Adapter> adapters();
}
