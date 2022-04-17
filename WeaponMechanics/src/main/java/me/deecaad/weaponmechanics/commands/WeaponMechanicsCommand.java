package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.commands.Argument;
import me.deecaad.core.commands.CommandBuilder;
import me.deecaad.core.commands.CommandData;
import me.deecaad.core.commands.CommandExecutor;
import me.deecaad.core.commands.HelpCommandBuilder;
import me.deecaad.core.commands.SuggestionsBuilder;
import me.deecaad.core.commands.Tooltip;
import me.deecaad.core.commands.arguments.*;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.core.utils.TableBuilder;
import me.deecaad.weaponmechanics.UpdateChecker;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.WeaponMechanicsAPI;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
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
import me.deecaad.weaponmechanics.weapon.projectile.HitBox;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.Projectile;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.ProjectileSettings;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.RayTraceResult;
import me.deecaad.weaponmechanics.weapon.reload.ammo.AmmoTypes;
import me.deecaad.weaponmechanics.weapon.shoot.recoil.Recoil;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.deecaad.core.commands.arguments.IntegerArgumentType.ITEM_COUNT;
import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static org.bukkit.ChatColor.*;

@SuppressWarnings("unchecked")
public class WeaponMechanicsCommand {

    public static String WIKI = "https://github.com/WeaponMechanics/MechanicsMain/wiki";
    public static char SYM = '\u27A2';

    public static Function<CommandData, Tooltip[]> WEAPON_SUGGESTIONS = (data) -> {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();
        return info.getSortedWeaponList().stream().map(Tooltip::of).toArray(Tooltip[]::new);
    };

