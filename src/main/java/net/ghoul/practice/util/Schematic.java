package net.ghoul.practice.util;

import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import lombok.Getter;
import org.bukkit.World;

import java.io.File;

@Getter
public class Schematic {
    private CuboidClipboard clipboard;

    public Schematic(File file) {
        try {
            this.clipboard = SchematicFormat.getFormat(file).load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pasteSchematic(World world, int x, int y, int z) {
        Vector pastePos = new Vector(x, y, z);
        EditSession editSession = (new EditSessionBuilder((new BukkitWorld(world))).limitUnlimited().fastmode(Boolean.TRUE).build());
        editSession.enableQueue();
        try {
            this.clipboard.place(editSession, pastePos, true);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
        editSession.flushQueue();
    }
}
