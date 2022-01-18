package net.ghoul.practice.match.types;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.ghoul.essentials.Essentials;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.profile.ProfileState;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.TaskUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.sitUtil.SitUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FFAMatch extends Match {

    private final Team team;

    public FFAMatch(Team team, Kit kit, Arena arena) {
        super(null, kit, arena, QueueType.UNRANKED);

        this.team = team;
    }

    @Override
    public boolean isSoloMatch() {
        return false;
    }

    @Override
    public boolean isTeamMatch() {
        return false;
    }

    @Override
    public boolean isFreeForAllMatch() {
        return true;
    }

    @Override
    public boolean isSumoMatch() {
        return false;
    }

    @Override
    public void setupPlayer(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);

        // If the player disconnected, skip any operations for them
        if (teamPlayer.isDisconnected()) return;

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

        if (getKit().getName().equalsIgnoreCase("boxing")) {
            player.getInventory().clear();

            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setContents(new ItemStack[36]);

            player.addPotionEffect(PotionEffectType.SPEED.createEffect(500000000, 1));

            player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.DAMAGE_ALL, 1).build());
        }

        TaskUtil.runLater(() -> SitUtil.sitPlayer(player), 5L);

        TaskUtil.runLater(() -> {
            Team team = getTeam(player);
            for (Player enemy : team.getPlayers()) {
                Profile profile = Profile.getByUuid(enemy.getPlayer());
                profile.handleVisibility();
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
        final Team teamA = new Team(new TeamPlayer(this.getPlayers().get(0)));
        final Team teamB = new Team(new TeamPlayer(this.getPlayers().get(1)));

        final List<Player> players2 = new ArrayList<>(this.getPlayers());
        Collections.shuffle(players2);

        for (final Player otherPlayer2 : players2) {
            if (!teamA.getLeader().getUuid().equals(otherPlayer2.getUniqueId())) {
                if (teamB.getLeader().getUuid().equals(otherPlayer2.getUniqueId())) continue;

                if (teamA.getTeamPlayers().size() > teamB.getTeamPlayers().size()) {
                    teamB.getTeamPlayers().add(new TeamPlayer(otherPlayer2));
                } else {
                    teamA.getTeamPlayers().add(new TeamPlayer(otherPlayer2));
                }
            }
        }

        teamA.getTeamPlayers().forEach(teamPlayerA -> teamPlayerA.getPlayer().teleport(this.getArena().getSpawn1()));
        teamB.getTeamPlayers().forEach(teamPlayerB -> teamPlayerB.getPlayer().teleport(this.getArena().getSpawn2()));
    }

    @Override
    public boolean onEnd() {
        Player winningTeam = getWinningPlayer();

        winningTeam.getPlayer().getNearbyEntities(50, 50, 50).forEach(entity -> {
            if (entity instanceof Item) {
                entity.remove();
            }
        });

        for (TeamPlayer teamPlayer : team.getTeamPlayers()) {
            if (!teamPlayer.isDisconnected()) {
                if (teamPlayer.equals(getTeamPlayer(winningTeam))) {
                    teamPlayer.getPlayer().sendMessage(CC.translate("&aYour team has won the party."));
                } else {
                    teamPlayer.getPlayer().sendMessage(CC.translate("&cYour team has loss the party."));
                }
            }
        }

        for (TeamPlayer teamPlayer : team.getTeamPlayers()) {
            if (!teamPlayer.isDisconnected() && teamPlayer.isAlive()) {
                Player player = teamPlayer.getPlayer();

                if (player != null) {
                    Profile profile = Profile.getByUuid(player.getUniqueId());
                    profile.handleVisibility();
                }
            }
        }

        TaskUtil.runLater(() -> {
            for (TeamPlayer firstTeamPlayer : team.getTeamPlayers()) {
                //Check if they didn't disconnect
                if (!firstTeamPlayer.isDisconnected()) {
                    Player player = firstTeamPlayer.getPlayer();

                    //Add Their Snapshot
                    if (player != null) {
                        if (player.isDead()) {
                            player.spigot().respawn();
                        }

                        //Reset the Player
                        player.setFireTicks(0);
                        player.updateInventory();
                        Profile profile = Profile.getByUuid(player.getUniqueId());
                        profile.setState(ProfileState.IN_LOBBY);
                        profile.setMatch(null);
                        profile.handleVisibility();
                        PlayerUtil.reset(player, false);
                        profile.refreshHotbar();
                        //Reset their Knockback Profile and Teleport them to Spawn
                        Ghoul.getInstance().getKnockbackManager().getKnockbackType().appleKitKnockback(player, getKit());
                        Essentials.teleportToSpawn(player);
                    }
                }
            }
        }, (getKit().getGameRules().isWaterkill()) ? 0L : 80L);
        return true;
    }

    @Override
    public boolean canEnd() {
        return team.getAliveTeamPlayers().size() == 1;
    }

    @Override
    public void onDeath(Player player, Player killer) {
        TaskUtil.runLater(() -> {
            TeamPlayer teamPlayer = getTeamPlayer(player);

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
        for (Player players : getPlayersAndSpectators()) {
            Profile.getByUuid(players.getUniqueId()).handleVisibility(players, player);
        }
    }

    @Override
    public Player getWinningPlayer() {
        if (team.getAliveTeamPlayers().size() == 1) {
            return team.getAliveTeamPlayers().get(0).getPlayer();
        } else {
            return null;
        }
    }

    @Override
    public Team getWinningTeam() {
        throw new UnsupportedOperationException("Cannot getInstance winning team from a Juggernaut match");
    }

    @Override
    public TeamPlayer getTeamPlayerA() {
        throw new UnsupportedOperationException("Cannot getInstance team player from a Juggernaut match");
    }

    @Override
    public TeamPlayer getTeamPlayerB() {
        throw new UnsupportedOperationException("Cannot getInstance team player from a Juggernaut match");
    }

    @Override
    public List<TeamPlayer> getTeamPlayers() {
        throw new UnsupportedOperationException("Cannot getInstance team player from a Juggernaut match");
    }

    @Override
    public List<Player> getPlayers() {
        return team.getPlayers();
    }

    public List<Player> getAlivePlayers() {
        List<Player> players = new ArrayList<>();
        for (Player player : team.getPlayers()) {
            if (getTeamPlayer(player).isAlive()) {
                players.add(player);
            }
        }
        return players;
    }

    @Override
    public Team getTeamA() {
        throw new UnsupportedOperationException("Cannot getInstance team from a Juggernaut match");
    }

    @Override
    public Team getTeamB() {
        throw new UnsupportedOperationException("Cannot getInstance team from a Juggernaut match");
    }

    @Override
    public Team getTeam(Player player) {
        return team;
    }

    @Override
    public TeamPlayer getTeamPlayer(Player player) {
        for (TeamPlayer teamPlayer : team.getTeamPlayers()) {
            if (teamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamPlayer;
            }
        }

        return null;
    }

    @Override
    public Team getOpponentTeam(Team team) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a Juggernaut match");
    }

    @Override
    public Team getOpponentTeam(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a Juggernaut match");
    }

    @Override
    public Player getOpponentPlayer(Player player) {
        throw new IllegalStateException("Cannot getInstance opponent player in Juggernaut match");
    }

    @Override
    public TeamPlayer getOpponentTeamPlayer(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a Juggernaut match");
    }

    @Override
    public int getRoundsNeeded(Team Team) {
        return 0;
    }

    @Override
    public int getRoundsNeeded(TeamPlayer teamPlayer) {
        return 0;
    }

    @Override
    public int getTotalRoundWins() {
        return 0;
    }

    @Override
    public ChatColor getRelationColor(Player viewer, Player target) {
        return ChatColor.RED;
    }

}
