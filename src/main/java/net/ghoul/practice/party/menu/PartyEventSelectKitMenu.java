package net.ghoul.practice.party.menu;

import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.enums.PartyEventType;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.team.Team;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.match.types.FFAMatch;
import net.ghoul.practice.match.types.TeamMatch;
import net.ghoul.practice.party.Party;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ItemBuilder;
import net.ghoul.practice.util.external.menu.Button;
import net.ghoul.practice.util.external.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.beans.ConstructorProperties;
import java.util.*;

public class PartyEventSelectKitMenu extends Menu {

    private final PartyEventType partyEventType;

    @Override
    public String getTitle(final Player player) {
        return "&cSelect a kit";
    }

    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        List<Integer> slots = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));

        for (int a = 0; a < 18; a++) {
            buttons.put(a, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 7).name(" ").build();
                }
            });
        }

        for (final Kit kit : Kit.getKits()) {
            if (kit.isEnabled() && !kit.getGameRules().isSumo()) {
                if (partyEventType == PartyEventType.FFA) {
                    if (kit.getGameRules().isPartyffa()) {
                        buttons.put(slots.remove(0), new SelectKitButton(this.partyEventType, kit));
                    }
                } else if (partyEventType == PartyEventType.SPLIT) {
                    if (kit.getGameRules().isPartysplit()) {
                        buttons.put(slots.remove(0), new SelectKitButton(this.partyEventType, kit));
                    }
                }
            }
        }
        return buttons;
    }

    @ConstructorProperties({"partyEventType"})
    public PartyEventSelectKitMenu(final PartyEventType partyEventType) {
        this.partyEventType=partyEventType;
    }

    private static class SelectKitButton extends Button {

        private final PartyEventType partyEventType;
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(this.kit.getDisplayIcon()).name("&c&l" + this.kit.getDisplayName()).build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
            player.closeInventory();
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }
            if (profile.getParty().getPlayers().size() <= 1) {
                player.sendMessage(CC.RED + "You do not have enough players in your party.");
                return;
            }
            final Party party = profile.getParty();
            final Arena arena;
            if (party.getPlayers().size() > 15) {
                arena = Arena.getRandomCustom(this.kit);
            } else {
                arena = Arena.getRandom(this.kit);
            }
            if (arena == null) {
                player.sendMessage(CC.RED + "There are no available arenas.");
                return;
            }
            arena.setActive(true);
            Match match;
            if (this.partyEventType == PartyEventType.FFA) {
                if (this.kit.getGameRules().isSumo()) {
                    player.sendMessage(CC.RED + "You cannot start an ffa with the kit sumo.");
                    player.closeInventory();
                    return;
                }
                if (!this.kit.getGameRules().isPartyffa()) {
                    player.sendMessage(CC.RED + "This kit is not for partys ffa.");
                    player.closeInventory();
                    return;
                }
                final Team team = new Team(new TeamPlayer(party.getLeader().getPlayer()));
                final List<Player> players = new ArrayList<>(party.getPlayers());
                match = new FFAMatch(team, this.kit, arena);
                for (final Player otherPlayer : players) {
                    if (team.getLeader().getUuid().equals(otherPlayer.getUniqueId())) continue;
                    team.getTeamPlayers().add(new TeamPlayer(otherPlayer));
                }
            } else {
                final Team teamA = new Team(new TeamPlayer(party.getPlayers().get(0)));
                final Team teamB = new Team(new TeamPlayer(party.getPlayers().get(1)));

                final List<Player> players2 = new ArrayList<>(party.getPlayers());
                Collections.shuffle(players2);

                if (this.kit.getGameRules().isSumo()) {
                    player.sendMessage(CC.translate("&cYou cannot use this kit in party."));
                    return;
                } else {
                    match = new TeamMatch(teamA, teamB, this.kit, arena);
                }
                for (final Player otherPlayer2 : players2) {
                    if (!teamA.getLeader().getUuid().equals(otherPlayer2.getUniqueId())) {
                        if (teamB.getLeader().getUuid().equals(otherPlayer2.getUniqueId())) {
                            continue;
                        }
                        if (teamA.getTeamPlayers().size() > teamB.getTeamPlayers().size()) {
                            teamB.getTeamPlayers().add(new TeamPlayer(otherPlayer2));
                        }
                        else {
                            teamA.getTeamPlayers().add(new TeamPlayer(otherPlayer2));
                        }
                    }
                }
            }
            match.start();
        }

        @ConstructorProperties({ "partyEventType", "kit" })
        public SelectKitButton(final PartyEventType partyEventType, final Kit kit) {
            this.partyEventType=partyEventType;
            this.kit = kit;
        }
    }
}