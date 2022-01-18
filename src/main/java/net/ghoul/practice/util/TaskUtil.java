package net.ghoul.practice.util;

import net.ghoul.practice.Ghoul;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskUtil {
    public TaskUtil() {
    }

    public static void run(Runnable runnable) {
        Ghoul.getInstance().getServer().getScheduler().runTask(Ghoul.getInstance(), runnable);
    }

    public static void runTimer(Runnable runnable, long delay, long timer) {
        Ghoul.getInstance().getServer().getScheduler().runTaskTimer(Ghoul.getInstance(), runnable, delay, timer);
    }

    public static void runTimer(BukkitRunnable runnable, long delay, long timer) {
        runnable.runTaskTimer(Ghoul.getInstance(), delay, timer);
    }

    public static void runLater(Runnable runnable, long delay) {
        Ghoul.getInstance().getServer().getScheduler().runTaskLater(Ghoul.getInstance(), runnable, delay);
    }

    public static void runAsyncTimer(Callable callable, long delay, long interval) {
        Ghoul.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(Ghoul.getInstance(), callable::call, delay, interval);
    }

    public static void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            runnable.run();
        else
            Bukkit.getScheduler().runTask(Ghoul.getInstance(), runnable);
    }

    public static void runAsync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTaskAsynchronously(Ghoul.getInstance(), runnable);
        else
            runnable.run();
    }

    public static void runLaterAsync(Runnable runnable, long delay) {
        Ghoul.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(Ghoul.getInstance(), runnable, delay);
    }

    public interface Callable {
        void call();
    }
}
