package me.deecaad.weaponmechanics.weapon.repair;

import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.IValidator;
import me.deecaad.core.file.SearchMode;
import me.deecaad.core.file.SearcherFilter;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.WeaponMechanics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validates the per-weapon Repair.Allowed_Kits section.
 */
@SearcherFilter(SearchMode.ON_DEMAND)
public class RepairValidator implements IValidator {

    @Override
    public String getKeyword() {
        return "Repair";
    }

    @Override
    public List<String> getAllowedPaths() {
        return Collections.singletonList(".Repair");
    }

    @Override
    public void validate(Configuration configuration, SerializeData data) throws SerializerException {
        List<?> configuredKits = data.of("Allowed_Kits").get(List.class).orElse(Collections.emptyList());
        List<String> allowedKits = new ArrayList<>(configuredKits.size());

        RepairConfig repairConfig = WeaponMechanics.getInstance().getConfiguration().getObject("Repair", RepairConfig.class);

        for (Object value : configuredKits) {
            if (!(value instanceof String kitName)) {
                throw data.exception("Allowed_Kits", "Every Allowed_Kits entry must be a repair-kit name.", "Found value: " + value);
            }

            if (repairConfig == null || repairConfig.getKit(kitName) == null) {
                throw data.exception("Allowed_Kits", "Unknown repair kit '" + kitName + "'.", "Define it under Repair.Kits in WeaponMechanics/config.yml first.");
            }

            allowedKits.add(kitName);
        }

        configuration.set(data.getKey() + ".Allowed_Kits", List.copyOf(allowedKits));
    }
}
