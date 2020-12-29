package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.MaterialHelper;
import me.deecaad.core.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Sticky implements Serializer<Sticky> {

    private boolean stickToAnyBlock;
    private boolean stickToAnyEntity;
    private boolean allowStickToEntitiesAfterStickBlock;
    private StickyData blocks, entities;

    /**
     * Empty for serializers
     */
    public Sticky() { }

    public Sticky(boolean stickToAnyBlock, boolean stickToAnyEntity, StickyData blocks, StickyData entities, boolean allowStickToEntitiesAfterStickBlock) {
        this.stickToAnyBlock = stickToAnyBlock;
        this.stickToAnyEntity = stickToAnyEntity;
        this.blocks = blocks;
        this.entities = entities;
        this.allowStickToEntitiesAfterStickBlock = allowStickToEntitiesAfterStickBlock;
    }

    public boolean canStick(Material material, byte data) {
        return stickToAnyBlock
                || (blocks != null && blocks.canStick((CompatibilityAPI.getVersion() >= 1.13 ? material.name() : material.name() + ":" + data)));
    }

    public boolean canStick(EntityType entityType) {
        return stickToAnyEntity
                || (entities != null && entities.canStick(entityType.name()));
    }

    public boolean isAllowStickToEntitiesAfterStickBlock() {
        return allowStickToEntitiesAfterStickBlock;
    }

    @Override
    public String getKeyword() {
        return "Sticky";
    }

    @Override
    public Sticky serialize(File file, ConfigurationSection configurationSection, String path) {
        StickyData blocks = tryStickyData(file, configurationSection, path + ".Blocks", true);
        StickyData entities = tryStickyData(file, configurationSection, path + ".Entities", false);
        boolean stickToAnyBlock = configurationSection.getBoolean(path + ".Blocks.Stick_To_Any_Block", false);
        boolean stickToAnyEntity = configurationSection.getBoolean(path + ".Entities.Stick_To_Any_Entity", false);
        if (blocks == null && entities == null && !stickToAnyBlock && !stickToAnyEntity) {
            return null;
        }
        boolean allowStickToEntitiesAfterStickBlock = configurationSection.getBoolean(path + ".Entities.Allow_Stick_To_Entities_After_Stick_Block", false);
        return new Sticky(stickToAnyBlock, stickToAnyEntity, blocks, entities, allowStickToEntitiesAfterStickBlock);
    }

    private StickyData tryStickyData(File file, ConfigurationSection configurationSection, String path, boolean blocks) {
        List<?> list = configurationSection.getList(path + ".List");
        if (list == null || list.isEmpty()) return null;

        Set<String> setList = new HashSet<>();
        for (Object data : list) {
            String dataToUpper = data.toString().toUpperCase();
            if (blocks) { // blocks
                ItemStack itemStack;
                try {
                    itemStack = MaterialHelper.fromStringToItemStack(dataToUpper);
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            StringUtils.foundInvalid("material"),
                            StringUtils.foundAt(file, path + ".List", dataToUpper),
                            StringUtils.debugDidYouMean(dataToUpper.split(":")[0], Material.class));
                    continue;
                }
                if (CompatibilityAPI.getVersion() >= 1.13) {
                    setList.add(itemStack.getType().name());
                } else {
                    setList.add(itemStack.getType().name() + ":" + itemStack.getData().getData());
                }
            } else { // entities
                EntityType entity;
                try {
                    entity = EntityType.valueOf(dataToUpper);
                } catch (IllegalArgumentException e) {
                    debug.log(LogLevel.ERROR,
                            StringUtils.foundInvalid("entity type"),
                            StringUtils.foundAt(file, path + ".List", dataToUpper),
                            StringUtils.debugDidYouMean(dataToUpper, EntityType.class));
                    continue;
                }
                setList.add(entity.name());
            }
        }
        if (setList.isEmpty()) return null;
        boolean whitelist = configurationSection.getBoolean(path + ".Whitelist", true);
        return new StickyData(whitelist, setList);
    }

    public static class StickyData {

        private final boolean whitelist;
        private final Set<String> list;

        public StickyData(boolean whitelist, Set<String> list) {
            this.whitelist = whitelist;
            this.list = list;
        }

        public boolean canStick(String key) {
            if (!whitelist) {
                // If blacklist and list contains key
                // -> Can't stick
                return !list.contains(key);
            }
            // If whitelist and list DOES NOT contain key
            // -> Can't stick
            return list.contains(key);
        }
    }
}