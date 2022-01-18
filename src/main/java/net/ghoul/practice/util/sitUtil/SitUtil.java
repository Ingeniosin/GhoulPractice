package net.ghoul.practice.util.sitUtil;

import net.minecraft.server.v1_8_R3.EntityCreeper;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class SitUtil {

    private static final HashMap<String, Integer> horses = new HashMap<>();

    public static void sitPlayer(Player player) {
        if (horses.get(player.getName()) != null) {
            horses.remove(player.getName());
        }

        Location location = player.getLocation();
        EntityCreeper creeper = new EntityCreeper(((CraftWorld) location.getWorld()).getHandle());
        CraftPlayer craftPlayer = (CraftPlayer) player;

        creeper.setLocation(location.getX(), location.getY(), location.getZ(), 0.0f, 0.0f);
        creeper.setInvisible(true);

        PacketPlayOutSpawnEntityLiving packetPlayOutSpawnEntityLiving = new PacketPlayOutSpawnEntityLiving(creeper);
        craftPlayer.getHandle().playerConnection.sendPacket(packetPlayOutSpawnEntityLiving);

        horses.put(player.getName(), creeper.getId());

        PacketPlayOutAttachEntity packetPlayOutAttachEntity = new PacketPlayOutAttachEntity(0, craftPlayer.getHandle(), creeper);
        craftPlayer.getHandle().playerConnection.sendPacket(packetPlayOutAttachEntity);

        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public static void unSitPlayer(Player player) {
        if (horses.get(player.getName()) != null) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(horses.get(player.getName()));
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

            player.setAllowFlight(false);
            player.setFlying(false);

            horses.remove(player.getName());
        }
    }
}
