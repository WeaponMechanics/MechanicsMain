package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.MessageHelper;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;

import static me.deecaad.weaponmechanics.WeaponMechanics.*;

public class WeaponInfoDisplay implements Serializer<WeaponInfoDisplay> {


    private static Constructor<?> packetPlayOutExperienceConstructor;

    static {
        if (CompatibilityAPI.getVersion() < 1.15) {
            packetPlayOutExperienceConstructor = ReflectionUtil.getConstructor(ReflectionUtil.getPacketClass("PacketPlayOutExperience"), float.class, int.class, int.class);
        }
    }

    private String actionBar;

    private String bossBar;
    private BarColor barColor;
    private BarStyle barStyle;

    private boolean showAmmoInBossBarProgress;
    private boolean showAmmoInExpLevel;
    private boolean showAmmoInExpProgress;

    private String dualWieldMainActionBar;
    private String dualWieldMainBossBar;
    private String dualWieldOffActionBar;
    private String dualWieldOffBossBar;

    /**
     * Empty constructor to be used as serializer
     */
    public WeaponInfoDisplay() { }

    public WeaponInfoDisplay(String actionBar, String bossBar, BarColor barColor, BarStyle barStyle,
                             boolean showAmmoInBossBarProgress, boolean showAmmoInExpLevel, boolean showAmmoInExpProgress,
                             String dualWieldMainActionBar, String dualWieldMainBossBar, String dualWieldOffActionBar, String dualWieldOffBossBar) {
        this.actionBar = actionBar;
        this.bossBar = bossBar;
        this.barColor = barColor;
        this.barStyle = barStyle;
        this.showAmmoInBossBarProgress = showAmmoInBossBarProgress;
        this.showAmmoInExpLevel = showAmmoInExpLevel;
        this.showAmmoInExpProgress = showAmmoInExpProgress;
        this.dualWieldMainActionBar = dualWieldMainActionBar;
        this.dualWieldMainBossBar = dualWieldMainBossBar;
        this.dualWieldOffActionBar = dualWieldOffActionBar;
        this.dualWieldOffBossBar = dualWieldOffBossBar;
    }

