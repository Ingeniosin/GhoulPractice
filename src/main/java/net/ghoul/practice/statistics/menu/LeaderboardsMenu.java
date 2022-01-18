package net.ghoul.practice.statistics.menu;

import com.google.common.collect.Lists;
import net.ghoul.practice.GhoulCache;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.kit.KitLeaderboards;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.elo.EloUtil;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import net.ghoulnetwork.core.Core;
import net.ghoulnetwork.core.managers.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.beans.ConstructorProperties;
import java.util.*;

public class LeaderboardsMenu extends Menu {

    private static final List<String> already = new ArrayList<>();
    private static final List<String> already1 = new ArrayList<>();

    private static final Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE,DyeColor.GRAY.getData(), CC.translate("&7"));

    @Override
    public String getTitle(final Player player) {
        return "&7Leaderboards";
    }
    
    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(19, new GlobalLeaderboardsButton());

        List<Integer> slots = new ArrayList<>(Arrays.asList(13, 14, 15, 21, 22, 23, 24, 25, 31, 32, 33));

        for (Kit kit : Kit.getKits()) {
            if (kit.getGameRules().isRanked() && kit.isEnabled()) {
                buttons.put(slots.remove(0), new KitLeaderboardsButton(kit));
            }
        }

        for (int i = 0; i < 45; i++) {
            buttons.putIfAbsent(i, BLACK_PANE);
        }
        return buttons;
    }

    private static class KitLeaderboardsButton extends Button {
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(final Player player) {
            List<String> lore = Lists.newArrayList();
            lore.add(CC.MENU_BAR);
            already.clear();
            int position = 1;
            for (final KitLeaderboards kitLeaderboards : this.kit.getRankedEloLeaderboards()) {
                if (!already.contains(kitLeaderboards.getName())) {
                    Player target = Bukkit.getPlayer(kitLeaderboards.getName());
                    if (target != null && target.isOnline()) {
                        PlayerData playerData = Core.INSTANCE.getPlayerManagement().getPlayerData(target.getUniqueId());
                        if (playerData.isDisguise()) {
                            lore.add("&7" + position + ". &e" + playerData.getDisguiseName() + "&7: " + EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                        } else {
                            lore.add("&7" + position + ". &e" + playerData.getHighestRank().getPrefix() + playerData.getNameWithColor() + "&7: " + EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                        }
                    } else {
                        lore.add("&7" + position + ". &e" + kitLeaderboards.getName() + "&7: " + EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                    }
                    already.add(kitLeaderboards.getName());
                    ++position;
                }
            }
            lore.add(CC.MENU_BAR);
            return new ItemBuilder(this.kit.getDisplayIcon()).name("&6&l" + this.kit.getDisplayName()).lore(lore).build();
        }

        @ConstructorProperties({ "kit" })
        public KitLeaderboardsButton(final Kit kit) {
            this.kit = kit;
        }
    }

    private static class GlobalLeaderboardsButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final List<String> lore =new ArrayList<>();
            int position = 1;
            lore.add(CC.MENU_BAR);
            already1.clear();
            for (final KitLeaderboards kitLeaderboards : Profile.getGlobalEloLeaderboards()) {
                if (!already1.contains(kitLeaderboards.getName())) {
                    Player target = Bukkit.getPlayer(kitLeaderboards.getName());
                    if (target != null && target.isOnline()) {
                        PlayerData playerData = Core.INSTANCE.getPlayerManagement().getPlayerData(target.getUniqueId());
                        if (playerData.isDisguise()) {
                            lore.add("&7" + position + ". &e" + playerData.getDisguiseName() + "&7: " + EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                        } else {
                            lore.add("&7" + position + ". &e" + playerData.getHighestRank().getPrefix() + playerData.getNameWithColor() + "&7: " + EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                        }
                    } else {
                        lore.add("&7" + position + ". &e" + kitLeaderboards.getName() + "&7: " + EloUtil.getEloRangeColor(kitLeaderboards.getElo()) + kitLeaderboards.getElo());
                    }
                    already1.add(kitLeaderboards.getName());
                    ++position;
                }
            }
            lore.add(CC.MENU_BAR);
            return new ItemBuilder(Material.NETHER_STAR).name("&6&lGlobal Stats").lore(lore).build();
        }
    }
}
