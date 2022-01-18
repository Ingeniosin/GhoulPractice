package net.ghoul.practice.util.scoreboard;

import net.ghoul.practice.providers.ScoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardTask extends BukkitRunnable {

    @Override
    public void run() {
        if (Board.getBoards().keySet().isEmpty()) return;

        try {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                ScoreProvider board = (ScoreProvider) Board.getBoards().get(player.getUniqueId());
                if (board != null) board.update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
