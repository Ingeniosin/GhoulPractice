package net.ghoul.practice.settings;

import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SettingsListener implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        String invName = ChatColor.stripColor(event.getInventory().getName());
        Inventory inventory = event.getInventory();
        Profile profile = Profile.getByUuid(player.getPlayer());

        if (!invName.equals("Settings")) return;

        inventory.setItem(8, new ItemBuilder(Material.DIAMOND_SWORD).name(CC.translate("&cAllowing duels: " +
                (profile.getSettings().isReceiveDuelRequests() ? "&7Enabled" : "&7Disabled"))).build());
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Profile profile = Profile.getByUuid(player.getPlayer());
        String invName = ChatColor.stripColor(event.getInventory().getName());

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        Material material = event.getCurrentItem().getType();

        if (!invName.equals("Settings")) return;

        event.setCancelled(true);

        switch (material) {
            case DIAMOND_SWORD:
                Button.playSuccess(player);
                profile.getSettings().setReceiveDuelRequests(!profile.getSettings().isReceiveDuelRequests());
                this.onInventoryOpen(new InventoryOpenEvent(event.getView()));
                break;
        }
    }
}
