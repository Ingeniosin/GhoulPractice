package net.ghoul.practice.party;

import lombok.Getter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.duel.DuelRequest;
import net.ghoul.practice.enums.PartyMessageType;
import net.ghoul.practice.enums.PartyPrivacyType;
import net.ghoul.practice.ghoul.essentials.Essentials;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.profile.ProfileState;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ChatComponentBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party {

    private final static List<Party> parties = new ArrayList<>();

    @Getter private final Player leader;
    private final List<UUID> players;
    private final List<PartyInvite> invites;
    private PartyPrivacyType privacy;
    private final int limit;
    
    public Party(final Player player) {
        this.leader = player;
        this.players = new ArrayList<>();
        this.invites = new ArrayList<>();
        this.privacy = PartyPrivacyType.CLOSED;

        this.players.add(player.getUniqueId());

        if (leader.hasPermission("party.bypasslimit")){
            limit = 50;
        } else {
            limit = 8;
        }

        parties.add(this);
    }

    public void broadcast(String messages) {
        this.getPlayers().forEach(player -> player.sendMessage(messages));
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();

        this.players.forEach(all -> {
            Player player = Bukkit.getPlayer(all);

            if (player != null) {
                players.add(player);
            }
        });

        return players;
    }
    
    public void setPrivacy(final PartyPrivacyType privacy) {
        this.privacy = privacy;
        this.broadcast(PartyMessageType.PRIVACY_CHANGED.format(privacy.toString()));

        getPlayers().forEach(player -> player.sendMessage(CC.translate("&eYour party privacy has been set to &c" + privacy + ".")));
        if (privacy == PartyPrivacyType.OPEN) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> player.spigot().sendMessage(new ChatComponentBuilder(CC.translate("&e" +
                    this.leader.getDisplayName() + " is hosting a public party &7(Click to accept)"))
                    .attachToEachPart(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + this.leader.getName()))
                    .attachToEachPart(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.GRAY + "Click to accept.").create()))
                    .create()));
        }
    }
    
    public PartyInvite getInvite(final UUID uuid) {
        for ( PartyInvite invite : this.invites )
            if (invite.getUuid().equals(uuid)) {
                if (invite.hasExpired()) {
                    return null;
                }
                return invite;
            }
        return null;
    }
    
    public void invite(final Player target) {

        this.invites.add(new PartyInvite(target.getUniqueId()));

        target.spigot().sendMessage(new ChatComponentBuilder(CC.translate("&eYou have received a party invitation from &c" + this.leader.getName()))
                .attachToEachPart(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + this.leader.getName()))
                .attachToEachPart(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.GRAY + "Click to accept.").create()))
                .create());

        this.broadcast(PartyMessageType.PLAYER_INVITED.format(target.getName()));
    }
    
    public void join(final Player player) {
        invites.removeIf(invite -> invite.getUuid().equals(player.getUniqueId()));
        players.add(player.getUniqueId());

        this.broadcast(PartyMessageType.PLAYER_JOINED.format(player.getName()));

        final Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setParty(this);

        if (profile.isInLobby() || profile.isInQueue()) {
            PlayerUtil.reset(player, false);
            profile.refreshHotbar();
            profile.handleVisibility();
        }

        for (Player players : this.getPlayers()) {
            if (players != null) {
                final Profile teamProfile = Profile.getByUuid(players.getUniqueId());
                teamProfile.handleVisibility(players, player);
            }
        }
    }

    public void leave(final Player player, final boolean kick) {
        this.broadcast(PartyMessageType.PLAYER_LEFT.format(player.getName(), kick ? "been kicked from" : "left"));
        this.getPlayers().removeIf(member -> member.getUniqueId().equals(player.getUniqueId()));
        final Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setParty(null);
        if (profile.isInLobby() || profile.isInQueue()) {
            profile.handleVisibility();
            PlayerUtil.reset(player, false);
            profile.refreshHotbar();
        }
        if (profile.isInFight()) {
            profile.getMatch().handleDeath(player, null, true);
            if (profile.getMatch().isTeamMatch()) {
                for (Player players : this.getPlayers()) {
                    player.hidePlayer(players);
                }
            }
            player.setFireTicks(0);
            player.updateInventory();
            profile.setState(ProfileState.IN_LOBBY);
            profile.setMatch(null);
            PlayerUtil.reset(player, false);
            profile.refreshHotbar();
            profile.handleVisibility();
            Essentials.teleportToSpawn(player);
        }
        for (Player players : this.getPlayers()) {
            if (players != null) {
                final Profile otherProfile = Profile.getByUuid(players.getUniqueId());
                otherProfile.handleVisibility(players, player);
            }
        }
    }

    public void disband() {
        this.broadcast(PartyMessageType.DISBANDED.format());
        final Profile leaderProfile = Profile.getByUuid(this.leader.getUniqueId());
        leaderProfile.getSentDuelRequests().values().removeIf(DuelRequest::isParty);
        this.getPlayers().forEach(player -> {
            if(Profile.getByUuid(player.getUniqueId()).isInFight()) {
                Profile.getByUuid(player.getUniqueId()).getMatch().handleDeath(player, this.leader, true);
            }
            Profile.getByUuid(player.getUniqueId());
            Profile.getByUuid(player.getUniqueId()).setParty(null);
            if (Profile.getByUuid(player.getUniqueId()).isInLobby() || Profile.getByUuid(player.getUniqueId()).isInQueue()) {
                Profile.getByUuid(player.getUniqueId()).refreshHotbar();
                Profile.getByUuid(player.getUniqueId()).handleVisibility();
            }
        });
        Party.parties.remove(this);
    }
    
    public void sendInformation(final Player player) {
        List<String> lines = new ArrayList<>();
        lines.add(CC.SB_BAR);
        lines.add(CC.translate("&cParty players: &f" + this.getPlayers().size()));
        lines.add(CC.translate(""));
        for (Player players : this.getPlayers()) {
            lines.add(CC.translate(" &7- &c" + players.getName()));
        }
        lines.add(CC.translate(""));
        lines.add(CC.translate("&cPrivacy: &f" + ChatColor.stripColor(this.privacy.toString())));
        lines.add(CC.SB_BAR);

        for (String line : lines) {
            player.sendMessage(line);
        }
    }
    
    public static void preload() {
        new BukkitRunnable() {
            public void run() {
                Party.getParties().forEach(party -> party.getInvites().removeIf(PartyInvite::hasExpired));
            }
        }.runTaskTimerAsynchronously(Ghoul.getInstance(), 100L, 100L);
        new BukkitRunnable() {
            public void run() {
                for (final Party party : Party.getParties()) {
                    if (party.getPrivacy() == PartyPrivacyType.OPEN) {
                        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.spigot().sendMessage(new ChatComponentBuilder(CC.translate("&e" +
                                party.leader.getDisplayName() + " &eis hosting a public party &7(Click to accept)"))
                                .attachToEachPart(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + party.leader.getName()))
                                .attachToEachPart(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.GRAY + "Click to accept.").create()))
                                .create()));
                    }
                }
            }
        }.runTaskTimerAsynchronously(Ghoul.getInstance(), 1800L, 1800L);
    }
    
    public PartyPrivacyType getPrivacy() {
        return this.privacy;
    }
    
    public List<PartyInvite> getInvites() {
        return this.invites;
    }
    
    public static List<Party> getParties() {
        return Party.parties;
    }
    
    public int getLimit() {
        return this.limit;
    }
}
