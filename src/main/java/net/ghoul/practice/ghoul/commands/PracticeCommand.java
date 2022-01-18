package net.ghoul.practice.ghoul.commands;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.ghoul.essentials.Essentials;
import net.ghoul.practice.ghoul.listener.GoldenHeads;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.chat.CC;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PracticeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (!player.hasPermission("practice.dev") && !player.isOp()) {
            player.sendMessage(CC.translate("&cNo permission."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(CC.MENU_BAR);
            player.sendMessage(CC.translate("&c/practice resetstats <player>"));
            player.sendMessage(CC.translate("&c/practice savearenas"));
            player.sendMessage(CC.translate("&c/practice savekits"));
            player.sendMessage(CC.translate("&c/practice setspawn"));
            player.sendMessage(CC.translate("&c/practice spawn"));
            player.sendMessage(CC.translate("&c/practice goldenhead"));
            player.sendMessage(CC.MENU_BAR);
            return true;
        }

        Player target;

        switch (args[0]) {
            case "resetstats":
                target = Bukkit.getPlayer(args[0]);

                if (target == null) {
                    player.sendMessage(CC.RED + "Either that player does not exist or you did not specify a name!");
                } else {
                    try {
                        Ghoul.getInstance().getMongoDatabase().getCollection("profiles").deleteOne(new Document("name", target.getName()));
                        PlayerUtil.getPlayer(target.getName()).kickPlayer("Your Profile was reset by an Admin, Please Rejoin!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "savearenas":
                Arena.getArenas().forEach(Arena::save);
                Arena.getCustomArenas().forEach(Arena::save);
                player.sendMessage(CC.translate("&7[&cGhoul&7] &c") + "Arenas have been saved!");
                break;
            case "savekits":
                Kit.getKits().forEach(Kit::save);
                player.sendMessage(CC.translate("&7[&cGhoul&7] &cSaving Kits..."));
                break;
            case "setspawn":
                Essentials.setSpawn(player.getLocation());
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7You have set the &cnew &7lobby &cspawn &7!"));
                break;
            case "spawn":
                Profile profile = Profile.getByUuid(player.getUniqueId());
                if (profile.isInSomeSortOfFight() && !profile.isInLobby()) {
                    player.sendMessage(CC.translate("Unable to teleport to spawn, Please finish your current task!"));
                }
                Essentials.teleportToSpawn(player);
                profile.refreshHotbar();
                break;
            case "goldenhead":
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7You received a &cGolden head&7."));
                player.getInventory().addItem(GoldenHeads.goldenHeadItem());
                break;

        }


        return true;
    }
}
