package net.ghoul.practice.match.types;

import lombok.Getter;
import lombok.Setter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.ghoul.essentials.Essentials;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.MatchSnapshot;
import net.ghoul.practice.match.MatchState;
import net.ghoul.practice.match.task.MatchStartTask;
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.profile.ProfileState;
import net.ghoul.practice.queue.Queue;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.TaskUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.elo.EloUtil;
import net.ghoul.practice.util.external.ChatComponentBuilder;
import net.ghoul.practice.util.external.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
public class SumoMatch extends Match {

    @Setter private TeamPlayer playerA;
    @Setter private TeamPlayer playerB;

    public SumoMatch(Queue queue, TeamPlayer playerA, TeamPlayer playerB, Kit kit, Arena arena, QueueType queueType) {
        super(queue, kit, arena, queueType);

        this.playerA = playerA;
        this.playerB = playerB;
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
        return false;
    }

    @Override
    public boolean isSumoMatch() {
        return true;
    }

    @Override
    public void setupPlayer(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);

        if (teamPlayer.isDisconnected()) {
            return;
        }
        teamPlayer.setAlive(true);

        PlayerUtil.reset(player);

        Profile profilePlayer = Profile.getByUuid(player.getUniqueId());

        profilePlayer.setState(ProfileState.IN_FIGHT);

