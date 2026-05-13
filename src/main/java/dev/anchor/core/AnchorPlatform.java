package dev.anchor.core;

import java.util.Objects;

public final class AnchorPlatform {

    private final String serverName;
    private final String serverVersion;
    private final String bukkitVersion;
    private final String minecraftVersion;
    private final String javaVersion;
    private final boolean paper;
    private final boolean folia;

    public AnchorPlatform(String serverName, String serverVersion, String bukkitVersion, String minecraftVersion,
                          String javaVersion, boolean paper, boolean folia) {
        this.serverName = Objects.requireNonNull(serverName, "serverName");
        this.serverVersion = Objects.requireNonNull(serverVersion, "serverVersion");
        this.bukkitVersion = Objects.requireNonNull(bukkitVersion, "bukkitVersion");
        this.minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        this.javaVersion = Objects.requireNonNull(javaVersion, "javaVersion");
        this.paper = paper;
        this.folia = folia;
    }

    public String serverName() {
        return serverName;
    }

    public String serverVersion() {
        return serverVersion;
    }

    public String bukkitVersion() {
        return bukkitVersion;
    }

    public String minecraftVersion() {
        return minecraftVersion;
    }

    public String javaVersion() {
        return javaVersion;
    }

    public boolean isPaper() {
        return paper;
    }

    public boolean isFolia() {
        return folia;
    }
}
