package net.ghoul.practice.arena.impl;

import lombok.Getter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.enums.ArenaType;
import net.ghoul.practice.util.external.LocationUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class StandaloneArena extends Arena {

    private final List<Arena> duplicates = new ArrayList<>();

    public StandaloneArena(String name) {
        super(name);
    }

    @Override
    public ArenaType getType() {
        return ArenaType.STANDALONE;
    }

    @Override
    public void save() {
        String path = "arenas." + getName();

        FileConfiguration configuration = Ghoul.getInstance().getArenasConfig().getConfiguration();
        configuration.set(path, null);
        configuration.set(path + ".type", getType().name());
        configuration.set(path + ".icon-material", displayIcon.getType().name());
        configuration.set(path + ".disable-pearls", disablePearls);
        configuration.set(path + ".display-name", displayName);
        configuration.set(path + ".custom-map-party", customMap);

        if (spawn1 != null) {
            configuration.set(path + ".spawn1", LocationUtil.serialize(spawn1));
        }

        if (spawn2 != null) {
            configuration.set(path + ".spawn2", LocationUtil.serialize(spawn2));
        }

        if (corner1 != null) {
            configuration.set(path + ".corner1", LocationUtil.serialize(corner1));
        }

        if (corner2 != null) {
            configuration.set(path + ".corner2", LocationUtil.serialize(corner2));
        }

        configuration.set(path + ".kits", getKits());

        if (!duplicates.isEmpty()) {
            int i = 0;

            for (Arena duplicate : duplicates) {
                i++;

                if (duplicate.getSpawn1() != null) {
                    configuration.set(path + ".duplicates." + i + ".spawn1", LocationUtil.serialize(duplicate.getSpawn1()));
                }
                if (duplicate.getSpawn2() != null) {
                    configuration.set(path + ".duplicates." + i + ".spawn2", LocationUtil.serialize(duplicate.getSpawn2()));
                }

                if (duplicate.getCorner1() != null) {
                    configuration.set(path + ".duplicates." + i + ".corner1", LocationUtil.serialize(duplicate.getCorner1()));
                }
                if (duplicate.getCorner2() != null) {
                    configuration.set(path + ".duplicates." + i + ".corner2", LocationUtil.serialize(duplicate.getCorner2()));
                }
            }
        }
        try {
            configuration.save(Ghoul.getInstance().getArenasConfig().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete() {
        FileConfiguration configuration = Ghoul.getInstance().getArenasConfig().getConfiguration();
        configuration.set("arenas." + getName(), null);

        try {
            configuration.save(Ghoul.getInstance().getArenasConfig().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
