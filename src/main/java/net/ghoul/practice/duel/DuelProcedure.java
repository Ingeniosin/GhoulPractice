package net.ghoul.practice.duel;

import lombok.Getter;
import lombok.Setter;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.ChatComponentBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

public class DuelProcedure {

    @Getter
    private final boolean party;
    @Getter
    private final Player sender;
    @Getter
    private final Player target;
    @Getter
    @Setter
    private Kit kit;
    @Getter
    @Setter
    private Arena arena;

    public DuelProcedure(Player sender, Player target, boolean party) {
        this.sender = sender;
        this.target = target;
        this.party = party;
    }

    public void send() {
        if (!sender.isOnline() || !target.isOnline()) return;

        DuelRequest request = new DuelRequest(sender.getUniqueId(), party);
        request.setKit(kit);
        request.setArena(arena);

        Profile senderProfile = Profile.getByUuid(sender.getUniqueId());
        senderProfile.setDuelProcedure(null);
        senderProfile.getSentDuelRequests().put(target.getUniqueId(), request);

        this.sender.sendMessage(CC.translate("&fYou sent a duel request to &c" + this.target.getName() + "&f with kit &c" + (this.kit.getDisplayName())));

        target.spigot().sendMessage(new ChatComponentBuilder(CC.translate("&fYou have received a duel request from &c" + this.sender.getName() +
                " &fwith kit &c" + this.kit.getDisplayName() + " &7(Click to accept)"))
                .attachToEachPart(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + sender.getName()))
                .attachToEachPart(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.GRAY + "Click to accept.").create()))
                .create());
    }
}
