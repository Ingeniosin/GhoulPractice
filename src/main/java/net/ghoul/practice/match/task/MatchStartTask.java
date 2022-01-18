package net.ghoul.practice.match.task;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.TitleType;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.MatchState;
import net.ghoul.practice.match.types.SumoMatch;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.elo.EloUtil;
import net.ghoul.practice.util.sitUtil.SitUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class MatchStartTask extends BukkitRunnable {

    private final Match match;
    private int seconds = 5;

    public MatchStartTask(Match match) {
        this.match = match;
    }

    @Override
    public void run() {
        if (match.isEnding()) {
            cancel();
            return;
        }
        if (seconds == 0) {
            if (match instanceof SumoMatch) {
                match.getPlayers().forEach(SitUtil::unSitPlayer);
                match.setState(MatchState.FIGHTING);
                match.setStartTimestamp(System.currentTimeMillis());
                match.broadcastMessage(CC.RED + "The round has started!");
            } else {
                match.setState(MatchState.FIGHTING);
                match.setStartTimestamp(System.currentTimeMillis());
                match.broadcastSound(Sound.LEVEL_UP);
                match.getPlayers().forEach(SitUtil::unSitPlayer);

                if (match.getKit().getName().equalsIgnoreCase("horse")) {
                    match.getPlayers().forEach(player -> {
                        if (player.isOnline()) {
                            Horse horse = (Horse) player.getWorld().spawnCreature(player.getLocation(), EntityType.HORSE);
                            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
                            horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING));
                            horse.setStyle(Horse.Style.BLACK_DOTS);
                            horse.setVariant(Horse.Variant.HORSE);
                            horse.setTamed(true);
                            horse.setOwner(player);
                            horse.setPassenger(player);
                        }
                    });
                }

                if (match.isSoloMatch()) {
                    if (match.getQueueType() == QueueType.RANKED) {
                        match.getPlayers().forEach(player -> {
                            Player opponent = match.getOpponentPlayer(player);
                            Profile profileOpponent = Profile.getByUuid(opponent.getUniqueId());

                            String elo = EloUtil.getEloRangeColor(profileOpponent.getStatisticsData().get(match.getKit()).getElo()) + profileOpponent.getStatisticsData().get(match.getKit()).getElo();

                            player.sendMessage(CC.translate("&cThe match against &7" + opponent.getName() + " &cwith ranking " + elo + " &chas been started."));
                        });
                    } else if (match.getQueueType() == QueueType.UNRANKED) {
                        match.getPlayers().forEach(player -> {
                            Player opponent = match.getOpponentPlayer(player);
                            player.sendMessage(CC.translate("&cThe match against &7" + opponent.getName() + " &chas been started."));
                        });
                    }
                }
            }
            cancel();
            return;
        }

        if (match.isSoloMatch()) {
            match.getPlayers().forEach(player -> LunarClientAPI.getInstance().sendTitle(player, TitleType.TITLE,
                    "§cStarting §7" + seconds + "s§c...", (float) 0.5, Duration.ofMillis(1), Duration.ofSeconds(1), Duration.ofMillis(1)));
        }

        match.broadcastMessage(CC.translate("&cStarting match in &7" + seconds + " &csecond" + (seconds == 1 ? "." : "s.") ));
        seconds--;
    }
}
