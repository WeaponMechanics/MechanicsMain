package me.deecaad.weaponmechanics.weapon.reload;

import me.deecaad.core.file.Serializer;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Ammo implements Serializer<Ammo> {

    private IAmmoType ammoType;

    /**
     * Empty constructor to be used as serializer
     */
    public Ammo() { }

    public Ammo(IAmmoType ammoType) {
        this.ammoType = ammoType;
    }

    /**
     * @param entityWrapper the entity
     * @return the amount of this type ammo entity currently has
     */
    public int getAmount(IEntityWrapper entityWrapper) {
        return ammoType.getAmount(entityWrapper);
    }

    /**
     * @param entityWrapper the entity
     * @param amount the amount to remove ammo
     * @return the amount of ammo that was removed from entity
     */
    public int remove(IEntityWrapper entityWrapper, int amount) {
        return ammoType.remove(entityWrapper, amount);
    }

    /**
     * @param entityWrapper the entity
     * @param amount the amount of ammo to give for entity
     */
    public void give(IEntityWrapper entityWrapper, int amount) {
        ammoType.give(entityWrapper, amount);
    }

    @Override
    public Set<String> allowOtherSerializers() {
        return new HashSet<>(Arrays.asList("Magazine", "Ammo", "Exp_Cost"));
    }

    @Override
    public String getKeyword() {
        return "Ammo";
    }

    @Override
    public Ammo serialize(File file, ConfigurationSection configurationSection, String path) {


        return null;
    }
}