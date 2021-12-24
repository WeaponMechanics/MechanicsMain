package me.deecaad.weaponmechanics.commands;

import me.deecaad.core.commands.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.GRAY;
import static net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

public class WikiCommand extends SubCommand {

    public static char SYM = '\u27A2';
    public static String WIKI = "https://github.com/DeeCaaD/WeaponMechanics-wiki/wiki";

    public WikiCommand() {
        super("wm", "wiki", "Shows links to the Wiki");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ComponentBuilder builder = new ComponentBuilder();
        builder.append("Weapon").color(GOLD).bold(true)
                .append("Mechanics").color(GRAY).bold(true)
                .append(" Wiki (Click an option)").color(GRAY).italic(true).append("\n");

        BaseComponent[] hover = new ComponentBuilder()
                .append("Click to go to Wiki.").italic(true)
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

    }

    private BaseComponent build(String name, BaseComponent[] hover) {
        BaseComponent component = new TextComponent(name);
        component.setColor(GOLD);
        component.setClickEvent(new ClickEvent(OPEN_URL, WIKI + "/" + name));
        component.setHoverEvent(new HoverEvent(SHOW_TEXT, hover));
        return component;
    }
}
