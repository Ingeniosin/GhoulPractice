package net.ghoul.practice.providers;

import net.ghoul.practice.GhoulCache;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.party.Party;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.Queue;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.scoreboard.Board;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreProvider extends Board {

    private final Player player;

    public ScoreProvider(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void update() {
        if (this.player == null || !this.player.isOnline()) return;

        Profile profile = Profile.getByUuid(player.getUniqueId());

        this.setTitle("§c§lPractice");

        final List<String> lines=new ArrayList<>();
        lines.add("§7§m-----------------");

        if (profile.isInLobby() || profile.isInQueue()) {
            lines.add("§fOnline: §c" + GhoulCache.getOnline());
            lines.add("§fIn Fights: §c" + GhoulCache.getInFights());
            if (profile.getParty() != null) {
                final Party party = profile.getParty();
                lines.add("");
                lines.add("§fYour Party: §c" + party.getPlayers().size() + "§7/§c" + party.getLimit());
            }
            if (profile.isInQueue()) {
                final Queue queue = profile.getQueue();
                lines.add("");
                lines.add("§fQueuing " + (queue.getQueueType() == QueueType.UNRANKED ? "Unranked:" : "Ranked:"));
                lines.add(" §8» §c" + queue.getKit().getDisplayName());
                lines.add("§fDuration: ");
                lines.add(" §8» §c" + queue.getDuration(player));
            }
        } else if (profile.isInFight()) {
            final Match match = profile.getMatch();
            if (match != null) {
                if (match.isSoloMatch()) {
                    final TeamPlayer self = match.getTeamPlayer(player);
                    final TeamPlayer opponent = match.getOpponentTeamPlayer(player);
                    if (opponent.isDisconnected()) {
                        lines.add("§fOpponent:");
                        lines.add(" §8» §cDisconnected");
                        lines.add("§fMatch Time:");
                        lines.add(" §8» §c" + profile.getMatch().getDuration());
                        if (match.getKit().getName().toLowerCase().contains("boxing")) {
                            lines.add("");
                            lines.add("§fYour hits: §c" + self.getHits());
                            lines.add("§fTheir hits: §c0");
                        } else {
                            lines.add("");
                            lines.add("§fYour Cps: §c" + profile.getClicks());
                            lines.add("§fTheir Cps: §c0");
                        }
                    } else {
                        Profile opponentProfile = Profile.getByUuid(opponent.getPlayer().getUniqueId());
                        lines.add("§fOpponent:");
                        lines.add(" §8» §c" + opponent.getUsername());
                        lines.add("§fMatch Time:");
                        lines.add(" §8» §c" + profile.getMatch().getDuration());
                        if (match.getKit().getName().toLowerCase().contains("boxing")) {
                            lines.add("");
                            lines.add("§fYour hits: §c" + self.getHits());
                            lines.add("§fTheir hits: §c" + opponent.getHits());
                        } else {
                            lines.add("");
                            lines.add("§fYour Cps: §c" + profile.getClicks());
                            lines.add("§fTheir Cps: §c" + opponentProfile.getClicks());
                        }
                    }
                } else if (match.isSumoMatch()) {
                    TeamPlayer opponent = match.getOpponentTeamPlayer(player);
                    int selfPoints = profile.getSumoRounds();

                    if (opponent.isDisconnected()) {
                        lines.add("§fOpponent:");
                        lines.add(" §8» §cDisconnected");
                        lines.add("§fPoints:");
                        lines.add(" §8» §c" + selfPoints + " §7/ §c0");
                        lines.add("");
                        lines.add("§fYour Cps: §c" + profile.getClicks());
                        lines.add("§fTheir Cps: §c0");
                    } else {
                        Profile targetProfile = Profile.getByUuid(opponent.getUuid());
                        int opPoints = targetProfile.getSumoRounds();

                        lines.add("§fOpponent:");
                        lines.add(" §8» §c" + opponent.getUsername());
                        lines.add("§fPoints:");
                        lines.add(" §8» §c" + selfPoints + " §7/ §c" + opPoints);
                        lines.add("");
                        lines.add("§fYour Cps: §c" + profile.getClicks());
                        lines.add("§fTheir Cps: §c" + targetProfile.getClicks());
                    }
                } else if (match.isTeamMatch() || match.isFreeForAllMatch()) {
                    final Team team = match.getTeam(player);
                    lines.add("§fPlayers: §c" + team.getAliveCount() + "§7/§c" + team.getTeamPlayers().size());
                    lines.add("§fMatch Time: §c" + match.getDuration());
                }
            }
        } else if (profile.isSpectating()) {
            final Match match = profile.getMatch();
            if (match != null) {
                lines.add("§fMatch Time: §c" + match.getDuration());
            }
        }
        lines.add("");
        lines.add("§cplay.ghoulpvp.us");
        lines.add("§7§m-----------------");
        this.setSlotsFromList(lines);
    }
}
