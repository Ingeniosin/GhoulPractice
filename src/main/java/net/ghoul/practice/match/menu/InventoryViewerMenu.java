package net.ghoul.practice.match.menu;

import lombok.Getter;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryViewerMenu implements Menu {

    @Getter
    private final Inventory inventory;
    private final Player target;

    public InventoryViewerMenu(Player target) {
        this.target = target;
        this.inventory = Bukkit.createInventory(this, 54, CC.RED + target.getName() + "'s Inventory");
    }

    @Override
    public void open(Player player) {
        update();
        player.openInventory(this.inventory);
    }

    private void update() {
        ItemStack[] armor = target.getInventory().getArmorContents();
        ItemStack[] contents = target.getInventory().getContents();

        inventory.setContents(contents);

        inventory.setItem(52, armor[0]);
        inventory.setItem(50, armor[1]);
        inventory.setItem(48, armor[2]);
        inventory.setItem(46, armor[3]);

        inventory.setItem(36, createGlass());
        inventory.setItem(37, createGlass());
        inventory.setItem(38, createGlass());
        inventory.setItem(39, createGlass());
        inventory.setItem(40, createGlass());
        inventory.setItem(41, createGlass());
        inventory.setItem(42, createGlass());
        inventory.setItem(43, createGlass());
        inventory.setItem(44, createGlass());
        inventory.setItem(45, createGlass());
        inventory.setItem(47, createGlass());
        inventory.setItem(49, createGlass());
        inventory.setItem(51, createGlass());
        inventory.setItem(53, createGlass());
    }

    public ItemStack createGlass() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta itemmeta = item.getItemMeta();
        item.setItemMeta(itemmeta);
        return item;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        if (!topInventory.equals(this.inventory)) return;

        if (topInventory.equals(clickedInventory)) {
            event.setCancelled(true);
        } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }
}