    public static void build() {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();

        MapArgumentType weaponDataMap = new MapArgumentType()
                .with("ammo", MapArgumentType.INT(1, 10, 30))
                .with("firemode", MapArgumentType.INT(0, 1, 2))
                .with("skipMainhand", MapArgumentType.INT(0, 1))
                .with("slot", MapArgumentType.INT(IntStream.rangeClosed(0, 40).boxed().toArray(Integer[]::new)));

        CommandBuilder command = new CommandBuilder("wm")
                .withAliases("weaponmechanics")
                .withPermission("weaponmechanics.admin")
                .withDescription("WeaponMechanics' main command")
                .withSubcommand(new CommandBuilder("give")
                        .withPermission("weaponmechanics.commands.give")
                        .withDescription("Gives the target(s) with requested weapon(s)")
                        .withArgument(new Argument<>("target", new EntityListArgumentType()).withDesc("Who to give the weapon(s) to"))
                        .withArgument(new Argument<>("weapon", new StringArgumentType().withLiterals("*", "**", "*r")).withDesc("Which weapon(s) to give").replace(WEAPON_SUGGESTIONS))
                        .withArgument(new Argument<>("amount", new IntegerArgumentType(1, 64), 1).withDesc("How many of each weapon to give").append(ITEM_COUNT))
                        .withArgument(new Argument<>("data", weaponDataMap, new HashMap<>()).withDesc("Extra data for the weapon"))
                        .executes(CommandExecutor.any((sender, args) -> {
                            give(sender, (List<Entity>) args[0], (String) args[1], (int) args[2], (Map) args[3]);
                        })))

                .withSubcommand(new CommandBuilder("get")
                        .withPermission("weaponmechanics.commands.get")
                        .withDescription("Gives you the requested weapon(s)")
                        .withArgument(new Argument<>("weapon", new StringArgumentType().withLiterals("*", "**", "*r")).withDesc("Which weapon(s) to give").replace(WEAPON_SUGGESTIONS))
                        .withArgument(new Argument<>("amount", new IntegerArgumentType(1, 64), 1).withDesc("How many of each weapon to give").append(ITEM_COUNT))
                        .withArgument(new Argument<>("data", weaponDataMap, new HashMap<>()).withDesc("Extra data for the weapon"))
                        .executes(CommandExecutor.entity((sender, args) -> {
                            give(sender, Collections.singletonList(sender), (String) args[0], (int) args[1], (Map) args[2]);
                        })))

                .withSubcommand(new CommandBuilder("ammo")
                        .withPermission("weaponmechanics.commands.ammo")
                        .withDescription("Gets ammo for held weapon")
                        .withArguments(new Argument<>("amount", new IntegerArgumentType(), 64).withDesc("How much ammo to give").replace(ITEM_COUNT))
                        .executes(CommandExecutor.player((sender, args) -> {
                            giveAmmo(sender, (int) args[0]);
                        })))

                .withSubcommand(new CommandBuilder("info")
                        .withPermission("weaponmechanics.commands.info")
                        .withDescription("Displays information about WeaponMechanics")
                        .executes(CommandExecutor.any((sender, args) -> {
                            info(sender);
                        })))

                .withSubcommand(new CommandBuilder("list")
                        .withPermission("weaponmechanics.commands.list")
                        .withDescription("Displays a table of weapons")
                        .withArgument(new Argument<>("page", new IntegerArgumentType(1), 1).withDesc("Which page to display")
                                .append(SuggestionsBuilder.range(1, 1 + info.getSortedWeaponList().size() / 16)))
                        .executes(CommandExecutor.any((sender, args) -> {
                            list(sender, (int) args[0]);
                        })))

                .withSubcommand(new CommandBuilder("wiki")
                        .withPermission("weaponmechanics.commands.wiki")
                        .withDescription("Gives you wiki links (click!)")
                        .executes(CommandExecutor.any((sender, args) -> {
                            wiki(sender);
                        })))

                .withSubcommand(new CommandBuilder("reload")
                        .withPermission("weaponmechanics.commands.reload")
                        .withDescription("Reloads config")
                        .executes(CommandExecutor.any((sender, args) -> {
                            WeaponMechanicsAPI.getInstance().onReload().thenRunSync(() -> sender.sendMessage(GREEN + "Reloaded configuration"));
                        })));


        // Explosion subcommands *mostly* share the same arguments, so we store
        // them in an array to avoid repetitive code.
        Argument<?>[] explosionArgs = new Argument<?>[]{
                new Argument<>("origin", new LocationArgumentType()).withDesc("Where the center of explosion is"),
                new Argument<>("exposure", new StringArgumentType(), "DEFAULT").withDesc("How to calculate entity damage").replace(SuggestionsBuilder.from(ExposureFactory.getInstance().getOptions())),
                new Argument<>("break", new BooleanArgumentType(), true).withDesc("true to have the explosion break blocks"),
                new Argument<>("blacklist", new BlockPredicateArgumentType(), BlockPredicateArgumentType.FALSE("none")).withDesc("Which blocks should not be broken"),
                new Argument<>("regeneration", new TimeArgumentType(), 200).withDesc("How long after should the blocks regenerate")
        };

        CommandBuilder test = new CommandBuilder("test")
                .withPermission("weaponmechanics.commands.test")
                .withDescription("Contains useful testing dev commands")

                .withSubcommand(new CommandBuilder("nbt")
                        .withPermission("weaponmechanics.commands.test.nbt")
                        .withDescription("Shows every NBT tag for the target's held item")
                        .withArgument(new Argument<>("target", new EntityArgumentType(), null).withDesc("Whose item should we investigate"))
                        .executes(CommandExecutor.any((sender, args) -> nbt(sender, (Entity) args[0]))))

                .withSubcommand(new CommandBuilder("explosion")
                        .withPermission("weaponmechanics.commands.test.explosion")
                        .withRequirements(LivingEntity.class::isInstance) // Only living entities can cause explosions
                        .withDescription("Spawns in an explosion that regenerates")
                        .withSubcommand(new CommandBuilder("sphere")
                                .withArgument(new Argument<>("radius", new DoubleArgumentType(0.1)).withDesc("The radius of the sphere").append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArguments(explosionArgs)
                                .executes(CommandExecutor.entity((entity, args) -> explode((LivingEntity) entity, new SphericalExplosion((double) args[0]), (Location) args[1], args[2].toString(), (boolean) args[3], (Predicate<Block>) args[4], (int) args[5]))))
                        .withSubcommand(new CommandBuilder("cube")
                                .withArgument(new Argument<>("width", new DoubleArgumentType(0.1)).withDesc("The horizontal size of the cube").append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArgument(new Argument<>("height", new DoubleArgumentType(0.1)).withDesc("The vertical size of the cube").append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArguments(explosionArgs)
                                .executes(CommandExecutor.entity((entity, args) -> explode((LivingEntity) entity, new CuboidExplosion((double) args[0], (double) args[1]), (Location) args[2], args[3].toString(), (boolean) args[4], (Predicate<Block>) args[5], (int) args[6]))))
                        .withSubcommand(new CommandBuilder("parabola")
                                .withArgument(new Argument<>("angle", new DoubleArgumentType(0.1)).withDesc("The slope of the parabola").append(SuggestionsBuilder.from(0.25, 0.5, 0.75, 1.0)))
                                .withArgument(new Argument<>("depth", new DoubleArgumentType(0.1)).withDesc("How far down to start the parabola").append(SuggestionsBuilder.from(5.0, 10.0, 15.0)))
                                .withArguments(explosionArgs)
                                .executes(CommandExecutor.entity((entity, args) -> explode((LivingEntity) entity, new ParabolicExplosion((double) args[0], (double) args[1]), (Location) args[2], args[3].toString(), (boolean) args[4], (Predicate<Block>) args[5], (int) args[6]))))
                        .withSubcommand(new CommandBuilder("vanilla")
                                .withArgument(new Argument<>("yield", new DoubleArgumentType(0.1)).withDesc("How big the explosion is").append(SuggestionsBuilder.from(0, 0.25, 0.5, 0.75, 1.0)))
                                .withArgument(new Argument<>("rays", new IntegerArgumentType(1)).withDesc("How accurate to ray-trace, should scale with <yield>").append(SuggestionsBuilder.from(8, 16, 24, 32)))
                                .withArguments(explosionArgs)
                                .executes(CommandExecutor.entity((entity, args) -> explode((LivingEntity) entity, new DefaultExplosion((double) args[0], (int) args[1]), (Location) args[2], args[3].toString(), (boolean) args[4], (Predicate<Block>) args[5], (int) args[6])))))

                .withSubcommand(new CommandBuilder("fakeentity")
                        .withPermission("weaponmechanics.commands.test.fakeentity")
                        .withRequirements(LivingEntity.class::isInstance)
                        .withDescription("Spawns in a fake entity")
                        .withArgument(new Argument<>("entity", new EntityTypeArgumentType()).withDesc("Which entity to spawn"))
                        .withArgument(new Argument<>("location", new LocationArgumentType()).withDesc("Where should the entity be spawned"))
                        .withArgument(new Argument<>("move", new StringArgumentType()).withDesc("How should the entity act").append(SuggestionsBuilder.from("none", "spin", "flash", "x", "y", "z")))
                        .withArgument(new Argument<>("time", new TimeArgumentType(), 600).withDesc("How long should the entity exist"))
                        .withArgument(new Argument<>("gravity", new BooleanArgumentType(), true).withDesc("Should the entity be effected by gravity"))
                        .withArgument(new Argument<>("name", new GreedyArgumentType(), null).withDesc("What is the entity's custom name"))
                        .executes(CommandExecutor.player((sender, args) -> spawn(sender, (Location) args[1], (EntityType) args[0], (String) args[2], (int) args[3], (boolean) args[4], (String) args[5]))))

                .withSubcommand(new CommandBuilder("firework")
                        .withPermission("weaponmechanics.commands.test.firework")
                        .withDescription("Spawns in a fake firework")
                        .withArgument(new Argument<>("location", new LocationArgumentType()).withDesc("Where to spawn the firework"))
                        .withArgument(new Argument<>("time", new TimeArgumentType(), 60).withDesc("How long before the firework explodes"))
                        .withArgument(new Argument<>("shape", new EnumArgumentType<>(FireworkEffect.Type.class), FireworkEffect.Type.BURST).withDesc("Which shape should the particles be spread"))
                        .withArgument(new Argument<>("color", new ColorArgumentType(), Color.RED).withDesc("Color of the particles"))
                        .withArgument(new Argument<>("fade", new ColorArgumentType(), Color.RED).withDesc("Fade color of the particles"))
                        .withArgument(new Argument<>("flicker", new BooleanArgumentType(), true).withDesc("Should the particles flash"))
                        .withArgument(new Argument<>("trail", new BooleanArgumentType(), true).withDesc("Should the firework have a trail"))
                        .executes(CommandExecutor.any((sender, args) -> {
                            firework((Location) args[0], (int) args[1], (FireworkEffect.Type) args[2], (Color) args[3], (Color) args[4], (boolean) args[5], (boolean) args[6]);
                        })))

                .withSubcommand(new CommandBuilder("hitbox")
                        .withPermission("weaponmechanics.commands.test.hitbox")
                        .withDescription("Shows the hitboxes of nearby entities")
                        .withArgument(new Argument<>("targets", new EntityListArgumentType()).withDesc("Whose hitbox to show"))
                        .withArgument(new Argument<>("time", new TimeArgumentType(), 200).withDesc("How long to show the hitbox"))
                        .executes(CommandExecutor.any((sender, args) -> {
                            hitbox(sender, (List<Entity>) args[0], (int) args[1]);
                        })))

                .withSubcommand(new CommandBuilder("ray")
                        .withPermission("weaponmechanics.commands.test.ray")
                        .withDescription("Ray traces blocks/entities")
                        .withRequirements(LivingEntity.class::isInstance)
                        .withArgument(new Argument<>("highlight-box", new BooleanArgumentType(), false).withDesc("false=show point, true=show hitbox"))
                        .withArgument(new Argument<>("distance", new IntegerArgumentType(1), 10).withDesc("How far to ray-trace"))
                        .withArgument(new Argument<>("time", new TimeArgumentType(), 200).withDesc("How long to show the particles"))
                        .executes(CommandExecutor.entity((sender, args) -> {
                            ray((LivingEntity) sender, (boolean) args[0], (int) args[1], (int) args[2]);
                        })))

                .withSubcommand(new CommandBuilder("recoil")
                        .withPermission("weaponmechanics.commands.test.recoil")
                        .withDescription("Test screen recoil")
                        .withArgument(new Argument<>("push", new TimeArgumentType()).withDesc("How long to push screen away"))
                        .withArgument(new Argument<>("recover", new TimeArgumentType()).withDesc("Time to return to center"))
                        .withArgument(new Argument<>("yaws", ListArgumentType.doubles(0.5, 1.0, 1.5)).withDesc("A random yaw to select"))
                        .withArgument(new Argument<>("pitches", ListArgumentType.doubles(0.5, 1.0, 1.5)).withDesc("A random pitch to select"))
                        .withArgument(new Argument<>("delay", new IntegerArgumentType(1, 20), 5).withDesc("Delay between shots"))
                        .withArgument(new Argument<>("time", new TimeArgumentType(), 100).withDesc("How long to shoot for"))
                        .executes(CommandExecutor.player((sender, args) -> {
                            recoil(sender, (int) args[0], (int) args[1], (List<Double>) args[2], (List<Double>) args[3], (int) args[4], (int) args[5]);
                        })))

                .withSubcommand(new CommandBuilder("shoot")
                        .withPermission("weaponmechanics.commands.test.shoot")
                        .withDescription("Test projectile shooting")
                        .withRequirements(LivingEntity.class::isInstance)
                        .withArgument(new Argument<>("speed", new DoubleArgumentType(0.0)).withDesc("How fast to move the projectile"))
                        .withArgument(new Argument<>("gravity", new DoubleArgumentType(), 0.05).withDesc("The downward acceleration"))
                        .withArgument(new Argument<>("disguise", new EntityTypeArgumentType(), null).withDesc("Which disguise to use"))
                        .executes(CommandExecutor.entity((sender, args) -> {
                            shoot((LivingEntity) sender, (double) args[0], (double) args[1], (EntityType) args[2]);
                        })));


        command.withSubcommand(test);
        command.registerHelp(HelpCommandBuilder.HelpColor.from(GOLD, GRAY, SYM));
        command.register();
    }

    public static void give(CommandSender sender, List<Entity> targets, String weaponTitle, int amount, Map<String, Object> data) {
        if (targets.isEmpty()) {
            sender.sendMessage(RED + "No entities were found");
            return;
        }

        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();
        List<Entity> entitiesGiven = new ArrayList<>();
        Set<String> weaponsGiven = new HashSet<>();

        // Handle random weapon key "*r"
        if ("*r".equalsIgnoreCase(weaponTitle))
            weaponTitle = NumberUtil.random(info.getSortedWeaponList());

        for (Entity loop : targets) {
            if (!loop.getType().isAlive())
                continue;

            LivingEntity entity = (LivingEntity) loop;
            boolean isPlayer = entity instanceof Player;
            Player player = isPlayer ? (Player) entity : null;

            // Loop through each possible weapon and give each gun to the player.
            if ("*".equalsIgnoreCase(weaponTitle) || "**".equalsIgnoreCase(weaponTitle)) {

                // Non-players can only be given 1 weapon at a time.
                if (!isPlayer)
                    continue;

                entitiesGiven.add(entity);
                for (String title : info.getSortedWeaponList()) {
                    if ("**".equalsIgnoreCase(weaponTitle) || player.getInventory().firstEmpty() != -1) {
                        info.giveOrDropWeapon(title, player, amount, data);
                        weaponsGiven.add(title);
                    }
                }
            }

            // Normal weapontitle
            else {
                weaponTitle = info.getWeaponTitle(weaponTitle);
                if (info.giveOrDropWeapon(weaponTitle, entity, amount, data)) {
                    entitiesGiven.add(entity);
                    weaponsGiven.add(weaponTitle);
                }
            }
        }

        // Probably only happens when somebody uses a complicated targeter, like
        // @e[type=wither_skeleton] while there are no wither skeletons nearby.
        if (entitiesGiven.isEmpty() || weaponsGiven.isEmpty()) {
            sender.sendMessage(RED + "No entities were given any weapons...");
            return;
        }

        String targetInfo = entitiesGiven.size() == 1 ? entitiesGiven.get(0).getName() : String.valueOf(entitiesGiven.size());
        String weaponInfo = weaponsGiven.size() == 1 ? amount + " " + weaponsGiven.stream().findAny().get() + (amount > 1 ? "s" : "") : weaponsGiven.size() + " weapons";

        // Show each target. This may be useful in case the user accidentally
        // gave the weapon(s) to too many people, and needs to check who got it.
        HoverEventSource<?> targetHover;
        if (entitiesGiven.size() == 1 && entitiesGiven.get(0) instanceof HoverEventSource) {
            targetHover = (HoverEventSource<?>) entitiesGiven.get(0);
        } else if (entitiesGiven.isEmpty()) {
            targetHover = null;
        } else {
            TextComponent.Builder builder = text().append(text(entitiesGiven.get(0).getName()));
            for (int i = 1; i < entitiesGiven.size(); i++)
                builder.append(text(", ")).append(text(entitiesGiven.get(i).getName()));
            targetHover = builder.build();
        }

        Style style = Style.style(NamedTextColor.GREEN);
        TextComponent.Builder builder = text()
                .append(text("Gave ", style))
                .append(text().content(targetInfo).style(style).hoverEvent(targetHover))
                .append(text(" ", style))
                .append(text().content(weaponInfo).style(style));

        MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(builder);
    }

    public static void giveAmmo(Player sender, int amount) {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();
        String title = info.getWeaponTitle(sender.getInventory().getItemInMainHand(), false);
        if (title == null) {
            sender.sendMessage(RED + "Not holding gun");
            return;
        }

        String path = title + ".Reload.Ammo.Ammo_Types";
        Configuration config = WeaponMechanics.getConfigurations();
        if (!config.containsKey(path)) {
            sender.sendMessage(RED + title + " does not use ammo");
            return;
        }

        AmmoTypes ammo = config.getObject(path, AmmoTypes.class);
        ammo.giveAmmo(sender.getInventory().getItemInMainHand(), WeaponMechanics.getPlayerWrapper(sender), amount, 64);

        sender.sendMessage(GREEN + "Sent ammo");
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

        sender.sendMessage("  " + GRAY + SYM + GOLD + " Server: " + GRAY + Bukkit.getName() + " " + Bukkit.getVersion());
        sender.sendMessage("  " + GRAY + SYM + GOLD + " MechanicsCore: " + GRAY + MechanicsCore.getPlugin().getDescription().getVersion());
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Java: " + GRAY + System.getProperty("java.version"));

        // Gets all supported plugins
        Set<String> softDepends = new LinkedHashSet<>(desc.getSoftDepend());
        softDepends.addAll(MechanicsCore.getPlugin().getDescription().getSoftDepend());
        softDepends.remove("MechanicsCore");
        softDepends.removeIf(name -> Bukkit.getPluginManager().getPlugin(name) == null);
        sender.sendMessage("  " + GRAY + SYM + GOLD + " Supported plugins: " + GRAY + String.join(", ", softDepends));
    }

    public static void list(CommandSender sender, int requestedPage) {
        InfoHandler info = WeaponMechanics.getWeaponHandler().getInfoHandler();
        List<String> weapons = info.getSortedWeaponList();

        // «»
        Style gold = Style.style(NamedTextColor.GOLD);
        Style gray = Style.style(NamedTextColor.GRAY);
        TextComponent table = new TableBuilder()
                .withConstraints(TableBuilder.DEFAULT_CONSTRAINTS.setColumns(3).setPixels(310)) // 10 pixel buffer
                .withElementChar('-')
                .withElementCharStyle(gold)
                .withFillChar('=')
                .withFillCharStyle(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .withHeader("Weapons [Page " + requestedPage + "/" + (weapons.size() / (3 * 8) + 1) + "]")
                .withHeaderStyle(gold)
                .withElementStyle(gray)
                .withLeft(text().content("«").style(gold)
                        .clickEvent(ClickEvent.runCommand("/wm list " + (requestedPage - 1)))
                        .hoverEvent(text("Click to go to page " + (requestedPage - 1), gray))
                        .build())
                .withRight(text().content("»").style(gold)
                        .clickEvent(ClickEvent.runCommand("/wm list " + (requestedPage + 1)))
                        .hoverEvent(text("Click to go to page " + (requestedPage + 1), gray))
                        .build())
                .withAttemptSinglePixelFix()
                .withSupplier(i -> {
                    int index = i + (requestedPage - 1) * 3 * 8;
                    if (weapons.size() <= index)
                        return empty();

                    String title = weapons.get(index);
                    ItemStack item = info.generateWeapon(title, 1);
                    return text().content(title.toUpperCase(Locale.ROOT))
                            .clickEvent(ClickEvent.runCommand("/wm get " + title))
                            .hoverEvent(item instanceof HoverEventSource
                                    ? (HoverEventSource<?>) item // Paper items implement this by default since 1.16
                                    : LegacyComponentSerializer.legacySection().deserialize(item.getItemMeta().getDisplayName()))
                            .build();
                })
                .build();

        MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(table);
    }

    public static void wiki(CommandSender sender) {
        List<String> pages = Arrays.asList("Information", "Skins", "Shooting", "Scoping",
                "Reloading", "Projectile", "Damage", "Explosion", "Firearms", "Melee",
                "Addons", "API", "Commands and Permissions", "References", "Placeholders",
                "General");

        Style gold = Style.style(NamedTextColor.GOLD);
        Style gray = Style.style(NamedTextColor.GRAY);
        TextComponent table = new TableBuilder()
                .withConstraints(TableBuilder.DEFAULT_CONSTRAINTS.setPixels(300))
                .withElementChar('-')
                .withElementCharStyle(gold)
                .withElementStyle(gray)
                .withAttemptSinglePixelFix()
                .withSupplier(i -> i >= pages.size() ? empty() : text().content(pages.get(i).toUpperCase(Locale.ROOT))
                        .clickEvent(ClickEvent.openUrl(WIKI + '/' + pages.get(i)))
                        .hoverEvent(text("Click to go to the wiki", gray))
                        .build())
                .build();

        MechanicsCore.getPlugin().adventure.sender(sender).sendMessage(table);
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
        WeaponMechanics.debug.info(tags);
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

    public static void hitbox(CommandSender sender, List<Entity> targets, int ticks) {
        Configuration basicConfiguration = WeaponMechanics.getBasicConfigurations();
        new BukkitRunnable() {
            int ticksPassed = 0;
            public void run() {

                for (Entity entity : targets) {
                    if (!(entity instanceof LivingEntity)) continue;
                    if (entity.equals(sender)) continue;

                    EntityType type = entity.getType();

                    double head = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.HEAD.name(), -1);
                    double body = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.BODY.name(), -1);
                    double legs = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.LEGS.name(), -1);
                    double feet = basicConfiguration.getDouble("Entity_Hitboxes." + type.name() + "." + DamagePoint.FEET.name(), -1);

                    if (head == -1 || body == -1 || legs == -1 || feet == -1) {
                        debug.log(LogLevel.ERROR, "Entity type " + type.name() + " is missing some of its damage point values, please add it",
                                "Located at file /WeaponMechanics/config.yml in Entity_Hitboxes." + type.name() + " in configurations",
                                "Its missing one of these: HEAD, BODY, LEGS or FEET");
                        continue;
                    }
                    double sumOf = head + body + legs + feet;
                    if (NumberUtil.equals(sumOf, 0.0)) { // If the numbers are not super close together (floating point issues)
                        debug.log(LogLevel.ERROR, "Entity type " + type.name() + " hit box values sum doesn't match 1.0",
                                "Located at file /WeaponMechanics/config.yml in Entity_Hitboxes." + type.name() + " in configurations",
                                "Now the total sum was " + sumOf + ", please make it 1.0.");
                        continue;
                    }

                    BoundingBox box = entity.getBoundingBox();
                    double max = box.getMaxY();
                    double height = box.getHeight();

                    double headY = max - (height * head);
                    double bodyY = max - (height * (head + body));
                    double legsY = max - (height * (head + body + legs));
                    double feetY = max - (height * (head + body + legs + feet)); // this could also be just box.getMinY()

                    for (double x = box.getMinX(); x <= box.getMaxX(); x += 0.25) {
                        for (double z = box.getMinZ(); z <= box.getMaxZ(); z += 0.25) {

                            if (head > 0.0) {
                                entity.getWorld().spawnParticle(Particle.REDSTONE, x, headY, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.RED, 1.0f), true);
                            }
                            if (body > 0.0) {
                                entity.getWorld().spawnParticle(Particle.REDSTONE, x, bodyY, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.ORANGE, 1.0f), true);
                            }
                            if (legs > 0.0) {
                                entity.getWorld().spawnParticle(Particle.REDSTONE, x, legsY, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.YELLOW, 1.0f), true);
                            }
                            if (feet > 0.0) {
                                entity.getWorld().spawnParticle(Particle.REDSTONE, x, feetY, z, 1, 0, 0, 0, 0.0001, new Particle.DustOptions(Color.GREEN, 1.0f), true);
                            }
                        }
                    }
                }

                ticksPassed += 5;
                if (ticksPassed >= ticks) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(WeaponMechanics.getPlugin(), 0, 5);
    }

    public static void firework(Location location, int time, FireworkEffect.Type type, Color color, Color fade, boolean flicker, boolean trail) {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
        FireworkEffect effect = FireworkEffect.builder()
                .with(type)
                .withColor(color)
                .withFade(fade)
                .flicker(flicker)
                .trail(trail)
                .build();
        meta.addEffect(effect);
        itemStack.setItemMeta(meta);

        Random random = new Random();

        FakeEntity fakeEntity = CompatibilityAPI.getEntityCompatibility().generateFakeEntity(location, EntityType.FIREWORK, itemStack);
        fakeEntity.setMotion(random.nextGaussian() * 0.001, 0.3, random.nextGaussian() * 0.001);
        fakeEntity.show();
        if (time == 0) {
            fakeEntity.playEffect(EntityEffect.FIREWORK_EXPLODE);
            fakeEntity.remove();
            return;
        }
        new BukkitRunnable() {
            public void run() {
                fakeEntity.playEffect(EntityEffect.FIREWORK_EXPLODE);
                fakeEntity.remove();
            }
        }.runTaskLater(WeaponMechanics.getPlugin(), time);
    }

    public static void ray(LivingEntity sender, boolean box, int distance, int ticks) {

        sender.sendMessage(ChatColor.GREEN + "Showing hitboxes in distance " + distance + " for " + NumberUtil.toTime(ticks / 20));
        IWeaponCompatibility weaponCompatibility = WeaponCompatibilityAPI.getWeaponCompatibility();

        new BukkitRunnable() {
            int ticker = 0;
            @Override
            public void run() {
                Location location = sender.getEyeLocation();
                Vector direction = location.getDirection();
                BlockIterator blocks = new BlockIterator(sender.getWorld(), location.toVector(), direction, 0.0, distance);

                while (blocks.hasNext()) {
                    Block block = blocks.next();

                    HitBox blockBox = weaponCompatibility.getHitBox(block);
                    if (blockBox == null) continue;

                    RayTraceResult rayTraceResult = blockBox.rayTrace(location.toVector(), direction);
                    if (rayTraceResult == null) continue;

                    if (box) {
                        rayTraceResult.outlineOnlyHitPosition(sender);
                    } else {
                        blockBox.outlineAllBoxes(sender);
                    }
                    sender.sendMessage("Block: " + block.getType());
                    break;
                }

                Collection<Entity> entities = sender.getWorld().getNearbyEntities(location, distance, distance, distance);
                if (!entities.isEmpty()) {
                    for (Entity entity : entities) {
                        if (!(entity instanceof LivingEntity) || sender.equals(entity)) continue;

                        HitBox entityBox = weaponCompatibility.getHitBox(entity);
                        if (entityBox == null) continue;

                        RayTraceResult rayTraceResult = entityBox.rayTrace(location.toVector(), direction);
                        if (rayTraceResult == null) continue;

                        if (box) {
                            rayTraceResult.outlineOnlyHitPosition(sender);
                        } else {
                            entityBox.outlineAllBoxes(sender);
                        }
                        sender.sendMessage("Entity: " + entity.getType());

                        break;
                    }
                }

                if (++ticker >= ticks) {
                    cancel();
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, 0);
    }

    public static void recoil(Player player, int push, int recover, List<Double> yaws, List<Double> pitches, int rate, int time) {
        Recoil recoil = new Recoil(push, recover, yaws.stream().map(Double::floatValue).collect(Collectors.toList()), pitches.stream().map(Double::floatValue).collect(Collectors.toList()), null, null);
        PlayerWrapper playerWrapper = WeaponMechanics.getPlayerWrapper(player);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {

                if (playerWrapper.isRightClicking()) {
                    recoil.start(player, true);
                }

                ticks += rate;
                if (ticks > time) {
                    cancel();
                }
            }
        }.runTaskTimer(WeaponMechanics.getPlugin(), 0, rate);
    }

    public static void shoot(LivingEntity sender, double speed, double gravity, EntityType entity) {
        ProjectileSettings projectileSettings = new ProjectileSettings(entity, null,
                gravity, false, -1, false,
                -1, 0.99, 0.96, 0.98, false, 600, -1);
        Projectile projectile = new Projectile(projectileSettings, null, null, null, null);
        projectile.shoot(sender, sender.getEyeLocation(), sender.getLocation().getDirection().multiply(speed), null, null);
    }
}