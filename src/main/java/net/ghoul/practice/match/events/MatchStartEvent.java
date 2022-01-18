package net.ghoul.practice.match.events;

import lombok.Getter;
import net.ghoul.practice.match.Match;
import org.bukkit.event.HandlerList;

@Getter
public class MatchStartEvent extends MatchEvent {
    private boolean cancelled = false;
    private static HandlerList handlers = new HandlerList();

    public MatchStartEvent(final Match match) {
        super(match);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean b) {
        this.cancelled = b;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
