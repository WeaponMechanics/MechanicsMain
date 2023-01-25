package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.placeholder.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ActionBarMechanic extends Mechanic {

    private String message;
    private int time;

    /**
     * Default constructor for serializer.
     */
    public ActionBarMechanic() {
    }

    public ActionBarMechanic(String message, int time) {
        this.message = message;
        this.time = time;
    }

    @Override
    public void use0(CastData cast) {
        if (!(cast.getTarget() instanceof Player player))
            return;

        String itemTitle = cast.getItemTitle();
        ItemStack itemStack = cast.getItemStack();
        Map<String, String> tempPlaceholders = cast.getTempPlaceholders();

        // Parse and send the message to the 1 player
        // TODO this method would benefit from having access to the target list
        MiniMessage PARSER = MechanicsCore.getPlugin().message;
        Component chat = PARSER.deserialize(PlaceholderAPI.applyPlaceholders(message, player, itemStack, itemTitle, null, tempPlaceholders));
        Audience audience = MechanicsCore.getPlugin().adventure.player(player);
        audience.sendActionBar(chat);

        // Action Bars are *NOT* timed in vanilla Minecraft. To get around this,
        // we resend the action bar on a timer. Since the action bar lasts for
        // 40 ticks before fading, the interval we resend the action bar is 40
        // ticks.
        if (time > 40) {
            new BukkitRunnable() {
                int ticker = 0;

                @Override
                public void run() {
                    ticker += 40;
                    if (ticker >= time) {
                        cancel();
                        return;
                    }

                    audience.sendActionBar(chat);
                }
            }.runTaskTimer(MechanicsCore.getPlugin(), 40 - (time % 40), 40);
        }
    }

    @Override
    public String getKeyword() {
        return "Action_Bar";
    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        String message = data.of("Message").assertExists().getAdventure();
        int time = data.of("Time").assertRange(40, Integer.MAX_VALUE).getInt(40);
        return applyParentArgs(data, new ActionBarMechanic(message, time));
    }
}