    public void send(PlayerWrapper playerWrapper, EquipmentSlot slot) {
        Player player = playerWrapper.getPlayer();
        MessageHelper messageHelper = playerWrapper.getMessageHelper();

        String mainWeapon = playerWrapper.getMainHandData().getCurrentWeaponTitle();
        String offWeapon = playerWrapper.getOffHandData().getCurrentWeaponTitle();

        ItemStack mainStack = player.getEquipment().getItemInMainHand();
        ItemStack offStack = player.getEquipment().getItemInOffHand();

        InfoHandler infoHandler = WeaponMechanics.getWeaponHandler().getInfoHandler();
        String checkCorrectMain = infoHandler.getWeaponTitle(mainStack, false);
        if (checkCorrectMain == null) {
            // No mainhand weapon
            mainStack = null;
            mainWeapon = null;
            playerWrapper.getMainHandData().setCurrentWeaponTitle(null);
        } else if (!checkCorrectMain.equals(mainWeapon)) {
            // Ensure that the weapon is actually same
            mainWeapon = checkCorrectMain;
        }

        String checkCorrectOff = infoHandler.getWeaponTitle(offStack, false);
        if (checkCorrectOff == null) {
            // No offhand weapon
            offStack = null;
            offWeapon = null;
            playerWrapper.getOffHandData().setCurrentWeaponTitle(null);
        } else if (!checkCorrectOff.equals(offWeapon)) {
            // Ensure that the weapon is actually same
            offWeapon = checkCorrectOff;
        }

        if (mainWeapon == null && offWeapon == null) return;

        // Mostly this is RIGHT, but some players may have it LEFT
        boolean hasInvertedMainHand = player.getMainHand() == MainHand.LEFT;

        boolean mainhand = slot == EquipmentSlot.HAND;
        boolean isDualWielding = mainWeapon != null && offWeapon != null && mainStack != null && offStack != null;

        if (actionBar != null) {
            if (isDualWielding) {
                StringBuilder builder = new StringBuilder();
                String dualWieldSplit = getBasicConfigurations().getString("Placeholder_Symbols.Dual_Wield_Split", " &7| ");
                String offHand, mainHand;

                // OFF HAND < dual wield split > MAIN HAND
                // IF inverted: MAIN HAND < dual wield split >
                if (mainWeapon.equals(offWeapon)) {
                    // Same weapon, use this info display for both
                    if (hasInvertedMainHand) {
                        offHand = PlaceholderAPI.applyPlaceholders(this.dualWieldMainActionBar != null ? this.dualWieldMainActionBar : this.actionBar, player, offStack, offWeapon, EquipmentSlot.OFF_HAND);
                        mainHand = PlaceholderAPI.applyPlaceholders(this.dualWieldOffActionBar != null ? this.dualWieldOffActionBar : this.actionBar, player, mainStack, mainWeapon, EquipmentSlot.HAND);
                    } else {
                        offHand = PlaceholderAPI.applyPlaceholders(this.dualWieldOffActionBar != null ? this.dualWieldOffActionBar : this.actionBar, player, offStack, offWeapon, EquipmentSlot.OFF_HAND);
                        mainHand = PlaceholderAPI.applyPlaceholders(this.dualWieldMainActionBar != null ? this.dualWieldMainActionBar : this.actionBar, player, mainStack, mainWeapon, EquipmentSlot.HAND);
                    }
                } else {
                    // Other weapon, use other info display
                    WeaponInfoDisplay mainDisplay = mainhand ? this : getConfigurations().getObject(mainWeapon + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                    WeaponInfoDisplay offDisplay = mainhand ? getConfigurations().getObject(offWeapon + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class) : this;
                    if (mainDisplay == null) mainDisplay = this;
                    if (offDisplay == null) offDisplay = this;

                    if (hasInvertedMainHand) {
                        offHand = PlaceholderAPI.applyPlaceholders((offDisplay.dualWieldMainActionBar == null || mainDisplay.dualWieldOffActionBar == null) ? offDisplay.actionBar : offDisplay.dualWieldMainActionBar,
                                player, offStack, offWeapon, EquipmentSlot.OFF_HAND);
                        mainHand = PlaceholderAPI.applyPlaceholders((mainDisplay.dualWieldOffActionBar == null || offDisplay.dualWieldMainActionBar == null) ? mainDisplay.actionBar : mainDisplay.dualWieldOffActionBar,
                                player, mainStack, mainWeapon, EquipmentSlot.HAND);
                    } else {
                        offHand = PlaceholderAPI.applyPlaceholders((offDisplay.dualWieldOffActionBar == null || mainDisplay.dualWieldMainActionBar == null) ? offDisplay.actionBar : offDisplay.dualWieldOffActionBar,
                                player, offStack, offWeapon, EquipmentSlot.OFF_HAND);
                        mainHand = PlaceholderAPI.applyPlaceholders((mainDisplay.dualWieldMainActionBar == null || offDisplay.dualWieldOffActionBar == null) ? mainDisplay.actionBar : mainDisplay.dualWieldMainActionBar,
                                player, mainStack, mainWeapon, EquipmentSlot.HAND);
                    }
                }

                if (hasInvertedMainHand) {
                    builder.append(mainHand).append(dualWieldSplit).append(offHand);
                } else {
                    builder.append(offHand).append(dualWieldSplit).append(mainHand);
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(builder.toString()));
            } else {
                if (mainhand) {
                    if (mainStack != null && mainStack.hasItemMeta()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PlaceholderAPI.applyPlaceholders(actionBar, player, mainStack, mainWeapon, slot)));
                    }
                } else if (offStack != null && offStack.hasItemMeta()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PlaceholderAPI.applyPlaceholders(actionBar, player, offStack, offWeapon, slot)));
                }
            }
        }

        double magazineProgress = -1;

        if (bossBar != null) {
            StringBuilder builder = new StringBuilder();

            if (isDualWielding) {
                String dualWieldSplit = getBasicConfigurations().getString("Placeholder_Symbols.Dual_Wield_Split", " &7| ");
                String offHand, mainHand;

                // OFF HAND < dual wield split > MAIN HAND
                // IF inverted: MAIN HAND < dual wield split >
                if (mainWeapon.equals(offWeapon)) {
                    // Same weapon, use this info display for both
                    if (hasInvertedMainHand) {
                        offHand = PlaceholderAPI.applyPlaceholders(this.dualWieldMainBossBar != null ? this.dualWieldMainBossBar : this.bossBar, player, offStack, offWeapon, EquipmentSlot.OFF_HAND);
                        mainHand = PlaceholderAPI.applyPlaceholders(this.dualWieldOffBossBar != null ? this.dualWieldOffBossBar : this.bossBar, player, mainStack, mainWeapon, EquipmentSlot.HAND);
                    } else {
                        offHand = PlaceholderAPI.applyPlaceholders(this.dualWieldOffBossBar != null ? this.dualWieldOffBossBar : this.bossBar, player, offStack, offWeapon, EquipmentSlot.OFF_HAND);
                        mainHand = PlaceholderAPI.applyPlaceholders(this.dualWieldMainBossBar != null ? this.dualWieldMainBossBar : this.bossBar, player, mainStack, mainWeapon, EquipmentSlot.HAND);
                    }
                } else {
                    // Other weapon, use other info display
                    WeaponInfoDisplay mainDisplay = mainhand ? this : getConfigurations().getObject(mainWeapon + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                    WeaponInfoDisplay offDisplay = mainhand ? getConfigurations().getObject(offWeapon + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class) : this;
                    if (mainDisplay == null) mainDisplay = this;
                    if (offDisplay == null) offDisplay = this;

                    if (hasInvertedMainHand) {
                        offHand = PlaceholderAPI.applyPlaceholders((offDisplay.dualWieldMainBossBar == null || mainDisplay.dualWieldOffBossBar == null) ? offDisplay.bossBar : offDisplay.dualWieldMainBossBar,
                                player, offStack, offWeapon, EquipmentSlot.OFF_HAND);
                        mainHand = PlaceholderAPI.applyPlaceholders((mainDisplay.dualWieldOffBossBar == null || offDisplay.dualWieldMainBossBar == null) ? mainDisplay.bossBar : mainDisplay.dualWieldOffBossBar,
                                player, mainStack, mainWeapon, EquipmentSlot.HAND);
                    } else {
                        offHand = PlaceholderAPI.applyPlaceholders((offDisplay.dualWieldOffBossBar == null || mainDisplay.dualWieldMainBossBar == null) ? offDisplay.bossBar : offDisplay.dualWieldOffBossBar,
                                player, offStack, offWeapon, EquipmentSlot.OFF_HAND);
                        mainHand = PlaceholderAPI.applyPlaceholders((mainDisplay.dualWieldMainBossBar == null || offDisplay.dualWieldOffBossBar == null) ? mainDisplay.bossBar : mainDisplay.dualWieldMainBossBar,
                                player, mainStack, mainWeapon, EquipmentSlot.HAND);
                    }
                }

                if (hasInvertedMainHand) {
                    builder.append(mainHand).append(dualWieldSplit).append(offHand);
                } else {
                    builder.append(offHand).append(dualWieldSplit).append(mainHand);
                }
            } else {
                if (mainhand) {
                    if (mainStack != null && mainStack.hasItemMeta()) {
                        builder.append(PlaceholderAPI.applyPlaceholders(bossBar, player, mainStack, mainWeapon, slot));
                    }
                } else if (offStack != null && offStack.hasItemMeta()) {
                    builder.append(PlaceholderAPI.applyPlaceholders(bossBar, player, offStack, offWeapon, slot));
                }
            }

            if (builder.length() != 0) {
                BossBar bossBar = messageHelper.getBossBar();
                if (bossBar == null) {
                    bossBar = Bukkit.createBossBar(builder.toString(), barColor, barStyle);
                    bossBar.addPlayer(player);
                    messageHelper.setBossBar(bossBar);
                } else {
                    Bukkit.getScheduler().cancelTask(messageHelper.getBossBarTask());
                    bossBar.setTitle(builder.toString());
                    bossBar.setColor(barColor);
                    bossBar.setStyle(barStyle);
                }
                if (showAmmoInBossBarProgress) {
                    magazineProgress = mainhand ? getMagazineProgress(mainStack, mainWeapon) : getMagazineProgress(offStack, offWeapon);
                    bossBar.setProgress(magazineProgress);
                }
                messageHelper.setBossBarTask(new BukkitRunnable() {
                    @Override
                    public void run() {
                        messageHelper.getBossBar().removeAll();
                        messageHelper.setBossBar(null);
                        messageHelper.setBossBarTask(0);
                    }
                }.runTaskLater(WeaponMechanics.getPlugin(), 40).getTaskId());
            }
        }

        if (showAmmoInExpLevel || showAmmoInExpProgress) {
            ItemStack useStack = mainhand ? mainStack : offStack;
            String useWeapon = mainhand ? mainWeapon : offWeapon;

            if (useStack == null || !useStack.hasItemMeta() || useWeapon == null) return;

            if (magazineProgress == -1) {
                magazineProgress = getMagazineProgress(useStack, useWeapon);
            }

            int lastExpTask = messageHelper.getExpTask();
            if (lastExpTask != 0) Bukkit.getServer().getScheduler().cancelTask(lastExpTask);

            if (CompatibilityAPI.getVersion() < 1.15) {
                CompatibilityAPI.getCompatibility().sendPackets(player,
                        ReflectionUtil.newInstance(packetPlayOutExperienceConstructor,
                                showAmmoInExpProgress ? (float) (magazineProgress != -1 ? magazineProgress : getMagazineProgress(useStack, useWeapon)) : player.getExp(),
                                player.getTotalExperience(),
                                showAmmoInExpLevel ? getAmmoLeft(useStack, useWeapon) : player.getLevel()));
                messageHelper.setExpTask(new BukkitRunnable() {
                    public void run() {
                        CompatibilityAPI.getCompatibility().sendPackets(player,
                                ReflectionUtil.newInstance(packetPlayOutExperienceConstructor,
                                        player.getExp(),
                                        player.getTotalExperience(),
                                        player.getLevel()));
                        messageHelper.setExpTask(0);
                    }
                }.runTaskLater(WeaponMechanics.getPlugin(), 40).getTaskId());
            } else {
                player.sendExperienceChange(showAmmoInExpProgress ? (float) (magazineProgress != -1 ? magazineProgress : getMagazineProgress(useStack, useWeapon)) : player.getExp(),
                        showAmmoInExpLevel ? getAmmoLeft(useStack, useWeapon) : player.getLevel());
                messageHelper.setExpTask(new BukkitRunnable() {
                    public void run() {
                        player.sendExperienceChange(player.getExp(), player.getLevel());
                        messageHelper.setExpTask(0);
                    }
                }.runTaskLater(WeaponMechanics.getPlugin(), 40).getTaskId());
            }
        }
    }

    private int getAmmoLeft(ItemStack weaponStack, String weaponTitle) {
        return getWeaponHandler().getReloadHandler().getAmmoLeft(weaponStack, weaponTitle);
    }

    private double getMagazineProgress(ItemStack weaponStack, String weaponTitle) {
        double progress = (double) getWeaponHandler().getReloadHandler().getAmmoLeft(weaponStack, weaponTitle) / (double) getConfigurations().getInt(weaponTitle + ".Reload.Magazine_Size");
        return NumberUtil.minMax(0.0, progress, 1.0);
    }

    @Override
    public String getKeyword() {
        return "Weapon_Info_Display";
    }

    @Override
    @Nonnull
    public WeaponInfoDisplay serialize(SerializeData data) throws SerializerException {

        // ACTION BAR
        String actionBarMessage = data.of("Action_Bar.Message").assertType(String.class).get(null);
        if (actionBarMessage != null) actionBarMessage = StringUtil.color(actionBarMessage);

        String bossBarMessage = data.of("Boss_Bar.Title").assertType(String.class).get(null);
        BarColor barColor = null;
        BarStyle barStyle = null;
        if (bossBarMessage != null) {
            barColor = data.of("Boss_Bar.Bar_Color").getEnum(BarColor.class, BarColor.WHITE);
            barStyle = data.of("Boss_Bar.Bar_Style").getEnum(BarStyle.class, BarStyle.SEGMENTED_20);
            bossBarMessage = StringUtil.color(bossBarMessage);
        }

        boolean expLevel = data.of("Show_Ammo_In.Exp_Level").getBool(false);
        boolean expProgress = data.of("Show_Ammo_In.Exp_Progress").getBool(false);
        boolean bossBarProgress = data.of("Show_Ammo_In.Boss_Bar_Progress").getBool(false);

        String dualWieldMainActionBar = data.of("Action_Bar.Dual_Wield.Main_Hand").assertType(String.class).get(null);
        String dualWieldMainBossBar = data.of("Boss_Bar.Dual_Wield.Main_Hand").assertType(String.class).get(null);

        String dualWieldOffActionBar = data.of("Action_Bar.Dual_Wield.Off_Hand").assertType(String.class).get(null);
        String dualWieldOffBossBar = data.of("Boss_Bar.Dual_Wield.Off_Hand").assertType(String.class).get(null);

        if (actionBarMessage == null && bossBarMessage == null && !expLevel && !expProgress) {
            throw data.exception(null, "Found an empty Weapon_Info_Display... Users won't be able to see any changes in their ammo!");
        }

        if (bossBarProgress && bossBarMessage == null) {
            throw data.exception(null, "In order for a boss bar to work properly, 'Show_Ammo_In.Boss_Bar_Progress: true' and the",
                    "boss bar needs to be defined in the message.");
        }

        return new WeaponInfoDisplay(actionBarMessage, bossBarMessage, barColor, barStyle,
                bossBarProgress, expLevel, expProgress,
                dualWieldMainActionBar, dualWieldMainBossBar, dualWieldOffActionBar, dualWieldOffBossBar);
    }
}