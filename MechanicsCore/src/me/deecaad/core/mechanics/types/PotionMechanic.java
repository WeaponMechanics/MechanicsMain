package me.deecaad.core.mechanics.types;

import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.casters.MechanicCaster;
import me.deecaad.core.mechanics.serialization.SerializerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

import static me.deecaad.core.MechanicsCore.debug;

@SerializerData(name = "potion", args = {"type~STRING~potion", "duration~INTEGER~dur", "amplifier~INTEGER~amp,level", "showParticles~BOOLEAN~particles"})
public class PotionMechanic extends Mechanic {

    private PotionEffect potion;
    
    /**
     * Default constructor for serializer
     */
    public PotionMechanic() {
    }

    public PotionEffect getPotion() {
        return potion;
    }

    public void setPotion(PotionEffect potion) {
        this.potion = potion;
    }

    @Override
    public Mechanic serialize(Map<String, Object> data) {

        String type = (String) data.getOrDefault("type", "POISON");
        int duration = (int) data.getOrDefault("duration", 0);
        int amplifier = (int) data.getOrDefault("amplifier", 0);
        boolean showParticles = (boolean) data.getOrDefault("showParticle", false);

        PotionEffectType effectType = PotionEffectType.getByName(type.trim().toUpperCase());
        if (effectType == null) {
            debug.error("Invalid potion effect: " + type);
        }

        setPotion(new PotionEffect(effectType, duration, amplifier, true, showParticles));
        return super.serialize(data);
    }

    @Override
    public void cast(MechanicCaster caster, Location target) {
        // Do nothing
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

    @Override
    public String toString() {
        return "PotionMechanic{" +
                "potion=" + potion +
                ", delay=" + delay +
                ", repeatAmount=" + repeatAmount +
                ", repeatInterval=" + repeatInterval +
                '}';
    }
}
