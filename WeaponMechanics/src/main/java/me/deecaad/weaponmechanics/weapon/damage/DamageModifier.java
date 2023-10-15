package me.deecaad.weaponmechanics.weapon.damage;

import me.deecaad.core.file.*;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.primitive.DoubleEntry;
import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DamageModifier implements Serializer<DamageModifier> {

    // For clamping bounds
    private double min;
    private double max;

    // Armor modifiers
    private double perArmorPoint;
    private DoubleMap<Material> armorModifiers;
    private DoubleMap<Enchantment> enchantmentModifiers;

    // DamagePoint modifiers
    private double headModifier;
    private double bodyModifier;
    private double armsModifier;
    private double legsModifier;
    private double feetModifier;
    private double backModifier;

    // Movement modifiers
    private double sneakingModifier;
    private double walkingModifier;
    private double swimmingModifier;
    private double sprintingModifier;
    private double inMidairModifier;

    // Misc modifiers
    private double shieldModifier;
    private DoubleMap<EntityType> entityTypeModifiers;
    private DoubleMap<PotionEffectType> potionEffectModifiers;


    /**
     * Default constructor for serializer
     */
    public DamageModifier() {
    }

    public DamageModifier(double min, double max, double perArmorPoint, DoubleMap<Material> armorModifiers, DoubleMap<Enchantment> enchantmentModifiers,
                          double headModifier, double bodyModifier, double armsModifier, double legsModifier, double feetModifier, double backModifier,
                          double sneakingModifier, double walkingModifier, double swimmingModifier, double sprintingModifier, double inMidairModifier,
                          double shieldModifier, DoubleMap<EntityType> entityTypeModifiers, DoubleMap<PotionEffectType> potionEffectModifiers) {
        this.min = min;
        this.max = max;
        this.perArmorPoint = perArmorPoint;
        this.armorModifiers = armorModifiers;
        this.enchantmentModifiers = enchantmentModifiers;
        this.headModifier = headModifier;
        this.bodyModifier = bodyModifier;
        this.armsModifier = armsModifier;
        this.legsModifier = legsModifier;
        this.feetModifier = feetModifier;
        this.backModifier = backModifier;
        this.sneakingModifier = sneakingModifier;
        this.walkingModifier = walkingModifier;
        this.swimmingModifier = swimmingModifier;
        this.sprintingModifier = sprintingModifier;
        this.inMidairModifier = inMidairModifier;
        this.shieldModifier = shieldModifier;
        this.entityTypeModifiers = entityTypeModifiers;
        this.potionEffectModifiers = potionEffectModifiers;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getPerArmorPoint() {
        return perArmorPoint;
    }

    public void setPerArmorPoint(double perArmorPoint) {
        this.perArmorPoint = perArmorPoint;
    }

    public DoubleMap<Material> getArmorModifiers() {
        return armorModifiers;
    }

    public void setArmorModifiers(DoubleMap<Material> armorModifiers) {
        this.armorModifiers = armorModifiers;
    }

    public DoubleMap<Enchantment> getEnchantmentModifiers() {
        return enchantmentModifiers;
    }

    public void setEnchantmentModifiers(DoubleMap<Enchantment> enchantmentModifiers) {
        this.enchantmentModifiers = enchantmentModifiers;
    }

    public double getHeadModifier() {
        return headModifier;
    }

    public void setHeadModifier(double headModifier) {
        this.headModifier = headModifier;
    }

    public double getBodyModifier() {
        return bodyModifier;
    }

    public void setBodyModifier(double bodyModifier) {
        this.bodyModifier = bodyModifier;
    }

    public double getArmsModifier() {
        return armsModifier;
    }

    public void setArmsModifier(double armsModifier) {
        this.armsModifier = armsModifier;
    }

    public double getLegsModifier() {
        return legsModifier;
    }

    public void setLegsModifier(double legsModifier) {
        this.legsModifier = legsModifier;
    }

    public double getFeetModifier() {
        return feetModifier;
    }

    public void setFeetModifier(double feetModifier) {
        this.feetModifier = feetModifier;
    }

    public double getBackModifier() {
        return backModifier;
    }

    public void setBackModifier(double backModifier) {
        this.backModifier = backModifier;
    }

    public double getSneakingModifier() {
        return sneakingModifier;
    }

    public void setSneakingModifier(double sneakingModifier) {
        this.sneakingModifier = sneakingModifier;
    }

    public double getWalkingModifier() {
        return walkingModifier;
    }

    public void setWalkingModifier(double walkingModifier) {
        this.walkingModifier = walkingModifier;
    }

    public double getSwimmingModifier() {
        return swimmingModifier;
    }

    public void setSwimmingModifier(double swimmingModifier) {
        this.swimmingModifier = swimmingModifier;
    }

    public double getSprintingModifier() {
        return sprintingModifier;
    }

    public void setSprintingModifier(double sprintingModifier) {
        this.sprintingModifier = sprintingModifier;
    }

    public double getInMidairModifier() {
        return inMidairModifier;
    }

    public void setInMidairModifier(double inMidairModifier) {
        this.inMidairModifier = inMidairModifier;
    }

    public DoubleMap<EntityType> getEntityTypeModifiers() {
        return entityTypeModifiers;
    }

    public void setEntityTypeModifiers(DoubleMap<EntityType> entityTypeModifiers) {
        this.entityTypeModifiers = entityTypeModifiers;
    }

    public DoubleMap<PotionEffectType> getPotionEffectModifiers() {
        return potionEffectModifiers;
    }

    public void setPotionEffectModifiers(DoubleMap<PotionEffectType> potionEffectModifiers) {
        this.potionEffectModifiers = potionEffectModifiers;
    }

    /**
     * Accumulates each rate that should be applied to the damage. The result
     * is clamped between {@link #getMin()} and {@link #getMax()}.
     *
     * @param wrapper    The victim being damaged.
     * @param point      Where the victim was hit.
     * @param isBackStab If the hit came from behind.
     * @return The clamped rate to multiply damage by. Defaults to 1.0.
     */
    public double getRate(@NotNull EntityWrapper wrapper, @Nullable DamagePoint point, boolean isBackStab) {
        LivingEntity victim = wrapper.getEntity();
        double rate = 1.0;

        // 95% of weapons should use this, since it is an "all encompassing" value.
        // Since armor may have custom amounts of protection, this attribute covers that.
        AttributeInstance armorAttribute = victim.getAttribute(Attribute.GENERIC_ARMOR);
        if (armorAttribute != null) {
            rate += perArmorPoint * armorAttribute.getValue();
        }

        // If admins want diamond armor to be invulnerable to damage, this is where they do it.
        // This is also where we check enchantment damage contributions
        EntityEquipment equipment = victim.getEquipment();
        if ((enchantmentModifiers != null || armorModifiers != null) && equipment != null) {
            for (ItemStack armor : equipment.getArmorContents()) {
                if (armor == null)
                    continue;

                // Armor type (DIAMOND, CHAIN, GOLD, etc.)
                if (armorModifiers != null)
                    rate += armorModifiers.get(armor.getType());

                // Enchantments (PROTECTION 4, PROJECTILE PROJECTION, etc)
                if (enchantmentModifiers != null) {
                    for (Map.Entry<Enchantment, Integer> entry : armor.getEnchantments().entrySet()) {
                        rate += enchantmentModifiers.get(entry.getKey()) * entry.getValue();
                    }
                }
            }
        }

        // Headshots do more damage than feet shots
        if (point != null) {
            rate += switch (point) {
                case HEAD -> headModifier;
                case BODY -> bodyModifier;
                case ARMS -> armsModifier;
                case LEGS -> legsModifier;
                case FEET -> feetModifier;
            };
        }

        // Backstabs hurt more
        if (isBackStab)
            rate += backModifier;

        // Running targets take more damage than stationary "braced" targets
        if (wrapper.isSneaking())
            rate += sneakingModifier;
        if (wrapper.isWalking())
            rate += walkingModifier;
        if (wrapper.isSwimming())
            rate += swimmingModifier;
        if (wrapper.isSprinting())
            rate += sprintingModifier;
        if (wrapper.isInMidair())
            rate += inMidairModifier;

        // For the shield modifier, the player must be blocking with a shield
        // and be facing the bullet.
        if (!isBackStab && wrapper.getEntity() instanceof Player player && player.isBlocking())
            rate += shieldModifier;

        // Do double damage to zombies, half damage to players (PVE scenario), for example
        if (entityTypeModifiers != null) {
            rate += entityTypeModifiers.get(victim.getType());
        }

        // Let potion effects contribute to the damage dealt
        if (potionEffectModifiers != null) {
            for (DoubleEntry<PotionEffectType> entry : potionEffectModifiers.entrySet()) {
                if (victim.hasPotionEffect(entry.getKey()))
                    rate += entry.getValue();
            }
        }

        // Clamp the rate within bounds
        return NumberUtil.minMax(min, rate, max);
    }

    public double clamp(double rate) {
        return NumberUtil.minMax(min, rate, max);
    }

    public double applyRates(double damage, EntityWrapper wrapper, DamagePoint point, boolean isBackStab) {
        return damage * getRate(wrapper, point, isBackStab);
    }

    @Override
    public String getKeyword() {
        return "Damage_Modifiers";
    }

    @NotNull
    @Override
    public DamageModifier serialize(@NotNull SerializeData data) throws SerializerException {
        double min = serializePercentage(data.of("Min"), "20%");
        double max = serializePercentage(data.of("Max"), "1000%");

        double perArmorPoint = serializePercentage(data.of("Per_Armor_Point"));

        // Per material armor modifiers
        DoubleMap<Material> armorModifiers = new DoubleMap<>();
        List<String[]> armorSplitList = data.ofList("Armor")
                .addArgument(Material.class, true)
                .addArgument(String.class, true).assertList().get();
        for (int i = 0; i < armorSplitList.size(); i++) {
            String[] split = armorSplitList.get(i);

            List<Material> armorMaterial = EnumUtil.parseEnums(Material.class, split[0]);
            double percentage = stringToDouble(split[1], data.ofList("Armor").getLocation(i));
            for (Material material : armorMaterial) {
                armorModifiers.put(material, percentage);
            }
        }

        // Per enchantment armor modifiers
        DoubleMap<Enchantment> enchantmentModifiers = new DoubleMap<>();
        List<String[]> enchantmentSplitList = data.ofList("Enchantments")
                .addArgument(Enchantment.class, true, true)
                .addArgument(String.class, true).assertList().get();
        for (int i = 0; i < enchantmentSplitList.size(); i++) {
            String[] split = enchantmentSplitList.get(i);

            // First try to get by key. If that fails, get by name. If that fails, send error
            Enchantment enchantment = null;
            if (ReflectionUtil.getMCVersion() >= 13)
                enchantment = Enchantment.getByKey(NamespacedKey.minecraft(split[0].toLowerCase(Locale.ROOT)));
            if (enchantment == null)
                enchantment = Enchantment.getByName(split[0].toUpperCase(Locale.ROOT));
            if (enchantment == null) {
                Iterable<String> options = Arrays.stream(Enchantment.values()).map(ench -> ReflectionUtil.getMCVersion() < 13 ? ench.getName() : ench.getKey().getKey()).toList();
                throw new SerializerOptionsException(this, "Enchantment", options, split[0], data.ofList("Enchantments").getLocation(i));
            }

            double percentage = stringToDouble(split[1], data.ofList("Enchantments").getLocation(i));
            enchantmentModifiers.put(enchantment, percentage);
        }

        double headModifier = serializePercentage(data.of("Head"));
        double bodyModifier = serializePercentage(data.of("Body"));
        double armsModifier = serializePercentage(data.of("Arms"));
        double legsModifier = serializePercentage(data.of("Legs"));
        double feetModifier = serializePercentage(data.of("Feet"));
        double backModifier = serializePercentage(data.of("Back"));

        double sneakingModifier = serializePercentage(data.of("Sneaking"));
        double walkingModifier = serializePercentage(data.of("Walking"));
        double swimmingModifier = serializePercentage(data.of("Swimming"));
        double sprintingModifier = serializePercentage(data.of("Sprinting"));
        double inMidairModifier = serializePercentage(data.of("In_Midair"));

        double shieldModifier = serializePercentage(data.of("Shielding"));

        DoubleMap<EntityType> entityTypeModifiers = new DoubleMap<>();
        List<String[]> entitySplitList = data.ofList("Entities")
                .addArgument(EntityType.class, true)
                .addArgument(String.class, true).assertList().get();
        for (int i = 0; i < entitySplitList.size(); i++) {
            String[] split = entitySplitList.get(i);

            List<EntityType> entityTypes = EnumUtil.parseEnums(EntityType.class, split[0]);
            double percentage = stringToDouble(split[1], data.ofList("Armor").getLocation(i));
            for (EntityType entity : entityTypes) {
                entityTypeModifiers.put(entity, percentage);
            }
        }

        DoubleMap<PotionEffectType> potionEffectModifiers = new DoubleMap<>();
        List<String[]> potionSplitList = data.ofList("Potions")
                .addArgument(PotionEffectType.class, true, true)
                .addArgument(String.class, true).assertList().get();
        for (int i = 0; i < potionSplitList.size(); i++) {
            String[] split = potionSplitList.get(i);

            // First try to get by key. If that fails, get by name. If that fails, send error
            PotionEffectType potion = null;
            if (ReflectionUtil.getMCVersion() >= 13)
                potion = PotionEffectType.getByKey(NamespacedKey.minecraft(split[0].toLowerCase(Locale.ROOT)));
            if (potion == null)
                potion = PotionEffectType.getByName(split[0].toUpperCase(Locale.ROOT));
            if (potion == null) {
                Iterable<String> options = Arrays.stream(Enchantment.values()).map(ench -> ReflectionUtil.getMCVersion() < 13 ? ench.getName() : ench.getKey().getKey()).toList();
                throw new SerializerOptionsException(this, "Potion", options, split[0], data.ofList("Potions").getLocation(i));
            }

            double percentage = stringToDouble(split[1], data.ofList("Potions").getLocation(i));
            potionEffectModifiers.put(potion, percentage);
        }

        return new DamageModifier(min, max, perArmorPoint, armorModifiers, enchantmentModifiers, headModifier, bodyModifier, armsModifier, legsModifier, feetModifier, backModifier,
                sneakingModifier, walkingModifier, swimmingModifier, sprintingModifier, inMidairModifier, shieldModifier, entityTypeModifiers, potionEffectModifiers);
    }

    /**
     * Let's people use +-20% instead of 0.2
     */
    private static double serializePercentage(SerializeData.ConfigAccessor accessor) throws SerializerException {
        return serializePercentage(accessor, "+0%");
    }

    /**
     * Let's people use +-20% instead of 0.2
     */
    private static double serializePercentage(SerializeData.ConfigAccessor accessor, String defaultVal) throws SerializerException {
        return stringToDouble(accessor.assertType(String.class).get(defaultVal), accessor.getLocation());
    }

    private static double stringToDouble(String str, String location) throws SerializerException {
        str = str.trim(); // Trim any leading or trailing whitespace
        boolean isNegative = str.startsWith("-"); // Check if the percentage is negative
        str = str.replace("%", ""); // Remove the percentage sign
        str = str.replace("+", ""); // Remove the plus sign if present
        str = str.replace("-", ""); // Remove the minus sign if present

        try {
            double value = Double.parseDouble(str); // Parse the string as a double
            value = value / 100; // Convert to a fraction
            return isNegative ? -value : value; // Apply the sign if it was negative
        } catch (NumberFormatException e) {
            throw new SerializerTypeException("DamageModifier", Double.class, String.class, str, location);
        }
    }
}
