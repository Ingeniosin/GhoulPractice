package net.ghoul.practice.kiteditor;

import lombok.Getter;
import lombok.Setter;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.kit.KitInventory;
import net.ghoul.practice.profile.ProfileState;

@Setter
@Getter
public class KitEditor {

    private boolean active;
    private boolean rename;
    private ProfileState previousState;
    private Kit selectedKit;
    private KitInventory selectedKitInventory;

    public boolean isRenaming() {
        return this.active && this.rename && this.selectedKit != null;
    }

}
