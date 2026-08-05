package me.deecaad.weaponmechanics.weapon.repair;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.weaponmechanics.utils.CustomTag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Global repair settings loaded from the top-level Repair section in config.yml.
 */
public final class RepairConfig implements Serializer<RepairConfig> {

    private static final Pattern KIT_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

    private final boolean enabled;
    private final Map<String, RepairKit> kits;
    private final boolean allowRepairWhileFull;
    private final boolean allowRepairWhileReloading;
    private final boolean allowRepairWhileShooting;
    private final boolean excessRepairIsWasted;

    public RepairConfig() {
        this(false, Collections.emptyMap(), false, false, false, true);
    }

    private RepairConfig(boolean enabled, Map<String, RepairKit> kits, boolean allowRepairWhileFull,
                         boolean allowRepairWhileReloading, boolean allowRepairWhileShooting,
                         boolean excessRepairIsWasted) {
        this.enabled = enabled;
        this.kits = Collections.unmodifiableMap(new LinkedHashMap<>(kits));
        this.allowRepairWhileFull = allowRepairWhileFull;
        this.allowRepairWhileReloading = allowRepairWhileReloading;
        this.allowRepairWhileShooting = allowRepairWhileShooting;
        this.excessRepairIsWasted = excessRepairIsWasted;
    }

    @Override
    public String getKeyword() {
        return "Repair";
    }

    @Override
    public boolean shouldSerialize(@NotNull SerializeData data) {
        // Weapon files also use a Repair section for Allowed_Kits. This serializer is only for the
        // single top-level section in WeaponMechanics/config.yml.
        return "Repair".equals(data.getKey());
    }

    @Override
    public @NotNull RepairConfig serialize(@NotNull SerializeData data) throws SerializerException {
        boolean enabled = data.of("Enabled").getBool().orElse(false);
        boolean allowRepairWhileFull = data.of("Allow_Repair_While_Full").getBool().orElse(false);
        boolean allowRepairWhileReloading = data.of("Allow_Repair_While_Reloading").getBool().orElse(false);
        boolean allowRepairWhileShooting = data.of("Allow_Repair_While_Shooting").getBool().orElse(false);
        boolean excessRepairIsWasted = data.of("Excess_Repair_Is_Wasted").getBool().orElse(true);

        Map<String, RepairKit> kits = new LinkedHashMap<>();
        ConfigurationSection kitsSection = data.of("Kits").get(ConfigurationSection.class).orElse(null);
        if (kitsSection != null) {
            ItemSerializer itemSerializer = new ItemSerializer();

            for (String kitName : kitsSection.getKeys(false)) {
                if (!KIT_NAME_PATTERN.matcher(kitName).matches()) {
                    throw data.exception("Kits." + kitName, "Repair kit names may only contain letters, numbers, and underscores.", "Found repair kit: " + kitName);
                }

                SerializeData kitData = data.move("Kits." + kitName);
                ItemStack item = itemSerializer.serializeWithTags(kitData.move("Item"), Map.of(CustomTag.REPAIR_KIT.getKey(), kitName));

                int repairAmount = kitData.of("Repair_Amount").assertExists().assertRange(1, null).getInt().getAsInt();
                boolean consumeItem = kitData.of("Consume_Item").getBool().orElse(true);

                kits.put(kitName, new RepairKit(kitName, item, repairAmount, consumeItem));
            }
        }

        return new RepairConfig(enabled, kits, allowRepairWhileFull, allowRepairWhileReloading, allowRepairWhileShooting, excessRepairIsWasted);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public RepairKit getKit(String name) {
        return kits.get(name);
    }

    public Map<String, RepairKit> getKits() {
        return kits;
    }

    public boolean isAllowRepairWhileFull() {
        return allowRepairWhileFull;
    }

    public boolean isAllowRepairWhileReloading() {
        return allowRepairWhileReloading;
    }

    public boolean isAllowRepairWhileShooting() {
        return allowRepairWhileShooting;
    }

    public boolean isExcessRepairWasted() {
        return excessRepairIsWasted;
    }
}
