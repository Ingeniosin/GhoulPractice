package net.ghoul.practice.match.events;

import com.google.common.base.Preconditions;
import net.ghoul.practice.match.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchSpectatorLeaveEvent extends Event implements Cancellable {
    private boolean cancelled = false;
    private static HandlerList handlers = new HandlerList();
    private Match match;
    private Player spectator;

    public MatchSpectatorLeaveEvent(Player spectator, Match match) {
        this.spectator = Preconditions.checkNotNull(spectator, "spectator");
        this.match = match;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean b) {
        this.cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Match getMatch() {
        return this.match;
    }

    public Player getSpectator() {
        return spectator;
    }
}
