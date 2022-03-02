package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.CommandBuilder;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.commands.SuggestionsBuilder;
import me.deecaad.core.commands.Tooltip;
import me.deecaad.core.commands.Argument;
import me.deecaad.core.commands.arguments.*;
import me.deecaad.core.commands.CommandExecutor;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.weapon.explode.BlockDamage;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.explode.Flashbang;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExposureFactory;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.RegenerationData;
import me.deecaad.weaponmechanics.weapon.explode.shapes.CuboidExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.DefaultExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ParabolicExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.SphericalExplosion;
import me.deecaad.weaponmechanics.weapon.info.InfoHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;
import static org.bukkit.ChatColor.*;

@SuppressWarnings("unchecked")
public class WeaponMechanicsCommand {

    public static String WIKI = "https://github.com/WeaponMechanics/MechanicsMain/wiki";
    public static char SYM = '\u27A2';

    public static Function<CommandData, Tooltip[]> WEAPON_SUGGESTIONS = (data) -> {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();
        List<String> weapons = info.getSortedWeaponList();
        List<Tooltip> temp = new ArrayList<>(weapons.size() + 3);

        // Some extra options, mostly for fun
        temp.add(Tooltip.of("\"*\"", "Gives target(s) all weapons, until inventory is filled"));
        temp.add(Tooltip.of("\"*r\"", "Gives target(s) a random weapon"));
        temp.add(Tooltip.of("\"**\"", "Gives target(s) all weapons, dropping extra on the ground"));

        // Add in the actual weapons
        weapons.forEach(weapon -> temp.add(Tooltip.of(weapon)));

        return temp.toArray(new Tooltip[0]);
    };

