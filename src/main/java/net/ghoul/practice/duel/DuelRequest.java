package net.ghoul.practice.duel;

import lombok.Getter;
import lombok.Setter;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.kit.Kit;

import java.util.UUID;

public class DuelRequest {

    @Getter
    private final UUID sender;
    @Getter
    private final boolean party;
    @Getter
    @Setter
    private Kit kit;
    @Getter
    @Setter
    private Arena arena;
    private final long timestamp = System.currentTimeMillis();

    DuelRequest(UUID sender, boolean party) {
        this.sender = sender;
        this.party = party;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.timestamp >= 30_000;
    }

}
