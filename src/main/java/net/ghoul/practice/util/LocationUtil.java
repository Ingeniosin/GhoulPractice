package net.ghoul.practice.util;

import net.ghoul.practice.util.chat.CC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class LocationUtil {

    private static final String[] FACES = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public static Location deserializeLocation(String input) {
        String[] attributes = input.split(":");

        World world = null;
        Double x = null;
        Double y = null;
        Double z = null;
        Float pitch = null;
        Float yaw = null;

        for (String attribute : attributes) {
            String[] split = attribute.split(";");

            if (split[0].equalsIgnoreCase("#w")) {
                world = Bukkit.getWorld(split[1]);
                continue;
            }

            if (split[0].equalsIgnoreCase("#x")) {
                x = Double.parseDouble(split[1]);
                continue;
            }

            if (split[0].equalsIgnoreCase("#y")) {
                y = Double.parseDouble(split[1]);
                continue;
            }

            if (split[0].equalsIgnoreCase("#z")) {
                z = Double.parseDouble(split[1]);
                continue;
            }

            if (split[0].equalsIgnoreCase("#p")) {
                pitch = Float.parseFloat(split[1]);
                continue;
            }

            if (split[0].equalsIgnoreCase("#yaw")) {
                yaw = Float.parseFloat(split[1]);
            }
        }

        if (world == null || x == null || y == null || z == null || pitch == null || yaw == null) {
            return null;
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static String serializeLocation(Location location) {
        return "#w;" + location.getWorld().getName() +
                ":#x;" + location.getX() +
                ":#y;" + location.getY() +
                ":#z;" + location.getZ() +
                ":#p;" + location.getPitch() +
                ":#yaw;" + location.getYaw();
    }

    public static boolean checkZone(Location location, int input) {
        return Math.abs(location.getBlockX()) <= input && Math.abs(location.getBlockZ()) <= input;
    }

    public static Location getScatterLocation(int border, String worldName) {
        int x = ThreadLocalRandom.current().nextInt(-border + 10, border - 10);
        int z = ThreadLocalRandom.current().nextInt(-border + 10, border - 10);

        World world = Bukkit.getWorld(worldName);
        Block block = world.getHighestBlockAt(x, z);
        Material relative = block.getRelative(BlockFace.DOWN).getType();

        if (block.getLocation().getY() < 40 || relative.name().endsWith("WATER") || relative.name().endsWith("LAVA")) {
            return getScatterLocation(border, worldName);
        }

        return block.getLocation().add(0, 0.5, 0);
    }

    public static void spawnHead(LivingEntity entity) {
        entity.getLocation().getBlock().setType(Material.NETHER_FENCE);
        entity.getWorld().getBlockAt(entity.getLocation().add(0.0D, 1.0D, 0.0D)).setType(Material.SKULL);

        Skull skull = (Skull) entity.getLocation().add(0.0D, 1.0D, 0.0D).getBlock().getState();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            skull.setOwner(player.getName());
        } else {
            skull.setOwner(ChatColor.stripColor(entity.getCustomName()));
        }

        skull.update();

        Block block = entity.getLocation().add(0.0D, 1.0D, 0.0D).getBlock();
        block.setData((byte) 1);
    }

    public static String getDirection(Player player) {
        return FACES[Math.round(player.getLocation().getYaw() / 45f) & 0x7];
    }

    public static Location getHighest(Location location) {
        int x = location.getBlockX();
        int y = 256;
        int z = location.getBlockZ();

        while (y > location.getY()) {
            Block block = location.getWorld().getBlockAt(x, --y, z);

            if (!block.isEmpty()) {
                location.setX(location.getBlockX() + 0.5);
                location.setY(block.getLocation().getBlockY() + 1);
                location.setZ(location.getBlockZ() + 0.5);

                return location;
            }
        }

        return location;
    }
}
