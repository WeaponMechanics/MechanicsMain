package me.deecaad.weaponcompatibility.projectile;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.weaponcompatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponcompatibility.shoot.IShootCompatibility;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.weapon.projectile.CustomProjectile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

public class Projectile_Reflection implements IProjectileCompatibility {

    // REFLECTION START

    // For getNMSEntityId

    private static Method entityTypesId;

    // For spawnDisguise

    private static Method worldGetHandle;
    private static Method getId;

    private static Method getBlock;
    private static Method getIBlockData;
    private static Constructor<?> entityFallingBlock;
    private static Method getCombinedId;

    private static Constructor<?> spawnEntityPacket;

    private static Method asNMSCopy;
    private static Constructor<?> entityItem;
    private static Method getDataWatcher;
    private static Constructor<?> entityMetadataPacket;

    private static Constructor<?> headRotationPacket;

    private static Method createEntity;
    private static Constructor<?> spawnEntityLivingPacket;

    // For updateDisguise

    private static Constructor<?> entityVelocityPacket;

    private static Constructor<?> vec3d;

    private static Constructor<?> entityMoveLookPacket;

    private static Field entityTeleportId;
    private static Field entityTeleportX;
    private static Field entityTeleportY;
    private static Field entityTeleportZ;
    private static Field entityTeleportYaw;
    private static Field entityTeleportPitch;
    private static Field entityTeleportGround;

    private static Constructor<?> entityTeleportPacket;

    // For destroyDisguise

    private static Constructor<?> entityDestroyPacket;

    // For bounding boxes

    private static Method entityHandle;
    private static Method entityBoundingBox;

    private static Field minXField;
    private static Field minYField;
    private static Field minZField;
    private static Field maxXField;
    private static Field maxYField;
    private static Field maxZField;

    // For getting default width and height

    private static Method getAsBukkitEntity;

    // REFLECTION END

    public Projectile_Reflection() {
        double version = CompatibilityAPI.getVersion();

        Class<?> entityClass = ReflectionUtil.getNMSClass("Entity");

        // For getNMSEntityId
        if (version <= 1.101) {
            entityTypesId = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("EntityTypes"), "a", entityClass);
        }

        Class<?> craftWorldClass = ReflectionUtil.getCBClass("CraftWorld");
        Class<?> blockClass = ReflectionUtil.getNMSClass("Block");
        Class<?> iBlockDataClass = ReflectionUtil.getNMSClass("IBlockData");
        Class<?> worldClass = ReflectionUtil.getNMSClass("World");
        Class<?> spawnEntityClass = ReflectionUtil.getNMSClass("PacketPlayOutSpawnEntity");
        Class<?> entityVelocityClass = ReflectionUtil.getNMSClass("PacketPlayOutEntityVelocity");
        Class<?> vec3dClass = ReflectionUtil.getNMSClass("Vec3D");
        Class<?> moveLookClass = ReflectionUtil.getNMSClass("PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");
        Class<?> teleportClass = ReflectionUtil.getNMSClass("PacketPlayOutEntityTeleport");

        // For spawnDisguise
        worldGetHandle = ReflectionUtil.getMethod(craftWorldClass, "getHandle");
        getId = ReflectionUtil.getMethod(entityClass, "getId");

