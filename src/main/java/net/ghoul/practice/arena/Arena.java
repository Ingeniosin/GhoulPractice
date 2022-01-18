package net.ghoul.practice.arena;

import lombok.Getter;
import lombok.Setter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.cuboid.Cuboid;
import net.ghoul.practice.arena.impl.SharedArena;
import net.ghoul.practice.arena.impl.StandaloneArena;
import net.ghoul.practice.arena.impl.TheBridgeArena;
import net.ghoul.practice.enums.ArenaType;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Arena {

    @Getter private static final List<Arena> arenas = new ArrayList<>();
    @Getter private static final List<Arena> customArenas = new ArrayList<>();
    protected String name;
    protected boolean active;
    @Setter public String displayName;
    @Setter protected Location spawn1, spawn2, corner1, corner2;
    @Setter protected boolean disablePearls, customMap;
    @Setter private List<String> kits = new ArrayList<>();
    @Setter private List<BlockState> blockStates = new ArrayList<>();
    @Setter public org.bukkit.inventory.ItemStack displayIcon;

    public Arena(String name) {
        this.name = name;
        this.displayName = CC.GREEN + name;
        this.displayIcon = new ItemStack(Material.PAPER);
    }

    public static void preload() {
        FileConfiguration configuration = Ghoul.getInstance().getArenasConfig().getConfiguration();

        if (configuration.contains("arenas")) {
            if (configuration.getConfigurationSection("arenas") == null) return;
            for (String arenaName : configuration.getConfigurationSection("arenas").getKeys(false)) {
                String path = "arenas." + arenaName;
                ArenaType arenaType = ArenaType.valueOf(configuration.getString(path + ".type"));
                Arena arena;
                if (arenaType == ArenaType.STANDALONE) {
                    arena = new StandaloneArena(arenaName);
                } else if (arenaType == ArenaType.SHARED) {
                    arena = new SharedArena(arenaName);
                } else if (arenaType == ArenaType.THEBRIDGE) {
                    arena = new TheBridgeArena(arenaName);
                } else {
                    continue;
                }
                if (configuration.contains(path + ".display-name")) {
                    arena.setDisplayName(CC.translate(configuration.getString(path + ".display-name")));
                }
                if (configuration.contains(path + ".icon-material")) {
                    arena.setDisplayIcon(new ItemBuilder(Material.valueOf(configuration.getString(path + ".icon-material"))).build());
                } else {
                    arena.setDisplayIcon(new ItemBuilder(Material.PAPER).durability(0).build());
                }

                if (configuration.contains(path + ".disable-pearls")) {
                    arena.setDisablePearls(configuration.getBoolean(path + ".disable-pearls"));
                } else {
                    arena.setDisablePearls(false);
                }

                if (configuration.contains(path + ".custom-map-party")) {
                    arena.setCustomMap(configuration.getBoolean(path + ".custom-map-party"));
                } else {
                    arena.setCustomMap(false);
                }

                if (configuration.contains(path + ".spawn1")) {
                    arena.setSpawn1(LocationUtil.deserialize(configuration.getString(path + ".spawn1")));
                }

                if (configuration.contains(path + ".spawn2")) {
                    arena.setSpawn2(LocationUtil.deserialize(configuration.getString(path + ".spawn2")));
                }

                if (configuration.contains(path + ".corner1")) {
                    arena.setCorner1(LocationUtil.deserialize(configuration.getString(path + ".corner1")));
                }

                if (configuration.contains(path + ".corner2")) {
                    arena.setCorner2(LocationUtil.deserialize(configuration.getString(path + ".corner2")));
                }

                if (arena instanceof TheBridgeArena && configuration.contains(path + ".redCuboid") && configuration.contains(path + ".blueCuboid")) {
                    Location location1;
                    Location location2;
                    //Declare the arena as type TheBridge
                    TheBridgeArena standaloneArena = (TheBridgeArena) arena;

                    //If "redCuboid" location exist then load it
                    location1 = LocationUtil.deserialize(configuration.getString(path + ".redCuboid.location1"));
                    location2 = LocationUtil.deserialize(configuration.getString(path + ".redCuboid.location2"));
                    standaloneArena.setRedCuboid(new Cuboid(location1, location2));

                    //If "blueCuboid" location exist then load it
                    location1 = LocationUtil.deserialize(configuration.getString(path + ".blueCuboid.location1"));
                    location2 = LocationUtil.deserialize(configuration.getString(path + ".blueCuboid.location2"));
                    standaloneArena.setBlueCuboid(new Cuboid(location1, location2));
                }

                if (configuration.contains(path + ".kits")) {
                    for (String kitName : configuration.getStringList(path + ".kits")) {
                        arena.getKits().add(kitName);
                    }
                }

                if (arena instanceof StandaloneArena && configuration.contains(path + ".duplicates")) {
                    for (String duplicateId : configuration.getConfigurationSection(path + ".duplicates").getKeys(false)) {
                        Location spawn1 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".spawn1"));
                        Location spawn2 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".spawn2"));

                        Arena duplicate = new Arena(arenaName);

                        duplicate.setSpawn1(spawn1);
                        duplicate.setSpawn2(spawn2);

                        if (configuration.contains(path + ".duplicates." + duplicateId + ".corner1")) {
                            Location corner1 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".corner1"));
                            duplicate.setCorner1(corner1);
                        }
                        if (configuration.contains(path + ".duplicates." + duplicateId + ".corner2")) {
                            Location corner2 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".corner2"));
                            duplicate.setCorner2(corner2);
                        }

                        duplicate.setKits(arena.getKits());

                        ((StandaloneArena) arena).getDuplicates().add(duplicate);

                        if (duplicate.isCustomMap()) {
                            Arena.getCustomArenas().add(duplicate);
                        } else {
                            Arena.getArenas().add(duplicate);
                        }

                        if (duplicate.getCorner1() != null && duplicate.getCorner2() != null) {
                            for (Block block : blocksFromTwoPoints(duplicate.getCorner1(), duplicate.getCorner2())) {
                                duplicate.getBlockStates().add(block.getState());
                            }
                        }
                    }
                }

                if (arena.getCorner1() != null && arena.getCorner2() != null) {
                    for (Block block : blocksFromTwoPoints(arena.getCorner1(), arena.getCorner2())) {
                        arena.getBlockStates().add(block.getState());
                    }
                }

                if (arena.isCustomMap()) {
                    Arena.getCustomArenas().add(arena);
                } else {
                    Arena.getArenas().add(arena);
                }
            }
        }

        Ghoul.logger("&cLoaded " + (Arena.getArenas().size() + Arena.getCustomArenas().size()) + " arenas.");
    }

    public static List<Block> blocksFromTwoPoints(Location loc1, Location loc2) {
        List<Block> blocks = new ArrayList<>();

        int topBlockX = (Math.max(loc1.getBlockX(), loc2.getBlockX()));
        int bottomBlockX = (Math.min(loc1.getBlockX(), loc2.getBlockX()));

        int topBlockY = (Math.max(loc1.getBlockY(), loc2.getBlockY()));
        int bottomBlockY = (Math.min(loc1.getBlockY(), loc2.getBlockY()));

        int topBlockZ = (Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        int bottomBlockZ = (Math.min(loc1.getBlockZ(), loc2.getBlockZ()));

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    public static ArenaType getTypeByName(String name) {
        for (ArenaType arena : ArenaType.values()) {
            if (arena.toString().equalsIgnoreCase(name)) {
                return arena;
            }
        }
        return null;
    }

    public static Arena getByName(String name) {
        for (Arena arena : arenas) {
            if (arena.getType() != ArenaType.DUPLICATE && arena.getName() != null &&
                    arena.getName().equalsIgnoreCase(name)) {
                return arena;
            }
        }

        return null;
    }

    public ItemStack getDisplayIcon() {
        return this.displayIcon.clone();
    }

    public static Arena getRandom(Kit kit) {
        List<Arena> _arenas = new ArrayList<>();

        for (Arena arena : arenas) {
            if (!arena.isSetup()) continue;

            if (!arena.getKits().contains(kit.getName())) continue;

            if (!arena.isCustomMap() && !arena.isActive() && (arena.getType() == ArenaType.STANDALONE || arena.getType() == ArenaType.DUPLICATE || arena.getType() == ArenaType.THEBRIDGE)) {
                _arenas.add(arena);
            } else if (!arena.isCustomMap() && !kit.getGameRules().isBuild() && arena.getType() == ArenaType.SHARED) {
                _arenas.add(arena);
            }
        }

        if (_arenas.isEmpty()) {
            return null;
        }

        return _arenas.get(ThreadLocalRandom.current().nextInt(_arenas.size()));
    }

    public static Arena getRandomCustom(Kit kit) {
        List<Arena> _arenas = new ArrayList<>();

        for (Arena arena : customArenas) {
            if (!arena.isSetup()) continue;

            if (!arena.getKits().contains(kit.getName())) continue;

            if (!arena.isActive() && (arena.getType() == ArenaType.STANDALONE || arena.getType() == ArenaType.DUPLICATE || arena.getType() == ArenaType.THEBRIDGE)) {
                _arenas.add(arena);
            } else if (arena.isCustomMap() && !kit.getGameRules().isBuild() && arena.getType() == ArenaType.SHARED) {
                _arenas.add(arena);
            }
        }

        if (_arenas.isEmpty()) {
            return null;
        }

        return _arenas.get(ThreadLocalRandom.current().nextInt(_arenas.size()));
    }

    public ArenaType getType() {
        return ArenaType.DUPLICATE;
    }

    public boolean isSetup() {
        return spawn1 != null && spawn2 != null;
    }

    public int getMaxBuildHeight() {
        int highest = (int) (Math.max(spawn1.getY(), spawn2.getY()));
        return highest + 5;
    }

    public Location getSpawn1() {
        if (spawn1 == null) {
            return null;
        }

        return spawn1.clone();
    }

    public Location getSpawn2() {
        if (spawn2 == null) {
            return null;
        }

        return spawn2.clone();
    }


    public void setActive(boolean active) {
        if (getType() != ArenaType.SHARED) {
            this.active = active;
        }
    }

    public void save() {

    }

    public void delete() {

    }

}
