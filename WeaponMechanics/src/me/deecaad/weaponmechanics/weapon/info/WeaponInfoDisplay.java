package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtils;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.MessageMechanic;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Constructor;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class WeaponInfoDisplay implements Serializer<WeaponInfoDisplay> {


    private static Constructor<?> packetPlayOutExperienceConstructor;

    static {
        if (CompatibilityAPI.getVersion() < 1.15) {
            packetPlayOutExperienceConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketPlayOutExperience"), float.class, int.class, int.class);
        }
    }

    private MessageMechanic messageMechanic;
    private boolean updateItemName;
    private boolean showAmmoInBossBarProgress;
    private boolean showAmmoInExpLevel;
    private boolean showAmmoInExpProgress;

    /**
     * Empty constructor to be used as serializer
     */
    public WeaponInfoDisplay() { }

    public WeaponInfoDisplay(MessageMechanic messageMechanic, boolean updateItemName, boolean showAmmoInBossBarProgress, boolean showAmmoInExpLevel, boolean showAmmoInExpProgress) {
        this.messageMechanic = messageMechanic;
        this.updateItemName = updateItemName;
        this.showAmmoInBossBarProgress = showAmmoInBossBarProgress;
        this.showAmmoInExpLevel = showAmmoInExpLevel;
        this.showAmmoInExpProgress = showAmmoInExpProgress;
    }

    public void send(IPlayerWrapper playerWrapper, String weaponTitle, ItemStack weaponStack) {

        if (messageMechanic != null) {
            CastData castData = new CastData(playerWrapper, weaponTitle, weaponStack);
            castData.setData(CastData.CommonDataTags.WEAPON_INFO.name(), true);
            messageMechanic.use(castData);
        }

        MessageHelper messageHelper = playerWrapper.getMessageHelper();

        if (showAmmoInBossBarProgress || showAmmoInExpLevel || showAmmoInExpProgress) {
            int magazineSize = WeaponMechanics.getConfigurations().getInt(weaponTitle + ".Reload.Magazine_Size");
            int ammoLeft = WeaponMechanics.getWeaponHandler().getReloadHandler().getAmmoLeft(weaponStack);
            double progress = (double) ammoLeft / (double) magazineSize;

            if (showAmmoInBossBarProgress) {
                BossBar currentInfoBossBar = messageHelper.getCurrentInfoBossBar();
                if (currentInfoBossBar != null) {
                    // No need to worry about removing or changing it since its handled in MessageMechanic
                    currentInfoBossBar.setProgress(progress);
                }
            }

            if (showAmmoInExpLevel || showAmmoInExpProgress) {

                int lastExpTask = messageHelper.getExpTask();
                if (lastExpTask != 0) {
                    Bukkit.getServer().getScheduler().cancelTask(lastExpTask);
                    messageHelper.setExpTask(0);
                }

                Player player = playerWrapper.getPlayer();
                if (CompatibilityAPI.getVersion() < 1.15) {
                    CompatibilityAPI.getCompatibility().sendPackets(player,
                            ReflectionUtil.newInstance(packetPlayOutExperienceConstructor,
                                    showAmmoInExpProgress ? (float) progress : player.getExp(),
                                    player.getTotalExperience(),
                                    showAmmoInExpLevel ? ammoLeft : player.getLevel()));
                    messageHelper.setExpTask(new BukkitRunnable() {
                        public void run() {
                            CompatibilityAPI.getCompatibility().sendPackets(player,
                                    ReflectionUtil.newInstance(packetPlayOutExperienceConstructor,
                                            player.getExp(),
                                            player.getTotalExperience(),
                                            player.getLevel()));
                            messageHelper.setExpTask(0);
                        }
                    }.runTaskLaterAsynchronously(WeaponMechanics.getPlugin(), 40).getTaskId());
                } else {
                    player.sendExperienceChange(showAmmoInExpProgress ? (float) progress : player.getExp(), showAmmoInExpLevel ? ammoLeft : player.getLevel());
                    messageHelper.setExpTask(new BukkitRunnable() {
                        public void run() {
                            player.sendExperienceChange(player.getExp(), player.getLevel());
                            messageHelper.setExpTask(0);
                        }
                    }.runTaskLaterAsynchronously(WeaponMechanics.getPlugin(), 40).getTaskId());
                }
            }
        }

        if (updateItemName) {
            String ogWeaponStackName = getConfigurations().getObject(weaponTitle + ".Info.Weapon_Item", ItemStack.class).getItemMeta().getDisplayName();

            ItemMeta itemMeta = weaponStack.getItemMeta();
            itemMeta.setDisplayName(PlaceholderAPI.applyPlaceholders(ogWeaponStackName, playerWrapper.getPlayer(), weaponStack, weaponTitle));
            weaponStack.setItemMeta(itemMeta);
        }

        if (messageHelper.allowItemUpdate()) {
            messageHelper.updateItemTime();
            doChangesIfRequired(getConfigurations().getObject(weaponTitle + ".Info.Weapon_Item", ItemStack.class), playerWrapper, weaponTitle, weaponStack);
        }
    }

    private void doChangesIfRequired(ItemStack og, IPlayerWrapper playerWrapper, String weaponTitle, ItemStack weaponStack) {

        double version = CompatibilityAPI.getVersion();
        if (weaponStack.getType() != og.getType()) {
            weaponStack.setType(og.getType());
        }
        if (version < 1.13 && weaponStack.getData().getData() != og.getData().getData()) {
            weaponStack.getData().setData(og.getData().getData());
        }

        boolean hasMetaChanges = false;
        ItemMeta meta = weaponStack.getItemMeta();
        ItemMeta ogMeta = og.getItemMeta();

        String ogDisplayName = ogMeta.getDisplayName();
        if (!meta.getDisplayName().equals(ogDisplayName)) {
            meta.setDisplayName(PlaceholderAPI.applyPlaceholders(ogDisplayName, playerWrapper.getPlayer(), weaponStack, weaponTitle));
            hasMetaChanges = true;
        }
        if (!meta.getLore().equals(ogMeta.getLore())) {
            meta.setLore(PlaceholderAPI.applyPlaceholders(ogMeta.getLore(), playerWrapper.getPlayer(), weaponStack, weaponTitle));
            hasMetaChanges = true;
        }

        if (version >= 1.132) {
            if (((org.bukkit.inventory.meta.Damageable) meta).getDamage() != ((org.bukkit.inventory.meta.Damageable) ogMeta).getDamage()) {
                ((org.bukkit.inventory.meta.Damageable) meta).setDamage(((org.bukkit.inventory.meta.Damageable) ogMeta).getDamage());
                hasMetaChanges = true;
            }
        } else if (weaponStack.getDurability() != og.getDurability()) {
            weaponStack.setDurability(og.getDurability());
            hasMetaChanges = true;
        }

        if (version >= 1.14 && meta.getCustomModelData() != ogMeta.getCustomModelData()) {
            meta.setCustomModelData(ogMeta.getCustomModelData());
            hasMetaChanges = true;
        }

        if (hasMetaChanges) {
            weaponStack.setItemMeta(meta);
        }
    }

    @Override
    public String getKeyword() {
        return "Weapon_Info_Display";
    }

    @Override
    public WeaponInfoDisplay serialize(File file, ConfigurationSection configurationSection, String path) {
        MessageMechanic messageMechanic = new MessageMechanic().serialize(file, configurationSection, path);
        boolean updateItemName = configurationSection.getBoolean(path + ".Update_Item_Name");
        boolean expLevel = configurationSection.getBoolean(path + ".Show_Ammo_In.Exp_Level");
        boolean expProgress = configurationSection.getBoolean(path + ".Show_Ammo_In.Exp_Progress");

        if (messageMechanic == null && !updateItemName && !expLevel && !expProgress) {
            debug.log(LogLevel.ERROR,
                    StringUtils.foundInvalid("weapon info display"),
                    StringUtils.foundAt(file, path),
                    "Either message has to be defined, Update_Item_Name true, Exp_Level true OR Exp_Progress true");
            return null;
        }

        boolean bossBarProgress = configurationSection.getBoolean(path + ".Show_Ammo_In.Boss_Bar_Progress");
        if (bossBarProgress && (messageMechanic == null || !messageMechanic.hasBossBar())) {
            debug.log(LogLevel.ERROR,
                    StringUtils.foundInvalid("boss bar"),
                    StringUtils.foundAt(file, path),
                    "When using Show_Ammo_In.Boss_Bar_Progress you need properly configured Boss_Bar configuration also");
            return null;
        }

        return new WeaponInfoDisplay(messageMechanic, updateItemName, bossBarProgress, expLevel, expProgress);
    }
}