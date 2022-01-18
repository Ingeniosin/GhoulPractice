package net.ghoul.practice.ghoul.commands;

import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleDuelsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        Profile profile = Profile.getByUuid(player.getUniqueId());
        boolean duelstate = profile.getSettings().isReceiveDuelRequests();
        player.sendMessage(CC.translate("&7Receiving Duels: " + (!duelstate ? "&cOn" : "&cOff")));
        profile.getSettings().setReceiveDuelRequests(!duelstate);
        return true;
    }
}
