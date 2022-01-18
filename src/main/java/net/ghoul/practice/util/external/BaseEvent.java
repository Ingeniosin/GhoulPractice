package net.ghoul.practice.util.external;

import net.ghoul.practice.Ghoul;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BaseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public void call() {
        Ghoul.getInstance().getServer().getPluginManager().callEvent(this);
    }

}
