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
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.profile.ProfileState;
import net.ghoul.practice.queue.Queue;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.TaskUtil;
import net.ghoul.practice.util.Utils;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.elo.EloUtil;
import net.ghoul.practice.util.external.ChatComponentBuilder;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.TimeUtil;
import net.ghoul.practice.util.sitUtil.SitUtil;
import net.ghoulnetwork.core.Core;
import net.ghoulnetwork.core.managers.logs.MatchLogs;
import net.ghoulnetwork.core.managers.player.PlayerData;
import net.ghoulnetwork.core.managers.player.PlayerStats;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Getter
public class SoloMatch extends Match {

    private final TeamPlayer playerA;
    private final TeamPlayer playerB;
    private final int playerARoundWins;
    private final int playerBRoundWins;
    @Getter @Setter private int hits1;
    @Getter @Setter private int hits2;

    public SoloMatch(Queue queue, TeamPlayer playerA, TeamPlayer playerB, Kit kit, Arena arena, QueueType queueType, int playerARoundWins, int playerBRoundWins) {
        super(queue, kit, arena, queueType);

        this.hits1 = 0;
        this.hits2 = 0;

        this.playerA = playerA;
        this.playerB = playerB;
        this.playerARoundWins = playerARoundWins;
        this.playerBRoundWins = playerBRoundWins;
    }

    @Override
    public boolean isSoloMatch() {
        return true;
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
        return false;
    }

    @Override
    public void setupPlayer(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);

        if (teamPlayer.isDisconnected()) return;

        teamPlayer.setAlive(true);

        PlayerUtil.reset(player);

        Profile profile = Profile.getByUuid(player.getUniqueId());

        profile.setState(ProfileState.IN_FIGHT);

        if (!getKit().getGameRules().isNoitems()) {
            TaskUtil.runLater(() -> Profile.getByUuid(player.getUniqueId()).getStatisticsData().get(this.getKit()).getKitItems().forEach((integer, itemStack) -> player.getInventory().setItem(integer, itemStack)), 10L);
        }
        if (!getKit().getGameRules().isCombo()) {
            player.setMaximumNoDamageTicks(getKit().getGameRules().getHitDelay());
        }

        if (getKit().getGameRules().isCombo()) {
            player.setMaximumNoDamageTicks(0);
            player.setNoDamageTicks(0);
        }

