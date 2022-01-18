package net.ghoul.practice.knockback;

import lombok.Getter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.knockback.types.GhoulSpigot;

public class KnockbackManager {

    @Getter
    public KnockbackType knockbackType;

    public KnockbackManager() {
        preload();
    }

    public void preload() {
        try {
            Class.forName("net.ghoulpvp.knockback.KnockbackModule");
            this.knockbackType = new GhoulSpigot();
        } catch (Exception e) {
            Ghoul.logger("&4Spigot is NOT Supported, Disabling Practice!");
            Ghoul.getInstance().shutDown();
        }
    }
}
