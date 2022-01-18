package net.ghoul.practice.match.menu;

import lombok.Getter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.Glow;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class MatchsMenu implements Menu {

    @Getter
    private final Inventory inventory;
    Player player;
    private int page;
    private BukkitTask runnable;

    public MatchsMenu() {
        this.page = 1;
        this.inventory = Bukkit.createInventory(this, 9*6, "Current Matches");
    }

    public void open(Player player) {
        this.player = player;
        update();
        player.openInventory(this.inventory);
    }

    private void update() {

        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (int a = 0; a < (9*6); a++) {
                    inventory.setItem(a, new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 7).name(" ").build());
                }

                inventory.setItem(0, new ItemBuilder(Material.STAINED_GLASS_PANE, page == 1 ? 5 : 14).name((page == 1 ? ChatColor.GRAY : ChatColor.RED) + "Previous Page").build());
                inventory.setItem(8, new ItemBuilder(Material.STAINED_GLASS_PANE, page + 1 > getTotalPages() ? 5 : 14).name((page + 1 > getTotalPages() ? ChatColor.GRAY : ChatColor.GREEN) + "Next Page").build());

                List<Match> matches = new ArrayList<>();
                for (Match match : Match.getMatches()) {
                    if (!match.isEnding() && match.isSoloMatch() && !matches.contains(match)) {
                        matches.add(match);
                    }
                }

                int slot = 9;
                int index = page * 45 - 45;
                while (slot < inventory.getSize() && matches.size() > index) {
                    Match match = matches.get(index);
                    String ranked = match.getQueueType() == QueueType.RANKED ? "Ranked" : "Unranked";

                    ItemBuilder itemBuilder = new ItemBuilder(match.getKit().getDisplayIcon());
                    itemBuilder.name(CC.translate("&e" + match.getTeamPlayerA().getUsername() + "'s fight"));

                    List<String> lore = new ArrayList<>();
                    lore.add(CC.translate("&cType: &f" + ranked));
                    lore.add(CC.translate(""));
                    lore.add(CC.translate("&cKit: &f" + ChatColor.stripColor(match.getKit().getDisplayName())));
                    lore.add(CC.translate("&cDuration: &f" + match.getDuration()));
                    lore.add(CC.translate(""));
                    lore.add(CC.translate("&cOpponent: &f" + match.getTeamPlayerB().getUsername()));
                    lore.add(CC.translate(""));
                    lore.add(CC.translate("&eClick to spectate!"));
                    itemBuilder.lore(lore);

                    if (match.getQueueType() == QueueType.RANKED) {
                        Glow glow = new Glow(70);
                        itemBuilder.enchantment(glow);
                    }

                    inventory.setItem(slot++, itemBuilder.build());
                    index++;
                }
            }
        }.runTaskTimerAsynchronously(Ghoul.getInstance(), 0L, 10L);
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        this.runnable.cancel();
    }

    private int getTotalPages() {
        return Match.getMatches().size() / 45 + 1;
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

            Material material = event.getCurrentItem().getType();
            String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

            if (material == Material.STAINED_GLASS_PANE) {
                switch (itemName) {
                    case "Previous Page":
                        if (page == 1) return;
                        this.page--;
                        update();
                        break;
                    case "Next Page":
                        if (page + 1 > this.getTotalPages()) return;
                        this.page++;
                        update();
                        break;
                }
                update();
            } else {
                if (itemName == null) return;

                Player target = Bukkit.getPlayer(itemName.replace("'s fight", ""));
                if (target == null) return;

                Profile targetProfile = Profile.getByUuid(target.getUniqueId());

                if (targetProfile.isInFight()) {
                    targetProfile.getMatch().addSpectator(player, target);
                } else {
                    player.sendMessage(CC.RED + "That player is not in a match or running event.");
                }
            }
        } else if (!topInventory.equals(clickedInventory) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }
}
