package net.ghoul.practice.queue.menu;

import net.ghoul.practice.kit.KitLeaderboards;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.menus.SelectQueueMenu;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.Queue;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.Glow;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.elo.EloUtil;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import net.ghoulnetwork.core.Core;
import net.ghoulnetwork.core.managers.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.beans.ConstructorProperties;
import java.util.*;

public class QueueSelectKitMenu extends Menu {

    private final QueueType queueType;
    private final List<String> already = new ArrayList<>();

    @Override
    public String getTitle(final Player player) {
        return "&cSelect a Kit";
    }

    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        int slot = 0;

        if (this.queueType == QueueType.UNRANKED) {

            for (int a = 0; a < 45; a++) {
                buttons.put(a, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player player) {
                        return new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 7).name("").build();
                    }
                });
            }

            buttons.put(44, new BackToQueueSelectorButton());

            for (final Queue queue : Queue.getQueues()) {
                if (queue.getKit().getInventorySlot() != 69) {
                    if (queue.getType() == this.queueType) {
                        buttons.put(queue.getKit().getInventorySlot(), new SelectKitButton(queue));
                    }
                }
            }
        } else if (this.queueType == QueueType.RANKED) {
            for (Queue queue : Queue.getQueues()) {
                if (queue.getKit().isEnabled() && queue.getKit().getGameRules().isRanked()) {
                    if (queue.getType() == this.queueType) {
                        buttons.put(slot++, new SelectKitButton(queue));
                    }
                }
            }
        }

        return buttons;
    }

    private static class BackToQueueSelectorButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.CARPET);
            itemBuilder.durability(14);
            itemBuilder.name(CC.translate("&cBack to queue selector."));
            return itemBuilder.build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            new SelectQueueMenu(player).open(player);
        }
    }

    @ConstructorProperties({"queueType"})
    public QueueSelectKitMenu(final QueueType queueType) {
        this.queueType = queueType;
    }

    private class SelectKitButton extends Button {

        private final Queue queue;

        @Override
        public ItemStack getButtonItem(final Player player) {
            int position = 1;

            already.clear();

            List<String> lore = new ArrayList<>();
            lore.add("&7&m----------------------");
            lore.add("&fIn Queue &8» &c" + this.queue.getPlayers().size());
            lore.add("&fIn Fight &8» &c" + Match.getInFights(this.queue));
            if (this.queue.getQueueType() == QueueType.RANKED) {
                lore.add("&7&m----------------------");
                if (this.queue.getKit().getRankedEloLeaderboards().size() >= 5) {
                    for (final KitLeaderboards kitLeaderboards : this.queue.getKit().getRankedEloLeaderboards()) {
                        if (kitLeaderboards != null) {
                            if (!already.contains(kitLeaderboards.getName())) {
                                Player target = Bukkit.getPlayer(kitLeaderboards.getName());
                                if (target != null && target.isOnline()) {
                                    PlayerData playerData = Core.INSTANCE.getPlayerManagement().getPlayerData(target.getUniqueId());
                                    if (playerData.isDisguise()) {
                                        lore.add("&7" + position + ". &e" + playerData.getDisguiseName() + "&7: " +
                                                EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                                    } else {
                                        lore.add("&7" + position + ". &e" + playerData.getHighestRank().getPrefix() + playerData.getNameWithColor() + "&7: " +
                                                EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                                    }
                                } else {
                                    lore.add("&7" + position + ". &e" + kitLeaderboards.getName() + "&7: " + EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                                }
                                already.add(kitLeaderboards.getName());
                                ++position;
                            }
                        }
                    }
                }
            } else {
                lore.add("");
                lore.add("&cClick to join queue.");
            }
            lore.add("&7&m----------------------");

            ItemBuilder itemBuilder = new ItemBuilder(this.queue.getKit().getDisplayIcon());
            itemBuilder.name(this.queue.getKit().getDisplayName());
            itemBuilder.lore(lore);

            if (this.queue.getKit().isGlow()) {
                Glow glow = new Glow(70);
                itemBuilder.enchantment(glow);
            }

            if (this.queue.getPlayers().size() > 0) {
                itemBuilder.amount(this.queue.getPlayers().size());
            }

            return itemBuilder.build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.isBusy()) {
                player.sendMessage(CC.RED + "You cannot queue right now.");
                return;
            }
            player.closeInventory();
            if (QueueSelectKitMenu.this.queueType == QueueType.UNRANKED) {
                this.queue.addPlayer(player, 0);
            } else if (QueueSelectKitMenu.this.queueType == QueueType.RANKED) {
                this.queue.addPlayer(player, profile.getStatisticsData().get(this.queue.getKit()).getElo());
            }
        }
        @ConstructorProperties({ "queue" })
        public SelectKitButton(final Queue queue) {
            this.queue = queue;
        }
    }
}
