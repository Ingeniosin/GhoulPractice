package net.ghoul.practice.util.nametags;

import lombok.Getter;
import net.ghoul.practice.Ghoul;
import net.ghoulnetwork.core.Core;
import net.ghoulnetwork.core.managers.player.PlayerData;
import net.ghoulnetwork.core.utilities.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NameTagManagement {

    @Getter
    private final List<UUID> players = new ArrayList<>();

    private String getTeamPrefix(String color) {
        return "NT_" + color;
    }

    public NameTagManagement() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Ghoul.getInstance(), new Update(), 2L, 2L);
    }

    public void createScoreboard(Player player) {
        if (player == null || !player.isOnline()) return;
        Scoreboard scoreboard = player.getScoreboard();

        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        player.setScoreboard(scoreboard);
        this.players.add(player.getUniqueId());
    }

    public void setup(Player player) {
        if (player.getScoreboard() == null) return;
        if (!this.players.contains(player.getUniqueId())) return;

        Scoreboard scoreboard = player.getScoreboard();

        for (ChatColor chatColor : ChatColor.values()) {
            if (!chatColor.isColor()) continue;

            Team team = scoreboard.getTeam(getTeamPrefix(chatColor.toString()));

            if (team == null) {
                team = scoreboard.registerNewTeam(getTeamPrefix(chatColor.toString()));
            }

            team.setPrefix(chatColor.toString());
        }

        for (Player online : Utilities.getOnlinePlayers()) {
            PlayerData playerData = Core.INSTANCE.getPlayerManagement().getPlayerData(online.getUniqueId());

            if (playerData == null) continue;

            String nameColor;
            if (playerData.isFrozen()) {
                nameColor = ChatColor.BLACK.toString();
            } else if (playerData.isDisguise()) {
                nameColor = ChatColor.GRAY.toString();
            } else if (playerData.getNameColor() != null) {
                nameColor = playerData.getNameColor();
            } else {
                nameColor = playerData.getHighestRank().getColor().toString();
            }

            Team add = scoreboard.getTeam(getTeamPrefix(nameColor));

            if (add != null) {
                if (!add.hasEntry(online.getName())) {
                    add.addEntry(online.getName());
                }

                if (playerData.isFrozen()) {
                    add.setPrefix(ChatColor.BLACK.toString());
                } else if (playerData.isDisguise()) {
                    add.setPrefix(ChatColor.GRAY.toString());
                } else if (playerData.getNameColor() != null) {
                    add.setPrefix(playerData.getNameColor());
                } else {
                    add.setPrefix(playerData.getHighestRank().getColor().toString());
                }
            }
        }

        if (player.getScoreboard() != scoreboard) {
            player.setScoreboard(scoreboard);
        }
    }

    public void unregister(Player player) {
        if (player.getScoreboard() == null) return;
        if (!this.players.contains(player.getUniqueId())) return;

        try {
            Scoreboard scoreboard = player.getScoreboard();

            for (ChatColor chatColor : ChatColor.values()) {
                if (!chatColor.isColor()) continue;

                Team team = scoreboard.getTeam(getTeamPrefix(chatColor.toString()));

                if (team != null) {
                    team.unregister();
                }
            }
        } catch (Exception ignored) {
        }
        this.players.remove(player.getUniqueId());
    }

    private class Update implements Runnable {

        @Override
        public void run() {
            Utilities.getOnlinePlayers().forEach(NameTagManagement.this::setup);
        }
    }
}


