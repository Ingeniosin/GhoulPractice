package net.ghoul.practice.kit.command;

import net.ghoul.practice.kit.Kit;
import net.ghoul.practice.profile.Profile;
import net.ghoul.practice.queue.Queue;
import net.ghoul.practice.queue.QueueType;
import net.ghoul.practice.statistics.StatisticsData;
import net.ghoul.practice.util.chat.CC;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class KitCommand implements CommandExecutor {

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
            player.sendMessage(CC.translate("&c/kit create &7<kit>"));
            player.sendMessage(CC.translate("&c/kit seticon &7<kit>"));
            player.sendMessage(CC.translate("&c/kit displayname &7<kit>"));
            player.sendMessage(CC.translate("&c/kit setkb &7<kit> <kb profile>"));
            player.sendMessage(CC.translate("&c/kit remove &7<kit>"));
            player.sendMessage(CC.translate("&c/kit hitdelay &7<kit> (1-20)"));
            player.sendMessage(CC.translate("&c/kit ranked &7<kit>"));
            player.sendMessage(CC.translate("&c/kit build &7<kit>"));
            player.sendMessage(CC.translate("&c/kit customDamages &7<kit>"));
            player.sendMessage(CC.translate("&c/kit bridge &7<kit>"));
            player.sendMessage(CC.translate("&c/kit sumo &7<kit>"));
            player.sendMessage(CC.translate("&c/kit combo &7<kit>"));
            player.sendMessage(CC.translate("&c/kit partyffa &7<kit>"));
            player.sendMessage(CC.translate("&c/kit partysplit &7<kit>"));
            player.sendMessage(CC.translate("&c/kit antifoodloss &7<kit>"));
            player.sendMessage(CC.translate("&c/kit bowhp &7<kit>"));
            player.sendMessage(CC.translate("&c/kit ffacenter &7<kit>"));
            player.sendMessage(CC.translate("&c/kit healthregen &7<kit>"));
            player.sendMessage(CC.translate("&c/kit infinitespeed &7<kit>"));
            player.sendMessage(CC.translate("&c/kit infinitestrength &7<kit>"));
            player.sendMessage(CC.translate("&c/kit noitems &7<kit>"));
            player.sendMessage(CC.translate("&c/kit showhealth &7<kit>"));
            player.sendMessage(CC.translate("&c/kit stickspawn &7<kit>"));
            player.sendMessage(CC.translate("&c/kit bedwars &7<kit>"));
            player.sendMessage(CC.translate("&c/kit spleef &7<kit>"));
            player.sendMessage(CC.translate("&c/kit voidspawn &7<kit>"));
            player.sendMessage(CC.translate("&c/kit waterkill &7<kit>"));
            player.sendMessage(CC.translate("&c/kit timed &7<kit>"));
            player.sendMessage(CC.translate("&c/kit inventorySlot &7<kit> <slot>"));
            player.sendMessage(CC.translate("&c/kit list"));
            player.sendMessage(CC.translate("&c/kit glow &7<kit>"));
            player.sendMessage(CC.translate("&c/kit getinv &7<kit>"));
            player.sendMessage(CC.translate("&c/kit setinv &7<kit>"));
            player.sendMessage(CC.translate("&c/kit save"));
            player.sendMessage(CC.CHAT_BAR);
            return true;
        }

        Kit kit;

        switch (args[0]) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit create &7<kit>"));
                    return true;
                }

                if (Kit.getByName(args[1]) != null) {
                    player.sendMessage(CC.translate("&7[&cPractice&7] &7A kit with that name already exists."));
                    return true;
                }

                final Kit kitCreate = new Kit(args[1]);
                kitCreate.save();
                Kit.getKits().add(kitCreate);
                kitCreate.setEnabled(true);
                kitCreate.getGameRules().setRanked(true);
                for ( Profile profile : Profile.getProfiles().values() ) {
                    profile.getStatisticsData().put(kitCreate, new StatisticsData());
                }
                if (kitCreate.isEnabled()) {
                    Queue unRanked = new Queue(kitCreate, QueueType.UNRANKED);
                    Queue ranked = new Queue(kitCreate, QueueType.RANKED);
                    kitCreate.setUnrankedQueue(unRanked);
                    kitCreate.setRankedQueue(ranked);
                }

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Successfully created a new kit &c" + kitCreate.getDisplayName() + "."));
                break;
            case "setkb":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/kit setkb &7<kit> <kb profile>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.setKnockbackProfile(args[2]);
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated knockback profile for &c" + kit.getName() +  " &7to &c" + args[2]));
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit remove &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.delete();
                Kit.getKits().forEach(Kit::save);
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Removed the kit &c" + kit.getName() + "&7."));
                break;
            case "hitdelay":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/kit hitdelay &7<kit> (1-20)"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                if (!NumberUtils.isNumber(args[2])) return true;

                kit.getGameRules().setHitDelay(Integer.parseInt(args[2]));
                kit.save();
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated &c" + kit.getName() + " &7hitdelay set to &c" + Integer.parseInt(args[2])));
                break;
            case "ranked":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit ranked &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setRanked(!kit.getGameRules().isRanked());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated ranked mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isRanked() ? "true!" : "false!")));
                break;
            case "build":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit build &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setBuild(!kit.getGameRules().isBuild());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated build mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isBuild() ? "true!" : "false!")));
                break;
            case "combo":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit combo &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setCombo(!kit.getGameRules().isCombo());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated combo mode for &c" + kit.getName() + " &7to &c" + (kit.getGameRules().isCombo() ? "true!" : "false!")));
                player.sendMessage(CC.translate("&8[&cTIP&8] &7This will set the No-Damage Ticks to 2 and players will be able to hit faster!"));

                break;
            case "partyffa":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit partyffa &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setPartyffa(!kit.getGameRules().isPartyffa());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated Party-FFA mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isPartyffa() ? "true!" : "false!")));
                break;
            case "partysplit":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit partysplit &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setPartysplit(!kit.getGameRules().isPartysplit());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated party-split mode for " + "&c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isPartysplit() ? "true!" : "false!")));
                break;
            case "antifoodloss":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit antifoodloss &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setAntifoodloss(!kit.getGameRules().isAntifoodloss());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated anti-food-loss mode for &c" + kit.getName() + " &7to &c" + (kit.getGameRules().isAntifoodloss() ? "true!" : "false!")));
                break;
            case "bowhp":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit bowhp &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setBowhp(!kit.getGameRules().isBowhp());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated bow-hp mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isBowhp() ? "true!" : "false!")));
                break;
            case "healthregen":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit healthregen &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setHealthRegeneration(!kit.getGameRules().isHealthRegeneration());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated health-regeneration mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isHealthRegeneration() ? "true!" : "false!")));
                break;
            case "infinitespeed":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit infinitespeed &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setInfinitespeed(!kit.getGameRules().isInfinitespeed());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated infinite-speed mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isInfinitespeed() ? "true!" : "false!")));
                break;
            case "infinitestrength":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit infinitestrength &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setInfinitestrength(!kit.getGameRules().isInfinitestrength());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated infinite-strength mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isInfinitestrength() ? "true!" : "false!")));
                break;
            case "showhealth":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit showhealth &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setShowHealth(!kit.getGameRules().isShowHealth());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated show-health mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isShowHealth() ? "true!" : "false!")));
                break;
            case "timed":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit timed &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setTimed(!kit.getGameRules().isTimed());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated timed mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isTimed() ? "true!" : "false!")));
                break;
            case "getinv":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit getinv &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                player.getInventory().setArmorContents(kit.getKitInventory().getArmor());
                player.getInventory().setContents(kit.getKitInventory().getContents());
                player.addPotionEffects(kit.getKitInventory().getEffects());
                player.updateInventory();
                player.sendMessage(CC.translate("&7[&cGhoul&7] &7You received the kit's inventory."));
                break;
            case "setinv":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit setinv &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getKitInventory().setArmor(player.getInventory().getArmorContents());
                kit.getKitInventory().setContents(player.getInventory().getContents());
                List<PotionEffect> potionEffects = new ArrayList<>(player.getActivePotionEffects());
                kit.getKitInventory().setEffects(potionEffects);
                kit.save();
                player.sendMessage((CC.translate("&7[&cGhoul&7] &cYou updated the kit's loadout.")));
                break;
            case "list":
                for (Kit kits : Kit.getKits() ) {
                    if (kits == null) {
                        player.sendMessage(CC.translate("&7There are no kits setup."));
                    } else {
                        player.sendMessage(CC.translate(" • " + (kits.isEnabled() ? CC.GREEN : CC.RED) + kits.getName() + (kits.getGameRules().isRanked() ? "&7[&cRanked&7]" : "&7[&eNot-Ranked&7]")));
                    }
                }
                break;
            case "save":
                for (Kit kits : Kit.getKits() ) {
                    kits.save();
                }
                sender.sendMessage(CC.translate("&7[&cGhoul&7] &7You saved &c" + Kit.getKits().size() +  " the kits!"));
                break;
            case "noitems":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit noitems &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setNoitems(!kit.getGameRules().isNoitems());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated no-items mode for &c" + kit.getName() + " &7to &c" + (kit.getGameRules().isNoitems() ? "true!" : "false!")));
                break;
            case "sumo":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit sumo &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setNoitems(!kit.getGameRules().isNoitems());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated sumo mode for &c"
                        + kit.getName() +  " &7to &c" + (kit.getGameRules().isSumo() ? "true!" : "false!")));
                break;
            case "spleef":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit sumo &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setSpleef(!kit.getGameRules().isSpleef());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated spleef mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isSpleef() ? "true!" : "false!")));
                break;
            case "seticon":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit seticon &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                ItemStack item = player.getItemInHand();

                if (item == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7Please hold a valid item in your hand!"));
                }

                kit.setDisplayIcon(item);
                kit.save();
                player.sendMessage(CC.translate("&7[&cGhoul&7] &cKit Icon set!"));
                break;
            case "displayname":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/kit displayname &7<kit> <name>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                String displayname = "";

                if (args.length == 3) {
                    displayname = args[2];
                }
                if (args.length == 4) {
                    displayname = args[2] + " " + args[3];
                }
                if (args.length == 5) {
                    displayname = args[2] + " " + args[3] + " " + args[4];
                }
                if (args.length == 6) {
                    displayname = args[2] + " " + args[3] + " " + args[4] + " " + args[5];
                }

                kit.setDisplayName(displayname);
                kit.save();
                player.sendMessage(CC.translate("&8[&cGhoul&8] &7Successfully updated the kit &c" + kit.getName() + "'s &7display name."));
                break;
            case "enable":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit enable &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.setEnabled(true);
                player.sendMessage(CC.translate("&7[&cGhoul&7] &cEnabled the kit " + kit));
                break;
            case "disable":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit disable &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.setEnabled(false);
                Queue.getQueues().remove(new Queue(kit, QueueType.RANKED));
                Queue.getQueues().remove(new Queue(kit, QueueType.UNRANKED));
                player.sendMessage(CC.translate("&7[&cGhoul&7] &cDisabled the kit " + kit.getName()));
                break;
            case "waterkill":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit waterkill &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setWaterkill(!kit.getGameRules().isWaterkill());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated water kill mode for &c" + kit.getName() +  " &7to &c" + (kit.getGameRules().isWaterkill() ? "true!" : "false!")));
                break;
            case "voidspawn":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit voidspawn &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.getGameRules().setVoidspawn(!kit.getGameRules().isVoidspawn());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated void spawn mode for &c"
                        + kit.getName() +  " &7to &c" + (kit.getGameRules().isVoidspawn() ? "true!" : "false!")));
                break;
            case "glow":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit glow &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.setGlow(!kit.isGlow());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated glow for &c" + kit.getName() +  " &7to &c" + (kit.isGlow() ? "true!" : "false!")));
                break;
            case "customDamages":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&c/kit customDamages &7<kit>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.setCustomDamages(!kit.isCustomDamages());
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated Custom Damages for &c" + kit.getName() +  " &7to &c" + (kit.isCustomDamages() ? "true!" : "false!")));
                break;
            case "inventoryslot":
                if (args.length < 3) {
                    player.sendMessage(CC.translate("&c/kit inventorySlot &7<kit> <slot>"));
                    return true;
                }

                kit = Kit.getByName(args[1]);
                if (kit == null) {
                    player.sendMessage(CC.translate("&7[&cGhoul&7] &7That kit does not exist."));
                    return true;
                }

                kit.setInventorySlot(Integer.parseInt(args[2]));
                kit.save();

                player.sendMessage(CC.translate("&7[&cGhoul&7] &7Updated &c" + kit.getName() + " &7inventory slot to &c" + args[2]));
                break;
        }
        return true;
    }


}