    public static void build() {

        MapArgumentType weaponDataMap = new MapArgumentType()
                .with("attachment", MapArgumentType.LIST.apply(SuggestionsBuilder.from("scope", "grip", "silencer")))
                .with("ammo", MapArgumentType.INT.apply(SuggestionsBuilder.from(1, 10, 30)));

        CommandBuilder command = new CommandBuilder("weaponmechanics")
                .withAliases("wm")
                .withPermission("weaponmechanics.admin")
                .withDescription("WeaponMechanics' main command")
                .withSubCommand(new CommandBuilder("give")
                        .withPermission("weaponmechanics.commands.give")
                        .withDescription("Gives the target(s) with requested weapon(s)")
                        .withArgument(new Argument<>("target", new EntityListArgumentType()))
                        .withArgument(new Argument<>("weapon", new StringArgumentType(true)).replace(WEAPON_SUGGESTIONS))
                        .withArgument(new Argument<>("amount", new IntegerArgumentType(1, 64), 1)
                                .append(SuggestionsBuilder.from(1, 16, 32, 64)))
                        .withArgument(new Argument<>("data", weaponDataMap, new HashMap<>()).replace(weaponDataMap.suggestions()))
                        .executes(CommandExecutor.any((sender, args) -> give(sender, (List<Entity>) args[0], (String) args[1], (int) args[2]))))

                .withSubCommand(new CommandBuilder("get")
                        .withPermission("weaponmechanics.commands.get")
                        .withDescription("Gives you the requested weapon(s)")
                        .withArgument(new Argument<>("weapon", new StringArgumentType(true)).replace(WEAPON_SUGGESTIONS))
                        .withArgument(new Argument<>("amount", new IntegerArgumentType(1), 1)
                                .append(SuggestionsBuilder.from(1, 16, 32, 64)))
                        .withArgument(new Argument<>("data", weaponDataMap, new HashMap<>()).replace(weaponDataMap.suggestions()))
                        .executes(CommandExecutor.entity((sender, args) -> give(sender, Collections.singletonList(sender), (String) args[0], (int) args[1]))))

                .withSubCommand(new CommandBuilder("info")
                        .withPermission("weaponmechanics.commands.info")
                        .withDescription("Displays version/debug information about WeaponMechanics and your server")
                        .executes(CommandExecutor.any((sender, args) -> info(sender))))

                .withSubCommand(new CommandBuilder("list")
                        .withPermission("weaponmechanics.commands.list")
                        .withDescription("Lists a table of weapons loaded by WeaponMechanics")
                        .withArgument(new Argument<>("page", new IntegerArgumentType(1), 1)
                                .append(data -> IntStream.range(1, 1 + WeaponMechanics.getWeaponHandler().getInfoHandler().getSortedWeaponList().size() / 16).mapToObj(Tooltip::of).toArray(Tooltip[]::new)))
                        .executes(CommandExecutor.any((sender, args) -> list(sender, (int) args[0]))))

                .withSubCommand(new CommandBuilder("wiki")
                        .withPermission("weaponmechanics.commands.wiki")
                        .withDescription("Shows useful (clickable) links to specific useful areas on the wiki")
                        .executes(CommandExecutor.any((sender, args) -> wiki(sender))))

                .withSubCommand(new CommandBuilder("reload")
                        .withPermission("weaponmechanics.commands.reload")
                        .withDescription("Reloads WeaponMechanics' weapon configuration without restarting the server")
                        .executes(CommandExecutor.any((sender, args) -> WeaponMechanicsAPI.getInstance().onReload().thenRunSync(() -> sender.sendMessage(GREEN + "Reloaded configuration")))));


        // Explosion subcommands *mostly* share the same arguments, so we store
        // them in an array to avoid repetitive code.
        Argument<?>[] explosionArgs = new Argument<?>[]{
                new Argument<>("location", new LocationArgumentType()),
                new Argument<>("exposure", new StringArgumentType(), "DEFAULT").replace(SuggestionsBuilder.from(ExposureFactory.getInstance().getOptions())),
                new Argument<>("break", new BooleanArgumentType(), true),
                new Argument<>("blacklist", new BlockPredicateType(), BlockPredicateType.FALSE("none")),
                new Argument<>("regeneration", new TimeArgumentType(), 200) // 20 minutes max
        };

        CommandBuilder test = new CommandBuilder("test")
                .withPermission("weaponmechanics.commands.test")
                .withDescription("Contains useful commands for developers and testing and debugging")
                .withSubCommand(new CommandBuilder("nbt")
                        .withPermission("weaponmechanics.commands.test.nbt")
                        .withDescription("Shows every NBT tag for the target's held item")
                        .withArgument(new Argument<>("target", new PlayerArgumentType(), null))
                        .executes(CommandExecutor.any((sender, args) -> nbt(sender, (Entity) args[0]))))

                .withSubCommand(new CommandBuilder("explosion")
                        .withPermission("weaponmechanics.commands.test.explosion")
                        .withRequirements(LivingEntity.class::isInstance)
                        .withDescription("Spawns in an explosion that regenerates")
                        .withSubCommand(new CommandBuilder("sphere")
                                .withArgument(new Argument<>("radius", new DoubleArgumentType(0.1)).append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArguments(explosionArgs)
                                .executes(CommandExecutor.entity((entity, args) -> explode((LivingEntity) entity, new SphericalExplosion((double) args[0]), (Location) args[1], args[2].toString(), (boolean) args[3], (Predicate<Block>) args[4], (int) args[5]))))
                        .withSubCommand(new CommandBuilder("cube")
                                .withArgument(new Argument<>("width", new DoubleArgumentType(0.1)).append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArgument(new Argument<>("height", new DoubleArgumentType(0.1)).append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArguments(explosionArgs)
                                .executes(CommandExecutor.entity((entity, args) -> explode((LivingEntity) entity, new CuboidExplosion((double) args[0], (double) args[1]), (Location) args[2], args[3].toString(), (boolean) args[4], (Predicate<Block>) args[5], (int) args[6]))))
                        .withSubCommand(new CommandBuilder("parabola")
                                .withArgument(new Argument<>("angle", new DoubleArgumentType(0.1)).append(SuggestionsBuilder.from(0, 0.25, 0.5, 0.75, 1.0)))
                                .withArgument(new Argument<>("depth", new DoubleArgumentType(0.1)).append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArguments(explosionArgs)
                                .executes(CommandExecutor.entity((entity, args) -> explode((LivingEntity) entity, new ParabolicExplosion((double) args[0], (double) args[1]), (Location) args[2], args[3].toString(), (boolean) args[4], (Predicate<Block>) args[5], (int) args[6]))))
                        .withSubCommand(new CommandBuilder("vanilla")
                                .withArgument(new Argument<>("yield", new DoubleArgumentType(0.1)).append(SuggestionsBuilder.from(0, 0.25, 0.5, 0.75, 1.0)))
                                .withArgument(new Argument<>("depth", new DoubleArgumentType(0.1)).append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArguments(explosionArgs)
                                .executes(CommandExecutor.entity((entity, args) -> explode((LivingEntity) entity, new DefaultExplosion((double) args[0]), (Location) args[1], args[2].toString(), (boolean) args[3], (Predicate<Block>) args[4], (int) args[5])))))

                .withSubCommand(new CommandBuilder("fakeentity")
                        .withPermission("weaponmechanics.commands.test.fakeentity")
                        .withRequirements(LivingEntity.class::isInstance)
                        .withDescription("Spawns in a fake entity")
                        .withArgument(new Argument<>("entity", new EntityTypeArgumentType()))
                        .withArgument(new Argument<>("location", new LocationArgumentType()))
                        .withArgument(new Argument<>("move", new StringArgumentType()).append(SuggestionsBuilder.from("none", "spin", "flash", "x", "y", "z")))
                        .withArgument(new Argument<>("time", new TimeArgumentType(), 600))
                        .withArgument(new Argument<>("gravity", new BooleanArgumentType(), true))
                        .withArgument(new Argument<>("name", new GreedyArgumentType(), null))
                        .executes(CommandExecutor.player((sender, args) -> spawn(sender, (Location) args[1], (EntityType) args[0], (String) args[2], (int) args[3], (boolean) args[4], (String) args[5]))));

        command.withSubCommand(test);
        command.register();
    }

    public static void give(CommandSender sender, List<Entity> targets, String weaponTitle, int amount) {

        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();
        List<ItemStack> weapons;

        switch (weaponTitle) {
            case "*":
            case "**":
                weapons = info.getSortedWeaponList().stream()
                        .map(weapon -> info.generateWeapon(weapon, amount))
                        .collect(Collectors.toList());
                break;
            case "*r":
                weapons = Collections.singletonList(info.generateWeapon(NumberUtil.random(info.getSortedWeaponList()), amount));
                break;
            default:
                weaponTitle = info.getWeaponTitle(weaponTitle);
                weapons = Collections.singletonList(info.generateWeapon(weaponTitle, amount));
        }

        if (targets.isEmpty()) {
            sender.sendMessage(RED + "No entities were found");
            return;
        }

        int count = 0;
        for (Entity entity : targets) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);

                Map<Integer, ItemStack> overflow = player.getInventory().addItem(weapons.toArray(new ItemStack[0]));
                if (weaponTitle.equals("**"))
                    overflow.values().forEach(item -> entity.getWorld().dropItem(entity.getLocation(), item));
                else if (!weaponTitle.equals("*"))
                    sender.sendMessage(RED + entity.getName() + "'s inventory was full!");

                count++;

            } else if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).getEquipment().setItemInMainHand(weapons.get(0));
                count++;

            }
        }

        if (count > 1) {
            sender.sendMessage(GRAY + "" + count + GREEN + " entities were given " + GRAY + amount + " " + weaponTitle + GREEN + "s");
        } else if (count == 1) {
            sender.sendMessage(GRAY + targets.get(0).getName() + GREEN + " was given " + GRAY + amount + " " + weaponTitle + GREEN + "s");
        } else {
            sender.sendMessage(RED + "No entities were given a weapon");
        }
    }

