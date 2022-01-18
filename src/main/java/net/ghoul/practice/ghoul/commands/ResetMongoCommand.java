package net.ghoul.practice.ghoul.commands;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import net.ghoulnetwork.core.Core;
import net.ghoulnetwork.core.managers.player.PlayerData;
import net.ghoulnetwork.core.utilities.Utilities;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetMongoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (!player.getName().equals("tomas_s")) return true;

        Profile.getCollection().drop();
        Profile.getAllProfiles().drop();

        Core.INSTANCE.getMongoManager().getDocumentation().drop();
        Core.INSTANCE.getMongoManager().getRanks().drop();
        Core.INSTANCE.getMongoManager().getTags().drop();
        Core.INSTANCE.getMongoManager().getGlobalCooldowns().drop();
        Core.INSTANCE.getMongoManager().getBans().drop();
        Core.INSTANCE.getMongoManager().getMutes().drop();
        Core.INSTANCE.getMongoManager().getKicks().drop();
        Core.INSTANCE.getMongoManager().getWarns().drop();
        Core.INSTANCE.getMongoManager().getBlacklists().drop();
        Core.INSTANCE.getMongoManager().getPunishPlayerData().drop();
        Core.INSTANCE.getMongoManager().getPunishHistory().drop();
        Core.INSTANCE.getMongoManager().getUhc_stats().drop();

        for (Player players : Utilities.getOnlinePlayers()) {
            PlayerData playerData = Core.INSTANCE.getPlayerManagement().getPlayerData(players.getUniqueId());
            Profile profile = Profile.getByUuid(players.getUniqueId());

            profile.save();
            playerData.saveData();
        }

        player.sendMessage(CC.translate("&eYa se reseteo todos las mongo-db."));
        return true;
    }
}
