package net.ghoul.practice.register;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.selection.ArenaSelectionListener;
import net.ghoul.practice.ghoul.listener.GoldenHeads;
import net.ghoul.practice.ghoul.listener.MOTDListener;
import net.ghoul.practice.hotbar.HotbarListener;
import net.ghoul.practice.kiteditor.KitEditorListener;
import net.ghoul.practice.match.cps.CPSListener;
import net.ghoul.practice.match.listener.DamageListener;
import net.ghoul.practice.match.listener.ExtraListener;
import net.ghoul.practice.match.listener.MatchListener;
import net.ghoul.practice.party.PartyListener;
import net.ghoul.practice.profile.ProfileListener;
import net.ghoul.practice.queue.QueueListener;
import net.ghoul.practice.settings.SettingsListener;
import net.ghoul.practice.util.events.WorldListener;
import org.bukkit.event.Listener;

import java.util.Arrays;

public class RegisterListeners {

    public static void register() {
        for (Listener listener : Arrays.asList(
                new ProfileListener(),
                new net.ghoul.practice.util.menu.MenuListener(),
                new net.ghoul.practice.util.external.menu.MenuListener(Ghoul.getInstance()),
                new ArenaSelectionListener(),
                new KitEditorListener(),
                new MOTDListener(),
                new PartyListener(),
                new HotbarListener(),
                new MatchListener(),
                new WorldListener(),
                new GoldenHeads(),
                new ExtraListener(),
                new QueueListener(),
                new DamageListener(),
                new SettingsListener(),
                new CPSListener())) {
            Ghoul.getInstance().getServer().getPluginManager().registerEvents(listener, Ghoul.getInstance());
        }
    }
}