    public static void info(CommandSender sender) {
        PluginDescriptionFile desc = WeaponMechanics.getPlugin().getDescription();
        sender.sendMessage("" + GRAY + GOLD + BOLD + "Weapon" + GRAY + BOLD + "Mechanics"
                + GRAY + ", v" + ITALIC + desc.getVersion());

        sender.sendMessage("  " + GRAY + SYM + GOLD + " Authors: " + GRAY + String.join(", ", desc.getAuthors()));
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Command:" + GRAY + " /weaponmechanics");

        // Informs the user about any updates
        UpdateChecker updateChecker = WeaponMechanics.getUpdateChecker();
        if (updateChecker != null && updateChecker.hasUpdate()) {
            updateChecker.onUpdateFound(sender, updateChecker.getSpigotResource());
        }

        // Sends information about the server version
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Server: " + GRAY + Bukkit.getName() + " " + Bukkit.getVersion());

        // Information about MechanicsCore
        sender.sendMessage("  " + GRAY + SYM + GOLD + " MechanicsCore: " + GRAY + MechanicsCore.getPlugin().getDescription().getVersion());

        // Information about java
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Java: " + GRAY + System.getProperty("java.version"));

        // Gets all supported plugins
        Set<String> softDepends = new LinkedHashSet<>(desc.getSoftDepend());
        softDepends.addAll(MechanicsCore.getPlugin().getDescription().getSoftDepend());
        softDepends.remove("MechanicsCore");
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Supported plugins: " + GRAY + String.join(", ", softDepends));
    }

