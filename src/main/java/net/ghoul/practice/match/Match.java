package net.ghoul.practice.match;

import lombok.Getter;
import lombok.Setter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.ghoul.essentials.Essentials;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.match.events.MatchEndEvent;
import net.ghoul.practice.match.events.MatchSpectatorJoinEvent;
import net.ghoul.practice.match.events.MatchSpectatorLeaveEvent;
import net.ghoul.practice.match.events.MatchStartEvent;
import net.ghoul.practice.match.task.*;
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.profile.ProfileState;
import net.ghoul.practice.queue.Queue;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ChatComponentBuilder;
import net.ghoul.practice.util.external.TimeUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.*;

@Getter
public abstract class Match {

    @Getter protected static List<Match> matches = new ArrayList<>();
    private final UUID matchId = UUID.randomUUID();
    private final net.ghoul.practice.queue.Queue queue;
    private final Kit kit;
    private final Arena arena;
    private final QueueType queueType;
    public Map<UUID, EnderPearl> pearlMap = new HashMap<>();
    @Setter public MatchState state = MatchState.STARTING;
    private final List<MatchSnapshot> snapshots = new ArrayList<>();
    public final List<UUID> spectators = new ArrayList<>();
    private final List<Entity> entities = new ArrayList<>();
    @Getter public BukkitTask task;
    @Setter private long startTimestamp;
    @Getter @Setter
    private BukkitTask matchWaterCheck;

    public Match(Queue queue, Kit kit, Arena arena, QueueType queueType) {
        this.queue = queue;
        this.kit = kit;
        this.arena = arena;
        this.queueType = queueType;

        matches.add(this);
    }

    public static void preload() {
        new MatchPearlCooldownTask().runTaskTimerAsynchronously(Ghoul.getInstance(), 2L, 2L);
        new MatchSnapshotCleanupTask().runTaskTimerAsynchronously(Ghoul.getInstance(), 20L * 5, 20L * 5);
    }

    public static void cleanup() {
        for (Match match : matches) {
            new MatchResetQueue(match.getArena()).start();
        }
        for (World world : Bukkit.getWorlds()){
            world.getEntities().forEach(entity -> {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            });
        }
    }

    public void start() {
        for (Player player : getPlayers()) {

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setState(ProfileState.IN_FIGHT);

            profile.setMatch(this);

            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                profile.handleVisibility(player, otherPlayer);
            }

            if (!getArena().getSpawn1().getChunk().isLoaded() || !getArena().getSpawn2().getChunk().isLoaded()) {
                getArena().getSpawn1().getChunk().load();
                getArena().getSpawn2().getChunk().load();
            }
            setupPlayer(player);
        }

        onStart();

        for (Player player : this.getPlayers()) {
            if (!Profile.getByUuid(player.getUniqueId()).getSentDuelRequests().isEmpty()) {
                Profile.getByUuid(player.getUniqueId()).getSentDuelRequests().clear();
            }
        }

        state = MatchState.STARTING;
        startTimestamp = -1;
        arena.setActive(true);

        if (!this.isSumoMatch() || !this.isSoloMatch()) {
            if (this.isFreeForAllMatch() || this.isTeamMatch()) {
                this.broadcastMessage(CC.RED + "The match has started!");
            }
        }

        if (getKit() != null) {
            if (getKit().getGameRules().isWaterkill() || getKit().getGameRules().isSumo()) {
                matchWaterCheck = new MatchWaterCheckTask(this).runTaskTimer(Ghoul.getInstance(), 80L, 80L);
            }
        }

