package net.ghoul.practice.duel.menu;

import lombok.AllArgsConstructor;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.enums.ArenaType;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DuelSelectArenaMenu extends Menu {

    String type;

    public DuelSelectArenaMenu(String type) {
        this.type = type;
    }

    @Override
    public String getTitle(Player player) {
        return "&7Select an arena";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        for (Arena arena : Arena.getArenas()) {
            if (!arena.isSetup()) continue;

            if (type.equalsIgnoreCase("normal")) {
                if (!arena.isCustomMap()) {
                    if (!arena.getKits().contains(profile.getDuelProcedure().getKit().getName())) continue;
                    if (profile.getDuelProcedure().getKit().getGameRules().isBuild() && arena.getType() == ArenaType.SHARED) continue;
                    if (arena.getType() == ArenaType.DUPLICATE) continue;
                }
            }

            buttons.put(buttons.size(), new SelectArenaButton(arena));
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
    private class SelectArenaButton extends Button {

        private final Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(arena.getDisplayIcon())
                    .name(arena.getDisplayName())
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (type.equalsIgnoreCase("normal")) {
                profile.getDuelProcedure().setArena(arena);
                profile.getDuelProcedure().send();

                Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

                player.closeInventory();
            }
        }
    }
}
