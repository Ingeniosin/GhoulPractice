package net.ghoul.practice.match.listener;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.arena.impl.StandaloneArena;
import net.ghoul.practice.arena.impl.TheBridgeArena;
import net.ghoul.practice.enums.HotbarType;
import net.ghoul.practice.hotbar.Hotbar;
import net.ghoul.practice.kit.KitInventory;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.MatchState;
import net.ghoul.practice.match.events.MatchStartEvent;
import net.ghoul.practice.match.menu.InventoryViewerMenu;
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.types.SoloMatch;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.BlockUtil;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.Cooldown;
import net.ghoul.practice.util.external.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

public class MatchListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        if (!event.hasItem()) {
            event.setCancelled(true);
            return;
        }
        if (event.getItem().getItemMeta().getDisplayName() == null) return;
        String itemName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
        if (profile.getMatch() == null) return;

        if (itemName.equals(ChatColor.stripColor("Stop Spectating"))) {
            profile.getMatch().removeSpectator(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (player.getItemInHand() == null) return;
        if (player.getItemInHand().getItemMeta() == null) return;
        if (player.getItemInHand().getItemMeta().getDisplayName() == null) return;

        if (profile.getMatch() == null) return;

        Entity entity = event.getRightClicked();
        if (!(entity instanceof Player)) return;

        Player target = (Player) entity;
        Profile profileTarget = Profile.getByUuid(target.getUniqueId());

        if (profileTarget.getMatch() == null) return;

        if (profileTarget.getMatch().getQueueType() == QueueType.RANKED) {
            player.sendMessage(CC.translate("&cYou cannot see inventories in ranked matches."));
            event.setCancelled(true);
            return;
        }

        String itemName = ChatColor.stripColor(player.getItemInHand().getItemMeta().getDisplayName());
        if (itemName.equals(ChatColor.stripColor("View Inventory (Right Click)"))) {
            new InventoryViewerMenu(target).open(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlaceEvent(final BlockPlaceEvent event) {
        final Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.isInFight()) {
            final Match match = profile.getMatch();
            if (match.getKit().getGameRules().isBuild() && profile.getMatch().isFighting()) {
                if (match.getKit().getGameRules().isSpleef()) {
                    event.setCancelled(true);
                    return;
                }
                final Arena arena = match.getArena();
                final int y = (int) event.getBlockPlaced().getLocation().getY();
                if (y > arena.getMaxBuildHeight()) {
                    if (!arena.getName().contains("cavefight") && !arena.getName().contains("nether") && !arena.getName().contains("fortress")) {
                        event.getPlayer().sendMessage(CC.RED + "You have reached the maximum build height.");
                        event.setCancelled(true);
                        return;
                    }
                }
                if (arena instanceof TheBridgeArena) {
                    TheBridgeArena standaloneArena = (TheBridgeArena) arena;
                    if (standaloneArena.getBlueCuboid() != null && standaloneArena.getBlueCuboid().contains(event.getBlockPlaced())) {
                        event.setCancelled(true);
                        return;
                    }
                    if (standaloneArena.getRedCuboid() != null && standaloneArena.getRedCuboid().contains(event.getBlockPlaced())) {
                        event.setCancelled(true);
                        return;
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.isInFight()) {
            Match match = profile.getMatch();
            if (match.getKit().getGameRules().isSpleef()) {
                if (material == Material.SNOW_BLOCK || material == Material.SNOW) {
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                    event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 1));
                    event.getPlayer().updateInventory();
                } else {
                    event.setCancelled(true);
                }
            } else if (match.getKit().getGameRules().isBuild() && profile.getMatch().isFighting()) {
                if (match.getKit().getName().equalsIgnoreCase("cavefight")) return;
                if (canBreak(material)) return;
            }
            event.setCancelled(true);
        }
    }

    private boolean canBreak(Material material) {
        return material.toString().equals("RED_ROSE") || material.toString().equals("YELLOW_FLOWER") || material.toString().equals("DEAD_BUSH") ||
                material.toString().equals("LONG_GRASS") || material.toString().equals("DOUBLE_PLANT") || material == Material.FIRE ||
                material == Material.CACTUS || material == Material.COBBLE_WALL || material == Material.WOOD || material == Material.COBBLESTONE ||
                material == Material.OBSIDIAN;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmptyEvent(final PlayerBucketEmptyEvent event) {
        final Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.isInFight()) {
            final Match match = profile.getMatch();
            if (match.getKit().getGameRules().isBuild() && profile.getMatch().isFighting()) {
                final Arena arena = match.getArena();
                final Block block = event.getBlockClicked().getRelative(event.getBlockFace());
                final int y = (int) block.getLocation().getY();
                if (y > arena.getMaxBuildHeight()) {
                    if (!arena.getName().contains("cavefight") && !arena.getName().contains("nether") && !arena.getName().contains("fortress")) {
                        event.getPlayer().sendMessage(CC.RED + "You have reached the maximum build height.");
                        event.setCancelled(true);
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPickupItemEvent(final PlayerPickupItemEvent event) {
        final Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.isSpectating()) {
            event.setCancelled(true);
            return;
        }
        if (profile.isInFight()) {
            if (!profile.getMatch().getTeamPlayer(event.getPlayer()).isAlive()) {
                event.setCancelled(true);
                return;
            }
            if (event.getItem().getItemStack().getType().name().contains("BOOK")) {
                event.setCancelled(true);
            }
        }
    }

    //TODO: Finish up the new Armor Class selector so that you wont have to use these checks
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItemEvent(final PlayerDropItemEvent event) {
        final Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.isSpectating()) {
            event.setCancelled(true);
        }
        if (event.getItemDrop().getItemStack().getType() == Material.BOOK || event.getItemDrop().getItemStack().getType() == Material.ENCHANTED_BOOK) {
            event.setCancelled(true);
            return;
        }
        if (event.getItemDrop().getItemStack().getType() == Material.INK_SACK) {
            event.getItemDrop().remove();
            return;
        }
        if (event.getItemDrop().getItemStack().isSimilar(Hotbar.getItems().get(HotbarType.DIAMOND_KIT))) {
            event.setCancelled(true);
            return;
        }
        if (event.getItemDrop().getItemStack().isSimilar(Hotbar.getItems().get(HotbarType.BARD_KIT))) {
            event.setCancelled(true);
            return;
        }
        if (event.getItemDrop().getItemStack().isSimilar(Hotbar.getItems().get(HotbarType.ARCHER_KIT))) {
            event.setCancelled(true);
            return;
        }
        if (event.getItemDrop().getItemStack().isSimilar(Hotbar.getItems().get(HotbarType.ROGUE_KIT))) {
            event.setCancelled(true);
            return;
        }
        if (profile.isInSomeSortOfFight()) {
            if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) {
                event.getItemDrop().setTicksLived(5940);
                return;
            }
            if (profile.getMatch() != null) {
                profile.getMatch().getEntities().add(event.getItemDrop());
            }
        }
    }

    //TODO: Finish up the new Armor Class selector so that you wont have to use these checks
    @EventHandler
    public void onItemSpawnEvent(final ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().isSimilar(Hotbar.getItems().get(HotbarType.DIAMOND_KIT))) {
            event.setCancelled(true);
            event.getEntity().remove();
        } else if (event.getEntity().getItemStack().isSimilar(Hotbar.getItems().get(HotbarType.BARD_KIT))) {
            event.setCancelled(true);
            event.getEntity().remove();
        } else if (event.getEntity().getItemStack().isSimilar(Hotbar.getItems().get(HotbarType.ARCHER_KIT))) {
            event.setCancelled(true);
            event.getEntity().remove();
        } else if (event.getEntity().getItemStack().isSimilar(Hotbar.getItems().get(HotbarType.ROGUE_KIT))) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

    @EventHandler// un fix rapidito :V
    public void onEntityFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isInFight() && profile.getMatch() != null) {
            Match match = profile.getMatch();
            if (match.getDuration().equals("Starting") || match.getDuration().equals("00:00")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        Player player = event.getEntity();
        Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

        player.getNearbyEntities(50, 50, 50).forEach(entity -> {
            if (entity instanceof Horse) {
                entity.remove();
            }
        });

        Ghoul.getInstance().getKnockbackManager().getKnockbackType().applyDefaultKnockback(player);
        event.getEntity().getPlayer().setNoDamageTicks(20);
        if (profile.isInFight() && profile.getMatch() != null) {

            if (profile.getMatch() instanceof SoloMatch) {
                event.getDrops().clear();
            }

            if (PlayerUtil.getLastDamager(event.getEntity()) instanceof CraftPlayer) {
                final Player killer = (Player) PlayerUtil.getLastDamager(event.getEntity());
                profile.getMatch().handleDeath(event.getEntity(), killer, false);
            } else {
                profile.getMatch().handleDeath(event.getEntity(), event.getEntity().getKiller(), false);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(event.getPlayer().getLocation());
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        if (profile.isInFight()) {
            profile.getMatch().handleRespawn(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunchEvent(final ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof ThrownPotion && event.getEntity().getShooter() instanceof Player) {
            final Player shooter = (Player) event.getEntity().getShooter();
            final Profile shooterData = Profile.getByUuid(shooter.getUniqueId());
            if (shooterData.isInFight() && shooterData.getMatch().isFighting()) {
                shooterData.getMatch().getTeamPlayer(shooter).incrementPotionsThrown();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHitEvent(final ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow && event.getEntity().getShooter() instanceof Player) {
            final Player shooter = (Player) event.getEntity().getShooter();
            final Profile shooterData = Profile.getByUuid(shooter.getUniqueId());
            if (shooterData.isInFight()) {
                shooterData.getMatch().getEntities().add(event.getEntity());
                shooterData.getMatch().getTeamPlayer(shooter).handleHit();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPotionSplashEvent(final PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player) {
            final Player shooter = (Player) event.getPotion().getShooter();
            final Profile shooterData = Profile.getByUuid(shooter.getUniqueId());
            if (shooterData.isSpectating()) {
                event.setCancelled(true);
            }
            if (shooterData.isInFight() && event.getIntensity(shooter) <= 0.5) {
                shooterData.getMatch().getTeamPlayer(shooter).incrementPotionsMissed();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            final Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());
            if (profile.isInFight() && !profile.getMatch().getKit().getGameRules().isHealthRegeneration()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Profile profile = Profile.getByUuid(player.getUniqueId());
        if (profile.getMatch() != null) {
            Match match = profile.getMatch();
            if (profile.getMatch().getKit() != null) {
                if (profile.getMatch().isSumoMatch() || profile.getMatch().getKit().getGameRules().isSumo()) {
                    if (match.getState() == MatchState.STARTING) {
                        Location from = event.getFrom();
                        Location to = event.getTo();
                        if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
                            player.teleport(from);
                            ((CraftPlayer) player).getHandle().playerConnection.checkMovement = false;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWater(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Profile profile = Profile.getByUuid(player);
        if (profile.getMatch() != null && profile.getMatch().isSumoMatch() || profile.getMatch() != null) {
            Match match = profile.getMatch();
            if (BlockUtil.isOnLiquid(to, 0) || BlockUtil.isOnLiquid(to, 1)) {
                match.handleDeath(player, match.getOpponentPlayer(player), false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.isInFight()) {
                final Match match = profile.getMatch();
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (profile.getMatch().getKit().getGameRules().isVoidspawn()) {
                        event.setDamage(0.0);
                        player.setFallDistance(0);
                        player.setHealth(20.0);
                        player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                        return;
                    }
                    profile.getMatch().handleDeath(player, null, false);
                    return;
                }
                if (!profile.getMatch().isFighting()) {
                    event.setCancelled(true);
                    return;
                }

                if (profile.getMatch().isTeamMatch() && !profile.getMatch().getTeamPlayer(player).isAlive()) {
                    event.setCancelled(true);
                    return;
                }
                if (profile.getMatch().getKit().getGameRules().isSumo()) {
                    event.setDamage(0.0);
                    player.setHealth(20.0);
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        Player attacker;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else {
            if (!(event.getDamager() instanceof Projectile)) {
                event.setCancelled(true);
                return;
            }
            if (!(((Projectile) event.getDamager()).getShooter() instanceof Player)) {
                event.setCancelled(true);
                return;
            }
            attacker = (Player) ((Projectile) event.getDamager()).getShooter();
        }
        if (attacker != null && event.getEntity() instanceof Player) {
            final Player damaged = (Player) event.getEntity();
            final Profile damagedProfile = Profile.getByUuid(damaged.getUniqueId());
            final Profile attackerProfile = Profile.getByUuid(attacker.getUniqueId());
            if (attackerProfile.isSpectating() || damagedProfile.isSpectating()) {
                event.setCancelled(true);
                return;
            }
            if (damagedProfile.isInFight() && attackerProfile.isInFight()) {
                final Match match = attackerProfile.getMatch();
                if (!damagedProfile.getMatch().getMatchId().equals(attackerProfile.getMatch().getMatchId())) {
                    event.setCancelled(true);
                    return;
                }
                if (!match.getTeamPlayer(damaged).isAlive() || (!match.getTeamPlayer(attacker).isAlive() && !match.isFreeForAllMatch())) {
                    event.setCancelled(true);
                    return;
                }
                if (match.isSoloMatch() || match.isFreeForAllMatch() || match.isSumoMatch()) {
                    attackerProfile.getMatch().getTeamPlayer(attacker).handleHit();
                    damagedProfile.getMatch().getTeamPlayer(damaged).resetCombo();

                    if (match.getKit().getName().toLowerCase().contains("boxing") || match.getKit().getName().toLowerCase().contains("spleef")) {
                        event.setDamage(0);
                    }

                    if (event.getDamager() instanceof Arrow) {
                        double health = Math.ceil(damaged.getHealth() - event.getFinalDamage()) / 2.0D;
                        if (match.getKit().getGameRules().isBowhp()) {
                            if (!attacker.getName().equalsIgnoreCase(damaged.getName())) {
                                if (event.getDamage() > 0.0) {
                                    attacker.sendMessage("§6" + damaged.getName() + "§3 is now at §6" + health + "§4❤");
                                }
                            }
                        }
                    }
                } else if (match.isTeamMatch()) {
                    final Team attackerTeam = match.getTeam(attacker);
                    final Team damagedTeam = match.getTeam(damaged);
                    if (attackerTeam == null || damagedTeam == null) {
                        event.setCancelled(true);
                    } else if (attackerTeam.equals(damagedTeam)) {
                        if (!damaged.getUniqueId().equals(attacker.getUniqueId())) {
                            event.setCancelled(true);
                        }
                    } else {
                        attackerProfile.getMatch().getTeamPlayer(attacker).handleHit();
                        damagedProfile.getMatch().getTeamPlayer(damaged).resetCombo();
                        if (event.getDamager() instanceof Arrow) {
                            final double health2 = Math.ceil(damaged.getHealth() - event.getFinalDamage()) / 2.0;
                            if (match.getKit() == null || match.getKit().getGameRules().isBowhp()) {
                                if (!attacker.getName().equalsIgnoreCase(damaged.getName())) {
                                    if (event.getDamage() > 0.0) {
                                        attacker.sendMessage("§6" + damaged.getName() + "§3 is now at §6" + health2 + "§4❤");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(final PlayerItemConsumeEvent event) {
        if (event.getItem().getType().equals(Material.POTION)) {
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Ghoul.getInstance(), () -> event.getPlayer().setItemInHand(new ItemStack(Material.AIR)), 1L);
        }
        if (event.getItem().getType() == Material.GOLDEN_APPLE && event.getItem().hasItemMeta() && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("Golden Head")) {
            final Player player = event.getPlayer();
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
            player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
        }
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.isInSomeSortOfFight()) {
                if (profile.getMatch() != null) {
                    if (profile.getMatch().getKit().getGameRules().isAntifoodloss()) {
                        if (event.getFoodLevel() >= 20) {
                            event.setFoodLevel(20);
                            player.setSaturation(20.0f);
                        }
                    } else {
                        event.setCancelled(false);
                    }
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(final PlayerQuitEvent event) {
        final Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.isInFight() && profile.getMatch().getState() == MatchState.FIGHTING) {
            profile.getMatch().handleDeath(event.getPlayer(), null, true);
        } else if (profile.isInMatch() && profile.getMatch().getState() == MatchState.FIGHTING) {
            profile.getMatch().handleDeath(event.getPlayer(), null, true);
        } else if (profile.isInMatch() || profile.isInFight() && profile.getMatch().getState() == MatchState.STARTING) {
            profile.getMatch().getTask().cancel();
            profile.getMatch().handleDeath(event.getPlayer(), null, true);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Profile profile = Profile.getByUuid(event.getWhoClicked().getUniqueId());
        if (profile.isSpectating()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(final InventoryInteractEvent event) {
        final Profile profile = Profile.getByUuid(event.getWhoClicked().getUniqueId());
        if (profile.isSpectating()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        final Projectile projectile = event.getEntity();
        if (projectile instanceof EnderPearl) {
            final EnderPearl enderPearl = (EnderPearl) projectile;
            final ProjectileSource source = enderPearl.getShooter();
            if (source instanceof Player) {
                final Player shooter = (Player) source;
                final Profile profile = Profile.getByUuid(shooter.getUniqueId());
                if (profile.isInFight()) {
                    if (!profile.getEnderpearlCooldown().hasExpired()) {
                        final String time = TimeUtil.millisToSeconds(profile.getEnderpearlCooldown().getRemaining());
                        final String context = "second" + (time.equalsIgnoreCase("1.0") ? "" : "s");
                        shooter.sendMessage(CC.RED + "You are on pearl cooldown for " + time + " " + context);
                        shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                        event.setCancelled(true);
                    } else {
                        profile.setEnderpearlCooldown(new Cooldown(16000L));
                        profile.getMatch().onPearl(shooter, enderPearl);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleportPearl(final PlayerTeleportEvent event) {
        final Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && profile.isInFight()) {
            profile.getMatch().removePearl(event.getPlayer());
        }
    }

    //TODO: Finish up the new Armor Class selector so that you wont have to use this at all
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        final Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.isSpectating()) {
            event.setCancelled(true);
        }
        if (event.getItem() != null && event.getAction().name().contains("RIGHT") && profile.isInFight()) {
            if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
                if (event.getItem().equals(Hotbar.getItems().get(HotbarType.DEFAULT_KIT))) {
                    final KitInventory kitInventory = profile.getMatch().getKit().getKitInventory();
                    event.getPlayer().getInventory().setArmorContents(kitInventory.getArmor());
                    event.getPlayer().getInventory().setContents(kitInventory.getContents());
                    event.getPlayer().getActivePotionEffects().clear();
                    if (profile.getMatch().getKit().getKitInventory().getEffects() != null) {
                        event.getPlayer().addPotionEffects(profile.getMatch().getKit().getKitInventory().getEffects());
                    }
                    event.getPlayer().updateInventory();
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName() && event.getItem().getItemMeta().hasLore()) {
                final String displayName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                if (displayName.endsWith(" (Right-Click)")) {
                    final String kitName = displayName.replace(" (Right-Click)", "");
                    for (final KitInventory kitInventory2 : profile.getStatisticsData().get(profile.getMatch().getKit()).getLoadouts()) {
                        if (kitInventory2 != null && ChatColor.stripColor(kitInventory2.getCustomName()).equals(kitName)) {
                            event.getPlayer().getInventory().setArmorContents(kitInventory2.getArmor());
                            event.getPlayer().getInventory().setContents(kitInventory2.getContents());
                            event.getPlayer().getActivePotionEffects().clear();
                            event.getPlayer().addPotionEffects(profile.getMatch().getKit().getKitInventory().getEffects());
                            event.getPlayer().updateInventory();
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            final Player player = event.getPlayer();
            if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && player.getItemInHand().getType() == Material.MUSHROOM_SOUP) {
                final int health = (int) player.getHealth();
                if (health == 20) {
                    player.getItemInHand().setType(Material.MUSHROOM_SOUP);
                } else if (health >= 13) {
                    player.setHealth(20.0);
                    player.getItemInHand().setType(Material.BOWL);
                } else {
                    player.setHealth(health + 7);
                    player.getItemInHand().setType(Material.BOWL);
                }
            }
            if ((event.getItem().getType() == Material.ENDER_PEARL || (event.getItem().getType() == Material.POTION && event.getItem().getDurability() >= 16000)) && profile.isInFight() && profile.getMatch().isStarting()) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
            if (event.getItem().getType() == Material.ENDER_PEARL && event.getClickedBlock() == null) {
                if (!profile.isInFight() || (profile.isInFight() && !profile.getMatch().isFighting())) {
                    event.setCancelled(true);
                    return;
                }
                if (profile.getMatch().isStarting()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(final PotionSplashEvent event) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                final Player player = (Player) entity;
                final Profile profile = Profile.getByUuid(player.getUniqueId());
                if (!profile.isSpectating()) {
                    continue;
                }
                event.setIntensity(player, 0.0);
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(final PlayerQuitEvent e) {
        final Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
        if (profile.isSpectating()) {
            profile.getMatch().removeSpectator(e.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamageEntity(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player damaged = (Player) e.getEntity();
            Profile damagedProfile = Profile.getByUuid(damaged.getUniqueId());
            if (damagedProfile.getMatch() != null) {
                Match match = damagedProfile.getMatch();
                if (match.isEnding()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPearlThrow(final ProjectileLaunchEvent event) {
        final Projectile projectile = event.getEntity();
        if (projectile instanceof EnderPearl) {
            final EnderPearl enderPearl = (EnderPearl) projectile;
            final ProjectileSource source = enderPearl.getShooter();
            if (source instanceof Player) {
                final Player shooter = (Player) source;
                final Profile profile = Profile.getByUuid(shooter.getUniqueId());
                if (profile.getMatch() != null && profile.getMatch().getArena() != null) {
                    if (profile.getMatch().getArena().isDisablePearls()) {
                        if (!profile.getEnderpearlCooldown().hasExpired()) {
                            shooter.sendMessage(CC.RED + "You can't pearl in this arena!");
                            shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
