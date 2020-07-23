package me.deecaad.core.mechanics.types;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.MechanicCaster;
import me.deecaad.core.mechanics.serialization.Argument;
import me.deecaad.core.mechanics.serialization.datatypes.DataType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

public class PotionMechanic extends Mechanic {

    private PotionEffect potion;

    public PotionMechanic() {
        super("potion",
                new Argument("type", DataType.STRING, "potion"),
                new Argument("duration", DataType.INTEGER, "dur"),
                new Argument("amplifier", DataType.INTEGER, "amp", "level"),
                new Argument("showParticles", DataType.BOOLEAN, "particles")
        );
    }

    public PotionEffect getPotion() {
        return potion;
    }

    public void setPotion(PotionEffect potion) {
        this.potion = potion;
    }

    @Override
    public Mechanic serialize(Map<String, Object> data) {

        String type = (String) data.get("type");
        int duration = (int) data.get("duration");
        int amplifier = (int) data.get("amplifier");
        boolean showParticles = (boolean) data.get("showParticle");

        PotionEffectType effectType = PotionEffectType.getByName(type.trim().toUpperCase());
        if (effectType == null) {
            debug.error("Invalid potion effect: " + type);
        }

        setPotion(new PotionEffect(effectType, duration, amplifier, true, showParticles));
        return this;
    }

    @Override
    public void cast(MechanicCaster caster, Location target) {
    }

    @Override
    public void cast(MechanicCaster caster, Entity target) {
        if (target.getType().isAlive()) {
            LivingEntity living = (LivingEntity) target;

            living.addPotionEffect(potion);
        }
    }

    @Override
    public void cast(MechanicCaster caster, Player target) {
        target.addPotionEffect(potion);
    }
}
