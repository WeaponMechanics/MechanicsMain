package me.deecaad.weaponmechanics.weapon

import me.deecaad.weaponmechanics.weapon.trigger.TriggerType
import me.deecaad.weaponmechanics.wrappers.EntityWrapper
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class WeaponSnapshot(
    val entityWrapper: EntityWrapper,
    val weaponTitle: String,
    val weaponStack: ItemStack,
    val slot: EquipmentSlot,
    val triggerType: TriggerType,
    val dualWield: Boolean,
    val victim: EntityWrapper?,
) {
    val weaponMeta: ItemMeta by lazy { weaponStack.itemMeta!! }
}
