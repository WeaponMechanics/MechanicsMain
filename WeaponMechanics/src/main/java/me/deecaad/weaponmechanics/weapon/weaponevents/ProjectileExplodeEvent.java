package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.BlockRegenSorter;
import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProjectileExplodeEvent extends ProjectileEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private List<Block> blocks;
    private BlockRegenSorter sorter;
    private DoubleMap<LivingEntity> entities;
    private boolean isCancelled;

    public ProjectileExplodeEvent(WeaponProjectile projectile, List<Block> blocks,
                                  BlockRegenSorter sorter, DoubleMap<LivingEntity> entities) {
        super(projectile);

        this.blocks = blocks;
        this.sorter = sorter;
        this.entities = entities;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public BlockRegenSorter getSorter() {
        return sorter;
    }

    public void setSorter(BlockRegenSorter sorter) {
        this.sorter = sorter;
    }

    public DoubleMap<LivingEntity> getEntities() {
        return entities;
    }

    public void setEntities(DoubleMap<LivingEntity> entities) {
        this.entities = entities;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
