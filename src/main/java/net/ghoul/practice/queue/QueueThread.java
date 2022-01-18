package net.ghoul.practice.queue;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.match.types.SoloMatch;
import net.ghoul.practice.match.types.SumoMatch;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class QueueThread extends Thread {

    Arena arena;
    Kit kit;

    @Override
    public void run() {
        while (true) {
            try {
                for (Queue queue : Queue.getQueues()) {
                    queue.getPlayers().forEach(QueueProfile::tickRange);

                    if (queue.getPlayers().size() < 2) continue;

                    for (QueueProfile firstQueueProfile : queue.getPlayers()) {
                        final Player firstPlayer = Bukkit.getPlayer(firstQueueProfile.getPlayerUuid());

                        if (firstPlayer == null) {
                            continue;
                        }

                        final Profile firstProfile = Profile.getByUuid(firstQueueProfile.getPlayerUuid());

                        for (QueueProfile secondQueueProfile : queue.getPlayers()) {
                            if (firstQueueProfile.equals(secondQueueProfile)) continue;

                            Player secondPlayer = Bukkit.getPlayer(secondQueueProfile.getPlayerUuid());
                            Profile secondProfile = Profile.getByUuid(secondQueueProfile.getPlayerUuid());

                            if (secondPlayer == null) continue;

                            if (queue.getType() == QueueType.RANKED) {
                                if (!firstQueueProfile.isInRange(secondQueueProfile.getElo()) || !secondQueueProfile.isInRange(firstQueueProfile.getElo())) {
                                    continue;
                                }
                            }
                            // Find arena
                            arena = Arena.getRandom(queue.getKit());

                            if (arena == null) {
                                queue.getPlayers().remove(firstQueueProfile);
                                queue.getPlayers().remove(secondQueueProfile);
                                firstPlayer.sendMessage(CC.translate("&cNo arenas available."));
                                secondPlayer.sendMessage(CC.translate("&cNo arenas available."));
                                continue;
                            }

                            if (arena.isActive()) continue;

                            if (queue.getKit().getGameRules().isBuild()) arena.setActive(true);

                            // Remove players from queue
                            queue.getPlayers().remove(firstQueueProfile);
                            queue.getPlayers().remove(secondQueueProfile);

                            TeamPlayer firstMatchPlayer=new TeamPlayer(firstPlayer);
                            TeamPlayer secondMatchPlayer=new TeamPlayer(secondPlayer);

                            if (queue.getType() == QueueType.RANKED) {
                                firstMatchPlayer.setElo(firstProfile.getStatisticsData().get(queue.getKit()).getElo());
                                secondMatchPlayer.setElo(secondProfile.getStatisticsData().get(queue.getKit()).getElo());
                                secondProfile.calculateGlobalElo();
                                firstProfile.calculateGlobalElo();
                            }
                            kit = queue.getKit();

                            // Create match
                            Match match;
                            if (queue.getKit().getGameRules().isSumo()) {
                                match = new SumoMatch(queue, firstMatchPlayer, secondMatchPlayer,
                                        queue.getKit(), arena, queue.getQueueType());
                            } else {
                                match = new SoloMatch(queue, firstMatchPlayer, secondMatchPlayer,
                                        queue.getKit(), arena, queue.getQueueType(), 0, 0);
                            }
                            new BukkitRunnable() {
                                public void run() {
                                    match.start();
                                }
                            }.runTask(Ghoul.getInstance());
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                continue;
            }
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e3) {
                e3.printStackTrace();
            }
        }
    }
}
