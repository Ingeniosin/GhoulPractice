package net.ghoul.practice.match.listener;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class DamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {

            if (event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                event.setDamage(event.getDamage() * 1.1);
                return;
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                event.setDamage(event.getDamage() * 1.4);
                return;
            }

            Player player = (Player) event.getEntity();
            double damage = event.getDamage();
            int armorP4 = checkArmorP4(player);

            double porcentaje = 0;

            if (armorP4 == 4) {
                porcentaje += 30;
            }

            if (armorP4 == 3) {
                porcentaje += 15;
            }

            if (armorP4 == 2) {
                porcentaje += 6;
            }

            porcentaje = porcentaje / 100;
            double d_final = (damage * porcentaje) + damage;
            event.setDamage(d_final);
        }
    }

    private int checkArmorP4(Player p) {
        int a = 0;
        for (ItemStack i : p.getInventory().getArmorContents()) {
            Map<Enchantment, Integer> itemEnchantments = i.getEnchantments();
            if (itemEnchantments.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL) && (itemEnchantments.get(Enchantment.PROTECTION_ENVIRONMENTAL) == 4)) {
                a = a + 1;
            }
        }
        return a;
    }
}
