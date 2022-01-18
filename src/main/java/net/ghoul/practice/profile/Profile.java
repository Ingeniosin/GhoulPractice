package net.ghoul.practice.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import lombok.Getter;
import lombok.Setter;
import net.ghoul.practice.Ghoul;
import net.ghoul.practice.duel.DuelProcedure;
import net.ghoul.practice.duel.DuelRequest;
import net.ghoul.practice.hotbar.Hotbar;
import net.ghoul.practice.hotbar.HotbarLayout;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.kit.KitInventory;
import net.ghoul.practice.kit.KitLeaderboards;
import net.ghoul.practice.kiteditor.KitEditor;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.match.team.TeamPlayer;
import net.ghoul.practice.party.Party;
import net.ghoul.practice.queue.Queue;
import net.ghoul.practice.queue.QueueProfile;
import net.ghoul.practice.settings.SettingsMeta;
import net.ghoul.practice.statistics.StatisticsData;
import net.ghoul.practice.util.InventoryUtil;
import net.ghoul.practice.util.PlayerUtil;
import net.ghoul.practice.util.TaskUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.external.Cooldown;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class Profile {
    @Getter private static final Map<UUID, Profile> profiles = new HashMap<>();
    @Getter private static final List<KitLeaderboards> globalEloLeaderboards = new ArrayList<>();
    @Getter private static MongoCollection<Document> allProfiles;
    @Getter private static MongoCollection<Document> collection;
    @Getter private final SettingsMeta settings = new SettingsMeta();
    @Getter private final KitEditor kitEditor = new KitEditor();
    @Getter private final Map<Kit, StatisticsData> statisticsData = new LinkedHashMap<>();
    @Getter @Setter String name;
    @Getter @Setter int globalElo = 800;
    @Getter @Setter int rankedWins = 0;
    @Getter @Setter int rankedLoss = 0;
    @Getter @Setter int matchsPlayed = 0;
    @Getter @Setter int winStreak = 0;
    @Getter @Setter int currentWinStreak = 0;
    @Getter @Setter int kills = 0;
    @Getter @Setter int sumoRounds = 0;
    @Getter private final UUID uuid;
    @Getter @Setter private ProfileState state;
    @Getter @Setter private Party party;
    @Getter @Setter private Match match;
    @Getter @Setter private Queue queue;
    @Getter @Setter private QueueProfile queueProfile;
    @Getter private Cooldown enderpearlCooldown = new Cooldown(0);
    @Getter private final Map<UUID, DuelRequest> sentDuelRequests = new HashMap<>();
    @Getter @Setter private DuelProcedure duelProcedure;
    @Getter @Setter private Player lastMessager;
    @Getter @Setter private boolean silent = false;
    @Getter @Setter private boolean visibility = false;
    @Getter @Setter private Player spectating;
    private final List<Long> clicks = new ArrayList<>();
    @Getter @Setter private int clickAmount = 0;

    public void addClick() {
        clicks.add(System.currentTimeMillis());
    }

    public int getClicks() {
        if (clicks.size() > 0) {
            clicks.removeIf(aLong -> aLong < System.currentTimeMillis() - 1000L);

            if (clicks.size() >= 20) {
                clickAmount++;
                if (clickAmount >= 200) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tempban " + name + " 30d AutoClick/Butterfly -s");
                    Bukkit.broadcastMessage(CC.translate("&c" + name + " has been banned for (AutoClick / Butterfly) by CONSOLE"));
                }
            }
            return clicks.size();
        }
        return 0;
    }

    public Profile(UUID uuid) {
        this.uuid = uuid;
        this.state = ProfileState.IN_LOBBY;

        for (Kit kit : Kit.getKits()) {
            this.statisticsData.put(kit, new StatisticsData());
        }
        this.calculateGlobalElo();
    }

    public static void preload() {
        collection = Ghoul.getInstance().getMongoDatabase().getCollection("profiles");

        // Players might have joined before the plugin finished loading
        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = new Profile(player.getUniqueId());

            try {
                TaskUtil.runAsync(profile::load);
            } catch (Exception e) {
                player.kickPlayer(CC.RED + "The server is loading...");
                continue;
            }
            profiles.put(player.getUniqueId(), profile);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Profile profile : Profile.getProfiles().values()) {
                    profile.save();
                }
            }
        }.runTaskTimerAsynchronously(Ghoul.getInstance(), 36000L, 36000L);

        Profile.loadAllProfiles();
        new BukkitRunnable() {
            @Override
            public void run() {
                Profile.loadAllProfiles();
                Kit.getKits().forEach(Kit::updateKitLeaderboards);
            }
        }.runTaskTimerAsynchronously(Ghoul.getInstance(), 36000L, 36000L);

        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalLeaderboards();
            }
        }.runTaskTimerAsynchronously(Ghoul.getInstance(), 36000L, 36000L);
    }

    public static Profile getByUuid(UUID uuid) {
        Profile profile = profiles.get(uuid);

        if (profile == null) {
            profile = new Profile(uuid);
        }

        return profile;
    }

    public static Profile getByUuid(Player player) {
        Profile profile = profiles.get(player.getUniqueId());

        if (profile == null) {
            profile = new Profile(player.getUniqueId());
        }

        return profile;
    }

    public static void loadAllProfiles() {
        allProfiles = Ghoul.getInstance().getMongoDatabase().getCollection("profiles");
    }

    public static void loadGlobalLeaderboards() {
        if (!getGlobalEloLeaderboards().isEmpty()) getGlobalEloLeaderboards().clear();
        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("globalElo")).limit(10).into(new ArrayList<>())) {
            KitLeaderboards kitLeaderboards = new KitLeaderboards();
            kitLeaderboards.setName((String) document.get("name"));
            kitLeaderboards.setElo((Integer) document.get("globalElo"));
            getGlobalEloLeaderboards().add(kitLeaderboards);
        }
    }

    public void load() {
        Document document = collection.find(Filters.eq("uuid", uuid.toString())).first();

        if (document == null) {
            this.save();
            return;
        }

        this.globalElo = document.getInteger("globalElo");
        this.rankedWins = document.getInteger("rankedWins");
        this.rankedLoss = document.getInteger("rankedLoss");
        this.matchsPlayed = document.getInteger("matchsPlayed");
        this.winStreak = document.getInteger("winStreak");

        Document options = (Document) document.get("settings");

        this.settings.setReceiveDuelRequests(options.getBoolean("receiveDuelRequests"));

        Document kitStatistics = (Document) document.get("kitStatistics");

        for (String key : kitStatistics.keySet()) {
            Document kitDocument = (Document) kitStatistics.get(key);
            Kit kit = Kit.getByName(key);

            if (kit != null) {
                StatisticsData statisticsData = new StatisticsData();
                if (kitDocument.getInteger("elo") != null) {
                    statisticsData.setElo(kitDocument.getInteger("elo"));
                } else {
                    kitDocument.put("elo", 0);
                }
                if(kitDocument.getInteger("won") != null) {
                    statisticsData.setWon(kitDocument.getInteger("won"));
                } else {
                    kitDocument.put("won", 0);
                }
                if(kitDocument.getInteger("lost") != null) {
                    statisticsData.setLost(kitDocument.getInteger("lost"));
                } else {
                    kitDocument.put("lost", 0);
                }
                if (kitDocument.getInteger("matches") != null) {
                    statisticsData.setMatches(kitDocument.getInteger("matches"));
                } else {
                    kitDocument.put("matches", 0);
                }
                this.statisticsData.put(kit, statisticsData);
            }
        }

        Document kitsDocument = (Document) document.get("loadouts");

        for (String key : kitsDocument.keySet()) {
            Kit kit = Kit.getByName(key);

            if (kit != null) {
                JsonArray kitsArray = new JsonParser().parse(kitsDocument.getString(key)).getAsJsonArray();
                KitInventory[] loadouts = new KitInventory[4];

                for (JsonElement kitElement : kitsArray) {
                    JsonObject kitObject = kitElement.getAsJsonObject();

                    KitInventory loadout = new KitInventory(kitObject.get("name").getAsString());
                    loadout.setArmor(InventoryUtil.deserializeInventory(kitObject.get("armor").getAsString()));
                    loadout.setContents(InventoryUtil.deserializeInventory(kitObject.get("contents").getAsString()));

                    loadouts[kitObject.get("index").getAsInt()] = loadout;
                }

                statisticsData.get(kit).setLoadouts(loadouts);
            }
        }
    }

    public void save() {
        Document document = new Document();
        document.put("uuid", uuid.toString());
        document.put("name", name);
        document.put("globalElo", globalElo);

        document.put("rankedWins", rankedWins);
        document.put("rankedLoss", rankedLoss);
        document.put("matchsPlayed", matchsPlayed);
        document.put("winStreak", winStreak);

        Document optionsDocument = new Document();
        optionsDocument.put("receiveDuelRequests", settings.isReceiveDuelRequests());
        document.put("settings", optionsDocument);

        Document kitStatisticsDocument = new Document();

        for (Map.Entry<Kit, StatisticsData> entry : statisticsData.entrySet()) {
            Document kitDocument = new Document();
            kitDocument.put("elo", entry.getValue().getElo());
            kitDocument.put("won", entry.getValue().getWon());
            kitDocument.put("lost", entry.getValue().getLost());
            kitDocument.put("matches", entry.getValue().getMatches());
            kitStatisticsDocument.put(entry.getKey().getName(), kitDocument);
        }
        document.put("kitStatistics", kitStatisticsDocument);

        Document kitsDocument = new Document();

        for (Map.Entry<Kit, StatisticsData> entry : statisticsData.entrySet()) {
            JsonArray kitsArray = new JsonArray();

            for (int i = 0; i < 4; i++) {
                KitInventory loadout = entry.getValue().getLoadout(i);

                if (loadout != null) {
                    JsonObject kitObject = new JsonObject();
                    kitObject.addProperty("index", i);
                    kitObject.addProperty("name", loadout.getCustomName());
                    kitObject.addProperty("armor", InventoryUtil.serializeInventory(loadout.getArmor()));
                    kitObject.addProperty("contents", InventoryUtil.serializeInventory(loadout.getContents()));
                    kitsArray.add(kitObject);
                }
            }

            kitsDocument.put(entry.getKey().getName(), kitsArray.toString());
        }

        document.put("loadouts", kitsDocument);

        Document eloKitsDocument = new Document();

        for (Kit kit : Kit.getKits()) {
            if (kit.isEnabled() && kit.getGameRules().isRanked()) {
                Document eloDocument = new Document();
                eloDocument.put(kit.getDisplayName(), this.getStatisticsData().get(kit).getElo());
                eloDocument.put("kitIcon", kit.getDisplayIcon().toString());
                eloKitsDocument.put(kit.getDisplayName(), eloDocument);
            }
        }

        document.put("eloKits", eloKitsDocument);

        collection.replaceOne(Filters.eq("uuid", uuid.toString()), document, new ReplaceOptions().upsert(true));
    }

    public void calculateGlobalElo() {
        int globalElo = 0;
        int kitCounter = 0;
        for (Kit kit : this.statisticsData.keySet()) {
            if (kit.getGameRules().isRanked()) {
                globalElo += this.statisticsData.get(kit).getElo();
                kitCounter++;
            }
        }
        this.globalElo = Math.round(globalElo / kitCounter);
    }

    public Integer getTotalWins() {
        return this.statisticsData.values().stream().mapToInt(StatisticsData::getWon).sum();
    }

    public Integer getTotalLost() {
        return this.statisticsData.values().stream().mapToInt(StatisticsData::getLost).sum();
    }

    public Integer getTotalMatches() {
        return this.statisticsData.values().stream().mapToInt(StatisticsData::getMatches).sum();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean canSendDuelRequest(Player player) {
        if (!sentDuelRequests.containsKey(player.getUniqueId())) {
            return true;
        }

        DuelRequest request = sentDuelRequests.get(player.getUniqueId());

        if (request.isExpired()) {
            sentDuelRequests.remove(player.getUniqueId());
            return true;
        } else {
            return false;
        }
    }

    public boolean isPendingDuelRequest(Player player) {
        if (!sentDuelRequests.containsKey(player.getUniqueId())) {
            return false;
        }

        DuelRequest request = sentDuelRequests.get(player.getUniqueId());

        if (request.isExpired()) {
            sentDuelRequests.remove(player.getUniqueId());
            return false;
        } else {
            return true;
        }
    }

    public boolean isInLobby() {
        return state == ProfileState.IN_LOBBY;
    }

    public boolean isInQueue() {
        return state == ProfileState.IN_QUEUE && queue != null && queueProfile != null;
    }

    public boolean isInMatch() {
        return match != null;
    }

    public boolean isInFight() {
        return state == ProfileState.IN_FIGHT && match != null;
    }

    public boolean isSpectating() {
        return state == ProfileState.SPECTATE_MATCH && (match != null);
    }

    public boolean isInSomeSortOfFight() {
        return (state == ProfileState.IN_FIGHT && match != null) || (state == ProfileState.IN_EVENT);
    }

    public boolean isBusy() {
        return isInQueue() || isInFight() || isSpectating();
    }

    public void refreshHotbar() {
        Player player = getPlayer();

        if (player != null) {
            PlayerUtil.reset(player, false);

            if (isInLobby()) {
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.LOBBY, this));
            } else if (isInQueue()) {
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.QUEUE, this));
            } else if (isSpectating()) {
                PlayerUtil.spectator(player);
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.MATCH_SPECTATE, this));
            } else if (isInFight()) {
                if (!match.getTeamPlayer(player).isAlive()) {
                    player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.MATCH_SPECTATE, this));
                }
            }

            player.updateInventory();
        }
    }

    public void handleVisibility(Player player, Player otherPlayer) {
        if (player == null || otherPlayer == null) return;
        boolean hide = true;
        if (isInFight()) {
            TeamPlayer teamPlayer = match.getTeamPlayer(otherPlayer);

            if (teamPlayer != null && teamPlayer.isAlive()) {
                hide = false;
            }
        } else if (isSpectating()) {
            TeamPlayer teamPlayer = match.getTeamPlayer(otherPlayer);
            if (teamPlayer != null && teamPlayer.isAlive()) {
                hide = false;
            }
        }

        if (hide) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.hidePlayer(otherPlayer);
                }
            }.runTask(Ghoul.getInstance());
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.showPlayer(otherPlayer);
                }
            }.runTask(Ghoul.getInstance());
        }
    }

    public void handleVisibility() {
        Player player = getPlayer();
        if (player != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                        handleVisibility(player, otherPlayer);
                    }
                }
            }.runTaskAsynchronously(Ghoul.getInstance());
        }
    }

    public void setEnderpearlCooldown(Cooldown cooldown) {
        this.enderpearlCooldown = cooldown;
    }
}
