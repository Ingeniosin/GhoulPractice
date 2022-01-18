package net.ghoul.practice.ghoul.commands;

import net.ghoul.practice.match.menu.MatchsMenu;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            new MatchsMenu().open(player);
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(CC.translate("&cPlease use: /spec <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(CC.RED + "A player with that name could not be found.");
            return true;
        }

        Profile playerProfile = Profile.getByUuid(player.getUniqueId());

        if (playerProfile.isBusy()) {
            player.sendMessage(CC.RED + "You must be in the lobby and not queueing to spectate.");
            return true;
        }

        if (playerProfile.getParty() != null) {
            player.sendMessage(CC.RED + "You must leave your party to spectate a match.");
            return true;
        }

        Profile targetProfile = Profile.getByUuid(target.getUniqueId());

        if (targetProfile.isInFight()) {
            targetProfile.getMatch().addSpectator(player, target);
        } else {
            player.sendMessage(CC.RED + "That player is not in a match or running event.");
        }
        return true;
    }
}
