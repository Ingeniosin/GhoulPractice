package net.ghoul.practice.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
    }

}
