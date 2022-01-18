package net.ghoul.practice.statistics.menu;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.elo.EloUtil;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import net.ghoulnetwork.core.utilities.CC;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StatsMenu extends Menu {

    private static final Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, DyeColor.GRAY.getData(), net.ghoul.practice.util.chat.CC.translate("&7"));
    private final Player target;

    public StatsMenu(Player target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) { return "§e" + this.target.getName() + " Statistics"; }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();

        buttons.put(19, new GlobalLeaderboardsButton());

        List<Integer> slots = new ArrayList<>(Arrays.asList(13, 14, 15, 21, 22, 23, 24, 25, 31, 32, 33, 34, 35));

        for (Kit kit : Kit.getKits()) {
            if (kit.getGameRules().isRanked()) {
                buttons.put(slots.remove(0), new KitsItems(kit));
            }
        }

        for (int i = 0; i < 45; i++) {
            buttons.putIfAbsent(i, BLACK_PANE);
        }

        return buttons;
    }

    @AllArgsConstructor
    private class KitsItems extends Button {

        Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            Profile data = Profile.getByUuid(target.getUniqueId());
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add(CC.translate("&8» &cElo: &7" + EloUtil.getEloRangeColor(data.getStatisticsData().get(kit).getElo()) + data.getStatisticsData().get(kit).getElo()));
            lore.add(CC.translate(""));
            lore.add(CC.translate("&8» &cWins: &7" + data.getStatisticsData().get(kit).getWon()));
            lore.add(CC.translate("&8» &cLoss: &7" + data.getStatisticsData().get(kit).getLost()));
            lore.add(CC.translate("&8» &cMatches: &7" + data.getStatisticsData().get(kit).getMatches()));
            lore.add(CC.MENU_BAR);
            return new ItemBuilder(kit.getDisplayIcon()).name(CC.translate("&c&l") + kit.getDisplayName()).lore(lore).build();
        }
    }

    private static class GlobalLeaderboardsButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add(CC.translate("&8» &cGlobal Elo: &7" + EloUtil.getEloRangeColor(profile.getGlobalElo()) + profile.getGlobalElo()));
            lore.add(CC.translate(""));
            lore.add(CC.translate("&8» &cTotal wins: &7" + profile.getTotalWins()));
            lore.add(CC.translate("&8» &cTotal losses: &7" + profile.getTotalLost()));
            lore.add(CC.translate("&8» &cTotal Matches: &7" + profile.getTotalMatches()));
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.NETHER_STAR).name(CC.translate("&6&lGlobal Stats")).lore(lore).build();
        }
    }
}
