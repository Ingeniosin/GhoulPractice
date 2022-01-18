package net.ghoul.practice.profile;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.GhoulCache;
import net.ghoul.practice.ghoul.essentials.Essentials;
import net.ghoul.practice.ghoul.essentials.event.SpawnTeleportEvent;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.MatchState;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.TaskUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.sitUtil.SitUtil;
import net.ghoulnetwork.core.Core;
import net.ghoulnetwork.core.managers.player.PlayerStats;
import net.ghoulnetwork.core.utilities.Utilities;
import net.ghoulnetwork.core.utilities.general.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.StringUtil;
import java.util.List;

public class ProfileListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getClickedBlock() == null) {
            return;
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem().getType() == Material.PAINTING) {
                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (!e.getPlayer().isOp())
                        e.setCancelled(true);
                }
            }

            if (e.getClickedBlock().getState() instanceof ItemFrame) {
                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (!e.getPlayer().isOp()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (!profile.isInSomeSortOfFight()) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (!(profile.isInSomeSortOfFight())) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (!profile.isInSomeSortOfFight()) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.isInSomeSortOfFight()) {
            if (!profile.isInFight()) {
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    if (!event.getPlayer().isOp()) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        } else {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void itemFrameItemRemoval(EntityDamageEvent e) {
        if (e.getEntity() instanceof ItemFrame) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketFill(PlayerBucketFillEvent event) {
        event.getPlayer().updateInventory();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        event.getPlayer().updateInventory();

        if (profile.isInSomeSortOfFight()) {
            if (!profile.isInFight()) {
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    if (!event.getPlayer().isOp()) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        } else {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void inventoryClickEvent(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            Player player = (Player) event.getWhoClicked();
            if (player.getGameMode() == GameMode.CREATIVE) return;
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getKitEditor().isActive()) return;

            if (profile.isInLobby()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

            if (profile.isInLobby() || profile.isInQueue()) {
                event.setCancelled(true);

                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    Essentials.teleportToSpawn((Player) event.getEntity());
                }
            }
        }
    }

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

            if (profile.isInLobby() || profile.isInQueue()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onSpawnTeleportEvent(SpawnTeleportEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        SitUtil.unSitPlayer(player);

        if (!profile.isBusy() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            PlayerUtil.reset(event.getPlayer(), false);
            player.getActivePotionEffects().clear();
            player.setHealth(20.0D);
            player.setFoodLevel(20);
            profile.handleVisibility();
            profile.refreshHotbar();
            TaskUtil.runLater(profile::refreshHotbar, 5L);
        }

        if (!player.isOnline()) return;

        Objective objective = player.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);

        if (objective != null) {
            objective.unregister();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        player.teleport(Essentials.spawn);

        Profile profile = new Profile(player.getUniqueId());
        if (!GhoulCache.getPlayerCache().containsKey(player.getName())) {
            GhoulCache.getPlayerCache().put(player.getName(), player.getUniqueId());
        }
        try {
            profile.load();
        } catch (Exception e) {
            e.printStackTrace();
            event.getPlayer().kickPlayer(CC.RED + "Failed to load your profile, please contact an Administrator!");
            return;
        }
        Profile.getProfiles().put(player.getUniqueId(), profile);
        profile.setName(player.getName().toLowerCase());
        Essentials.teleportToSpawn(player);
        profile.getKitEditor().setActive(false);
        profile.getKitEditor().setRename(false);
        profile.refreshHotbar();
        profile.setClickAmount(0);
        TaskUtil.runLaterAsync(profile::handleVisibility, 5L);

        player.sendMessage(CC.translate("&8&m---------------------------"));
        player.sendMessage(CC.translate("&cTop Global wins 10$"));
        player.sendMessage(CC.translate("&cTop FinalFight wins Ghoul++"));
        player.sendMessage(CC.translate("&cTop CaveFight wins Ghoul++"));
        player.sendMessage(CC.translate("&8&m---------------------------"));

        TaskUtil.runLater(() -> Ghoul.getInstance().getNameTagManagement().createScoreboard(player), 60L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.getMatch() != null && profile.getState() == ProfileState.IN_FIGHT) {
            Match match = profile.getMatch();
            if (match.getState() == MatchState.STARTING || match.getState() == MatchState.FIGHTING) {
                profile.getMatch().onDisconnect(event.getPlayer());
            }
        }
        if (profile.isInQueue()) {
            profile.getQueue().removePlayer(profile.getQueueProfile());
        }
        profile.save();
        PlayerStats playerStats = PlayerStats.getByUuid(event.getPlayer().getUniqueId());

        Tasks.runAsync(Ghoul.getInstance(), () -> {
            playerStats.save();
            Ghoul.getInstance().getNameTagManagement().unregister(event.getPlayer());
        });
    }

    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent event) {
        if (event.getReason() != null) {
            if (event.getReason().contains("Flying is not enabled")) {
                event.setCancelled(true);
            }
        }
        Tasks.runAsync(Ghoul.getInstance(), () -> Ghoul.getInstance().getNameTagManagement().unregister(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPressurePlate(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent event) {
        List<String> completions = (List<String>) event.getTabCompletions();
        completions.clear();
        String token = event.getLastToken();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (StringUtil.startsWithIgnoreCase(p.getName(), token)) {
                completions.add(p.getName());
            }
        }
    }
}
