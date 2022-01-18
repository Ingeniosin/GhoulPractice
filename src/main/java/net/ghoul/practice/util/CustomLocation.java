package net.ghoul.practice.util;

import net.ghoul.practice.Ghoul;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.Location;
import org.bukkit.World;

import java.beans.ConstructorProperties;
import java.text.DecimalFormat;

public class CustomLocation {
    public void setWorld(String world) {
        this.world = world;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @ConstructorProperties({"world", "x", "y", "z", "yaw", "pitch"})
    public CustomLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    private final long timestamp = System.currentTimeMillis();

    private String world;

    private double x;

    private double y;

    private double z;

    private float yaw;

    private float pitch;

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getWorld() {
        return this.world;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public CustomLocation(double x, double y, double z) {
        this(x, y, z, 0.0F, 0.0F);
    }

    public CustomLocation(String world, double x, double y, double z) {
        this(world, x, y, z, 0.0F, 0.0F);
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch) {
        this("arenas", x, y, z, yaw, pitch);
    }

    public static CustomLocation fromBukkitLocation(Location location) {
        return new CustomLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location
                .getYaw(), location.getPitch());
    }

    public static CustomLocation stringToLocation(String string) {
        String[] split = string.split(",");
        CustomLocation customLocation = new CustomLocation(split[0], Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.valueOf(split[5]).floatValue());
        return customLocation;
    }

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static String locationToString(CustomLocation location) {
        String loc = location.getWorld() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
        return loc;
    }

    public Location toBukkitLocation() {
        return new Location(toBukkitWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public double getGroundDistanceTo(CustomLocation location) {
        return Math.sqrt(Math.pow(this.x - location.x, 2.0D) + Math.pow(this.z - location.z, 2.0D));
    }

    public double getDistanceTo(CustomLocation location) {
        return Math.sqrt(Math.pow(this.x - location.x, 2.0D) + Math.pow(this.y - location.y, 2.0D) + Math.pow(this.z - location.z, 2.0D));
    }

    public World toBukkitWorld() {
        if (this.world == null)
            return Ghoul.getInstance().getServer().getWorlds().get(0);
        return Ghoul.getInstance().getServer().getWorld(this.world);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CustomLocation))
            return false;
        CustomLocation location = (CustomLocation) obj;
        return (location.x == this.x && location.y == this.y && location.z == this.z && location.pitch == this.pitch && location.yaw == this.yaw);
    }

    public String toString() {
        return (new ToStringBuilder(this))
                .append("x", this.x)
                .append("y", this.y)
                .append("z", this.z)
                .append("yaw", this.yaw)
                .append("pitch", this.pitch)
                .append("arenas", this.world)
                .append("timestamp", this.timestamp)
                .toString();
    }
}
