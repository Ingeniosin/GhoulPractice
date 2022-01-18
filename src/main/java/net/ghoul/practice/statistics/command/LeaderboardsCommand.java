package net.ghoul.practice.statistics.command;

import net.ghoul.practice.statistics.menu.LeaderboardsMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaderboardsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        new LeaderboardsMenu().openMenu(player);
        return true;
    }
}
