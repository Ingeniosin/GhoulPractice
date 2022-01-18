package net.ghoul.practice.kiteditor.menu;

import lombok.AllArgsConstructor;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.menu.QueueSelectKitMenu;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitEditorSelectKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&7Select a kit";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int a = 0; a < 45; a++) {
            buttons.put(a, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 7).name("").build();
                }
            });
        }

        Kit.getKits().forEach(kit -> {
            if (kit.isEnabled()) {
                if (kit.getInventorySlot() != 69) {
                    buttons.put(kit.getInventorySlot(), new KitDisplayButton(kit));
                }
            }
        });

        return buttons;
    }

    @AllArgsConstructor
    private static class KitDisplayButton extends Button {

        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&cClick to edit this kit.");
            return new ItemBuilder(kit.getDisplayIcon())
                    .name(kit.getDisplayName()).lore(lore)
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            player.closeInventory();

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getKitEditor().setSelectedKit(kit);
            profile.getKitEditor().setPreviousState(profile.getState());

            new KitManagementMenu(kit).openMenu(player);
        }

    }
}
