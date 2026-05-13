package dev.anchor.economy;

import dev.anchor.core.ProviderPriority;
import dev.anchor.core.ServiceStatus;
import java.util.Objects;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class VaultEconomyProvider implements EconomyProvider {

    private final Economy economy;

    public VaultEconomyProvider(Economy economy) {
        this.economy = Objects.requireNonNull(economy, "economy");
    }

    @Override
    public double getBalance(UUID playerId) {
        return economy.getBalance(resolvePlayer(playerId));
    }

    @Override
    public void deposit(UUID playerId, double amount) {
        economy.depositPlayer(resolvePlayer(playerId), amount);
    }

    @Override
    public void withdraw(UUID playerId, double amount) {
        economy.withdrawPlayer(resolvePlayer(playerId), amount);
    }

    @Override
    public boolean has(UUID playerId, double amount) {
        return economy.has(resolvePlayer(playerId), amount);
    }

    @Override
    public String format(double amount) {
        return economy.format(amount);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getProviderName() {
        return "Vault/" + economy.getName();
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.AVAILABLE;
    }

    @Override
    public ProviderPriority getPriority() {
        return ProviderPriority.HIGH;
    }

    private OfflinePlayer resolvePlayer(UUID playerId) {
        return Bukkit.getOfflinePlayer(playerId);
    }
}
