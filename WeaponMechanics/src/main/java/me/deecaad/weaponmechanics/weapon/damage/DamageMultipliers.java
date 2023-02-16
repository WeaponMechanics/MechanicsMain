package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DamageMultipliers implements Serializer<DamageMultipliers> {

    private double min;
    private double max;
    private Map<EntityType, Double> perEntity;
    private Map<PotionEffectType, Double> perPotion;
    private boolean perDamagePoint;
    private Map<Material, Double> perArmor;
    private double perArmorAttribute;
    private Map<Enchantment, List<Double>> perEnchantment;
    private double sneaking;
    private double walking;
    private double swimming;
    private double sprinting;
    private double inMidair;
    private DamageDropoff dropoff;

    /**
     * Default constructor for serializer.
     */
    public DamageMultipliers() {
    }

    public DamageMultipliers(double min, double max, Map<EntityType, Double> perEntity, Map<PotionEffectType, Double> perPotion,
                             boolean perDamagePoint, Map<Material, Double> perArmor, double perArmorAttribute,
                             Map<Enchantment, List<Double>> perEnchantment, double sneaking, double walking, double swimming, double sprinting, double inMidair) {
        this.min = min;
        this.max = max;
        this.perEntity = perEntity;
        this.perPotion = perPotion;
        this.perDamagePoint = perDamagePoint;
        this.perArmor = perArmor;
        this.perArmorAttribute = perArmorAttribute;
        this.perEnchantment = perEnchantment;
        this.sneaking = sneaking;
        this.walking = walking;
        this.swimming = swimming;
        this.sprinting = sprinting;
        this.inMidair = inMidair;
    }

    @Override
    public String getKeyword() {
        return "Damage_Multipliers";
    }

    @NotNull
    @Override
    public DamageMultipliers serialize(SerializeData data) throws SerializerException {
        return null;
    }
}
