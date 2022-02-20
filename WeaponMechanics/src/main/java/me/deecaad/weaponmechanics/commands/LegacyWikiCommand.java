package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

@CommandPermission(permission = "weaponmechanics.command.wiki")
@Deprecated
public class LegacyWikiCommand extends SubCommand {

    public static char SYM = '\u27A2';
    public static String WIKI = "https://github.com/WeaponMechanics/MechanicsMain/wiki";

    public LegacyWikiCommand() {
        super("wm", "wiki", "Shows links to the Wiki");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ComponentBuilder builder = new ComponentBuilder();
        builder.append("Weapon").color(GOLD).bold(true)
                .append("Mechanics").color(GRAY).bold(true)
                .append(" Wiki (Click an option)").bold(false).color(GRAY).italic(true)
                .append("\n").italic(false);

        BaseComponent[] hover = new ComponentBuilder()
                .append("Click to go to Wiki.").color(GRAY).italic(true)
                .create();

        builder.append("  " + SYM + " ").color(GRAY).append(build("Information", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Skins", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Projectile", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Shooting", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Reloading", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Damage", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Explosion", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Scoping", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Firearms", hover)).append("\n");
        builder.append("  " + SYM + " ").color(GRAY).append(build("Melee", hover));

        sender.spigot().sendMessage(builder.create());
    }

    private BaseComponent build(String name, BaseComponent[] hover) {
        BaseComponent component = new TextComponent(name);
        component.setColor(GOLD);
        component.setClickEvent(new ClickEvent(OPEN_URL, WIKI + "/" + name));
        component.setHoverEvent(new HoverEvent(SHOW_TEXT, hover));
        return component;
    }
}
