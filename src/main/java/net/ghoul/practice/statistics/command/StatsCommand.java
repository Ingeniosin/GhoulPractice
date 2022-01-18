package net.ghoul.practice.statistics.command;

import net.ghoul.practice.statistics.menu.StatsMenu;
import net.ghoul.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) {
            new StatsMenu(player).openMenu(player);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(CC.translate("&cThis player is offline."));
            return true;
        }

        new StatsMenu(target).openMenu(player);
        return true;
    }
}