        if (getKit().getGameRules().isInfinitespeed()) {
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(500000000, 1));
        }
        if (getKit().getGameRules().isInfinitestrength()) {
            player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(500000000, 1));
        }

        Ghoul.getInstance().getKnockbackManager().getKnockbackType().appleKitKnockback(player, getKit());

        Location spawn = playerA.equals(teamPlayer) ? getArena().getSpawn1() : getArena().getSpawn2();

        player.teleport(spawn);

        teamPlayer.setPlayerSpawn(spawn);

        if (getKit().getName().equalsIgnoreCase("boxing")) {
            player.getInventory().clear();

            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setContents(new ItemStack[36]);

            player.addPotionEffect(PotionEffectType.SPEED.createEffect(500000000, 1));

            player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).enchantment(Enchantment.DAMAGE_ALL, 1).build());
        }

        TaskUtil.runLater(() -> SitUtil.sitPlayer(player), 5L);

        TaskUtil.runLater(() -> {
            for (Player players : Utils.getOnlinePlayers()) {
                if (player.isOnline()) {
                    players.showPlayer(player);
                    player.showPlayer(players);
                }
            }
        }, 100L);

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
        if (getKit().getGameRules().isTimed()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!getState().equals(MatchState.FIGHTING))
                        return;

                    if (getDuration().equalsIgnoreCase("01:00") || (getDuration().equalsIgnoreCase("01:01") && getState().equals(MatchState.FIGHTING)) || (getDuration().equalsIgnoreCase("01:02") && getState().equals(MatchState.FIGHTING))) {
                        onEnd();
                        cancel();
                    }
                }
            }.runTaskTimer(Ghoul.getInstance(), 20L, 20L);
        }
    }

    @Override
    public boolean onEnd() {
        for (TeamPlayer teamPlayer : new TeamPlayer[]{getTeamPlayerA(), getTeamPlayerB()}) {
            if (!teamPlayer.isDisconnected() && teamPlayer.isAlive()) {
                Player player = teamPlayer.getPlayer();

                if (player != null) {
                    if (teamPlayer.isAlive()) {
                        MatchSnapshot snapshot = new MatchSnapshot(player, false);
                        snapshot.setPotionsThrown(teamPlayer.getPotionsThrown());
                        snapshot.setPotionsMissed(teamPlayer.getPotionsMissed());
                        snapshot.setLongestCombo(teamPlayer.getLongestCombo());
                        snapshot.setTotalHits(teamPlayer.getHits());

                        getSnapshots().add(snapshot);
                    }
                }
            }
        }

        for (MatchSnapshot snapshot : getSnapshots()) {
            snapshot.setCreatedAt(System.currentTimeMillis());
            MatchSnapshot.getSnapshots().put(snapshot.getUuid(), snapshot);
        }

        TaskUtil.runLater(() -> {
            for (TeamPlayer teamPlayer : new TeamPlayer[]{getTeamPlayerA(), getTeamPlayerB()}) {
                if (!teamPlayer.isDisconnected()) {
                    Player player = teamPlayer.getPlayer();

                    if (player != null) {
                        if (player.isDead()) {
                            player.spigot().respawn();
                        }

                        player.setFireTicks(0);
                        player.updateInventory();

                        Profile profile = Profile.getByUuid(player.getUniqueId());
                        if (profile.getMatch() != null) {
                            profile.setState(ProfileState.IN_LOBBY);
                            profile.setMatch(null);
                            TaskUtil.runSync(profile::refreshHotbar);
                            profile.handleVisibility();
                            Ghoul.getInstance().getKnockbackManager().getKnockbackType().applyDefaultKnockback(player);

                            Essentials.teleportToSpawn(player);
                        }
                    }
                }
            }
        }, (getKit().getGameRules().isWaterkill()) ? 0L : 80L);

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

        winningProfile.getStatisticsData().get(getKit()).incrementWon();
        losingProfile.getStatisticsData().get(getKit()).incrementLost();

        winningProfile.getStatisticsData().get(getKit()).incrementMatches();
        losingProfile.getStatisticsData().get(getKit()).incrementMatches();

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

        winningPlayer.getNearbyEntities(50, 50, 50).forEach(entity -> {
            if (entity instanceof Item) {
                entity.remove();
            }
        });

        TaskUtil.runAsync(() -> {
            winningPlayer.setSprinting(true);
            winningPlayer.setAllowFlight(true);
            winningPlayer.setFlying(true);
        });

        EntityLiving entityLiving = ((CraftPlayer) winningPlayer).getHandle();

        double h = 0;

        if (winningPlayer.getHealth() > 0) {
            h += winningPlayer.getHealth();
        }

        if (entityLiving.getAbsorptionHearts() > 0) {
            h += entityLiving.getAbsorptionHearts();
        }

        String health = new DecimalFormat("#.#").format(Math.round((h / 2) * 2.0D) / 2.0D);

        if (health.equals("0")) {
            health = "0.5";
        }

        saveLogs(winningPlayer, losingPlayer, health);

        winningProfile.setMatchsPlayed(winningProfile.getMatchsPlayed() + 1);
        losingProfile.setMatchsPlayed(losingProfile.getMatchsPlayed() + 1);

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

            String oldRangeWinner = EloUtil.getEloRange(winningProfile.getStatisticsData().get(getKit()).getElo());
            String newRangeWinner = EloUtil.getEloRange(winningProfile.getStatisticsData().get(getKit()).getElo());

            int winnerEloChange = newWinnerElo - oldWinnerElo;
            int loserEloChange = oldLoserElo - newLoserElo;

            String eloMessage = CC.translate("&a" + winningPlayer.getName() + " (" + newWinnerElo + ") (+" + winnerEloChange + ") &7has beaten &c" + losingPlayer.getName() +
                    " (" + newLoserElo + ") (-" + loserEloChange + ")");

            for (Player player : new Player[]{winningPlayer}) {
                player.sendMessage(eloMessage);
            }

            for (Player player : new Player[]{losingPlayer}) {
                player.sendMessage(eloMessage);
            }

            PlayerData playerData = Core.INSTANCE.getPlayerManagement().getPlayerData(winningPlayer.getUniqueId());

            winningProfile.setRankedWins(winningProfile.getRankedWins() + 1);
            winningProfile.setKills(winningProfile.getKills() + 1);

            losingProfile.setRankedLoss(winningProfile.getRankedLoss() + 1);
            losingProfile.setKills(0);

            if (winningProfile.getKills() > winningProfile.getWinStreak()) {
                winningProfile.setWinStreak(winningProfile.getKills());
            }

            playerData.addCoins(5);
            winningPlayer.sendMessage(CC.translate("&cYou have earned &75 &ccoins."));

            String message = "&7" + winningPlayer.getName() + " &ehas upranked to " + EloUtil.getEloRange(newWinnerElo) + " &ein kit &7" + ChatColor.stripColor(getKit().getDisplayName()) + ".";

            if (message.contains("Bronze") || message.contains("Silver")) return true;

            if (!oldRangeWinner.equals(newRangeWinner)) {
                Bukkit.getServer().getOnlinePlayers().forEach(players -> players.sendMessage(CC.translate(message)));
            }
        }
        return true;
    }

    private void saveLogs(Player winner, Player loser, String health) {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        String matchType = this.getQueueType() == QueueType.RANKED ? "Ranked" : "Unranked";

        PlayerStats winnerStats = PlayerStats.getByUuid(winner.getUniqueId());
        PlayerStats loserStats = PlayerStats.getByUuid(loser.getUniqueId());

        Profile winningProfile = Profile.getByUuid(winner.getUniqueId());
        Profile losingProfile = Profile.getByUuid(loser.getUniqueId());

        winnerStats.getMatchLogs().add(new MatchLogs(getMatchsPlayed(winningProfile.getMatchsPlayed()), winner.getName(), loser.getName(),
                health, format.format(now), matchType, getKit().getDisplayName(), getKit().getDisplayIcon().getType().toString(), TimeUtil.millisToTimer(getElapsedDuration()),
                false,0,0,0,""));

        loserStats.getMatchLogs().add(new MatchLogs(getMatchsPlayed(losingProfile.getMatchsPlayed()), winner.getName(), loser.getName(),
                health, format.format(now), matchType, getKit().getDisplayName(), getKit().getDisplayIcon().getType().toString(), TimeUtil.millisToTimer(getElapsedDuration()),
                false,0,0,0,""));
    }

    private int getMatchsPlayed(int amount) {
        return (amount == 0 ? 1 : amount + 1);
    }

    @Override
    public boolean canEnd() {
        return !playerA.isAlive() || !playerB.isAlive();
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
        if (player == null) return null;

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
        return playerARoundWins + playerBRoundWins;
    }

    @Override
    public int getRoundsNeeded(TeamPlayer teamPlayer) {
        if (playerA.equals(teamPlayer)) {
            return 3 - playerARoundWins;
        } else if (playerB.equals(teamPlayer)) {
            return 3 - playerBRoundWins;
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
        if (!getKit().getGameRules().isSumo()) {

            TeamPlayer teamPlayer = getTeamPlayer(deadPlayer);

            MatchSnapshot snapshot = new MatchSnapshot(deadPlayer, true);
            snapshot.setPotionsMissed(teamPlayer.getPotionsMissed());
            snapshot.setPotionsThrown(teamPlayer.getPotionsThrown());
            snapshot.setLongestCombo(teamPlayer.getLongestCombo());
            snapshot.setTotalHits(teamPlayer.getHits());

            // Add snapshot to list
            getSnapshots().add(snapshot);

        } else if (getKit().getGameRules().isSumo() && getRoundsNeeded(getPlayerA()) == 0 || getRoundsNeeded(getPlayerB()) == 0) {
            PlayerUtil.reset(deadPlayer);

            for (Player otherPlayer : getPlayersAndSpectators()) {
                Profile profile = Profile.getByUuid(otherPlayer.getUniqueId());
                profile.handleVisibility(otherPlayer, deadPlayer);
            }
        }
    }

    @Override
    public void onRespawn(Player player) {
        TaskUtil.runLater(() -> {
            player.setFireTicks(0);
            player.updateInventory();

            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.getMatch() != null) {
                profile.setState(ProfileState.IN_LOBBY);
                profile.setMatch(null);
                TaskUtil.runSync(profile::refreshHotbar);
                profile.handleVisibility();
                Ghoul.getInstance().getKnockbackManager().getKnockbackType().applyDefaultKnockback(player);

                Essentials.teleportToSpawn(player);
            }
        }, 2L);
    }

    @Override
    public org.bukkit.ChatColor getRelationColor(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return org.bukkit.ChatColor.GREEN;
        }

        return org.bukkit.ChatColor.RED;
    }
}
