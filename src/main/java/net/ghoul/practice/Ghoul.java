package net.ghoul.practice;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.Setter;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.ghoul.essentials.Essentials;
import net.ghoul.practice.hotbar.Hotbar;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.knockback.KnockbackManager;
import net.ghoul.practice.match.Match;
import net.ghoul.practice.party.Party;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.QueueThread;
import net.ghoul.practice.register.RegisterCommands;
import net.ghoul.practice.register.RegisterListeners;
import net.ghoul.practice.util.ConfigFile;
import net.ghoul.practice.util.Glow;
import net.ghoul.practice.util.InventoryUtil;
import net.ghoul.practice.util.chat.CC;
import net.ghoul.practice.util.config.BasicConfigurationFile;
import net.ghoul.practice.util.nametags.NameTagManagement;
import net.ghoul.practice.util.scoreboard.ScoreboardTask;
import net.ghoulpvp.gSpigotLoader;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Horse;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.error.YAMLException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
@Setter
public class Ghoul extends JavaPlugin {

    @Getter
    @Setter
    private static Ghoul Array;
    private BasicConfigurationFile mainConfig, arenasConfig, kitsConfig, eventsConfig, messagesConfig;
    public static Random random;
    private MongoDatabase mongoDatabase;
    private MongoClient mongoClient;
    private KnockbackManager knockbackManager;
    private Essentials essentials;
    private boolean disabling = false;
    private ConfigFile holograms;
    private NameTagManagement nameTagManagement;

    public static Ghoul getInstance() {
        return Array;
    }

    @Override
    public void onEnable() {
        gSpigotLoader.INSTANCE.getConfig().setPracticeMode(true);

        Array = this;
        random = new Random();

        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);

        //Seteup All the Configs
        mainConfig = new BasicConfigurationFile(this, "config");
        arenasConfig = new BasicConfigurationFile(this, "arenas");
        kitsConfig = new BasicConfigurationFile(this, "kits");
        eventsConfig = new BasicConfigurationFile(this, "events");
        messagesConfig = new BasicConfigurationFile(this, "messages");
        holograms = new ConfigFile(this, "holograms.yml");

        RegisterCommands.register();
        RegisterListeners.register();
        registerAll();

        knockbackManager = new KnockbackManager();

        Arrays.asList(Material.WORKBENCH, Material.STICK, Material.WOOD_PLATE, Material.WOOD_BUTTON, Material.SNOW_BLOCK
        ).forEach(InventoryUtil::removeCrafting);

        for (World world : this.getServer().getWorlds()) {
            world.setDifficulty(Difficulty.EASY);
            world.setTime(0L);
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.setWeatherDuration(0);
            world.setMonsterSpawnLimit(0);
            world.setAnimalSpawnLimit(0);
            world.setAutoSave(false);
        }

        this.registerEssentials();
        this.nameTagManagement = new NameTagManagement();

        initWorld();
        registerGlow();
    }

    private void initWorld() {
        Bukkit.getWorld("world").getEntities().forEach(entity -> {
            if (entity instanceof Horse) {
                entity.remove();
            }
        });
    }

    @Override
    public void onDisable() {
        disabling = true;
        Match.cleanup();
        Kit.getKits().forEach(Kit::save);
        Arena.getArenas().forEach(Arena::save);
        Profile.getProfiles().values().forEach(Profile::save);
        Bukkit.getWorld("world").save();

        if (this.mongoDatabase != null && this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    private void registerAll() {
        try {
            preLoadMongo();
        } catch (NullPointerException | MongoInterruptedException | MongoInternalException | MongoCommandException | MongoClientException e) {
            logger(CC.CHAT_BAR);
            logger("&4&lMongo Internal Error");
            logger("&cMongo is not setup correctly!");
            logger(CC.CHAT_BAR);
            this.shutDown();
            return;
        }

        try {
            Arena.preload();
        } catch (YAMLException e) {
            logger(CC.CHAT_BAR);
            logger("&cError Loading Arenas: &cYML Error");
            logger(CC.CHAT_BAR);
            this.shutDown();
            return;
        }

        new Hotbar();
        Match.preload();
        Party.preload();

        Profile.preload();

        essentials = new Essentials();

        try {
            Kit.preload();
        } catch (YAMLException e) {
            logger(CC.CHAT_BAR);
            logger("&cError Loading Kits: &cYML Error");
            logger(CC.CHAT_BAR);
            this.shutDown();
        }

        Match.cleanup();
    }

    private void registerEssentials() {
        new ScoreboardTask().runTaskTimerAsynchronously(this, 0, 2L);
        new QueueThread().start();
        Profile.loadGlobalLeaderboards();
    }

    public static void logger(String message) {
        String msg = CC.translate("&c[Ghoul Practice] &f" + message);
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    private void preLoadMongo() {
        if (getMainConfig().getBoolean("MONGODB.AUTHENTICATION.ENABLED")) {
            MongoCredential credential = MongoCredential.createCredential(
                    getMainConfig().getString("MONGODB.AUTHENTICATION.USERNAME"),
                    getMainConfig().getString("MONGODB.AUTHENTICATION.DATABASE"),
                    getMainConfig().getString("MONGODB.AUTHENTICATION.PASSWORD").toCharArray()
            );

            mongoClient = new MongoClient(new ServerAddress(getMainConfig().getString("MONGODB.ADDRESS"),
                    getMainConfig().getInteger("MONGODB.PORT")), Collections.singletonList(credential));
        } else {
            mongoClient = new MongoClient(getMainConfig().getString("MONGODB.ADDRESS"),
                    getMainConfig().getInteger("MONGODB.PORT"));
        }

        mongoDatabase = mongoClient.getDatabase(getMainConfig().getString("MONGODB.DATABASE"));
    }

    public void shutDown() {
        this.onDisable();
        logger("Shutting down Practice.");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public void registerGlow() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Glow glow = new Glow(70);
            Enchantment.registerEnchantment(glow);
        }
        catch (IllegalArgumentException ignored){
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
