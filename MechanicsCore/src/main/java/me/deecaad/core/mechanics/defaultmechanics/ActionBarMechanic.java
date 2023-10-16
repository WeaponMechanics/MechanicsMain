package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.placeholder.PlaceholderMessage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionBarMechanic extends Mechanic {

    private PlaceholderMessage message;
    private int time;

    /**
     * Default constructor for serializer.
     */
    public ActionBarMechanic() {
    }

    public ActionBarMechanic(String message, int time) {
        this.message = new PlaceholderMessage(message);
        this.time = time;
    }

    public String getMessage() {
        return message.getTemplate();
    }

    public PlaceholderMessage getPlaceholderMessage() {
        return message;
    }

    public int getTime() {
        return time;
    }

    @Override
    public void use0(CastData cast) {
        if (!(cast.getTarget() instanceof Player player))
            return;

        // Parse and send the message to the 1 player
        // TODO this method would benefit from having access to the target list
        Component component = message.replaceAndDeserialize(cast);
        Audience audience = MechanicsCore.getPlugin().adventure.player(player);
        audience.sendActionBar(component);

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

                    audience.sendActionBar(component);
                }
            }.runTaskTimer(MechanicsCore.getPlugin(), 40 - (time % 40), 40);
        }
    }

    @Override
    public String getKeyword() {
        return "Action_Bar";
    }

    @Override
    public @Nullable String getWikiLink() {
        return "https://cjcrafter.gitbook.io/mechanics/mechanics/action-bar";
    }

    @NotNull
    @Override
    public Mechanic serialize(@NotNull SerializeData data) throws SerializerException {
        String message = data.of("Message").assertExists().getAdventure();
        int time = data.of("Time").assertRange(40, Integer.MAX_VALUE).getInt(40);
        return applyParentArgs(data, new ActionBarMechanic(message, time));
    }
}