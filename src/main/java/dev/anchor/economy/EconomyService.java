package dev.anchor.economy;

import dev.anchor.core.AnchorService;
import java.util.UUID;

public interface EconomyService extends AnchorService {

    double getBalance(UUID playerId);

    void deposit(UUID playerId, double amount);

    void withdraw(UUID playerId, double amount);

    boolean has(UUID playerId, double amount);

    String format(double amount);
}
