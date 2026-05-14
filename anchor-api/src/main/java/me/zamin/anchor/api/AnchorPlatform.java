package me.zamin.anchor.api;

/**
 * Detected runtime platform information for the current server.
 *
 * @param serverName server implementation name
 * @param serverVersion full server version string
 * @param bukkitVersion Bukkit API version string
 * @param minecraftVersion Minecraft version string
 * @param javaVersion Java runtime version string
 * @param paper whether Paper-compatible APIs were detected
 * @param folia whether Folia runtime classes were detected
 */
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
