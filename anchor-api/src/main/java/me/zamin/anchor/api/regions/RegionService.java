package me.zamin.anchor.api.regions;

import java.util.Set;
import me.zamin.anchor.api.AnchorService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface RegionService extends AnchorService {

    boolean canBuild(Player player, Location location);

    boolean canBreak(Player player, Location location);

    boolean canInteract(Player player, Location location);

    boolean canPvp(Player player, Location location);

    boolean isInRegion(Location location, String regionId);

    Set<String> getRegionsAt(Location location);
}
