package net.ghoul.practice.match.types;

import lombok.Getter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.ghoul.essentials.Essentials;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.MatchState;
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.profile.ProfileState;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.TaskUtil;
import net.ghoul.practice.util.Utils;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.sitUtil.SitUtil;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TeamMatch extends Match {

    private final Team teamA;
    private final Team teamB;
    private final int teamARoundWins = 0;
    private final int teamBRoundWins = 0;

    public TeamMatch(Team teamA, Team teamB, Kit kit, Arena arena) {
        super(null, kit, arena, QueueType.UNRANKED);

        this.teamA = teamA;
        this.teamB = teamB;
    }

    @Override
    public boolean isSoloMatch() {
        return false;
    }

    @Override
    public boolean isTeamMatch() {
        return true;
    }

    @Override
    public boolean isFreeForAllMatch() {
        return false;
    }

    @Override
    public boolean isSumoMatch() {
        return false;
    }

    @Override
    public void setupPlayer(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);

        // If the player disconnected, skip any operations for them
        if (teamPlayer.isDisconnected()) {
            return;
        }

        teamPlayer.setAlive(true);

        PlayerUtil.reset(player);

        Profile profilePlayer = Profile.getByUuid(player.getUniqueId());

        profilePlayer.setState(ProfileState.IN_FIGHT);

        if (!getKit().getGameRules().isCombo()) {
            player.setMaximumNoDamageTicks(getKit().getGameRules().getHitDelay());
        }

        if (getKit().getGameRules().isCombo()) {
            player.setMaximumNoDamageTicks(0);
            player.setNoDamageTicks(3);
        }

        if (getKit().getGameRules().isInfinitespeed()) {
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(500000000, 2));
        }
        if (getKit().getGameRules().isInfinitestrength()) {
            player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(500000000, 2));
        }

        if (!getKit().getGameRules().isNoitems()) {
            Profile.getByUuid(player.getUniqueId()).getStatisticsData().get(this.getKit()).getKitItems().forEach((integer, itemStack) -> player.getInventory().setItem(integer, itemStack));
        }

        Ghoul.getInstance().getKnockbackManager().getKnockbackType().appleKitKnockback(player, getKit());

        Team team = getTeam(player);

        Location spawn = team.equals(teamA) ? getArena().getSpawn1() : getArena().getSpawn2();

        player.teleport(spawn);

        teamPlayer.setPlayerSpawn(spawn);

        TaskUtil.runLater(() -> SitUtil.sitPlayer(player), 5L);

        TaskUtil.runLater(() -> {
            for (Player players : Utils.getOnlinePlayers()) {
                players.showPlayer(player);
                player.showPlayer(players);
            }
        }, 20);

        if (getKit().getGameRules().isShowHealth()) {
            Objective objective = player.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);

            if (objective == null) {
                objective = player.getScoreboard().registerNewObjective("showhealth", "health");
            }

            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setDisplayName(CC.DARK_RED + "❤");
        }
    }

    @Override
    public void cleanPlayer(Player player) {

    }

    @Override
    public void onStart() {
        if (getPlayers().size() < 1) {
            return;
        }

        if (getKit().getGameRules().isTimed())
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!getState().equals(MatchState.FIGHTING))
                        return;

                    if (getDuration().equalsIgnoreCase("01:00")) {
                        onEnd();
                        cancel();
                    }
                }
            }.runTaskTimer(Ghoul.getInstance(), 20L, 20L);
    }

    @Override
    public boolean onEnd() {
        Team winningTeam = getWinningTeam();

        for (Player player : this.getPlayers()) {
            if (player.isOnline()) {
                if (winningTeam.containsPlayer(player)) {
                    player.sendMessage(CC.translate("&aYour team has won the party."));
                } else {
                    player.sendMessage(CC.translate("&cYour team has loss the party."));
                }

                player.getNearbyEntities(50, 50, 50).forEach(entity -> {
                    if (entity instanceof Item) {
                        entity.remove();
                    }
                });
            }
        }

        for (TeamPlayer teamPlayer : getTeamPlayers()) {
            if (!teamPlayer.isDisconnected() && teamPlayer.isAlive()) {
                Player player = teamPlayer.getPlayer();

                if (player != null) {
                    Profile profile = Profile.getByUuid(player.getUniqueId());
                    profile.handleVisibility();
                }
            }
        }

        TaskUtil.runLater(() -> {
            for (TeamPlayer firstTeamPlayer : getTeamPlayers()) {
                if (!firstTeamPlayer.isDisconnected()) {
                    Player player = firstTeamPlayer.getPlayer();

                    if (player != null) {
                        if (player.isDead()) {
                            player.spigot().respawn();
                        }

                        player.setFireTicks(0);
                        player.updateInventory();

                        Profile profile = Profile.getByUuid(player.getUniqueId());
                        profile.setState(ProfileState.IN_LOBBY);
                        profile.setMatch(null);
                        PlayerUtil.reset(player, false);
                        profile.refreshHotbar();
                        profile.handleVisibility();
                        Ghoul.getInstance().getKnockbackManager().getKnockbackType().applyDefaultKnockback(player);
                        Essentials.teleportToSpawn(player);
                        PlayerUtil.reset(player, false);
                        profile.refreshHotbar();
                    }
                }
            }
        }, (getKit().getGameRules().isWaterkill()) ? 0L : 80L);
        return true;
    }

    @Override
    public boolean canEnd() {
        return teamA.getAliveTeamPlayers().isEmpty() || teamB.getAliveTeamPlayers().isEmpty();
    }

    @Override
    public void onDeath(Player player, Player killer) {
        TaskUtil.runLater(() -> {
            TeamPlayer teamPlayer = getTeamPlayer(player);

            PlayerUtil.reset(player);

            if (!canEnd() && !teamPlayer.isDisconnected()) {
                if (player.isDead()) {
                    player.spigot().respawn();
                }
                Profile profile = Profile.getByUuid(player.getUniqueId());
                PlayerUtil.reset(player, false);
                profile.refreshHotbar();
                player.setAllowFlight(true);
                player.setFlying(true);
                profile.setState(ProfileState.SPECTATE_MATCH);
                TaskUtil.runLaterAsync(() -> PlayerUtil.spectator(player), 2L);
            }
        }, 50L);
    }

    @Override
    public void onRespawn(Player player) {
        if (getKit().getGameRules().isSumo() && !isEnding()) {
            for (TeamPlayer teamPlayer : teamA.getTeamPlayers()) {
                if (teamPlayer.isDisconnected()) {
                    continue;
                }

                Player toPlayer = teamPlayer.getPlayer();

                if (toPlayer != null && toPlayer.isOnline()) {
                    toPlayer.teleport(getArena().getSpawn1());
                }
            }

            for (TeamPlayer teamPlayer : teamB.getTeamPlayers()) {
                if (teamPlayer.isDisconnected()) {
                    continue;
                }

                Player toPlayer = teamPlayer.getPlayer();

                if (toPlayer != null && toPlayer.isOnline()) {
                    toPlayer.teleport(getArena().getSpawn2());
                }
            }
        } else {
            for (Player players : getPlayersAndSpectators()) {
                Profile.getByUuid(players.getUniqueId()).handleVisibility(players, player);
            }
        }
    }

    @Override
    public Player getWinningPlayer() {
        throw new UnsupportedOperationException("Cannot getInstance solo winning player from a TeamMatch");
    }

    @Override
    public Team getWinningTeam() {
        if (getKit().getGameRules().isTimed()) {
            if (teamA.getAliveTeamPlayers().isEmpty()) {
                return teamB;
            } else if (teamB.getAliveTeamPlayers().isEmpty()) {
                return teamA;
            } else if (teamA.getTotalHits() > teamB.getTotalHits()) {
                return teamA;
            } else {
                return teamB;
            }
        } else {
            if (teamA.getAliveTeamPlayers().isEmpty()) {
                return teamB;
            } else if (teamB.getAliveTeamPlayers().isEmpty()) {
                return teamA;
            } else {
                return null;
            }
        }
    }

    @Override
    public TeamPlayer getTeamPlayerA() {
        throw new UnsupportedOperationException("Cannot getInstance solo match player from a TeamMatch");
    }

    @Override
    public TeamPlayer getTeamPlayerB() {
        throw new UnsupportedOperationException("Cannot getInstance solo match player from a TeamMatch");
    }

    @Override
    public List<TeamPlayer> getTeamPlayers() {
        List<TeamPlayer> teamPlayers = new ArrayList<>();
        teamPlayers.addAll(teamA.getTeamPlayers());
        teamPlayers.addAll(teamB.getTeamPlayers());
        return teamPlayers;
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();

        teamA.getTeamPlayers().forEach(TeamPlayer -> {
            Player player = TeamPlayer.getPlayer();

            if (player != null) {
                players.add(player);
            }
        });

        teamB.getTeamPlayers().forEach(TeamPlayer -> {
            Player player = TeamPlayer.getPlayer();

            if (player != null) {
                players.add(player);
            }
        });

        return players;
    }

    @Override
    public List<Player> getAlivePlayers() {
        List<Player> players = new ArrayList<>();

        teamA.getTeamPlayers().forEach(TeamPlayer -> {
            Player player = TeamPlayer.getPlayer();

            if (player != null) {
                if (TeamPlayer.isAlive()) {
                    players.add(player);
                }
            }
        });

        teamB.getTeamPlayers().forEach(TeamPlayer -> {
            Player player = TeamPlayer.getPlayer();

            if (player != null) {
                if (TeamPlayer.isAlive()) {
                    players.add(player);
                }
            }
        });

        return players;
    }

    @Override
    public Team getTeamA() {
        return teamA;
    }

    @Override
    public Team getTeamB() {
        return teamB;
    }

    @Override
    public Team getTeam(Player player) {
        for (TeamPlayer teamTeamPlayer : teamA.getTeamPlayers()) {
            if (teamTeamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamA;
            }
        }

        for (TeamPlayer teamTeamPlayer : teamB.getTeamPlayers()) {
            if (teamTeamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamB;
            }
        }

        return null;
    }

    @Override
    public TeamPlayer getTeamPlayer(Player player) {
        for (TeamPlayer teamPlayer : teamA.getTeamPlayers()) {
            if (teamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamPlayer;
            }
        }

        for (TeamPlayer teamPlayer : teamB.getTeamPlayers()) {
            if (teamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamPlayer;
            }
        }

        return null;
    }

    @Override
    public Team getOpponentTeam(Team team) {
        if (teamA.equals(team)) {
            return teamB;
        } else if (teamB.equals(team)) {
            return teamA;
        } else {
            return null;
        }
    }

    @Override
    public Team getOpponentTeam(Player player) {
        if (teamA.containsPlayer(player)) {
            return teamB;
        } else if (teamB.containsPlayer(player)) {
            return teamA;
        } else {
            return null;
        }
    }

    @Override
    public Player getOpponentPlayer(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance solo opponent player from TeamMatch");
    }

    @Override
    public TeamPlayer getOpponentTeamPlayer(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance solo opponent match player from TeamMatch");
    }

    @Override
    public int getTotalRoundWins() {
        return teamARoundWins + teamBRoundWins;
    }

    @Override
    public int getRoundsNeeded(TeamPlayer teamPlayer) {
        throw new UnsupportedOperationException("Cannot getInstance solo rounds needed from TeamMatch");
    }

    @Override
    public int getRoundsNeeded(Team Team) {
        if (teamA.equals(Team)) {
            return 3 - teamARoundWins;
        } else if (teamB.equals(Team)) {
            return 3 - teamBRoundWins;
        } else {
            return -1;
        }
    }

    @Override
    public org.bukkit.ChatColor getRelationColor(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return org.bukkit.ChatColor.GREEN;
        }

        Team team = getTeam(target);
        Team viewerTeam = getTeam(viewer);

        if (team == null || viewerTeam == null) {
            return org.bukkit.ChatColor.AQUA;
        }

        if (team.equals(viewerTeam)) {
            return org.bukkit.ChatColor.GREEN;
        } else {
            return org.bukkit.ChatColor.RED;
        }
    }

}
