package net.ghoul.practice.register;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.command.ArenaCommand;
import net.ghoul.practice.duel.command.DuelCommand;
import net.ghoul.practice.ghoul.commands.PracticeCommand;
import net.ghoul.practice.ghoul.commands.ResetMongoCommand;
import net.ghoul.practice.ghoul.commands.SpectateCommand;
import net.ghoul.practice.ghoul.commands.ToggleDuelsCommand;
import net.ghoul.practice.kit.command.KitCommand;
import net.ghoul.practice.match.command.ViewInventoryCommand;
import net.ghoul.practice.party.command.PartyCommand;
import net.ghoul.practice.statistics.command.LeaderboardsCommand;
import net.ghoul.practice.statistics.command.StatsCommand;

public class RegisterCommands {
    
    public static void register() {
        Ghoul.getInstance().getCommand("kit").setExecutor(new KitCommand());
        Ghoul.getInstance().getCommand("arena").setExecutor(new ArenaCommand());
        Ghoul.getInstance().getCommand("duel").setExecutor(new DuelCommand());
        Ghoul.getInstance().getCommand("toggleduel").setExecutor(new ToggleDuelsCommand());
        Ghoul.getInstance().getCommand("practice").setExecutor(new PracticeCommand());
        Ghoul.getInstance().getCommand("spectate").setExecutor(new SpectateCommand());
        Ghoul.getInstance().getCommand("viewinv").setExecutor(new ViewInventoryCommand());
        Ghoul.getInstance().getCommand("leaders").setExecutor(new LeaderboardsCommand());
        Ghoul.getInstance().getCommand("party").setExecutor(new PartyCommand());
        Ghoul.getInstance().getCommand("statsprac").setExecutor(new StatsCommand());
        Ghoul.getInstance().getCommand("spectate").setExecutor(new SpectateCommand());
        Ghoul.getInstance().getCommand("resetmongo").setExecutor(new ResetMongoCommand());
    }
}
