package me.deecaad.weaponmechanics.weapon.projectile;

import me.deecaad.core.file.Serializer;
import me.deecaad.weaponmechanics.compatibility.projectile.HitBox;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.io.File;

public class Sticky implements Serializer<Sticky> {

    private boolean allowStickToEntitiesAfterStickBlock;
    private ProjectileListData<Material> blocks;
    private ProjectileListData<EntityType> entities;

    /**
     * Empty for serializers
     */
    public Sticky() { }

    public Sticky(boolean allowStickToEntitiesAfterStickBlock,
                  ProjectileListData<Material> blocks, ProjectileListData<EntityType> entities) {
        this.allowStickToEntitiesAfterStickBlock = allowStickToEntitiesAfterStickBlock;
        this.blocks = blocks;
        this.entities = entities;
    }

    public boolean canStick(Material material) {
        return blocks != null && blocks.isValid(material) != null;
    }

    public boolean canStick(EntityType entityType) {
        return entities != null && entities.isValid(entityType) != null;
    }

    public boolean isAllowStickToEntitiesAfterStickBlock() {
        return allowStickToEntitiesAfterStickBlock;
    }

    /**
     * @return true if was able to stick
     */
    public boolean handleSticky(CustomProjectile customProjectile, CollisionData collision) {
        Block block = collision.getBlock();
        if (block != null) {
            return canStick(block.getType())
                    && customProjectile.setStickedData(new StickedData(block.getLocation(), collision.getHitLocation()));
        }
        LivingEntity livingEntity = collision.getLivingEntity();
        return canStick(livingEntity.getType())
                && customProjectile.setStickedData(new StickedData(livingEntity, collision.getHitLocation()));
    }

    public boolean updateProjectileLocation(CustomProjectile customProjectile, Vector location, Vector lastLocation,
                                            Collisions throughCollisions, Collisions bouncyCollisions, HitBox projectileBox) {
        StickedData stickedData = customProjectile.getStickedData();
        Vector newLoc = stickedData.getNewLocation();
        if (newLoc == null) { // If this is null, either entity is dead or block isn't there anymore
            customProjectile.setStickedData(null);
            return false;
        } else {
            if (stickedData.getLivingEntity() != null) {
                customProjectile.addDistanceTravelled(location.distance(lastLocation));
            }

            customProjectile.setLocation(newLoc);

            Projectile projectile = customProjectile.getProjectileSettings();

            if (stickedData.isBlockStick() && projectile.getSticky().isAllowStickToEntitiesAfterStickBlock()) {

                // Stick to new entity if possible

                projectileBox.update(location, projectile.getProjectileWidth(), projectile.getProjectileHeight());

                CollisionData entityInBox = projectileBox.getEntityInBox(customProjectile.getWorld(),
                        entity -> entity.getEntityId() == customProjectile.getShooter().getEntityId()
                                || !projectile.getSticky().canStick(entity.getType()));
                if (entityInBox != null
                        && (throughCollisions == null || !throughCollisions.contains(entityInBox))
                        && (bouncyCollisions == null || !bouncyCollisions.contains(entityInBox))) {

                    if (!customProjectile.handleEntityHit(entityInBox, new Vector(0, 0, 0))) {
                        handleSticky(customProjectile, entityInBox);
                    }
                }
            }

            Block blockAtLocation = customProjectile.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            if (blockAtLocation.isEmpty()) {
                customProjectile.setLastKnownAirLocation(location.clone().toLocation(customProjectile.getWorld()));
            }

            customProjectile.updateDisguiseLocationAndMotion();
            return true;
        }
    }

    @Override
    public String getKeyword() {
        return "Sticky";
    }

    @Override
    public Sticky serialize(File file, ConfigurationSection configurationSection, String path) {
        ProjectileListData<Material> blocks = new ProjectileListData<Material>().serialize(Material.class, file, configurationSection, path + ".Blocks");
        ProjectileListData<EntityType> entities = new ProjectileListData<EntityType>().serialize(EntityType.class, file, configurationSection, path + ".Entities");

        if (blocks == null && entities == null) {
            return null;
        }

        boolean allowStickToEntitiesAfterStickBlock = configurationSection.getBoolean(path + ".Entities.Allow_Stick_To_Entities_After_Stick_Block", false);
        return new Sticky(allowStickToEntitiesAfterStickBlock, blocks, entities);
    }
}