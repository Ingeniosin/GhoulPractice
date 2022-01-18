package net.ghoul.practice.party.command;

import net.ghoul.practice.enums.PartyMessageType;
import net.ghoul.practice.enums.PartyPrivacyType;
import net.ghoul.practice.party.Party;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        final Profile profile = Profile.getByUuid(player.getUniqueId());

        if (args.length == 0) {
            player.sendMessage(CC.MENU_BAR);
            player.sendMessage(CC.translate("&c/party create"));
            player.sendMessage(CC.translate("&c/party info"));
            player.sendMessage(CC.translate("&c/party invite <player>"));
            player.sendMessage(CC.translate("&c/party join <player>"));
            player.sendMessage(CC.translate("&c/party kick <player>"));
            player.sendMessage(CC.translate("&c/party chat"));
            player.sendMessage(CC.translate("&c/party close"));
            player.sendMessage(CC.translate("&c/party disband"));
            player.sendMessage(CC.translate("&c/party leader <player>"));
            player.sendMessage(CC.translate("&c/party leave"));
            player.sendMessage(CC.MENU_BAR);
            return true;
        }

        Player target;

        switch (args[0]) {
            case "create":
                if (profile.getParty() != null) {
                    player.sendMessage(CC.RED + "You already have a party.");
                    return true;
                }
                if (!profile.isInLobby()) {
                    player.sendMessage(CC.RED + "You must be in the lobby to create a party.");
                    return true;
                }
                profile.setParty(new Party(player));
                profile.refreshHotbar();
                player.sendMessage(PartyMessageType.CREATED.format());
                break;
            case "info":
                if (profile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party.");
                    return true;
                }
                profile.getParty().sendInformation(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/party invite <player>"));
                    return true;
                }

                target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    player.sendMessage(CC.RED + "A player with that name could not be found.");
                    return true;
                }
                if (profile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party.");
                    return true;
                }
                if (profile.getParty().getInvite(target.getUniqueId()) != null) {
                    player.sendMessage(CC.RED + "That player has already been invited to your party.");
                    return true;
                }
                if (profile.getParty().getPlayers().contains(target)) {
                    player.sendMessage(CC.RED + "That player is already in your party.");
                    return true;
                }
                if (profile.getParty().getPrivacy() == PartyPrivacyType.OPEN) {
                    player.sendMessage(CC.RED + "The party state is Open. You do not need to invite players.");
                    return true;
                }
                final Profile targetData = Profile.getByUuid(target.getUniqueId());
                if (targetData.isBusy()) {
                    player.sendMessage(target.getDisplayName() + CC.RED + " is currently busy.");
                    return true;
                }
                profile.getParty().invite(target);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/party join <player>"));
                    return true;
                }

                target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    player.sendMessage(CC.RED + "A player with that name could not be found.");
                    return true;
                }
                if (player.hasMetadata("frozen")) {
                    player.sendMessage(CC.RED + "You cannot join a party while frozen.");
                    return true;
                }
                if (profile.isBusy()) {
                    player.sendMessage(CC.RED + "You can not do that right now");
                    return true;
                }
                if (profile.getParty() != null) {
                    player.sendMessage(CC.RED + "You already have a party.");
                    return true;
                }
                final Profile targetProfile = Profile.getByUuid(target.getUniqueId());
                final Party party = targetProfile.getParty();
                if (party == null) {
                    player.sendMessage(CC.RED + "A party with that name could not be found.");
                    return true;
                }
                if (party.getPrivacy() == PartyPrivacyType.CLOSED && party.getInvite(player.getUniqueId()) == null) {
                    player.sendMessage(CC.RED + "You have not been invited to that party.");
                    return true;
                }
                if (party.getPlayers().size() >= party.getLimit()) {
                    player.sendMessage(CC.RED + "That party is full and cannot hold anymore players.");
                    return true;
                }
                party.join(player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/party kick <player>"));
                    return true;
                }

                target = Bukkit.getPlayer(args[1]);

                if (profile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party.");
                    return true;
                }
                if (!profile.getParty().getLeader().getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(CC.RED + "You are not the leader of your party.");
                    return true;
                }
                if (!profile.getParty().getPlayers().contains(target)) {
                    player.sendMessage(CC.RED + "That player is not a member of your party.");
                    return true;
                }
                if (player.equals(target)) {
                    player.sendMessage(CC.RED + "You cannot kick yourself from your party.");
                    return true;
                }
                player.sendMessage(CC.GREEN + "Successfully kicked that player");
                target.sendMessage(CC.RED + "You have been kicked from the party");
                profile.getParty().leave(target, true);
                break;
            case "chat":
                if (profile.getParty() == null) {
                    player.sendMessage(CC.translate("&8[&c&lParty&8] &7You don't have a party."));
                    return true;
                }
                profile.getSettings().setPartyChat(!profile.getSettings().isPartyChat());
                player.sendMessage(CC.translate((profile.getSettings().isPartyChat() ? "&cYou are now speaking in party chat!" : "&cYou are now speaking in Global Chat")));
                break;
            case "close":
                if (!player.hasPermission("practice.donator")) {
                    player.sendMessage(CC.translate("&7You do not have permission to use Party Settings."));
                    player.sendMessage(CC.translate("&7Please consider buying a Rank at &cstore.ghouluhc.club &7!"));
                    return true;
                }
                if (profile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party.");
                    return true;
                }
                if (!profile.getParty().getLeader().getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(CC.RED + "You are not the leader of your party.");
                    return true;
                }
                profile.getParty().setPrivacy(PartyPrivacyType.CLOSED);
                break;
            case "open":
                if (!player.hasPermission("practice.donator")) {
                    player.sendMessage(CC.translate("&7You do not have permission to use Party Settings."));
                    player.sendMessage(CC.translate("&7Please consider buying a Rank at &cstore.ghouluhc.club &7!"));
                    return true;
                }
                if (profile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party.");
                    return true;
                }
                if (!profile.getParty().getLeader().getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(CC.RED + "You are not the leader of your party.");
                    return true;
                }
                profile.getParty().setPrivacy(PartyPrivacyType.OPEN);
                break;
            case "disband":
                if (profile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party.");
                    return true;
                }
                if (!profile.getParty().getLeader().getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(CC.RED + "You are not the leader of your party.");
                    return true;
                }
                if (profile.getMatch() != null) {
                    player.sendMessage(CC.RED + "You can not do that when you're in a match");
                    return true;
                }
                profile.getParty().disband();
                break;
            case "leave":
                if (profile.getParty() == null) {
                    player.sendMessage(CC.RED + "You do not have a party.");
                    return true;
                }
                if (profile.getParty().getLeader().getUniqueId().equals(player.getUniqueId())) {
                    profile.getParty().disband();
                } else {
                    profile.getParty().leave(player, false);
                }
                break;
        }

        return true;
    }
}