        getBlock = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("util.CraftMagicNumbers"), "getBlock", Material.class);
        getIBlockData = ReflectionUtil.getMethod(blockClass, "getBlockData");
        entityFallingBlock = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("EntityFallingBlock"), worldClass, double.class, double.class, double.class, iBlockDataClass);
        getCombinedId = ReflectionUtil.getMethod(blockClass, "getCombinedId", iBlockDataClass);

        spawnEntityPacket = version < 1.14 ?
                ReflectionUtil.getConstructor(spawnEntityClass, entityClass, int.class, int.class) :
                ReflectionUtil.getConstructor(spawnEntityClass, entityClass, int.class);

        asNMSCopy = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
        entityItem = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("EntityItem"), worldClass, double.class, double.class, double.class, ReflectionUtil.getNMSClass("ItemStack"));
        getDataWatcher = ReflectionUtil.getMethod(entityClass, "getDataWatcher");
        entityMetadataPacket = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutEntityMetadata"), int.class, ReflectionUtil.getNMSClass("DataWatcher"), boolean.class);

        headRotationPacket = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutEntityHeadRotation"), entityClass, byte.class);

        createEntity = ReflectionUtil.getMethod(craftWorldClass, "createEntity", Location.class, Class.class);
        spawnEntityLivingPacket = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutSpawnEntityLiving"), ReflectionUtil.getNMSClass("EntityLiving"));

        // For updateDisguise
        entityVelocityPacket = version < 1.14 ?
                ReflectionUtil.getConstructor(entityVelocityClass, int.class, double.class, double.class, double.class) :
                ReflectionUtil.getConstructor(entityVelocityClass, int.class, vec3dClass);

        if (version >= 1.14) {
            vec3d = ReflectionUtil.getConstructor(vec3dClass, double.class, double.class, double.class);
        }

        entityMoveLookPacket = version < 1.09 ?
                ReflectionUtil.getConstructor(moveLookClass, int.class, byte.class, byte.class, byte.class, byte.class, byte.class, boolean.class)
                : version < 1.14 ?
                ReflectionUtil.getConstructor(moveLookClass, int.class, long.class, long.class, long.class, byte.class, byte.class, boolean.class)
                : ReflectionUtil.getConstructor(moveLookClass, int.class, short.class, short.class, short.class, byte.class, byte.class, boolean.class);

        if (version >= 1.09) {
            entityTeleportId = ReflectionUtil.getField(teleportClass, "a");
            entityTeleportX = ReflectionUtil.getField(teleportClass, "b");
            entityTeleportY = ReflectionUtil.getField(teleportClass, "c");
            entityTeleportZ = ReflectionUtil.getField(teleportClass, "d");
            entityTeleportYaw = ReflectionUtil.getField(teleportClass, "e");
            entityTeleportPitch = ReflectionUtil.getField(teleportClass, "f");
            entityTeleportGround = ReflectionUtil.getField(teleportClass, "g");
        }

        entityTeleportPacket = version < 1.09 ?
                ReflectionUtil.getConstructor(teleportClass, int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class)
                : ReflectionUtil.getConstructor(teleportClass);

        // For destroyDisguise

        entityDestroyPacket = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutEntityDestroy"), Array.newInstance(int.class, 0).getClass());

        // For bounding boxes

        entityHandle = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("entity.CraftEntity"), "getHandle");
        entityBoundingBox = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("Entity"), "getBoundingBox");

        Class<?> axisAlignedBBClazz = ReflectionUtil.getNMSClass("AxisAlignedBB");

        minXField = ReflectionUtil.getField(axisAlignedBBClazz, "a");
        minYField = ReflectionUtil.getField(axisAlignedBBClazz, "b");
        minZField = ReflectionUtil.getField(axisAlignedBBClazz, "c");

        maxXField = ReflectionUtil.getField(axisAlignedBBClazz, "d");
        maxYField = ReflectionUtil.getField(axisAlignedBBClazz, "e");
        maxZField = ReflectionUtil.getField(axisAlignedBBClazz, "f");

        // For getting default width and height

        getAsBukkitEntity = ReflectionUtil.getMethod(ReflectionUtil.getNMSClass("Entity"), "getBukkitEntity");
    }

    @Override
    public void spawnDisguise(CustomProjectile customProjectile, Vector location, Vector motion) {
        double version = CompatibilityAPI.getVersion();

        // Calculate yaw and pitch before spawning
        customProjectile.calculateYawAndPitch();

        EntityType projectileDisguise = customProjectile.projectile.getProjectileDisguise();
        Object spawn;
        Object metadata = null;
        Object headRotation;

        World world = customProjectile.getWorld();
        float yaw = customProjectile.getProjectileDisguiseYaw();
        float pitch = customProjectile.getProjectileDisguisePitch();

        switch (projectileDisguise) {
            case FALLING_BLOCK:
                Object worldServer = ReflectionUtil.invokeMethod(worldGetHandle, world);
                Object nmsBlock = ReflectionUtil.invokeMethod(getBlock, null, customProjectile.projectile.getProjectileStack().getType());
                Object nmsIBlockData = ReflectionUtil.invokeMethod(getIBlockData, nmsBlock);
                Object nmsEntityFallingBlock = ReflectionUtil.newInstance(entityFallingBlock, worldServer, location.getX(), location.getY(), location.getZ(), nmsIBlockData);

                customProjectile.setProjectileDisguiseId((int) ReflectionUtil.invokeMethod(getId, nmsEntityFallingBlock));

                if (version < 1.14) {
                    spawn = ReflectionUtil.newInstance(spawnEntityPacket, nmsEntityFallingBlock, getNMSEntityId(version, nmsEntityFallingBlock), ReflectionUtil.invokeMethod(getCombinedId, null, nmsIBlockData));
                } else {
                    spawn = ReflectionUtil.newInstance(spawnEntityPacket, nmsEntityFallingBlock, ReflectionUtil.invokeMethod(getCombinedId, null, nmsIBlockData));
                }
                headRotation = ReflectionUtil.newInstance(headRotationPacket, nmsEntityFallingBlock, convertYawToByte(customProjectile, yaw));

                customProjectile.projectileDisguiseNMSEntity = nmsEntityFallingBlock;
                break;
            case DROPPED_ITEM:
                worldServer = ReflectionUtil.invokeMethod(worldGetHandle, world);
                Object nmsStack = ReflectionUtil.invokeMethod(asNMSCopy, null, customProjectile.projectile.getProjectileStack());
                Object nmsEntityItem = ReflectionUtil.newInstance(entityItem, worldServer, location.getX(), location.getY(), location.getZ(), nmsStack);

                customProjectile.setProjectileDisguiseId((int) ReflectionUtil.invokeMethod(getId, nmsEntityItem));

                if (version < 1.14) {
                    spawn = ReflectionUtil.newInstance(spawnEntityPacket, nmsEntityItem, getNMSEntityId(version, nmsEntityItem), 1);
                } else {
                    spawn = ReflectionUtil.newInstance(spawnEntityPacket, nmsEntityItem, 1);
                }

                metadata = ReflectionUtil.newInstance(entityMetadataPacket, customProjectile.getProjectileDisguiseId(), ReflectionUtil.invokeMethod(getDataWatcher, nmsEntityItem), false);
                headRotation = ReflectionUtil.newInstance(headRotationPacket, nmsEntityItem, convertYawToByte(customProjectile, yaw));

                customProjectile.projectileDisguiseNMSEntity = nmsEntityItem;
                break;
            default:
                Object nmsEntity = ReflectionUtil.invokeMethod(createEntity, world, location.toLocation(world, yaw, pitch), projectileDisguise.getEntityClass());

                customProjectile.setProjectileDisguiseId((int) ReflectionUtil.invokeMethod(getId, nmsEntity));

                if (projectileDisguise.isAlive()) {
                    spawn = ReflectionUtil.newInstance(spawnEntityLivingPacket, nmsEntity);
                } else {
                    if (version < 1.14) {
                        spawn = ReflectionUtil.newInstance(spawnEntityPacket, nmsEntity, getNMSEntityId(version, nmsEntity), 1);
                    } else {
                        spawn = ReflectionUtil.newInstance(spawnEntityPacket, nmsEntity, 1);
                    }
                }
                headRotation = ReflectionUtil.newInstance(headRotationPacket, nmsEntity, convertYawToByte(customProjectile, yaw));

                customProjectile.projectileDisguiseNMSEntity = nmsEntity;
                break;
        }

        if (metadata == null) {
            sendUpdatePackets(customProjectile, 22500, spawn, headRotation);
        } else {
            sendUpdatePackets(customProjectile, 22500, spawn, metadata, headRotation);
        }
        updateDisguise(customProjectile, location, motion, location);
    }

    @Override
    public void updateDisguise(CustomProjectile customProjectile, Vector location, Vector motion, Vector lastLocation) {
        double version = CompatibilityAPI.getVersion();

        // Calculate yaw and pitch before doing updates
        customProjectile.calculateYawAndPitch();

        int projectileDisguiseId = customProjectile.getProjectileDisguiseId();
        float yaw = customProjectile.getProjectileDisguiseYaw();
        float pitch = customProjectile.getProjectileDisguisePitch();

        Object velocity;
        if (version < 1.14) {
            velocity = ReflectionUtil.newInstance(entityVelocityPacket, projectileDisguiseId, motion.getX(), motion.getY(), motion.getZ());
        } else {
            velocity = ReflectionUtil.newInstance(entityVelocityPacket, projectileDisguiseId, ReflectionUtil.newInstance(vec3d, motion.getX(), motion.getY(), motion.getZ()));
        }

        Object move;
        if (version < 1.09) {
            // https://wiki.vg/Data_types#Fixed-point_numbers
            if (customProjectile.getMotionLength() > 4) {
                int x = floor(location.getX() * 32);
                int y = floor(location.getY() * 32);
                int z = floor(location.getZ() * 32);

                move = ReflectionUtil.newInstance(entityTeleportPacket, projectileDisguiseId, x, y, z, convertYawToByte(customProjectile, yaw), convertPitchToByte(customProjectile, pitch), false);
            } else {
                byte x = (byte) floor((location.getX() - lastLocation.getX()) * 32);
                byte y = (byte) floor((location.getY() - lastLocation.getY()) * 32);
                byte z = (byte) floor((location.getZ() - lastLocation.getZ()) * 32);

                move = ReflectionUtil.newInstance(entityMoveLookPacket, projectileDisguiseId, x, y, z, convertYawToByte(customProjectile, yaw), convertPitchToByte(customProjectile, pitch), false);
            }
        } else if (customProjectile.getMotionLength() > 8) { // 1.9 -> newer
            move = ReflectionUtil.newInstance(entityTeleportPacket);
            ReflectionUtil.setField(entityTeleportId, move, projectileDisguiseId);
            ReflectionUtil.setField(entityTeleportX, move, location.getX());
            ReflectionUtil.setField(entityTeleportY, move, location.getY());
            ReflectionUtil.setField(entityTeleportZ, move, location.getZ());
            ReflectionUtil.setField(entityTeleportYaw, move, convertYawToByte(customProjectile, yaw));
            ReflectionUtil.setField(entityTeleportPitch, move, convertPitchToByte(customProjectile, pitch));
            ReflectionUtil.setField(entityTeleportGround, move, false);
        } else {
            // (currentX * 32 - prevX * 32) * 128
            short x = (short) ((location.getX() * 32 - lastLocation.getX() * 32) * 128);
            short y = (short) ((location.getY() * 32 - lastLocation.getY() * 32) * 128);
            short z = (short) ((location.getZ() * 32 - lastLocation.getZ() * 32) * 128);

            if (version < 1.14) {
                move = ReflectionUtil.newInstance(entityMoveLookPacket, projectileDisguiseId, (long) x, (long) y, (long) z, convertYawToByte(customProjectile, yaw), convertPitchToByte(customProjectile, pitch), false);
            } else {
                move = ReflectionUtil.newInstance(entityMoveLookPacket, projectileDisguiseId, x, y, z, convertYawToByte(customProjectile, yaw), convertPitchToByte(customProjectile, pitch), false);
            }
        }
        sendUpdatePackets(customProjectile, 8050, velocity, move);
    }

    @Override
    public void destroyDisguise(CustomProjectile customProjectile) {
        Object destroy = ReflectionUtil.newInstance(entityDestroyPacket, new int[]{ customProjectile.getProjectileDisguiseId() });

        sendUpdatePackets(customProjectile, 22500, destroy);
    }

    @Override
    public double[] getDefaultWidthAndHeight(EntityType entityType) {
        World world = Bukkit.getWorlds().get(0);
        Location location = new Location(world, 1, 100, 1);
        Object nmsEntity = ReflectionUtil.invokeMethod(createEntity, world, location, entityType.getEntityClass());
        org.bukkit.entity.Entity entity = (org.bukkit.entity.Entity) ReflectionUtil.invokeMethod(getAsBukkitEntity, nmsEntity);
        IShootCompatibility shootCompatibility = WeaponCompatibilityAPI.getShootCompatibility();
        return new double[]{ shootCompatibility.getWidth(entity), shootCompatibility.getHeight(entity) };
    }

    @Override
    public HitBox getHitBox(Entity entity) {
        if (entity.isInvulnerable()) return null;

        if (CompatibilityAPI.getVersion() >= 1.132) {
            BoundingBox boundingBox = entity.getBoundingBox();
            return new HitBox(boundingBox.getMin(), boundingBox.getMax());
        }

        Object nmsEntity = ReflectionUtil.invokeMethod(entityHandle, entity);
        Object boundingBox = ReflectionUtil.invokeMethod(entityBoundingBox, nmsEntity);

        double minX = (double) ReflectionUtil.invokeField(minXField, boundingBox),
                minY = (double) ReflectionUtil.invokeField(minYField, boundingBox),
                minZ = (double) ReflectionUtil.invokeField(minZField, boundingBox),

                maxX = (double) ReflectionUtil.invokeField(maxXField, boundingBox),
                maxY = (double) ReflectionUtil.invokeField(maxYField, boundingBox),
                maxZ = (double) ReflectionUtil.invokeField(maxZField, boundingBox);

        return new HitBox(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    }

    @Override
    public HitBox getHitBox(Block block) {
        if (block.isEmpty() || block.isLiquid()) return null;

        if (CompatibilityAPI.getVersion() >= 1.132) {
            if (block.isPassable()) return null;

            BoundingBox boundingBox = block.getBoundingBox();
            return new HitBox(boundingBox.getMin(), boundingBox.getMax());
        }

        // Dummy support for block bounding boxes since they have changed so much constantly
        if (!block.getType().isSolid()) return null;

        Location blockLocation = block.getLocation().clone();
        Vector min = blockLocation.toVector();

        // Basically every block is now 1x1x1 size when using reflection
        // But blocks after 1.13 R2 are accurate and this is most likely only used if server runs version like 1.8 R2 or lower
        Vector max = blockLocation.add(1.0, 1.0, 1.0).toVector();
        return new HitBox(min, max);
    }

    private int getNMSEntityId(double version, Object nmsEntity) {
        if (version <= 1.101) {
            // This using reflections since many versions to support
            return (int) ReflectionUtil.invokeMethod(entityTypesId, null, nmsEntity);
        } else if (version == 1.111) {
            // Easier to make these two like this
            return net.minecraft.server.v1_11_R1.EntityTypes.b.a(((net.minecraft.server.v1_11_R1.Entity) nmsEntity).getClass());
        } else if (version == 1.121) {
            // Easier to make these two like this
            return net.minecraft.server.v1_12_R1.EntityTypes.b.a(((net.minecraft.server.v1_12_R1.Entity) nmsEntity).getClass());
        } else if (version == 1.131) {
            return net.minecraft.server.v1_13_R1.EntityTypes.REGISTRY.a(((net.minecraft.server.v1_13_R1.Entity) nmsEntity).P());
        } else if (version == 1.132) {
            return net.minecraft.server.v1_13_R2.IRegistry.ENTITY_TYPE.a(((net.minecraft.server.v1_13_R2.Entity) nmsEntity).P());
        } else {
            debug.log(LogLevel.ERROR, "Tried to get NMS entity id in version newer than 1.13 R2...?",
                    "It is not required for PacketPlayOutSpawnEntity since constructor changed in 1.14 R1.");
            return 1;
        }
    }
}