        task = new MatchStartTask(this).runTaskTimer(Ghoul.getInstance(), 20L, 20L);
        for (Player shooter : getPlayers()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Profile shooterData = Profile.getByUuid(shooter.getUniqueId());

                    if (shooterData.isInFight()) {
                        int potions = 0;
                        for (ItemStack item : shooter.getInventory().getContents()) {
                            if (item == null)
                                continue;
                            if (item.getType() == Material.AIR)
                                continue;
                            if (item.getType() != Material.POTION)
                                continue;
                            if (item.getDurability() != (short) 16421)
                                continue;
                            potions++;
                        }
                        shooterData.getMatch().getTeamPlayer(shooter).setPotions(potions);
                    } else {
                        cancel();
                    }

                }
            }.runTaskTimerAsynchronously(Ghoul.getInstance(), 0L, 5L);
        }
        final MatchStartEvent event = new MatchStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void end() {
        if (onEnd()) {
            state = MatchState.ENDING;
        } else {
            return;
        }

        Player winningPlayer = getWinningPlayer();

        if (winningPlayer != null && winningPlayer.isOnline()) {
            winningPlayer.getInventory().clear();
            winningPlayer.getInventory().setArmorContents(new ItemStack[4]);
            winningPlayer.getInventory().setContents(new ItemStack[36]);
            winningPlayer.getNearbyEntities(50, 50, 50).forEach(entity -> {
                if (entity instanceof Item) {
                    entity.remove();
                }
            });
        }

        getPlayers().forEach(this::removePearl);

        getSpectators().forEach(this::removeSpectator);
        entities.forEach(Entity::remove);

        //new MatchResetTask(this).runTask(Ghoul.getInstance());
        new MatchResetQueue(getArena()).start();

        getArena().setActive(false);

        matches.remove(this);

        final MatchEndEvent event = new MatchEndEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        Bukkit.getScheduler().runTaskLaterAsynchronously(Ghoul.getInstance(), () -> getPlayers().forEach(player -> ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0)), 10L);
        Bukkit.getScheduler().runTaskLaterAsynchronously(Ghoul.getInstance(), () -> getPlayers().forEach(player -> ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0)), 20L);
    }

    public void onDisconnect(Player dead) {
        // Don't continue if the match is already ending
        if (!(state == MatchState.STARTING || state == MatchState.FIGHTING)) return;

        TeamPlayer deadGamePlayer = getTeamPlayer(dead);

        if (deadGamePlayer != null) {
            deadGamePlayer.setDisconnected(true);

            if (deadGamePlayer.isAlive()) {
                onDeath(dead, (Player) PlayerUtil.getLastDamager(dead));
            }
        }
    }

    public void handleDeath(Player deadPlayer, Player killerPlayer, boolean disconnected) {
        TeamPlayer teamPlayer = this.getTeamPlayer(deadPlayer);

        if (teamPlayer == null) return;

        teamPlayer.setDisconnected(disconnected);

        if (!teamPlayer.isAlive()) return;

        teamPlayer.setAlive(false);

        List<Player> playersAndSpectators = getPlayersAndSpectators();

        for (Player player : playersAndSpectators) {
            if (teamPlayer.isDisconnected()) {
                player.sendMessage(CC.RED + deadPlayer.getName() + CC.GRAY + " has disconnected.");
                continue;
            }
            if (getQueueType() != QueueType.RANKED) {
                if (killerPlayer == null) {
                    player.sendMessage(CC.RED + deadPlayer.getName() + CC.GRAY + " has died.");
                } else {
                    String health = new DecimalFormat("#.#").format(Math.round((killerPlayer.getHealth() / 2) * 2.0D) / 2.0D);

                    player.sendMessage(CC.RED + deadPlayer.getName() + CC.GRAY + " was slain by " + CC.RED
                            + killerPlayer.getName() + CC.YELLOW + " (" + CC.WHITE + (health.equals("0") ? "0.5" : health) + CC.DARK_RED + "❤" + CC.YELLOW + ")");
                }
            }
        }

        onDeath(deadPlayer, killerPlayer);

        if ((isSumoMatch()) && disconnected) {
            end();
            return;
        }

        if (canEnd()) {
            end();
        }

        if (killerPlayer == null) return;

        killerPlayer.playSound(killerPlayer.getLocation(), Sound.LEVEL_UP, 0.5F, 5F);
    }

    public void addSpectator(Player player, Player target) {
        spectators.add(player.getUniqueId());
        PlayerUtil.spectator(player);
        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setMatch(this);
        profile.setState(ProfileState.SPECTATE_MATCH);
        profile.refreshHotbar();
        profile.handleVisibility();
        player.teleport(target.getLocation());
        player.spigot().setCollidesWithEntities(false);

        if (!profile.isSilent()) {
            for (Player otherPlayer : getPlayers()) {
                otherPlayer.sendMessage(CC.YELLOW + player.getName() + " is now spectating your match.");
            }
        }

        target.hidePlayer(player);

        Bukkit.getScheduler().runTaskLaterAsynchronously(Ghoul.getInstance(), () -> {
            if (this.isSoloMatch()) {
                target.hidePlayer(player);
                this.getOpponentPlayer(target).hidePlayer(player);
            }
            else if (this.isSumoMatch()) {
                target.hidePlayer(player);
                this.getOpponentPlayer(target).hidePlayer(player);
            } else if (this.isTeamMatch()) {
                for ( Player targetPlayers : this.getTeam(target).getPlayers() ) {
                    targetPlayers.hidePlayer(player);
                }
                for (Player targetPlayers : this.getOpponentTeam(target).getPlayers()) {
                    targetPlayers.hidePlayer(player);
                }
            } else if (this.isFreeForAllMatch()) {
                for (Player targetPlayers : this.getPlayers()) {
                    targetPlayers.hidePlayer(player);
                }
            }
        }, 2L);

        Bukkit.getPluginManager().callEvent(new MatchSpectatorJoinEvent(player, this));
    }

    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setState(ProfileState.IN_LOBBY);
        profile.setMatch(null);
        PlayerUtil.reset(player);
        profile.refreshHotbar();
        profile.handleVisibility();
        Essentials.teleportToSpawn(player);
        player.spigot().setCollidesWithEntities(true);

        if (state != MatchState.ENDING) {
            for (Player otherPlayer : getPlayers()) {
                if (!profile.isSilent()) {
                    otherPlayer.sendMessage(CC.RED + player.getName() + CC.YELLOW + " is no longer spectating your match.");
                }
            }
        }

        Bukkit.getPluginManager().callEvent(new MatchSpectatorLeaveEvent(player, this));
    }

    public void handleRespawn(Player player) {
        onRespawn(player);
    }

    public String replace(String input) {
        input = input.replace("{arena}", this.getArena().getName())
                .replace("{kit}", this.getKit().getName());

        return input;
    }

    public static int getInFights(Queue queue) {
        int i = 0;

        for (Match match : matches) {
            if (match.getQueue() != null && (match.isFighting() || match.isStarting())) {
                if (match.getQueue() != null && match.getQueue().equals(queue)) {
                    i = i + match.getTeamPlayers().size();
                }
            }
        }
        return i;
    }

    public boolean isStarting() {
        return state == MatchState.STARTING;
    }

    public boolean isFighting() {
        return state == MatchState.FIGHTING;
    }

    public boolean isEnding() {
        return state == MatchState.ENDING;
    }

    public void onPearl(Player player, EnderPearl pearl) {
        this.pearlMap.put(player.getUniqueId(), pearl);
    }

    public void removePearl(Player player) {
        final EnderPearl pearl;
        if (player != null) {
            if ((pearl = this.pearlMap.remove(player.getUniqueId())) != null) {
                pearl.remove();
            }
        }
    }

    public String getDuration() {
        if (isStarting()) {
            return "Starting";
        } else if (isEnding()) {
            return "Ending";
        } else {
            return TimeUtil.millisToTimer(getElapsedDuration());
        }
    }

    public List<Player> getPlayersAndSpectators() {
        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(getPlayers());
        allPlayers.addAll(getSpectators());
        return allPlayers;
    }

    protected HoverEvent getHoverEvent(TeamPlayer teamPlayer) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder("")
                .parse("&cClick to view " + teamPlayer.getUsername() + "'s inventory.").create());
    }

    protected ClickEvent getClickEvent(TeamPlayer teamPlayer) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewinv " + teamPlayer.getUuid().toString());
    }

    public long getElapsedDuration() {
        return System.currentTimeMillis() - startTimestamp;
    }

    public void broadcastMessage(String message) {
        getPlayers().forEach(player -> player.sendMessage(message));
        getSpectators().forEach(player -> player.sendMessage(message));
    }

    public void broadcastSound(Sound sound) {
        getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, 0.5F, 10F));
        getSpectators().forEach(player -> player.playSound(player.getLocation(), sound, 0.5F, 10F));
    }

    public List<Player> getSpectators() {
        return PlayerUtil.convertUUIDListToPlayerList(spectators);
    }

    public abstract boolean isSoloMatch();

    public abstract boolean isTeamMatch();

    public abstract boolean isFreeForAllMatch();

    public abstract boolean isSumoMatch();

    public abstract void setupPlayer(Player player);

    public abstract void cleanPlayer(Player player);

    public abstract void onStart();

    public abstract boolean onEnd();

    public abstract boolean canEnd();

    public abstract void onDeath(Player player, Player killer);

    public abstract void onRespawn(Player player);

    public abstract Player getWinningPlayer();

    public abstract Team getWinningTeam();

    public abstract TeamPlayer getTeamPlayerA();

    public abstract TeamPlayer getTeamPlayerB();

    public abstract List<TeamPlayer> getTeamPlayers();

    public abstract List<Player> getPlayers();

    public abstract List<Player> getAlivePlayers();

    public abstract Team getTeamA();

    public abstract Team getTeamB();

    public abstract Team getTeam(Player player);

    public abstract TeamPlayer getTeamPlayer(Player player);

    public abstract Team getOpponentTeam(Team Team);

    public abstract Team getOpponentTeam(Player player);

    public abstract TeamPlayer getOpponentTeamPlayer(Player player);

    public abstract Player getOpponentPlayer(Player player);

    public abstract int getTotalRoundWins();

    public abstract int getRoundsNeeded(TeamPlayer teamPlayer);

    public abstract int getRoundsNeeded(Team Team);

    public abstract ChatColor getRelationColor(Player viewer, Player target);
}