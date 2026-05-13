package me.zamin.anchor.adapters;

import java.util.Objects;
import java.util.UUID;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.economy.EconomyService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

public final class VaultEconomyService implements EconomyService {

    private final Economy economy;

    public VaultEconomyService(Economy economy) {
        this.economy = Objects.requireNonNull(economy, "economy");
    }

    @Override
    public double getBalance(UUID playerId) {
        return economy.getBalance(Bukkit.getOfflinePlayer(playerId));
    }

    @Override
    public void deposit(UUID playerId, double amount) {
        economy.depositPlayer(Bukkit.getOfflinePlayer(playerId), amount);
    }

    @Override
    public void withdraw(UUID playerId, double amount) {
        economy.withdrawPlayer(Bukkit.getOfflinePlayer(playerId), amount);
    }

    @Override
    public boolean has(UUID playerId, double amount) {
        return economy.has(Bukkit.getOfflinePlayer(playerId), amount);
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
    public String providerName() {
        return "Vault/" + economy.getName();
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.AVAILABLE;
    }
}
