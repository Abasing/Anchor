package dev.anchor.internal.adapters;

import dev.anchor.adapters.Adapter;
import dev.anchor.adapters.AdapterManager;
import dev.anchor.core.AnchorLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SimpleAdapterManager implements AdapterManager {

    private final List<Adapter> adapters = new ArrayList<>();
    private final AnchorLogger logger;

    public SimpleAdapterManager(AnchorLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void register(Adapter adapter) {
        adapters.add(Objects.requireNonNull(adapter, "adapter"));
    }

    @Override
    public void enableAll() {
        for (Adapter adapter : adapters) {
            try {
                adapter.enable();
            } catch (Throwable throwable) {
                if (adapter instanceof AbstractProviderAdapter<?> providerAdapter) {
                    providerAdapter.markFailed();
                }
                logger.error("Failed to enable adapter " + adapter.getName(), throwable);
            }
        }
    }

    @Override
    public void disableAll() {
        List<Adapter> copy = new ArrayList<>(adapters);
        Collections.reverse(copy);
        for (Adapter adapter : copy) {
            try {
                adapter.disable();
            } catch (Throwable throwable) {
                logger.error("Failed to disable adapter " + adapter.getName(), throwable);
            }
        }
    }

    @Override
    public Collection<Adapter> adapters() {
        return Collections.unmodifiableList(adapters);
    }
}
