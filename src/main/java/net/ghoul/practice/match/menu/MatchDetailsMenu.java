package net.ghoul.practice.match.menu;

import lombok.AllArgsConstructor;
import net.ghoul.practice.match.MatchSnapshot;
import net.ghoul.practice.util.InventoryUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import net.ghoul.practice.util.external.menu.button.DisplayButton;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@AllArgsConstructor
public class MatchDetailsMenu extends Menu {

    private final MatchSnapshot snapshot;

    @Override
    public String getTitle(Player player) {
        return "&6Inventory of " + snapshot.getUsername();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        ItemStack[] fixedContents = InventoryUtil.fixInventoryOrder(snapshot.getContents());

        for (int i = 0; i < fixedContents.length; i++) {
            ItemStack itemStack = fixedContents[i];

            if (itemStack != null && itemStack.getType() != Material.AIR) {
                buttons.put(i, new DisplayButton(itemStack, true));
            }
        }

        for (int i = 0; i < snapshot.getArmor().length; i++) {
            ItemStack itemStack = snapshot.getArmor()[i];

            if (itemStack != null && itemStack.getType() != Material.AIR) {
                buttons.put(39 - i, new DisplayButton(itemStack, true));
            }
        }

        int pos = 45;

        buttons.put(pos++, new HealthButton(snapshot.getHealth()));

        if (snapshot.shouldDisplayRemainingPotions()) {
            buttons.put(pos++, new PotionsButton(snapshot.getUsername(), snapshot.getRemainingPotions()));
        }

        buttons.put(pos, new StatisticsButton(snapshot));

        if (this.snapshot.getOpponent() != null) {
            buttons.put(53, new SwitchInventoryButton(this.snapshot.getOpponent()));
        }

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        player.sendMessage(CC.translate("&cViewing " + snapshot.getUsername() + "'s inventory."));
    }

    @AllArgsConstructor
    private class SwitchInventoryButton extends Button {

        private final UUID opponent;

        @Override
        public ItemStack getButtonItem(Player player) {
            MatchSnapshot snapshot = MatchSnapshot.getByUuid(opponent);

            if (snapshot != null) {
                return new ItemBuilder(Material.LEVER)
                        .name("&6Opponent's Inventory")
                        .lore("&eSwitch to &a" + snapshot.getUsername() + "&e's inventory")
                        .build();
            } else {
                return new ItemStack(Material.AIR);
            }
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (snapshot.getOpponent() != null) {
                player.chat("/viewinv " + snapshot.getOpponent().toString());
            }
        }

    }

    @AllArgsConstructor
    private static class HealthButton extends Button {

        private final double health;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.MELON)
                    .name("&cHealth: &a" + health + "/10 &4" + StringEscapeUtils.unescapeJava("\u2764"))
                    .amount((int) (health == 0 ? 1 : health))
                    .build();
        }
    }

    @AllArgsConstructor
    private static class PotionsButton extends Button {

        private final String name;
        private final int potions;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.POTION)
                    .durability(16421)
                    .amount(potions == 0 ? 1 : potions)
                    .name("&dPotions")
                    .lore("&a" + name + " &ehad &a" + potions + " &epotion" + (potions == 1 ? "" : "s") + " left.")
                    .build();
        }
    }

    @AllArgsConstructor
    private static class StatisticsButton extends Button {

        private final MatchSnapshot snapshot;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .name("&6Statistics")
                    .lore(Arrays.asList(
                            "&aTotal Hits: &e" + snapshot.getTotalHits(),
                            "&aLongest Combo: &e" + snapshot.getLongestCombo(),
                            "&aPotions Thrown: &e" + snapshot.getPotionsThrown(),
                            "&aPotions Missed: &e" + snapshot.getPotionsMissed(),
                            "&aPotion Accuracy: &e" + snapshot.getPotionAccuracy()
                    ))
                    .build();
        }
    }
}
