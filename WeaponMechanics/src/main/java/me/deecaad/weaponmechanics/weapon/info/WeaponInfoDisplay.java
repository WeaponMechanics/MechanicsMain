package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.CommonDataTags;
import me.deecaad.weaponmechanics.mechanics.defaultmechanics.MessageMechanic;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;

import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

public class WeaponInfoDisplay implements Serializer<WeaponInfoDisplay> {


    private static Constructor<?> packetPlayOutExperienceConstructor;

    static {
        if (CompatibilityAPI.getVersion() < 1.15) {
            packetPlayOutExperienceConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getPacketClass("PacketPlayOutExperience"), float.class, int.class, int.class);
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
            castData.setData(CommonDataTags.WEAPON_INFO.name(), true);
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

        boolean hasMetaChanges = false;
        ItemMeta meta = weaponStack.getItemMeta();
        ItemMeta ogMeta = og.getItemMeta();

        String ogDisplayName = ogMeta.getDisplayName();
        if (ogMeta.hasDisplayName() != meta.hasDisplayName()
                || (ogMeta.hasDisplayName() && !ogMeta.getDisplayName().equalsIgnoreCase(meta.getDisplayName()))) {
            meta.setDisplayName(PlaceholderAPI.applyPlaceholders(ogDisplayName, playerWrapper.getPlayer(), weaponStack, weaponTitle));
            hasMetaChanges = true;
        }

        if (ogMeta.hasLore() != meta.hasLore()
                || (ogMeta.hasLore() && !ogMeta.getLore().equals(meta.getLore()))) {
            meta.setLore(PlaceholderAPI.applyPlaceholders(ogMeta.getLore(), playerWrapper.getPlayer(), weaponStack, weaponTitle));
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
    @Nonnull
    public WeaponInfoDisplay serialize(SerializeData data) throws SerializerException {
        MessageMechanic messageMechanic = data.of().serialize(MessageMechanic.class);
        boolean updateItemName = data.of("Update_Item_Name").assertType(Boolean.class).get(false);
        boolean expLevel = data.of("Show_Ammo_In.Exp_Level").assertType(Boolean.class).get(false);
        boolean expProgress = data.of("Show_Ammo_In.Exp_Progress").assertType(Boolean.class).get(false);
        boolean bossBarProgress = data.of("Show_Ammo_In.Boss_Bar_Progress").assertType(Boolean.class).get(messageMechanic != null && messageMechanic.hasBossBar());

        if (messageMechanic == null && !updateItemName && !expLevel && !expProgress) {
            data.exception(null, "Found an empty Weapon_Info_Display... Users won't be able to see any changes in their ammo!");
        }


        if (bossBarProgress && (messageMechanic == null || messageMechanic.hasBossBar())) {
            data.exception(null, "In order for a boss bar to work properly, 'Show_Ammo_In.Boss_Bar_Progress: true' and the",
                    "boss bar needs to be defined in the message.");
        }

        return new WeaponInfoDisplay(messageMechanic, updateItemName, bossBarProgress, expLevel, expProgress);
    }
}