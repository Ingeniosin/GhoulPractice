package net.ghoul.practice.queue;

import lombok.Getter;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.profile.ProfileState;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class Queue {

    @Getter private static final List<Queue> queues = new ArrayList<>();
    @Getter private static final Map<Kit, Queue> queueMap = new HashMap<>();
    private final Map<UUID, Long> playerQueueTime = new HashMap<>();
    private final UUID uuid;
    private final Kit kit;
    private final QueueType type;
    private final LinkedList<QueueProfile> players = new LinkedList<>();

    public Queue(Kit kit, QueueType type) {
        this.kit = kit;
        this.type = type;
        this.uuid = UUID.randomUUID();
        queues.add(this);
        queueMap.put(kit, this);
    }

    public static Queue getByUuid(UUID uuid) {
        for (Queue queue : queues) {
            if (queue.getUuid().equals(uuid)) {
                return queue;
            }
        }
        return null;
    }

    public static Queue getByKit(Kit kit) {
        if (queueMap.containsKey(kit)) {
            return queueMap.get(kit);
        }
        return null;
    }

    public String getQueueName() {
        if (type == QueueType.RANKED) {
            return "Ranked " + kit.getName();
        } else if (type == QueueType.UNRANKED) {
            return "Unranked " + kit.getName();
        } else {
            throw new AssertionError();
        }
    }

    public Queue getRankedType() {
        if (type != QueueType.RANKED) {
            for ( Queue queue : queues ) {
                if (queue.getKit() == kit) {
                    if (queue.getQueueType() != type) {
                        return queue;
                    }
                }
            }
        }
        return null;
    }

    public void addPlayer(Player player, int elo) {
        QueueProfile queueProfile = new QueueProfile(player.getUniqueId());
        queueProfile.setElo(elo);
        this.playerQueueTime.put(player.getUniqueId(), System.currentTimeMillis());
        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setQueue(this);
        profile.setQueueProfile(queueProfile);
        profile.setState(ProfileState.IN_QUEUE);
        PlayerUtil.reset(player, false);
        profile.refreshHotbar();

        player.sendMessage(CC.YELLOW + "Searching match for " + this.getQueueName());
        this.players.add(queueProfile);
    }

    public void removePlayer(QueueProfile queueProfile) {
        players.remove(queueProfile);

        Player player = Bukkit.getPlayer(queueProfile.getPlayerUuid());

        if (player != null && player.isOnline()) {
            player.sendMessage(CC.RED + "You have left the " + this.getQueueName() + " queue.");
        }

        Profile profile = Profile.getByUuid(queueProfile.getPlayerUuid());
        profile.setQueue(null);
        profile.setQueueProfile(null);
        profile.setState(ProfileState.IN_LOBBY);
        PlayerUtil.reset(profile.getPlayer(), false);
        profile.refreshHotbar();
    }

    public String getDuration(Player player) {
        return TimeUtil.millisToTimer(getPlayerQueueTime(player.getUniqueId()));
    }

    public long getPlayerQueueTime(UUID uuid) {
        return System.currentTimeMillis() - this.playerQueueTime.get(uuid);
    }

    public QueueType getQueueType() {
        return type;
    }
}
