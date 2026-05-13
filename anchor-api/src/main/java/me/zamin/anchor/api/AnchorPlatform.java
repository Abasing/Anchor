package me.zamin.anchor.api;

public record AnchorPlatform(
    String serverName,
    String serverVersion,
    String bukkitVersion,
    String minecraftVersion,
    String javaVersion,
    boolean paper,
    boolean folia
) {
}
