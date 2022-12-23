package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.file.inline.InlineSerializer;
import me.deecaad.core.mechanics.CastData;

import java.util.List;

/**
 * A targeter returns a list of targets. A target can be a {@link org.bukkit.Location},
 * an {@link org.bukkit.entity.Entity}, or a {@link org.bukkit.entity.Player}.
 */
public abstract class Targeter extends InlineSerializer<Targeter> {

    public abstract List<CastData> getTargets(CastData cast);
}