        if (getKit().getGameRules().isInfinitespeed()) {
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(500000000, 2));
        }
        if (getKit().getGameRules().isInfinitestrength()) {
            player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(500000000, 2));
        }

        Ghoul.getInstance().getKnockbackManager().getKnockbackType().appleKitKnockback(player, getKit());

        Location spawn = playerA.equals(teamPlayer) ? getArena().getSpawn1() : getArena().getSpawn2();

        if (spawn.getBlock().getType() == Material.AIR) {
            player.teleport(spawn);
        } else {
            player.teleport(spawn.add(0, 2, 0));
        }

        if (getKit().getName().equalsIgnoreCase("boxing")) {
            player.getInventory().clear();

            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setContents(new ItemStack[36]);

            player.addPotionEffect(PotionEffectType.SPEED.createEffect(500000000, 1));

            player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.DAMAGE_ALL, 1).build());
        }

    }

    @Override
    public void cleanPlayer(Player player) {

    }

    @Override
    public void onStart() {
    }

    @Override
    public boolean onEnd() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (TeamPlayer teamPlayer : new TeamPlayer[]{getTeamPlayerA(), getTeamPlayerB()}) {
                    if (!teamPlayer.isDisconnected()) {
                        Player player = teamPlayer.getPlayer();
                        Player opponent = getOpponentPlayer(player);

                        if (player != null) {

                            player.setFireTicks(0);
                            player.updateInventory();

                            Profile profile = Profile.getByUuid(player.getUniqueId());
                            profile.setState(ProfileState.IN_LOBBY);
                            profile.setMatch(null);
                            profile.refreshHotbar();
                            profile.handleVisibility();
                            Ghoul.getInstance().getKnockbackManager().getKnockbackType().applyDefaultKnockback(player);

                            Essentials.teleportToSpawn(player);
                        }
                    }
                }
            }
        }.runTaskLaterAsynchronously(Ghoul.getInstance(), (getKit().getGameRules().isWaterkill() || getKit().getGameRules().isSumo()) ? 0L : 80L);

        Player winningPlayer = getWinningPlayer();
        Player losingPlayer = getOpponentPlayer(winningPlayer);

        TeamPlayer winningTeamPlayer = getTeamPlayer(winningPlayer);
        TeamPlayer losingTeamPlayer = getTeamPlayer(losingPlayer);

        ChatComponentBuilder inventoriesBuilder = new ChatComponentBuilder("");

        inventoriesBuilder.append("Winner: ").color(ChatColor.GREEN).append(winningPlayer.getName()).color(ChatColor.YELLOW);
        inventoriesBuilder.setCurrentHoverEvent(getHoverEvent(winningTeamPlayer)).setCurrentClickEvent(getClickEvent(winningTeamPlayer)).append(" | ").color(ChatColor.GRAY).append("Loser: ").color(ChatColor.RED).append(losingPlayer.getName()).color(ChatColor.YELLOW);
        inventoriesBuilder.setCurrentHoverEvent(getHoverEvent(losingTeamPlayer)).setCurrentClickEvent(getClickEvent(losingTeamPlayer));

        List<BaseComponent[]> components = new ArrayList<>();
        components.add(new ChatComponentBuilder("").parse("&eMatch Details &7(Click name to view)").create());
        components.add(inventoriesBuilder.create());

        Profile winningProfile = Profile.getByUuid(winningPlayer.getUniqueId());
        Profile losingProfile = Profile.getByUuid(losingPlayer.getUniqueId());

        if (getQueueType() == QueueType.UNRANKED) {
            winningProfile.getStatisticsData().get(getKit()).incrementWon();
            losingProfile.getStatisticsData().get(getKit()).incrementLost();
        }

        List<BaseComponent[]> CHAT_BAR = new ArrayList<>();
        CHAT_BAR.add(0, new ChatComponentBuilder("").parse("").create());

        for (Player player : new Player[]{winningPlayer, losingPlayer}) {
            CHAT_BAR.forEach(components1 -> player.spigot().sendMessage(components1));
            components.forEach(components1 -> player.spigot().sendMessage(components1));
            CHAT_BAR.forEach(components1 -> player.spigot().sendMessage(components1));
        }

        for (Player player : this.getSpectators()) {
            CHAT_BAR.forEach(components1 -> player.spigot().sendMessage(components1));
            components.forEach(components1 -> player.spigot().sendMessage(components1));
            CHAT_BAR.forEach(components1 -> player.spigot().sendMessage(components1));
        }

        TaskUtil.runAsync(() -> {
            winningPlayer.getInventory().clear();
            winningPlayer.setSprinting(true);
            winningPlayer.setAllowFlight(true);
            winningPlayer.setFlying(true);
        });

        TaskUtil.runLater(() -> {
            if (!winningPlayer.isOnline()) return;

            winningPlayer.getInventory().setHelmet(null);
            winningPlayer.getInventory().setChestplate(null);
            winningPlayer.getInventory().setLeggings(null);
            winningPlayer.getInventory().setBoots(null);
            winningPlayer.getPlayer().getInventory().clear();
            winningPlayer.getInventory().setArmorContents(new ItemStack[4]);
            winningPlayer.getInventory().setContents(new ItemStack[36]);
        }, 10);

        if (getQueueType() == QueueType.RANKED) {

            int oldWinnerElo = winningTeamPlayer.getElo();
            int oldLoserElo = losingTeamPlayer.getElo();
            int newWinnerElo = EloUtil.getNewRating(oldWinnerElo, oldLoserElo, true);
            int newLoserElo = EloUtil.getNewRating(oldLoserElo, oldWinnerElo, false);
            winningProfile.getStatisticsData().get(getKit()).setElo(newWinnerElo);
            losingProfile.getStatisticsData().get(getKit()).setElo(newLoserElo);
            winningProfile.getStatisticsData().get(getKit()).incrementWon();
            losingProfile.getStatisticsData().get(getKit()).incrementLost();
            winningProfile.calculateGlobalElo();
            losingProfile.calculateGlobalElo();

            int winnerEloChange = newWinnerElo - oldWinnerElo;
            int loserEloChange = oldLoserElo - newLoserElo;

            String eloMessage = CC.translate("&a" + winningPlayer.getName() + " (" + newWinnerElo + ") (+" + winnerEloChange + ") &7has beaten &c" + losingPlayer.getName() +
                    " (" + newLoserElo + ") (-" + loserEloChange + ") &7in " + getKit().getName() + " kit.");

            for (Player player : new Player[]{winningPlayer}) {
                player.sendMessage(eloMessage);
            }

            for (Player player : new Player[]{losingPlayer}) {
                player.sendMessage(eloMessage);
            }
        }

        if (getMatchWaterCheck() != null) {
            getMatchWaterCheck().cancel();
        }

        winningProfile.setSumoRounds(0);
        losingProfile.setSumoRounds(0);

        return true;
    }

    @Override
    public boolean canEnd() {
        if (getRoundsNeeded(playerA) == 0 || getRoundsNeeded(playerB) == 0) return true;
        return playerA.isDisconnected() || playerB.isDisconnected();
    }

    @Override
    public Player getWinningPlayer() {
        if (getKit().getGameRules().isTimed()) {
            if (playerA.isDisconnected()) {
                return playerB.getPlayer();
            } else if (playerB.isDisconnected()) {
                return playerB.getPlayer();
            } else if (playerA.getHits() > playerB.getHits()) {
                return playerA.getPlayer();
            } else {
                return playerB.getPlayer();
            }
        } else {
            if (playerA.isDisconnected() || !playerA.isAlive()) {
                return playerB.getPlayer();
            } else {
                return playerA.getPlayer();
            }
        }
    }

    @Override
    public Team getWinningTeam() {
        throw new UnsupportedOperationException("Cannot getInstance winning team from a SoloMatch");
    }

    @Override
    public TeamPlayer getTeamPlayerA() {
        return playerA;
    }

    @Override
    public TeamPlayer getTeamPlayerB() {
        return playerB;
    }

    @Override
    public List<TeamPlayer> getTeamPlayers() {
        return Arrays.asList(playerA, playerB);
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();

        Player playerA = this.playerA.getPlayer();

        if (playerA != null) {
            players.add(playerA);
        }

        Player playerB = this.playerB.getPlayer();

        if (playerB != null) {
            players.add(playerB);
        }

        return players;
    }

    @Override
    public List<Player> getAlivePlayers() {
        List<Player> players = new ArrayList<>();

        Player playerA = this.playerA.getPlayer();

        if (playerA != null) {
            players.add(playerA);
        }

        Player playerB = this.playerB.getPlayer();

        if (playerB != null) {
            players.add(playerB);
        }

        return players;
    }

    @Override
    public Team getTeamA() {
        throw new UnsupportedOperationException("Cannot getInstance team from a SoloMatch");
    }

    @Override
    public Team getTeamB() {
        throw new UnsupportedOperationException("Cannot getInstance team from a SoloMatch");
    }

    @Override
    public Team getTeam(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance team from a SoloMatch");
    }

    @Override
    public TeamPlayer getTeamPlayer(Player player) {
        if (playerA.getUuid().equals(player.getUniqueId())) {
            return playerA;
        } else if (playerB.getUuid().equals(player.getUniqueId())) {
            return playerB;
        } else {
            return null;
        }
    }

    @Override
    public Team getOpponentTeam(Team team) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a SoloMatch");
    }

    @Override
    public Team getOpponentTeam(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a SoloMatch");
    }

    @Override
    public Player getOpponentPlayer(Player player) {
        if (player == null) {
            return null;
        }

        if (playerA.getUuid().equals(player.getUniqueId())) {
            return playerB.getPlayer();
        } else if (playerB.getUuid().equals(player.getUniqueId())) {
            return playerA.getPlayer();
        } else {
            return null;
        }
    }

    @Override
    public TeamPlayer getOpponentTeamPlayer(Player player) {
        if (playerA.getUuid().equals(player.getUniqueId())) {
            return playerB;
        } else if (playerB.getUuid().equals(player.getUniqueId())) {
            return playerA;
        } else {
            return null;
        }
    }

    @Override
    public int getTotalRoundWins() {
        Profile aProfile = Profile.getByUuid(playerA.getUuid());
        Profile bProfile = Profile.getByUuid(playerB.getUuid());
        return aProfile.getSumoRounds() + bProfile.getSumoRounds();
    }

    @Override
    public int getRoundsNeeded(TeamPlayer teamPlayer) {
        Profile aProfile = Profile.getByUuid(playerA.getUuid());
        Profile bProfile = Profile.getByUuid(playerB.getUuid());

        if (playerA.equals(teamPlayer)) {
            return (3 - aProfile.getSumoRounds());
        } else if (playerB.equals(teamPlayer)) {
            return (3 - bProfile.getSumoRounds());
        } else {
            return -1;
        }
    }

    @Override
    public int getRoundsNeeded(Team team) {
        throw new UnsupportedOperationException("Cannot getInstance team round wins from SoloMatch");
    }

    @Override
    public void onDeath(Player deadPlayer, Player killerPlayer) {
        Profile aProfile = Profile.getByUuid(playerA.getUuid());
        Profile bProfile = Profile.getByUuid(playerB.getUuid());

        if (deadPlayer.isOnline()) {
            if (getRoundsNeeded(playerA) != 0 || getRoundsNeeded(playerB) != 0) {
                if (getWinningPlayer().getUniqueId().toString().equals(playerA.getUuid().toString())) {
                    aProfile.setSumoRounds(aProfile.getSumoRounds() + 1);
                } else if (getWinningPlayer().getUniqueId().toString().equals(playerB.getUuid().toString())) {
                    bProfile.setSumoRounds(bProfile.getSumoRounds() + 1);
                }

                getWinningPlayer().getPlayer().sendMessage(CC.translate("&aYou have won this round!"));
                getOpponentPlayer(getWinningPlayer()).getPlayer().sendMessage(CC.translate("&cYou have lost this round!"));

                if (aProfile.getSumoRounds() >= 3 || bProfile.getSumoRounds() >= 3) {
                    TeamPlayer roundWinner = getTeamPlayer(getWinningPlayer());
                    TeamPlayer roundLoser = getOpponentTeamPlayer(getWinningPlayer());

                    PlayerUtil.reset(deadPlayer);

                    for (Player otherPlayer : getPlayersAndSpectators()) {
                        Profile profile = Profile.getByUuid(otherPlayer.getUniqueId());
                        profile.handleVisibility(otherPlayer, deadPlayer);
                    }
                    end();
                } else {
                    //Send Round Messages
                    playerA.getPlayer().sendMessage(CC.translate("&fYou need to win &c" + getRoundsNeeded(playerA) +  " &fmore rounds to win!"));
                    playerB.getPlayer().sendMessage(CC.translate("&fYou need to win &c" + getRoundsNeeded(playerB) + " &fmore rounds to win!"));

                    //Setup the Player
                    setupPlayer(playerA.getPlayer());
                    setupPlayer(playerB.getPlayer());

                    //Handle visibility for both
                    playerA.getPlayer().showPlayer(playerB.getPlayer());
                    playerB.getPlayer().showPlayer(playerA.getPlayer());

                    //Restart the match
                    onStart();
                    setState(MatchState.STARTING);
                    setStartTimestamp(-1);
                    new MatchStartTask(this).runTaskTimer(Ghoul.getInstance(), 20L, 20L);
                }
            }
        }
    }

    @Override
    public void onRespawn(Player player) {
        Essentials.teleportToSpawn(player);
    }

    @Override
    public org.bukkit.ChatColor getRelationColor(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return org.bukkit.ChatColor.GREEN;
        }

        if (playerA.getUuid().equals(viewer.getUniqueId()) || playerB.getUuid().equals(viewer.getUniqueId())) {
            return org.bukkit.ChatColor.RED;
        } else {
            return org.bukkit.ChatColor.GREEN;
        }
    }
}
