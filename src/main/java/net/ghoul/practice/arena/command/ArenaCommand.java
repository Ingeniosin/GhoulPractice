package net.ghoul.practice.arena.command;

import net.ghoul.practice.Ghoul;
import net.ghoul.practice.arena.Arena;
import net.ghoul.practice.arena.generator.ArenaGenerator;
import net.ghoul.practice.arena.impl.SharedArena;
import net.ghoul.practice.arena.impl.StandaloneArena;
import net.ghoul.practice.arena.impl.TheBridgeArena;
import net.ghoul.practice.enums.ArenaType;
import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.util.Schematic;
import net.ghoul.practice.util.chat.CC;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArenaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (!player.hasPermission("practice.dev") && !player.isOp() && !player.getName().equals("tomas_s")) {
            player.sendMessage(CC.translate("&cNo permission."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(CC.CHAT_BAR);
            player.sendMessage(CC.translate("&c/arena create &7<name> <Shared|Standalone|TheBridge>"));
            player.sendMessage(CC.translate("&c/arena remove &7<name>"));
            player.sendMessage(CC.translate("&c/arena kitlist &7<Arena>"));
            player.sendMessage(CC.translate("&c/arena setdisplayname &7<name> <displayname>"));
            player.sendMessage(CC.translate("&c/arena seticon &7<Arena>"));
            player.sendMessage(CC.translate("&c/arena customarena &7<Arena>"));
            player.sendMessage(CC.translate("&c/arena setspawn &7<Arena> <1/2>"));
            player.sendMessage(CC.translate("&c/arena setcorner &7<Arena> <1/2>"));
            player.sendMessage(CC.translate("&c/arena addkit &7<Arena> <Kit>"));
            player.sendMessage(CC.translate("&c/arena addnormalkits &7<Arena> <Kit>"));
            player.sendMessage(CC.translate("&c/arena addbuildkits &7<Arena> <Kit>"));
            player.sendMessage(CC.translate("&c/arena removekit &7<Arena> <Kit>"));
            player.sendMessage(CC.translate("&c/arena disablepearls &7<Arena>"));
            player.sendMessage(CC.translate("&c/arena generator &7<Arena>"));
            player.sendMessage(CC.translate("&c/arena save"));
            player.sendMessage(CC.CHAT_BAR);
            return true;
        }

        Arena arena;
        Kit kit;
        String name;

        switch (args[0]) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/arena create &7<name> <Shared|Standalone|TheBridge>"));
                    return true;
                }

                name = args[1];
                String type = args[2];

                if (!type.equalsIgnoreCase("standalone") && !type.equalsIgnoreCase("shared") && !type.equalsIgnoreCase("bridge")) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Invalid Type."));
                    return true;
                }

                if (Arena.getArenas().contains(Arena.getByName(name))) {
                    if (type.equalsIgnoreCase("shared")) {
                        player.sendMessage(CC.translate("&8[&cGhoul&8] &7You can't convert a Shared arena to a duped one."));
                        return true;
                    }
                    arena = new Arena(name);

                    Location loc1 =
                            new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
                    arena.setSpawn1(loc1);
                    arena.setSpawn2(loc1);
                    arena.setCorner1(loc1);
                    arena.setCorner2(loc1);
                    StandaloneArena sarena = (StandaloneArena) Arena.getByName(name);
                    assert sarena != null;
                    sarena.getDuplicates().add(arena);
                    player.sendMessage(CC.translate("&8[&cGhoul&8] &7Saved a duplicate arena from &c" + name + "&8(&7#&c" + sarena.getDuplicates().size() + "&8)"));
                } else {
                    if (type.equalsIgnoreCase("shared")){
                        arena = new SharedArena(name);
                    } else if (type.equalsIgnoreCase("bridge")) {
                        arena = new TheBridgeArena(name);
                        player.sendMessage(CC.translate("&8[&bTIP&8] &7Please note that 'Red' is set to Spawn 1 and 'Blue' is set to Spawn 2."));
                    } else if (type.equalsIgnoreCase("standalone")){
                        arena = new StandaloneArena(name);
                    } else {
                        arena = new StandaloneArena(name);
                    }

                    Location loc1 = new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY(),
                            player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

                    arena.setSpawn1(loc1);
                    arena.setSpawn2(loc1);
                    player.sendMessage(CC.translate("&8[&cGhoul&8] &7Successfully created an Arena called &c" + name + "&7 of type &c" + type));
                }
                Arena.getArenas().add(arena);
                Arena.getArenas().forEach(Arena::save);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/arena remove &7<name>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                name = args[1];

                if (arena != null) {
                    arena.delete();
                    Arena.getArenas().remove(arena);
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully removed the arena &c" + name));
                }
                break;
            case "kitlist":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/arena kitlist &7<name>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);

                if (arena == null) return true;

                player.sendMessage(CC.MENU_BAR);
                for (String string : arena.getKits()) {
                    Kit kits = Kit.getByName(string);
                    if (kits == null) {
                        player.sendMessage(CC.GRAY + "There are no kits for this arena.");
                        return true;
                    }
                    player.sendMessage(CC.GRAY + " • " + kits.getName());
                }
                player.sendMessage(CC.MENU_BAR);
                break;
            case "seticon":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/arena seticon &7<name>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                ItemStack item = player.getItemInHand();
                if (item == null) return true;

                arena.setDisplayIcon(item);
                arena.save();
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully set the &carena icon &7to the &citem&7 in your hand."));
                break;
            case "customarena":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/arena customarena &7<name>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                if (arena.isCustomMap()) {
                    arena.setCustomMap(false);
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully &cenabled &7custom arenas in the arena &c" + arena.getName()));
                } else {
                    arena.setCustomMap(true);
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully &cdisabled &7custom arenas in the arena &c" + arena.getName()));
                }
                arena.save();
                break;
            case "setspawn": {
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/arena setspawn &7<name> <1|2>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                if (!NumberUtils.isNumber(args[2])) return true;

                int spawn = Integer.parseInt(args[2]);

                Location loc = new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY(),
                        player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

                if (spawn == 1) {
                    arena.setSpawn1(loc);
                } else if (spawn == 2) {
                    arena.setSpawn2(loc);
                }
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully updated the spawn position of &c" + arena.getName() + "&8 (&7Position: " + spawn + "&8)"));
                arena.save();
                break;
            }
            case "setcorner": {
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/arena setcorner &7<name> <1|2>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                if (!NumberUtils.isNumber(args[2])) return true;

                int spawn = Integer.parseInt(args[2]);

                Location loc = new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY(),
                        player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

                if (spawn == 1) {
                    arena.setCorner1(loc);
                } else if (spawn == 2) {
                    arena.setCorner2(loc);
                }
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully updated the corner position of &c" + arena.getName() + "&8 (&7Position: " + spawn + "&8)"));
                arena.save();
                break;
            }
            case "setdisplayname":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/arena setdisplayname &7<name> <displayname>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                arena.setDisplayName(args[2]);
                arena.save();
                player.sendMessage(CC.translate("&8[&cGhoul&8] &7Successfully updated the arena &c" + arena.getName() + "'s &7display name."));
                break;
            case "addkit":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/arena addkit &7<name> <kit>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                kit = Kit.getByName(args[2]);
                if (kit == null) return true;

                if (arena.getType() == ArenaType.SHARED && kit.getGameRules().isBuild()) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7The arena is set to type shared and you can't add build kits to it!"));
                    return true;
                }

                if (!arena.getKits().contains(kit.getName())) {
                    arena.getKits().add(kit.getName());
                }

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully added the kit &c" + kit.getName() + "&7 to &c" + arena.getName()));
                arena.save();
                break;
            case "addnormalkits":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/arena addnormalkits &7<name> <kit>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                kit = Kit.getByName(args[2]);
                if (kit == null) return true;

                for (Kit kits : Kit.getKits()) {
                    if (kits == null) {
                        player.sendMessage(CC.translate("&7[&cGhoul&7] &7There are no kits."));
                        return true;
                    }
                    if (kits.getGameRules().isBuild() || kit.getGameRules().isSpleef() || kits.getGameRules().isSumo() || kits.getGameRules().isNoitems() || kits.getGameRules().isWaterkill()) {
                        return true;
                    }

                    if (!arena.getKits().contains(kits.getName())) {
                        arena.getKits().add(kits.getName());
                    }
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully added the kit &c" + kits.getName() + "&7 to &c" + arena.getName() + "&7."));
                }
                arena.save();
                break;
            case "addbuildkits":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/arena addbuildkits &7<name> <kit>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                kit = Kit.getByName(args[2]);
                if (kit == null) return true;

                for (Kit kits : Kit.getKits()) {
                    if (kits == null) {
                        player.sendMessage(CC.translate("&7[&cGhoul&7] &7There are no kits setup."));
                        return true;
                    }
                    if (kits.getGameRules().isBuild()) {
                        if (!arena.getKits().contains(kits.getName())) {
                            arena.getKits().add(kits.getName());
                        }
                    }
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully added the kit &c" + kits.getName() + "&7 to &c" + arena.getName()));
                }
                arena.save();
                break;
            case "removekit":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/arena removekit &7<name> <kit>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                kit = Kit.getByName(args[2]);
                if (kit == null) return true;

                if (arena.getKits().contains(kit.getName())) {
                    arena.getKits().remove(kit.getName());

                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully removed the kit &c" + kit.getName() + " &7from &c" + arena.getName()));
                    arena.save();
                }
                break;
            case "disablepearls":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/arena disablepearls &7<name>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                if (arena.isDisablePearls()) {
                    arena.setDisablePearls(false);
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully &cenabled &7pearls in the arena &c" + arena.getName()));
                } else {
                    arena.setDisablePearls(true);
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully &cdisabled &7pearls in the arena &c" + arena.getName()));
                }
                break;
            case "tp":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/arena tp &7<name>"));
                    return true;
                }

                arena = Arena.getByName(args[1]);
                if (arena == null) return true;

                player.teleport(arena.getSpawn1());
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully &cteleported &7to the arena &c" + arena.getName() + "&7!"));
                break;
            case "save":
                for (Arena arenas : Arena.getArenas()) {
                    arenas.save();
                }
                sender.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully saved &c" + Arena.getArenas().size() + " &7arenas!"));
                break;
            case "list":
                player.sendMessage(CC.CHAT_BAR);
                player.sendMessage(CC.translate("&cAvailable arenas:"));
                player.sendMessage("");
                if (Arena.getArenas().isEmpty()) {
                    player.sendMessage(CC.GRAY + "There are no arenas setup.");
                    player.sendMessage(CC.CHAT_BAR);
                    return true;
                }
                List<Arena> plains = new ArrayList<>();
                List<Arena> desert = new ArrayList<>();
                List<Arena> deathmatch = new ArrayList<>();
                List<Arena> cavefight = new ArrayList<>();
                List<Arena> savanna = new ArrayList<>();
                List<Arena> others = new ArrayList<>();

                for (final Arena arenas : Arena.getArenas()) {
                    if (arenas.getName().contains("plains")) {
                        plains.add(arenas);
                    } else if (arenas.getName().contains("desert")) {
                        desert.add(arenas);
                    } else if (arenas.getName().contains("deathmatch")) {
                        deathmatch.add(arenas);
                    } else if (arenas.getName().contains("cavefight")) {
                        cavefight.add(arenas);
                    } else if (arenas.getName().contains("savanna")) {
                        savanna.add(arenas);
                    } else {
                        others.add(arenas);
                    }
                }

                player.sendMessage(CC.translate("&cPlains Arena: &f" + plains.size()));
                player.sendMessage(CC.translate("&cDesert Arena: &f" + desert.size()));
                player.sendMessage(CC.translate("&cDeathMatch Arena: &f" + deathmatch.size()));
                player.sendMessage(CC.translate("&cCaveFight Arena: &f" + cavefight.size()));
                player.sendMessage(CC.translate("&cSavanna Arena: &f" + savanna.size()));
                player.sendMessage(CC.translate("&cOthers Arena: &f" + others.size()));
                player.sendMessage(CC.CHAT_BAR);
                break;
            case "generator":
                if (args.length != 7) {
                    player.sendMessage(CC.RED + "Usage: /arena generate <schematic> <times> <startingX> <startingZ> <incrementX> <incrementZ>");
                    return true;
                }

                String schematicName = args[1];
                int times = Integer.parseInt(args[2]);
                int startingX = Integer.parseInt(args[3]);
                int startingZ = Integer.parseInt(args[4]);
                int incrementX = Integer.parseInt(args[5]);
                int incrementZ = Integer.parseInt(args[6]);

                File schematicsFolder = new File(Ghoul.getInstance().getDataFolder().getPath() + File.separator + "schematics");
                if (!schematicsFolder.exists()) {
                    sender.sendMessage(ChatColor.RED + "The schematics folder does not exist.");
                    return true;
                }
                File schematicFile = new File(schematicsFolder, schematicName + ".schematic");
                if (!schematicFile.exists()) {
                    sender.sendMessage(ChatColor.RED + "That schematic doesn't exist.");
                    return true;
                }

                ArenaGenerator generator = new ArenaGenerator(Ghoul.getInstance(), player.getWorld(), new Schematic(schematicFile), player);
                for (int i = 1; i <= times; i++) {
                    generator.generate(i, startingX, startingZ);
                    startingX += incrementX;
                    startingZ += incrementZ;
                }
                break;
        }

        return true;
    }

}
