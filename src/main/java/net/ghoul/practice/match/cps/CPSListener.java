package net.ghoul.practice.match.cps;

import net.ghoul.practice.profile.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CPSListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            Player player = event.getPlayer();
            Profile profile = Profile.getByUuid(player.getPlayer());
            profile.addClick();
        }
    }
}
