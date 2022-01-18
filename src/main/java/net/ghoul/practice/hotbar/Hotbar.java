package net.ghoul.practice.hotbar;

import net.ghoul.practice.enums.HotbarType;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Hotbar {
    public static Map<HotbarType, ItemStack> items;
    
    public Hotbar() {
        preload();
    }
    
    public static void preload() {
        Hotbar.items.put(HotbarType.SELECT_QUEUE_TYPE, new ItemBuilder(Material.DIAMOND_SWORD).name(CC.RED + "Queues" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.QUEUE_JOIN_UNRANKED, new ItemBuilder(Material.IRON_SWORD).name(CC.RED + "Join Unranked Queue" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.QUEUE_JOIN_RANKED, new ItemBuilder(Material.DIAMOND_SWORD).name(CC.RED + "Join Ranked Queue" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.QUEUE_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Queue" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.PARTY_EVENTS, new ItemBuilder(Material.DIAMOND_AXE).name(CC.RED + "Party Events" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.PARTY_CREATE, new ItemBuilder(Material.NAME_TAG).name(CC.RED + "Create Party" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.PARTY_DISBAND, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Disband Party" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.PARTY_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Party" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.PARTY_INFO, new ItemBuilder(Material.PAPER).name(CC.RED + "Party Information" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.OTHER_PARTIES, new ItemBuilder(Material.REDSTONE_TORCH_ON).name(CC.RED + "Duel Other Parties" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.SETTINGS_MENU, new ItemBuilder(Material.BLAZE_POWDER).name(CC.RED + "Settings" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.KIT_EDITOR, new ItemBuilder(Material.BOOK).name(CC.RED + "Kit Editor" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.SPECTATE_STOP, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Stop Spectating" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.VIEW_INVENTORY, new ItemBuilder(Material.BOOK).name(CC.RED + "View Inventory" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.EVENT_JOIN, new ItemBuilder(Material.NETHER_STAR).name(CC.RED + "Join Event" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.SUMO_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Sumo" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.DEFAULT_KIT, new ItemBuilder(Material.BOOK).name(CC.RED + "Default Kit" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.DIAMOND_KIT, new ItemBuilder(Material.DIAMOND_SWORD).name(CC.RED + "Diamond Kit" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.BARD_KIT, new ItemBuilder(Material.BLAZE_POWDER).name(CC.RED + "Bard Kit" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.ROGUE_KIT, new ItemBuilder(Material.GOLD_SWORD).name(CC.RED + "Rogue Kit" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.ARCHER_KIT, new ItemBuilder(Material.BOW).name(CC.RED + "Archer Kit" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.MATCHSLIST, new ItemBuilder(Material.PAPER).name(CC.RED + "Current Matches" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.STATS, new ItemBuilder(Material.ITEM_FRAME).name(CC.RED + "Your stats" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.COSMETICS, new ItemBuilder(Material.EYE_OF_ENDER).name(CC.RED + "Cosmetics" + CC.GRAY + " (Right Click)").build());
    }
    
    public static ItemStack[] getLayout(final HotbarLayout layout, final Profile profile) {
        final ItemStack[] toReturn = new ItemStack[9];
        Arrays.fill(toReturn, null);
        switch (layout) {
            case LOBBY: {
                if (profile.getParty() == null) {
                    toReturn[0] = Hotbar.items.get(HotbarType.SELECT_QUEUE_TYPE);
                    toReturn[1] = Hotbar.items.get(HotbarType.MATCHSLIST);

                    toReturn[3] = Hotbar.items.get(HotbarType.COSMETICS);
                    toReturn[4] = Hotbar.items.get(HotbarType.PARTY_CREATE);
                    toReturn[5] = Hotbar.items.get(HotbarType.STATS);

                    toReturn[7] = Hotbar.items.get(HotbarType.SETTINGS_MENU);
                    toReturn[8] = Hotbar.items.get(HotbarType.KIT_EDITOR);
                    break;
                }
                if (profile.getParty().getLeader().getUniqueId().equals(profile.getUuid())) {
                    toReturn[0] = Hotbar.items.get(HotbarType.PARTY_EVENTS);
                    toReturn[1] = Hotbar.items.get(HotbarType.PARTY_INFO);
                    toReturn[4] = Hotbar.items.get(HotbarType.OTHER_PARTIES);
                    toReturn[7] = Hotbar.items.get(HotbarType.KIT_EDITOR);
                    toReturn[8] = Hotbar.items.get(HotbarType.PARTY_DISBAND);
                    break;
                }
                toReturn[0] = Hotbar.items.get(HotbarType.PARTY_INFO);
                toReturn[4] = Hotbar.items.get(HotbarType.OTHER_PARTIES);
                toReturn[7] = Hotbar.items.get(HotbarType.KIT_EDITOR);
                toReturn[8] = Hotbar.items.get(HotbarType.PARTY_LEAVE);
                break;
            }
            case QUEUE: {
                toReturn[0] = Hotbar.items.get(HotbarType.QUEUE_LEAVE);
                toReturn[1] = Hotbar.items.get(HotbarType.MATCHSLIST);
                toReturn[4] = Hotbar.items.get(HotbarType.STATS);
                toReturn[7] = Hotbar.items.get(HotbarType.COSMETICS);
                toReturn[8] = Hotbar.items.get(HotbarType.SETTINGS_MENU);
                break;
            }
            case SUMO_SPECTATE: {
                toReturn[0] = Hotbar.items.get(HotbarType.SUMO_LEAVE);
                break;
            }
            case MATCH_SPECTATE: {
                toReturn[0] = Hotbar.items.get(HotbarType.VIEW_INVENTORY);
                toReturn[8] = Hotbar.items.get(HotbarType.SPECTATE_STOP);
                break;
            }
        }
        return toReturn;
    }
    
    public static HotbarType fromItemStack(final ItemStack itemStack) {
        for (final Map.Entry<HotbarType, ItemStack> entry : getItems().entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(itemStack)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public static Map<HotbarType, ItemStack> getItems() {
        return Hotbar.items;
    }
    
    static {
        Hotbar.items = new HashMap<>();
    }
}