    public static void list(CommandSender sender, int requestedPage) {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();

        // We need information to build a table of weapons. There should have 8
        // rows and 2 columns (Since the standard size chat has 10 rows, and
        // enough space for 2 columns of weapon-titles). We reserve 2 rows for
        // page turning buttons and a header.
        int maxPerPage = 2 * 8;
        List<String> weapons = info.getSortedWeaponList();

        // Check to see if the page exists
        if (requestedPage < 0 || requestedPage * maxPerPage >= weapons.size()) {
            sender.sendMessage(net.md_5.bungee.api.ChatColor.RED + "The page you requested (" + (requestedPage + 1) + ") does not exist.");
            return;
        }

        // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/map/MapFont.html
        // MapFont allows us to evaluate the length, in pixels, of a string. MC
        // chat (by default) is 320 pixels wide.
        ComponentBuilder builder = new ComponentBuilder();
        builder.append("==================").color(net.md_5.bungee.api.ChatColor.GOLD)
                .append("  WeaponMechanics  ").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true)
                .append("==================").color(net.md_5.bungee.api.ChatColor.GOLD).italic(false)
                .append("\n");

        int cellSize = 160 - MinecraftFont.Font.getWidth(" » ") * 2;
        int i;
        for (i = requestedPage * maxPerPage; i < (requestedPage + 1) * maxPerPage && i < weapons.size(); i++) {

            // Each table cell needs to fit within the pixel size limit. This
            // prevents an empty row from being created and messing up the
            // table's format. If a weapon-title is longer then the cell size,
            // the weapon-title is trimmed down to the proper size.
            StringBuilder cell = new StringBuilder(weapons.get(i));
            while (MinecraftFont.Font.getWidth(cell.toString()) < cellSize)
                cell.append(' ');
            while (MinecraftFont.Font.getWidth(cell.toString()) > cellSize)
                cell.setLength(cell.length() - 1);

            ItemStack weapon = info.generateWeapon(weapons.get(i), 1);
            ComponentBuilder hover = new ComponentBuilder();

            // We want to display the gun so the player knows: 1) Exactly which
            // weapon they are choosing, 2) That they can click the buttons
            // TODO Use show item using NMS? SHOW_ITEM enum is useless, so...
            if (weapon == null) {
                hover.append("Error in weapon config checking, check console!").color(net.md_5.bungee.api.ChatColor.RED);
            } else if (weapon.hasItemMeta()) {
                ItemMeta meta = weapon.getItemMeta();
                assert meta != null;

                hover.append(TextComponent.fromLegacyText(meta.getDisplayName()));
                if (meta.hasLore() && meta.getLore() != null) {
                    for (String str : meta.getLore())
                        hover.append(TextComponent.fromLegacyText(str));
                }
            }

            // Add the weapon-title with hover/click events to the table.
            builder.append(" \u27A2 ").reset().color(net.md_5.bungee.api.ChatColor.GOLD)
                    .append(cell.toString()).color(net.md_5.bungee.api.ChatColor.GRAY)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm get " + weapons.get(i)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));

            // After filling the 2 columns, we can move to the next row.
            if (i % 2 == 1)
                builder.append("\n");
        }

        // If there weren't enough weapons to fill up a row completely, then
        // we need to add a new line for the page selector.
        if (i % 2 == 1)
            builder.append("\n");

