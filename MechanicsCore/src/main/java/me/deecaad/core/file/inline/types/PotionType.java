package me.deecaad.core.file.inline.types;

import me.deecaad.core.file.inline.ArgumentType;
import me.deecaad.core.file.inline.InlineException;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class PotionType implements ArgumentType<PotionEffectType> {

    public PotionType() {
    }

    @Override
    public PotionEffectType serialize(String str) throws InlineException {
        return PotionEffectType.getByName(str.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public String example() {
        return "poison";
    }
}
