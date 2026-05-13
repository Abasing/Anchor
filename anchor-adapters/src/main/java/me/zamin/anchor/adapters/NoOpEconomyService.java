package me.zamin.anchor.adapters;

import java.text.DecimalFormat;
import java.util.UUID;
import me.zamin.anchor.api.ServiceStatus;
import me.zamin.anchor.api.economy.EconomyService;

public final class NoOpEconomyService implements EconomyService {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.00");

    @Override
    public double getBalance(UUID playerId) {
        return 0.0D;
    }

    @Override
    public void deposit(UUID playerId, double amount) {
    }

    @Override
    public void withdraw(UUID playerId, double amount) {
    }

    @Override
    public boolean has(UUID playerId, double amount) {
        return false;
    }

    @Override
    public String format(double amount) {
        return FORMAT.format(amount);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String providerName() {
        return "Unavailable";
    }

    @Override
    public ServiceStatus status() {
        return ServiceStatus.UNAVAILABLE;
    }
}
