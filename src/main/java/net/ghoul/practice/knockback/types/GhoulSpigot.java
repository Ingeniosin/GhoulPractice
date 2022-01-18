package net.ghoul.practice.knockback.types;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.knockback.KnockbackType;
import net.ghoulpvp.knockback.KnockbackModule;
import net.ghoulpvp.knockback.KnockbackProfile;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class GhoulSpigot implements KnockbackType {

    private final KnockbackModule knockbackModule = KnockbackModule.INSTANCE;

    public GhoulSpigot() {
        Ghoul.logger("&cFound Ghoul Spigot!");
    }

    @Override
    public void applyKnockback(Player player, String string) {
        KnockbackProfile profile = knockbackModule.profiles.get(string);
        ((CraftPlayer) player).getHandle().setKnockback(profile);
    }

    @Override
    public void appleKitKnockback(Player player, Kit kit) {
        KnockbackProfile profile;
        if (kit.getKnockbackProfile() != null) {
            profile = knockbackModule.profiles.get(kit.getKnockbackProfile());
        } else {
            profile = KnockbackModule.getDefault();
        }
        ((CraftPlayer) player).getHandle().setKnockback(profile);
    }

    @Override
    public void applyDefaultKnockback(Player player) {
        KnockbackProfile profile = KnockbackModule.getDefault();
        ((CraftPlayer) player).getHandle().setKnockback(profile);
    }
}
