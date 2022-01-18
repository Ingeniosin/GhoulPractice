package net.ghoul.practice.util;

import net.ghoul.practice.Ghoul;

public class Description {
    public static String getVersion() {
        return Ghoul.getInstance().getDescription().getVersion();
    }

    public static String getAuthor() {
        return Ghoul.getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", "");
    }

    public static String getName() {
        return Ghoul.getInstance().getDescription().getName();
    }
}
