package net.ghoul.practice.menus;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.queue.menu.QueueSelectKitMenu;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

public class SelectQueueMenu implements Menu {

    private final Inventory inventory;
    private BukkitTask runnable;
    private final Player player;

    public SelectQueueMenu(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, "Select Queue Type");
    }

    public void open(Player player) {
        update();
        player.openInventory(this.inventory);
    }

    private void update() {
        for (int a = 0; a < 27; a++) {
            this.inventory.setItem(a, new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 15).name("").build());
        }

        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (player == null || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                int unrankedPlaying = 0;
                int rankedPlaying = 0;

                int queueUnrankedPlaying = 0;
                int queueRankedPlaying = 0;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Profile profile = Profile.getByUuid(player.getUniqueId());
                    final Match match = profile.getMatch();

                    if (profile.isInQueue()) {
                        if (profile.getQueue().getQueueType() == QueueType.RANKED) {
                            queueRankedPlaying++;
                        } else if (profile.getQueue().getQueueType() == QueueType.UNRANKED) {
                            queueUnrankedPlaying++;
                        }
                    }

                    if (match != null) {
                        if (match.getQueueType() == QueueType.RANKED) {
                            rankedPlaying++;
                        } else {
                            unrankedPlaying++;
                        }
                    }
                }

                inventory.setItem(11, new ItemBuilder(Material.IRON_SWORD).name(CC.RED + "Unranked Queue").lore(Arrays.asList(CC.MENU_BAR,
                        CC.WHITE + "In Fight" + CC.DARK_GRAY + " » " + CC.RED + unrankedPlaying,
                        CC.WHITE + "In Queue" + CC.DARK_GRAY + " » " + CC.RED + queueUnrankedPlaying,
                        "", CC.RED + "Click to play!", CC.MENU_BAR)).build());

                inventory.setItem(15, new ItemBuilder(Material.DIAMOND_SWORD).name(CC.RED + "Ranked Queue").lore(Arrays.asList(CC.MENU_BAR,
                        CC.WHITE + "In Fight" + CC.DARK_GRAY + " » " + CC.RED + rankedPlaying,
                        CC.WHITE + "In Queue" + CC.DARK_GRAY + " » " + CC.RED + queueRankedPlaying,
                        "", CC.RED + "Click to play!", CC.MENU_BAR)).build());

            }
        }.runTaskTimerAsynchronously(Ghoul.getInstance(), 0L, 20L);
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        this.runnable.cancel();
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        if (!topInventory.equals(this.inventory)) return;
        if (topInventory.equals(clickedInventory)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            Material material = event.getCurrentItem().getType();

            switch (material) {
                case IRON_SWORD:
                    if (!profile.isBusy()) {
                        new QueueSelectKitMenu(QueueType.UNRANKED).openMenu(player);
                        break;
                    }
                    break;
                case DIAMOND_SWORD:
                    if ((profile.getTotalWins() + profile.getTotalLost()) < 10) {
                        player.sendMessage(CC.translate("&cYou need to play 10 unranked matches."));
                        player.closeInventory();
                        break;
                    }
                    if (!profile.isBusy()) {
                        new QueueSelectKitMenu(QueueType.RANKED).openMenu(player);
                        break;
                    }
                    break;
            }
        } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
