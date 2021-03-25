package me.deecaad.weaponmechanics.weapon.weaponevents;

import me.deecaad.core.utils.primitive.DoubleMap;
import me.deecaad.weaponmechanics.weapon.explode.regeneration.BlockRegenSorter;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class ProjectileExplodeEvent {

    private List<Block> blocks;
    private BlockRegenSorter sorter;
    private DoubleMap<LivingEntity> entities;

    public ProjectileExplodeEvent(List<Block> blocks, BlockRegenSorter sorter, DoubleMap<LivingEntity> entities) {
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
}
