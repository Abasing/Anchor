package dev.anchor.core;

public interface AnchorService {

    boolean isAvailable();

    String getProviderName();

    ServiceStatus getStatus();
}
