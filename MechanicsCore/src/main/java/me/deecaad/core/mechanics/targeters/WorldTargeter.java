package me.deecaad.core.mechanics.targeters;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.CastData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;

public class WorldTargeter extends Targeter {

    private String worldName;
    private World worldCache;

    /**
     * Default constructor for serializer.
     */
    public WorldTargeter() {
    }

    public WorldTargeter(String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    public World getWorldCache() {
        return worldCache;
    }

    public void setWorldCache(World worldCache) {
        this.worldCache = worldCache;
    }

    @Override
    public boolean isEntity() {
        return true;
    }

    @Override
    public Iterator<CastData> getTargets0(CastData cast) {
        if (worldCache == null || worldName == null)
            worldCache = worldName == null ? cast.getSource().getWorld() : Bukkit.getWorld(worldName);

        // User may have typed the name of the world wrong... It is case-sensitive
        if (worldCache == null) {
            MechanicsCore.debug.warn("There was an error getting the world for '" + worldName  + "'");
            return Collections.emptyIterator();
        }

        // Loop through every living entity in the world
        Iterator<LivingEntity> entityIterator = worldCache.getLivingEntities().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return entityIterator.hasNext();
            }

            @Override
            public CastData next() {
                cast.setTargetEntity(entityIterator.next());
                return cast;
            }
        };
    }

    @Override
    public String getKeyword() {
        return "World";
    }

    @Nullable
    @Override
    public String getWikiLink() {
        return "https://github.com/WeaponMechanics/MechanicsMain/wiki/WorldTargeter";
    }

    @NotNull
    @Override
    public Targeter serialize(@NotNull SerializeData data) throws SerializerException {
        String worldName = data.of("World").assertType(String.class).get(null);
        return applyParentArgs(data, new WorldTargeter(worldName));
    }

    /**
     * Returns <code>true</code> if this targeter uses the default values. This
     * is checked in the {@link me.deecaad.core.mechanics.PlayerEffectMechanicList}
     * to determine if a mechanic is eligible to have its targeters cached for
     * improved performance.
     *
     * @return true if this has default values.
     */
    public boolean isDefaultValues() {
        return !isEye() && getOffset() == null && worldName == null;
    }
}
