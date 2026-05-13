package dev.anchor.economy;

import dev.anchor.core.ProviderPriority;
import dev.anchor.core.ServiceStatus;
import java.text.DecimalFormat;
import java.util.UUID;

public final class NoOpEconomyProvider implements EconomyProvider {

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
    public String getProviderName() {
        return "Unavailable";
    }

    @Override
    public ServiceStatus getStatus() {
        return ServiceStatus.UNAVAILABLE;
    }

    @Override
    public ProviderPriority getPriority() {
        return ProviderPriority.LOWEST;
    }
}
