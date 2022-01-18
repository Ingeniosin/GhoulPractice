package net.ghoul.practice.duel.menu;

import lombok.AllArgsConstructor;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuelSelectKitMenu extends Menu {

    String type;

    public DuelSelectKitMenu(String type) {
        this.type = type;
    }

    @Override
    public String getTitle(Player player) {
        return "&7Select a kit";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        boolean party = Profile.getByUuid(player.getUniqueId()).getParty() != null;

        for (Kit kit : Kit.getKits()) {
            if (kit.isEnabled()) {
                if (!(kit.getGameRules().isTimed() && party && kit.isEnabled())) {
                    if (kit.getInventorySlot() != 69) {
                        buttons.put(kit.getInventorySlot(), new SelectKitButton(kit));
                    }
                }
            }
        }
        return buttons;
    }

    @Override
    public void onClose(Player player) {
        if (!isClosedByMenu()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setDuelProcedure(null);
        }
    }

    @AllArgsConstructor
    private static class SelectKitButton extends Button {

        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&cClick to send a duel with this kit.");
            return new ItemBuilder(kit.getDisplayIcon())
                    .name(kit.getDisplayName()).lore(lore)
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getDuelProcedure() == null) {
                player.sendMessage(CC.RED + "Could not find duel procedure.");
                return;
            }

            Arena arena = Arena.getRandom(kit);

            profile.getDuelProcedure().setKit(kit);
            profile.getDuelProcedure().setArena(arena);

            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            if (player.hasPermission("practice.donator")) {
                new DuelSelectArenaMenu("normal").openMenu(player);
            } else {
                player.closeInventory();
                profile.getDuelProcedure().send();
            }
        }
    }
}
