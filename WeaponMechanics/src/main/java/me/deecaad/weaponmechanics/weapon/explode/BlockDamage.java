package me.deecaad.weaponmechanics.weapon.explode;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ChanceSerializer;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.BlockDamageData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import java.util.*;

public class BlockDamage implements Serializer<BlockDamage> {

    /**
     * Determines whether a block is broken, cracked, or skip damage.
     */
    public enum BreakMode { CANCEL, BREAK, CRACK }

    /**
     * Holds data from config for a material.
     *
     * @param mode            How the block should be damaged (or not damaged).
     * @param blockDurability The maximum number of hits the block can take.
     * @param mask            if mode == BREAK, the broken block is replaced with this.
     */
    public record DamageConfig(BreakMode mode, int blockDurability, Material mask) {
    }


    private double dropBlockChance;
    private int damage;

    private int defaultBlockDurability;
    private Material defaultMask;
    private BreakMode defaultMode;

    private Map<Material, DamageConfig> blocks;


    /**
     * Default constructor for serializers
     */
    public BlockDamage() {
    }

    /**
     * Constructor with arguments.
     *
     * @param dropBlockChance        The [0, 1] chance to drop broken blocks as items.
     * @param damage                 The damage (usually 1) each hit.
     * @param defaultBlockDurability The default health any block has.
     * @param defaultMask            The default mask (usually AIR) any block has.
     * @param defaultMode            The default breaking mode any block has.
     * @param blocks                 Per-block overrides.
     */
    public BlockDamage(double dropBlockChance, int damage, int defaultBlockDurability, Material defaultMask,
                       BreakMode defaultMode, Map<Material, DamageConfig> blocks) {
        this.dropBlockChance = dropBlockChance;
        this.damage = damage;
        this.defaultBlockDurability = defaultBlockDurability;
        this.defaultMask = defaultMask;
        this.defaultMode = defaultMode;
        this.blocks = blocks;
    }

    public double getDropBlockChance() {
        return dropBlockChance;
    }

