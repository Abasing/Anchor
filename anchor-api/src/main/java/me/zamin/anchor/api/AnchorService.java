package me.zamin.anchor.api;

public interface AnchorService {

    boolean isAvailable();

    String providerName();

    ServiceStatus status();
}
