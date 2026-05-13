package me.zamin.anchor.api.economy;

import java.util.UUID;
import me.zamin.anchor.api.AnchorService;

public interface EconomyService extends AnchorService {

    double getBalance(UUID playerId);

    void deposit(UUID playerId, double amount);

    void withdraw(UUID playerId, double amount);

    boolean has(UUID playerId, double amount);

    String format(double amount);
}
