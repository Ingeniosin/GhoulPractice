package net.ghoul.practice.util.menu.type;

import net.ghoul.practice.util.menu.Menu;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.ParameterizedType;

public abstract class ChestMenu<T extends JavaPlugin> implements Menu {

    protected final JavaPlugin plugin;
    protected final Inventory inventory;

    public ChestMenu(String title, int size) {
        this.plugin = JavaPlugin.getPlugin((Class) (((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]));
        this.inventory = this.plugin.getServer().createInventory(this, size, title.length() > 32 ? title.substring(0, 32) : title);
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}