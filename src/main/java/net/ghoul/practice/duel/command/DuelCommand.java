package net.ghoul.practice.duel.command;

import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.arena.impl.StandaloneArena;
import net.ghoul.practice.duel.DuelProcedure;
import net.ghoul.practice.duel.DuelRequest;
import net.ghoul.practice.duel.menu.DuelSelectKitMenu;
import net.ghoul.practice.enums.ArenaType;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.match.types.SoloMatch;
import net.ghoul.practice.match.types.SumoMatch;
import net.ghoul.practice.match.types.TeamMatch;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(CC.MENU_BAR);
            player.sendMessage(CC.translate("&c/duel <player>"));
            player.sendMessage(CC.translate("&c/duel accept <player>"));
            player.sendMessage(CC.MENU_BAR);
            return true;
        }

        Player target;

        if (args[0].equals("accept")) {

            if (args.length < 2) {
                player.sendMessage(CC.translate("&c/duel accept <player>"));
                return true;
            }

            target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                player.sendMessage(CC.RED + "That player is no longer online.");
                return true;
            }

            if (player.hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot duel a player while being frozen.");
                return true;
            }

            if (target.hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot duel a player who's frozen.");
                return true;
            }

            Profile senderProfile = Profile.getByUuid(player.getUniqueId());

            if (senderProfile.isBusy()) {
                player.sendMessage(CC.RED + "You cannot duel anyone right now.");
                return true;
            }

            Profile receiverProfile = Profile.getByUuid(target.getUniqueId());

            if (!receiverProfile.isPendingDuelRequest(player)) {
                player.sendMessage(CC.RED + "You do not have a pending duel request from " + target.getName() + CC.RED + ".");
                return true;
            }

            if (receiverProfile.isBusy()) {
                player.sendMessage(CC.translate(CC.RED + target.getDisplayName()) + CC.RED + " is currently busy.");
                return true;
            }

            DuelRequest request = receiverProfile.getSentDuelRequests().get(player.getUniqueId());

            if (request == null) {
                return true;
            }

            if (request.isParty()) {
                if (senderProfile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party to duel with.");
                    return true;
                } else if (receiverProfile.getParty() == null) {
                    player.sendMessage(CC.RED + "That player does not have a party to duel with.");
                    return true;
                }
            } else {
                if (senderProfile.getParty() != null) {
                    player.sendMessage(CC.RED + "You cannot duel whilst in a party.");
                    return true;
                } else if (receiverProfile.getParty() != null) {
                    player.sendMessage(CC.RED + "That player is in a party and cannot duel right now.");
                    return true;
                }
            }

            Arena arena = request.getArena();

            if (arena == null) {
                player.sendMessage(CC.RED + "Tried to start a match but the arena was invalid.");
                return true;
            }

            if (arena.isActive()) {
                if (arena.getType().equals(ArenaType.STANDALONE)) {
                    StandaloneArena sarena = (StandaloneArena) arena;
                    if (sarena.getDuplicates() != null) {
                        boolean foundarena = false;
                        for (Arena darena : sarena.getDuplicates()) {
                            if (!darena.isActive()) {
                                arena = darena;
                                foundarena = true;
                                break;
                            }
                        }
                        if (!foundarena) {
                            player.sendMessage(CC.RED + "The arena you were dueled was a build match and there were no arenas found.");
                            return true;
                        }
                    }
                } else {
                    player.sendMessage(CC.RED + "The arena you were dueled was a build match and there were no arenas found.");
                    return true;
                }
            }
            if (!arena.getType().equals(ArenaType.SHARED)) {
                arena.setActive(true);
            }

            Match match;

            if (request.isParty()) {
                Team teamA = new Team(new TeamPlayer(player));

                for (Player partyMember : senderProfile.getParty().getPlayers()) {
                    if (!partyMember.getPlayer().equals(player)) {
                        teamA.getTeamPlayers().add(new TeamPlayer(partyMember));
                    }
                }

                Team teamB = new Team(new TeamPlayer(target));

                for (Player partyMember : receiverProfile.getParty().getPlayers()) {
                    if (!partyMember.getPlayer().equals(target)) {
                        teamB.getTeamPlayers().add(new TeamPlayer(partyMember));
                    }
                }

                if (request.getKit().getGameRules().isSumo()) {
                    player.sendMessage(CC.translate("&cYou cannot use this kit in party."));
                    return true;
                } else {
                    match = new TeamMatch(teamA, teamB, request.getKit(), arena);
                }
            } else {
                if(request.getKit().getGameRules().isSumo()) {
                    match = new SumoMatch(null, new TeamPlayer(player), new TeamPlayer(target), request.getKit(), arena, QueueType.UNRANKED);
                } else {
                    match = new SoloMatch(null, new TeamPlayer(player), new TeamPlayer(target), request.getKit(), arena,
                            QueueType.UNRANKED, 0, 0);
                }
            }
            match.start();
        } else {

            target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                player.sendMessage(CC.RED + "A player with that name could not be found.");
                return true;
            }
            if (player.hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot duel a player while being frozen.");
                return true;
            }
            if (target.hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot duel a player who's frozen.");
                return true;
            }
            if (player.getUniqueId().equals(target.getUniqueId())) {
                player.sendMessage(CC.RED + "You cannot duel yourself.");
                return true;
            }
            final Profile senderProfile=Profile.getByUuid(player.getUniqueId());
            final Profile receiverProfile=Profile.getByUuid(target.getUniqueId());
            if (senderProfile.isBusy()) {
                player.sendMessage(CC.RED + "You cannot duel anyone right now.");
                return true;
            }
            if (receiverProfile.isBusy()) {
                player.sendMessage(CC.translate(CC.RED + target.getDisplayName()) + CC.RED + " is currently busy.");
                return true;
            }
            if (!receiverProfile.getSettings().isReceiveDuelRequests()) {
                player.sendMessage(CC.RED + "That player is not accepting any duel requests at the moment.");
                return true;
            }
            if (!senderProfile.canSendDuelRequest(player)) {
                player.sendMessage(CC.RED + "You have already sent that player a duel request.");
                return true;
            }
            if (senderProfile.getParty() != null && receiverProfile.getParty() == null) {
                player.sendMessage(CC.RED + "That player is not in a party.");
                return true;
            }
            final DuelProcedure procedure = new DuelProcedure(player, target, senderProfile.getParty() != null);
            senderProfile.setDuelProcedure(procedure);
            new DuelSelectKitMenu("normal").openMenu(player);
        }
        return true;
    }
}
