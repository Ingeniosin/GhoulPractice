package net.ghoul.practice.match.task;

import net.ghoul.practice.arena.Arena;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class MatchResetQueue extends Thread {

    Arena arena;

    public MatchResetQueue(Arena arena) {
        this.arena = arena;
    }

    @Override
    public void run() {
        while (true) {
            try {
                arena.getBlockStates().forEach(oldBlock -> {
                    Location newLocation = new Location(oldBlock.getWorld(), oldBlock.getLocation().getBlockX(), oldBlock.getLocation().getBlockY(), oldBlock.getLocation().getZ());

                    Block newBlock = newLocation.getBlock();

                    if (oldBlock.getType() != newBlock.getType()) {
                        Block originalBlock = oldBlock.getLocation().getBlock();
                        originalBlock.setType(oldBlock.getType());
                        originalBlock.setData(oldBlock.getRawData());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(50L);
                }
                catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                continue;
            }
            try {
                Thread.sleep(50L);
            }
            catch (InterruptedException e3) {
                e3.printStackTrace();
            }
        }
    }
}
