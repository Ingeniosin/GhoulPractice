package net.ghoul.practice.arena.generator;

import lombok.Getter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.util.Schematic;
import net.ghoul.practice.util.TaskUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.beans.ConstructorProperties;

@Getter
public class ArenaGenerator {

    private final Ghoul plugin;
    private final World world;
    private final Schematic schematic;
    private final Player player;

    @ConstructorProperties({"plugin", "world", "schematic", "player"})
    public ArenaGenerator(Ghoul plugin, World world, Schematic schematic, Player player) {
        this.plugin = plugin;
        this.world = world;
        this.schematic = schematic;
        this.player = player;
    }

    public void generate(int id, int startingX, int startingZ) {
        int minX = startingX - this.schematic.getClipboard().getWidth() - 100;
        int maxX = startingX + this.schematic.getClipboard().getWidth() + 100;
        int minZ = startingZ - this.schematic.getClipboard().getLength() - 100;
        int maxZ = startingZ + this.schematic.getClipboard().getLength() + 100;
        int maxY = 72 + this.schematic.getClipboard().getHeight();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = 72; y < maxY; y++) {
                    if (this.world.getBlockAt(x, y, z).getType() != Material.AIR) {
                        player.sendMessage(String.format("§7Couldn't create schematic §c%1$s, §7there are blocks nearby", id));
                        return;
                    }
                }
            }
        }
        TaskUtil.run(() -> {
            try {
                schematic.pasteSchematic(world, startingX, 76, startingZ);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            player.sendMessage(String.format("§7Pasted schematic §c%4$s §7at §c%1$s§7, §c%2$s§7, §c%3$s", startingX, 76, startingZ, id));
        });
    }
}
