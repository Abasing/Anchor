package me.zamin.anchor.api.economy;

import java.util.UUID;
import me.zamin.anchor.api.AnchorService;

/**
 * Stable abstraction over server economy providers such as Vault-backed
 * economy implementations.
 * <p>
 * All methods accept non-null player UUIDs. When the provider is unavailable,
 * implementations must fail safely through fallback behavior instead of
 * crashing dependent plugins.
 */
public interface EconomyService extends AnchorService {

    /**
     * Returns a player's balance.
     * <p>
     * Safe on Paper and Folia. This method does not guarantee main-thread-only
     * execution requirements for third-party providers, so callers should avoid
     * invoking it from uncontrolled async contexts unless they know their
     * provider is safe.
     *
     * @param playerId non-null player UUID
     * @return current balance, or {@code 0.0} when unavailable fallback is active
     */
    double getBalance(UUID playerId);

    /**
     * Deposits funds to a player.
     *
     * @param playerId non-null player UUID
     * @param amount amount to deposit
     */
    void deposit(UUID playerId, double amount);

    /**
     * Withdraws funds from a player.
     *
     * @param playerId non-null player UUID
     * @param amount amount to withdraw
     */
    void withdraw(UUID playerId, double amount);

    /**
     * Returns whether the player has at least the supplied amount.
     *
     * @param playerId non-null player UUID
     * @param amount amount to test
     * @return {@code true} if the player can afford the amount
     */
    boolean has(UUID playerId, double amount);

    /**
     * Formats an amount using the active provider when available.
     *
     * @param amount amount to format
     * @return non-null formatted string
     */
    String format(double amount);
}
