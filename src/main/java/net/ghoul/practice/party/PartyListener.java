package net.ghoul.practice.party;

import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.chat.CC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPartyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String chatMessage = event.getMessage();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        Party party = profile.getParty();

        if (party != null) {
            if (chatMessage.startsWith("@")) {
                event.setCancelled(true);
                String message = CC.translate("&7» " + player.getDisplayName() + ChatColor.GRAY + ": " + ChatColor.AQUA + chatMessage.replace("@", ""));
                party.broadcast(message);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.getParty() != null) {
            if (profile.getParty().getLeader().equals(event.getPlayer())) {
                profile.getParty().disband();
            } else {
                profile.getParty().leave(event.getPlayer(), false);
            }
        }
    }
}