    public void setDropBlockChance(double dropBlockChance) {
        this.dropBlockChance = dropBlockChance;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDefaultBlockDurability() {
        return defaultBlockDurability;
    }

    public void setDefaultBlockDurability(int defaultBlockDurability) {
        this.defaultBlockDurability = defaultBlockDurability;
    }

    public Material getDefaultMask() {
        return defaultMask;
    }

    public void setDefaultMask(Material defaultMask) {
        this.defaultMask = defaultMask;
    }

    public BreakMode getDefaultMode() {
        return defaultMode;
    }

    public void setDefaultMode(BreakMode defaultMode) {
        this.defaultMode = defaultMode;
    }

    public Map<Material, DamageConfig> getBlocks() {
        return blocks;
    }

    public void setBlocks(Map<Material, DamageConfig> blocks) {
        this.blocks = blocks;
    }

    public BreakMode getBreakMode(Block block) {
        return getBreakMode(block.getType());
    }

    /**
     * Returns the behavior of the given material. Should it break? Should it
     * crack? Or should it ignore all damage?
     *
     * @param material The non-null material to check.
     * @return The non-null break mode of the material.
     */
    public BreakMode getBreakMode(Material material) {
        DamageConfig config = blocks.get(material);
        return config == null ? defaultMode : config.mode;
    }

    public int getDurability(Block block) {
        return getDurability(block.getType());
    }

    /**
     * Returns the number of hits before the block is broken, or <code>-1</code>
     * if the block should not be broken.
     *
     * @param material The non-null material to check.
     * @return The number of hits before the block breaks.
     */
    public int getDurability(Material material) {
        DamageConfig config = blocks.get(material);
        return config == null ? defaultBlockDurability : config.blockDurability;
    }

    public Material getMask(Block block) {
        return getMask(block.getType());
    }

    /**
     * Returns the block the given material should be replaced with, or
     * <code>null</code> if the block shouldn't be replaced.
     *
     * @param material The non-null material to check.
     * @return The nullable material to use as a mask.
     */
    public Material getMask(Material material) {
        DamageConfig config = blocks.get(material);
        return config == null ? defaultMask : config.mask;
    }

    /**
     * Returns <code>true</code> if at least 1 block can be broken through the
     * {@link #damage(Block, Player, boolean)} method. Useful for checking if
     * a weapon can be used for griefing.
     *
     * @return true if blocks can be broken.
     */
    public boolean canBreakBlocks() {
        if (defaultMode == BreakMode.BREAK)
            return true;

        return blocks.values().stream().anyMatch(config -> config.mode == BreakMode.BREAK);
    }

    /**
     * Applies damage to te given block. Any previous damage that was applied
     * is STACKED to see if the block should be broken. Depending on the block,
     * damage might be skipped, the block may be cracked, or the block may
     * break.
     *
     * <p>Regeneration is not handled automatically. You have to check the
     * return value and handle regeneration in a schedules task. If you pass
     * <code>true</code>, blocks will be broken WITHOUT block updates. If you
     * pass <code>false</code>, blocks will be broken and neighboring blocks
     * will be updated (making accurate regeneration impossible).
     *
     * <blockquote><pre>{@code
     *      boolean regenerate = true;
     *      BlockDamageData.DamageData data = BlockDamage#damage(block, null, regenerate);
     *
     *      if (regenerate) {
     *          if (blockDamage.isBreak() && data.isBroken()) {
     *              new BukkitRunnable() {
     *                  public void run() {
     *                      data.regenerate();
     *                      data.remove();
     *                  }
     *              }.runTaskLater(plugin, 200L); // 10 seconds
     *          }
     *      } else if (data.isBroken()) {
     *          data.remove();
     *      }
     * }</pre></blockquote>
     *
     * @param block        The non-null block to damage.
     * @param player       The nullable player who is breaking block, explosions should always give null
     * @param isRegenerate Use true if you want to have perfect regeneration.
     * @return The DamageData wrapping the block, or null if no damage was applied.
     */
    @Nullable
    public BlockDamageData.DamageData damage(Block block, @Nullable Player player, boolean isRegenerate) {
        BreakMode blockBreakMode = getBreakMode(block);
        if (blockBreakMode != BreakMode.CANCEL && !BlockDamageData.isBroken(block)) {

            boolean dropItems = blockBreakMode == BreakMode.BREAK;
            Collection<ItemStack> drops = null;

            // Only use these if its BREAK, not CRACK
            if (blockBreakMode == BreakMode.BREAK) {

                // #307, people don't want protection plugins to interfere sometimes
                boolean disableBlockBreakEvent = WeaponMechanics.getBasicConfigurations().getBool("Disable_Block_Break_Event");

                // When you provide the player to this method, other plugins can
                // cancel the block damage. This is used for protection plugins
                // (other than world-edit, which is checked separately).
                if (!disableBlockBreakEvent && player != null) {

                    BlockBreakEvent breakEvent = new BlockBreakEvent(block, player) {
                        @Override
                        public @NotNull String getEventName() {
                            return "WeaponMechanicsBlockDamage";
                        }
                    };
                    Bukkit.getPluginManager().callEvent(breakEvent);

                    if (breakEvent.isCancelled())
                        return null;

                    // Added in 1.12
                    if (ReflectionUtil.getMCVersion() >= 12) {
                        dropItems = breakEvent.isDropItems();
                    }
                }

                // Calculate dropped blocks BEFORE the block is broken.
                drops = NumberUtil.chance(dropBlockChance) ? block.getDrops() : null;
                if (block.getState() instanceof InventoryHolder inv && !isRegenerate) {
                    if (drops == null)
                        drops = new ArrayList<>();

                    if (inv instanceof Chest)
                        Collections.addAll(drops, ((Chest) inv).getBlockInventory().getContents());
                    else
                        Collections.addAll(drops, inv.getInventory().getContents());
                }

            }

            int max = getDurability(block);
            BlockDamageData.DamageData data = BlockDamageData.damage(block, (double) damage / (double) max, getBreakMode(block) == BreakMode.BREAK, isRegenerate, getMask(block));
            if (data.isBroken() && drops != null && dropItems) {

                Location location = block.getLocation();
                for (ItemStack item : drops) {
                    if (item != null)
                        block.getWorld().dropItemNaturally(location, item);
                }
            }

            return data;
        }

        return null;
    }

    @Override
    public String getKeyword() {
        return "Block_Damage";
    }

    @Override
    public boolean letPassThrough(@NotNull String key) {
        return key.endsWith("Spawn_Falling_Block_Chance") || key.endsWith("Regenerate_After_Ticks");
    }

    @Override
    @NotNull
    public BlockDamage serialize(@NotNull SerializeData data) throws SerializerException {

        // Added November 1st, 2022 to detect out-dated configurations
        if (data.has("Block_List") || data.has("Shots_To_Break_Blocks")) {
            throw data.exception(null, "Found an outdated configuration!",
                    "You need to update your 'Block_Damage' to match the new format");
        }

        Double dropBlockChance = data.of("Drop_Broken_Block_Chance").serialize(new ChanceSerializer());
        if (dropBlockChance == null)
            dropBlockChance = 0.0;

        int damagePerHit = data.of("Damage_Per_Hit").assertPositive().getInt(1);
        int defaultBlockDurability = data.of("Default_Block_Durability").assertPositive().getInt(1);
        BreakMode defaultMode = data.of("Default_Mode").getEnum(BreakMode.class, BreakMode.BREAK);
        Material defaultMask = data.of("Default_Mask").getEnum(Material.class, Material.AIR);

        Map<Material, DamageConfig> blocks = new EnumMap<>(Material.class);
        List<String[]> list = data.ofList("Blocks")
                .addArgument(Material.class, true)
                .addArgument(BreakMode.class, true)
                .addArgument(int.class, false).assertArgumentPositive()
                .addArgument(Material.class, false)
                .assertExists().assertList().get();

        for (int i = 0; i < list.size(); i++) {
            String[] split = list.get(i);

            List<Material> materials = EnumUtil.parseEnums(Material.class, split[0]);
            BreakMode mode = BreakMode.valueOf(split[1].toUpperCase(Locale.ROOT));
            int blockDurability = split.length > 2 ? Integer.parseInt(split[2]) : -1;
            Material mask = split.length > 3 ? Material.valueOf(split[3]) : null;

            // Cannot apply a mask to blocks that cannot be broken
            if (mode != BreakMode.BREAK && mask != null) {
                throw data.listException("Blocks", i, "You cannot use material masks with '" + mode + "'",
                        SerializerException.forValue(String.join(" ", split)),
                        "In order to use masks, use 'BREAK' mode");
            }

            // Cannot use durability or masks with blocks that cancel durability
            if (mode == BreakMode.CANCEL && blockDurability != -1) {
                throw data.listException("Blocks", i, "You cannot use durability with 'CANCEL'",
                        SerializerException.forValue(String.join(" ", split)),
                        "In order to use durability, use 'BREAK' or 'CRACK'");
            }

            // If user wants to break or crack this block, then they are
            // trying to override the default block durability. Hence,
            // block durability is a required argument.
            if ((mode == BreakMode.CRACK || mode == BreakMode.BREAK) && blockDurability == -1) {
                int goodNumber = ReflectionUtil.getMCVersion() < 13 ? 1 : (int) materials.get(0).getBlastResistance() + 1;
                throw data.listException("Blocks", i, "When using '" + mode + "', you MUST also use durability",
                        SerializerException.forValue(String.join(" ", split)),
                        "For example, try '" + String.join(" ", split) + " " + goodNumber + "' instead");
            }

            // Illegal value... how can something have 0 health?
            if (blockDurability == 0) {
                throw data.listException("Blocks", i, "Tried to use '0' for block durability, must be at least '1'");
            }

            if (mask == null)
                mask = Material.AIR;

            DamageConfig config = new DamageConfig(mode, blockDurability, mask);
            for (Material mat : materials)
                blocks.put(mat, config);
        }

        return new BlockDamage(dropBlockChance, damagePerHit, defaultBlockDurability, defaultMask, defaultMode, blocks);
    }
}
