package net.ghoul.practice.match.command;

import net.ghoul.practice.match.MatchSnapshot;
import net.ghoul.practice.match.menu.MatchDetailsMenu;
import net.ghoul.practice.util.chat.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ViewInventoryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        MatchSnapshot cachedInventory;

        try {
            cachedInventory = MatchSnapshot.getByUuid(UUID.fromString(args[0]));
        } catch (Exception e) {
            cachedInventory = MatchSnapshot.getByName(args[0]);
        }

        if (cachedInventory == null) {
            player.sendMessage(CC.RED + "Couldn't find that inventory.");
            return true;
        }

        new MatchDetailsMenu(cachedInventory).openMenu(player);
        return true;
    }
}
