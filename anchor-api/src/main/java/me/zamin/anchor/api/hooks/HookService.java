package me.zamin.anchor.api.hooks;

import java.util.Collection;
import java.util.Optional;
import me.zamin.anchor.api.AnchorService;

public interface HookService extends AnchorService {

    Collection<HookStatus> all();

    Optional<HookStatus> find(String hookName);
}
