package net.ghoul.practice.party.menu;

import net.ghoul.practice.enums.PartyEventType;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

public class PartyEventSelectEventMenu extends Menu {

    @Override
    public String getTitle(final Player player) {
        return "&cSelect a party event";
    }

    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(0, new SelectEventButton(PartyEventType.FFA));
        buttons.put(1, new SelectEventButton(PartyEventType.SPLIT));
        buttons.put(6, new SelectEventButton(PartyEventType.RANKED_2V2));
        buttons.put(7, new SelectEventButton(PartyEventType.RANKED_3V3));
        buttons.put(8, new SelectEventButton(PartyEventType.RANKED_4V4));
        return buttons;
    }

    private static class SelectEventButton extends Button {

        private final PartyEventType partyEventType;

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(this.partyEventType.getMaterial()).name("&6&l" + this.partyEventType.getName()).build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }
            if (this.partyEventType == PartyEventType.FFA || this.partyEventType == PartyEventType.SPLIT) {
                Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
                new PartyEventSelectKitMenu(this.partyEventType).openMenu(player);
            }
            if (this.partyEventType == PartyEventType.RANKED_2V2 || this.partyEventType == PartyEventType.RANKED_3V3
                    || this.partyEventType == PartyEventType.RANKED_4V4) {
                player.closeInventory();
                player.sendMessage(CC.translate("&cSoon..."));
            }
        }

        @ConstructorProperties({ "partyEventType" })
        public SelectEventButton(final PartyEventType partyEventType) {
            this.partyEventType=partyEventType;
        }
    }
}
