package dev.anchor.core;

public interface AnchorProvider {

    String getProviderName();

    ProviderPriority getPriority();
}
