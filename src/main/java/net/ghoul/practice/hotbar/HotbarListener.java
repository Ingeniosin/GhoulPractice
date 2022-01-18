package net.ghoul.practice.hotbar;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.enums.HotbarType;
import net.ghoul.practice.enums.PartyMessageType;
import net.ghoul.practice.kiteditor.menu.KitEditorSelectKitMenu;
import net.ghoul.practice.match.menu.MatchsMenu;
import net.ghoul.practice.menus.SelectQueueMenu;
import net.ghoul.practice.party.Party;
import net.ghoul.practice.party.menu.OtherPartiesMenu;
import net.ghoul.practice.party.menu.PartyEventSelectEventMenu;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.profile.ProfileState;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.queue.menu.QueueSelectKitMenu;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.config.BasicConfigurationFile;
import net.ghoulnetwork.core.managers.cosmetics.CosmeticMenu;
import net.ghoulnetwork.core.managers.stats.menu.StatsMenu;
import net.ghoulnetwork.core.menus.settings.SettingsMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class HotbarListener implements Listener {

    BasicConfigurationFile config = Ghoul.getInstance().getMessagesConfig();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getItem() != null && event.getAction().name().contains("RIGHT")) {
            final Player player = event.getPlayer();
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            final HotbarType hotbarType= Hotbar.fromItemStack(event.getItem());
            if (hotbarType == null) return;
            event.setCancelled(true);
            switch (hotbarType) {
                case SETTINGS_MENU:
                    new SettingsMenu().open(player);
                    break;
                case COSMETICS:
                    new CosmeticMenu().open(player);
                    break;
                case STATS:
                    new StatsMenu(player.getName()).open(player);
                    break;
                case MATCHSLIST:
                    new MatchsMenu().open(player);
                    break;
                case SELECT_QUEUE_TYPE:
                    if (!profile.isBusy()) {
                        new SelectQueueMenu(player).open(player);
                        break;
                    }
                    break;
                case QUEUE_JOIN_RANKED:
                    if ((profile.getTotalWins() + profile.getTotalLost()) < 10) {
                        player.sendMessage(CC.translate("&cYou need to play 10 unranked matches."));
                        player.closeInventory();
                        break;
                    }
                    if (!profile.isBusy()) {
                        new QueueSelectKitMenu(QueueType.RANKED).openMenu(event.getPlayer());
                        break;
                    }
                    break;
                case QUEUE_JOIN_UNRANKED:
                    if (!profile.isBusy()) {
                        new QueueSelectKitMenu(QueueType.UNRANKED).openMenu(event.getPlayer());
                        break;
                    }
                    break;
                case QUEUE_LEAVE:
                    if (profile.isInQueue()) {
                        profile.getQueue().removePlayer(profile.getQueueProfile());
                        break;
                    }
                    break;
                case PARTY_EVENTS: {
                    new PartyEventSelectEventMenu().openMenu(player);
                    break;
                }
                case OTHER_PARTIES: {
                    new OtherPartiesMenu().openMenu(event.getPlayer());
                    break;
                }
                case PARTY_INFO: {
                    profile.getParty().sendInformation(player);
                    break;
                }
                case KIT_EDITOR: {
                    if (profile.isInLobby() || profile.isInQueue()) {
                        new KitEditorSelectKitMenu().openMenu(event.getPlayer());
                        break;
                    }
                    break;
                }
                case PARTY_CREATE: {
                    if (profile.getParty() != null) {
                        player.sendMessage(CC.translate(config.getString("Party.Already-Have-Party")));
                        return;
                    }
                    if (!profile.isInLobby()) {
                        player.sendMessage(CC.translate(config.getString("Party.Not-In-Lobby")));
                        return;
                    }
                    profile.setParty(new Party(player));
                    PlayerUtil.reset(player, false);
                    profile.refreshHotbar();
                    player.sendMessage(PartyMessageType.CREATED.format());
                    break;
                }
                case PARTY_DISBAND: {
                    if (profile.getParty() == null) {
                        player.sendMessage(CC.translate(config.getString("Party.Dont-Have-Party")));
                        return;
                    }
                    if (!profile.getParty().getLeader().getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(CC.translate(config.getString("Party.Not-Leader")));
                        return;
                    }
                    profile.getParty().disband();
                    break;
                }
                case PARTY_INFORMATION: {
                    if (profile.getParty() == null) {
                        player.sendMessage(CC.translate(config.getString("Party.Dont-Have-Party")));
                        return;
                    }
                    profile.getParty().sendInformation(player);
                    break;
                }
                case PARTY_LEAVE: {
                    if (profile.getParty() == null) {
                        player.sendMessage(CC.translate(config.getString("Party.Dont-Have-Party")));
                        return;
                    }
                    if (profile.getParty().getLeader().getUniqueId().equals(player.getUniqueId())) {
                        profile.getParty().disband();
                        break;
                    }
                    profile.getParty().leave(player, false);
                    break;
                }
                case SPECTATE_STOP: {
                    if (profile.isInFight() && !profile.getMatch().getTeamPlayer(player).isAlive()) {
                        profile.getMatch().getTeamPlayer(player).setDisconnected(true);
                        profile.setState(ProfileState.IN_LOBBY);
                        profile.setMatch(null);
                        break;
                    }
                    if (!profile.isSpectating()) {
                        player.sendMessage(CC.RED + "You are not spectating a match.");
                        break;
                    }
                    if (profile.getMatch() != null) {
                        profile.getMatch().removeSpectator(player);
                        break;
                    }
                    break;
                }
                default: {}
            }
        }
    }
}

