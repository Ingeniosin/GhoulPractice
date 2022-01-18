package net.ghoul.practice.enums;

import org.bukkit.Material;

import java.beans.ConstructorProperties;

public enum PartyEventType {
    FFA("&cFFA", Material.QUARTZ),
    SPLIT("&cSplit", Material.LEASH),
    RANKED_2V2("&cRanked 2v2", Material.EMERALD),
    RANKED_3V3("&cRanked 3v3", Material.BLAZE_ROD),
    RANKED_4V4("&cRanked 4v4", Material.NETHER_BRICK);

    private final String name;
    private final Material material;

    @ConstructorProperties({ "name", "lore", "material" })
    PartyEventType(final String name, final Material material) {
        this.name = name;
        this.material = material;
    }

    public String getName() {
        return this.name;
    }

    public Material getMaterial() {
        return this.material;
    }
}
