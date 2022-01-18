package net.ghoul.practice.match.listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ExtraListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        Material material = event.getItem().getType();
        switch (material) {
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_BOOTS:
                event.setDamage(getRandom(1,2));
                break;
        }
    }

    public static int getRandom(int lower, int upper) {
        Random random = new Random();
        return random.nextInt((upper - lower) + 1) + lower;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Projectile)) return;
        Projectile projectile = (Projectile) e.getDamager();
        if (!(projectile.getShooter() instanceof Player && e.getEntity() instanceof Player)) return;
        Player player = (Player) projectile.getShooter();
        if (player.getItemInHand().getType() != Material.FISHING_ROD) return;
        e.setCancelItemDamage(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void rodFix(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Projectile)) return;
        Projectile projectile = (Projectile)e.getDamager();
        if (!(projectile.getShooter() instanceof Player)) return;
        Player player = (Player)projectile.getShooter();
        player.getItemInHand().setDurability((short)(player.getItemInHand().getDurability() - 1));
    }

    @EventHandler
    public void fishingThrow(ProjectileLaunchEvent e) {
        Projectile hook = e.getEntity();
        if (e.getEntityType().equals(EntityType.FISHING_HOOK))
            hook.setVelocity(hook.getVelocity().multiply(1.05));
    }

    @EventHandler
    public void ItemBreakEvent(PlayerItemBreakEvent e) {
        Player p = e.getPlayer();
        p.playSound(p.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack itemStack = event.getEntity().getItemStack();
        if (itemStack.getType() == Material.SAPLING || itemStack.getType() == Material.SEEDS)
            event.setCancelled(true);
    }
}
