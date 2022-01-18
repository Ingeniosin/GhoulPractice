package net.ghoul.practice.knockback;

import net.ghoul.practice.kit.Kit;
import org.bukkit.entity.Player;

public interface KnockbackType {

    void applyKnockback(Player p, String s);

    void appleKitKnockback(Player p, Kit kit);

    void applyDefaultKnockback(Player p);
}
