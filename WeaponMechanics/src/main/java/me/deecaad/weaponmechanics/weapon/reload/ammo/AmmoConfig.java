package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.weaponmechanics.weapon.trigger.Trigger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AmmoConfig implements Serializer<AmmoConfig> {

    private Mechanics outOfAmmoMechanics;
    private Trigger switchTrigger;
    private Mechanics switchMechanics;
    private List<Ammo> ammunitions;

    /**
     * Default constructor for serializer.
     */
    public AmmoConfig() {
    }

    @Override
    public String getKeyword() {
        return "Ammo";
    }

    @NotNull
    @Override
    public AmmoConfig serialize(SerializeData data) throws SerializerException {
        Mechanics mechanics = data.of("Out_Of_Ammo_Mechanics").serialize(Mechanics.class);
        Trigger switchTrigger = data.of("Switch_Ammo_Types.Trigger").serialize(Trigger.class);
        Mechanics switchMechanics = data.of("Switch_Ammo_Types.Trigger").serialize(Mechanics.class);
        List<String> ammunitionStrings = data.of("Ammos").assertType(List.class).assertExists().get();

        List<Ammo> ammunitions = new ArrayList<>(ammunitionStrings.size());
        for (String ammoTitle : ammunitionStrings) {
            ammunitions.add(AmmoRegistry.AMMO_REGISTRY.get(ammoTitle));
        }


        return new AmmoConfig();
    }
}
