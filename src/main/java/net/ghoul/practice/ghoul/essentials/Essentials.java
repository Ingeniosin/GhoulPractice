package net.ghoul.practice.ghoul.essentials;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.ghoul.essentials.event.SpawnTeleportEvent;
import net.ghoul.practice.util.external.LocationUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spigotmc.AsyncCatcher;

import java.io.IOException;

public class Essentials {

    public static Location spawn;

    public Essentials() {

        spawn = LocationUtil.deserialize(Ghoul.getInstance().getMainConfig().getStringOrDefault("Practice.Spawn", null));
    }

    public static void setSpawn(Location location) {
        spawn = location;

        Ghoul.getInstance().getMainConfig().getConfiguration().set("Practice.Spawn", LocationUtil.serialize(spawn));

        try {
            Ghoul.getInstance().getMainConfig().getConfiguration().save(Ghoul.getInstance().getMainConfig().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void teleportToSpawn(Player player) {
        Location location = spawn;
        AsyncCatcher.enabled = false;
        SpawnTeleportEvent event = new SpawnTeleportEvent(player, location);
        event.call();

        if (!event.isCancelled() && event.getLocation() != null) {
            player.teleport(event.getLocation());
        }
    }
}