        // Add the 'previous page' and 'next page' options below the table
        builder.append("================== ").reset().color(net.md_5.bungee.api.ChatColor.GOLD)
                .append("«").color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to go to the previous page")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm list " + (requestedPage)))
                .append("                   ").reset()
                .append("»").color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Click to go to the next page")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm list " + (requestedPage + 2)))
                .append(" ==================").color(net.md_5.bungee.api.ChatColor.GOLD).bold(false);

        sender.spigot().sendMessage(builder.create());
    }

    public static void wiki(CommandSender sender) {

        ComponentBuilder builder = new ComponentBuilder();
        builder.append("Weapon").color(net.md_5.bungee.api.ChatColor.GOLD).bold(true)
                .append("Mechanics").color(net.md_5.bungee.api.ChatColor.GRAY).bold(true)
                .append(" Wiki (Click an option)").bold(false).color(net.md_5.bungee.api.ChatColor.GRAY).italic(true)
                .append("\n").italic(false);

        BaseComponent[] hover = new ComponentBuilder()
                .append("Click to go to Wiki.").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true)
                .create();

        // We cannot add any more lines than this since the player's chat will
        // overflow (hiding information from the user).
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Information", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Skins", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Projectile", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Shooting", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Reloading", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Damage", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Explosion", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Scoping", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Firearms", hover)).append("\n");
        builder.append("  " + SYM + " ").color(net.md_5.bungee.api.ChatColor.GRAY).append(build("Melee", hover));

        sender.spigot().sendMessage(builder.create());
    }

    private static BaseComponent build(String name, BaseComponent[] hover) {
        BaseComponent component = new TextComponent(name);
        component.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        component.setClickEvent(new ClickEvent(OPEN_URL, WIKI + "/" + name));
        component.setHoverEvent(new HoverEvent(SHOW_TEXT, hover));
        return component;
    }

    public static void nbt(CommandSender sender, Entity target) {
        LivingEntity entity;

        // When target is null, the command sender should be used as the target
        if (target == null) {
            if (sender instanceof LivingEntity) {
                target = (LivingEntity) sender;
            } else {
                sender.sendMessage(RED + "NBT is an Entity only command!");
                return;
            }
        }

        if (target instanceof LivingEntity) {
            entity = (LivingEntity) target;
        } else {
            sender.sendMessage(RED + "Target must be a creature, player, or armor stand! Got: " + target.getType());
            return;
        }

        if (entity.getEquipment() == null) {
            sender.sendMessage(RED + entity.getName() + " did not have any equipment");
            return;
        }

        ItemStack item = entity.getEquipment().getItemInMainHand();
        if (!item.hasItemMeta()) {
            sender.sendMessage(RED + entity.getName() + "'s " + item.getType() + " did not have any NBT data.");
            return;
        }

        String tags = CompatibilityAPI.getNBTCompatibility().getNBTDebug(item);
        WeaponMechanics.debug.debug(tags);
        sender.sendMessage(StringUtil.color(tags));
    }

    public static void explode(LivingEntity cause, ExplosionShape shape, Location origin, String exposureString, boolean isBreakBlocks, Predicate<Block> blackList, int regen) {
        cause.sendMessage(GREEN + "Spawning explosion in 5 seconds");

        new BukkitRunnable() {
            @Override
            public void run() {
                RegenerationData regeneration = new RegenerationData(regen, Math.max(1, (int) shape.getArea() / 100), 1);
                BlockDamage blockDamage = new BlockDamage(isBreakBlocks, 1, 1, true, 0.0, new HashMap<>(), new HashMap<>()) {
                    @Override
                    public boolean isBlacklisted(Block block) {
                        return blackList.test(block);
                    }
                };

                ExplosionExposure exposure = ReflectionUtil.newInstance(ExposureFactory.getInstance().getMap().get(exposureString));
                Explosion explosion = new Explosion(shape, exposure, blockDamage, regeneration, null, 0.0, true,
                        null, null, new Flashbang(10.0, null), null);
                explosion.explode(cause, origin, null);
            }
        }.runTaskLater(WeaponMechanics.getPlugin(), 100);
    }

    public static void spawn(Player player, Location location, EntityType type, String moveType, int time, boolean gravity, String name) {
        FakeEntity entity = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(location, type, type == EntityType.DROPPED_ITEM ? new ItemStack(Material.STONE_AXE) : null);
        entity.setGravity(gravity);
        entity.setDisplay(name);
        entity.show(player);
        entity.setMotion(0, 0, 0);

        new BukkitRunnable() {

            // Some temp vars for the different move types
            int ticksAlive = 0;
            boolean flash = true;

            @Override
            public void run() {
                if (ticksAlive++ >= time) {
                    entity.remove();
                    cancel();
                    return;
                }

                switch (moveType) {
                    case "spin":
                        entity.setRotation(entity.getYaw() + 5.0f, entity.getYaw() / 2.0f);
                        break;
                    case "flash":
                        if (ticksAlive % 10 == 0) {
                            flash = !flash;
                            entity.setGlowing(flash);
                            entity.updateMeta();
                        }
                        break;
                    case "y":
                        //entity.setMotion(0, 0.08, 0);
                        entity.setPosition(entity.getX(), entity.getY() + 0.1, entity.getZ());
                        break;
                    case "x":
                        //entity.setMotion(0.08, 0, 0);
                        entity.setPosition(entity.getX() + 0.1, entity.getY(), entity.getZ());
                        break;
                    case "z":
                        //entity.setMotion(0.08, 0, 0);
                        entity.setPosition(entity.getX(), entity.getY(), entity.getZ() + 0.1);
                        break;
                }
            }
        }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, 0);
    }
}
