package me.deecaad.weaponmechanics.weapon.shoot;

import me.deecaad.weaponmechanics.utils.CustomTag;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponSelectiveFireChangeEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public enum SelectiveFireState {

    SINGLE(0),
    BURST(1),
    AUTO(2);

    private final int id;

    SelectiveFireState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    /**
     * Helper function to set item's selective fire state and call a
     * {@link me.deecaad.weaponmechanics.weapon.weaponevents.WeaponSelectiveFireChangeEvent}.
     */
    public static void setState(EntityWrapper entity, String weaponTitle, ItemStack item, SelectiveFireState oldState, SelectiveFireState newState) {
        WeaponSelectiveFireChangeEvent event = new WeaponSelectiveFireChangeEvent(weaponTitle, item, entity.getEntity(), oldState, newState);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        CustomTag.SELECTIVE_FIRE.setInteger(item, event.getNewState().id);
    }
}