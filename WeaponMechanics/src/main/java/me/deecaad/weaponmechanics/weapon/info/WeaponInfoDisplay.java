package me.deecaad.weaponmechanics.weapon.info;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.placeholder.PlaceholderAPI;
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
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;

import static me.deecaad.weaponmechanics.WeaponMechanics.getWeaponHandler;
import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;
import static me.deecaad.weaponmechanics.WeaponMechanics.getConfigurations;

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

    public void send(PlayerWrapper playerWrapper, ItemStack knownStack, EquipmentSlot slot) {

        Player player = playerWrapper.getPlayer();
        String mainWeapon = playerWrapper.getMainHandData().getCurrentWeaponTitle();
        String offWeapon = playerWrapper.getOffHandData().getCurrentWeaponTitle();
        MessageHelper messageHelper = playerWrapper.getMessageHelper();

        boolean isDualWielding = mainWeapon != null && offWeapon != null;
        boolean isKnownMain = slot == EquipmentSlot.HAND;

        ItemStack mainStack = null;
        ItemStack offStack = null;
        if (isKnownMain) {
            mainStack = knownStack;
            if (offWeapon != null) offStack = player.getEquipment().getItemInOffHand();
        } else {
            if (mainWeapon != null) mainStack = player.getEquipment().getItemInMainHand();
            offStack = knownStack;
        }

        if (actionBar != null) {
            if (isDualWielding) {
                StringBuilder builder = new StringBuilder();
                String dualWieldSplit = getBasicConfigurations().getString("Placeholder_Symbols.Dual_Wield_Split", " &7| ");
                // OFF HAND < dual wield split > MAIN HAND
                if (mainWeapon.equals(offWeapon)) {
                    // Same weapon, use this info display for both
                    builder.append(PlaceholderAPI.applyPlaceholders(this.dualWieldOffActionBar != null ? this.dualWieldOffActionBar : this.actionBar, player, offStack, offWeapon))
                            .append(dualWieldSplit)
                            .append(PlaceholderAPI.applyPlaceholders(this.dualWieldMainActionBar != null ? this.dualWieldMainActionBar : this.actionBar, player, mainStack, mainWeapon));
                } else {
                    // Other weapon, use other info display
                    WeaponInfoDisplay other = getConfigurations().getObject((isKnownMain ? mainWeapon : offWeapon) + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                    builder.append(PlaceholderAPI.applyPlaceholders(isKnownMain && other != null ? (other.dualWieldOffActionBar != null ? other.dualWieldOffActionBar : other.actionBar)
                            : (this.dualWieldOffActionBar != null ? this.dualWieldOffActionBar : this.actionBar), player, offStack, offWeapon))
                            .append(dualWieldSplit)
                            .append(PlaceholderAPI.applyPlaceholders(isKnownMain || other == null ? (this.dualWieldMainActionBar != null ? this.dualWieldMainActionBar : this.actionBar)
                            : (other.dualWieldMainActionBar != null ? other.dualWieldMainActionBar : other.actionBar), player, mainStack, mainWeapon));
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(builder.toString()));
            } else {
                if (isKnownMain) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PlaceholderAPI.applyPlaceholders(actionBar, player, mainStack, mainWeapon)));
                } else {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PlaceholderAPI.applyPlaceholders(actionBar, player, offStack, offWeapon)));
                }
            }
        }

        double magazineProgress = -1;

        if (bossBar != null) {
            StringBuilder builder = new StringBuilder();
            if (isDualWielding) {
                String dualWieldSplit = getBasicConfigurations().getString("Placeholder_Symbols.Dual_Wield_Split", " &7| ");
                // OFF HAND < dual wield split > MAIN HAND
                if (mainWeapon.equals(offWeapon)) {
                    // Same weapon, use this info display for both
                    builder.append(PlaceholderAPI.applyPlaceholders(this.dualWieldOffBossBar != null ? this.dualWieldOffBossBar : this.bossBar, player, offStack, offWeapon))
                            .append(dualWieldSplit)
                            .append(PlaceholderAPI.applyPlaceholders(this.dualWieldMainBossBar != null ? this.dualWieldMainBossBar : this.bossBar, player, mainStack, mainWeapon));
                } else {
                    // Other weapon, use other info display
                    WeaponInfoDisplay other = getConfigurations().getObject((isKnownMain ? mainWeapon : offWeapon) + ".Info.Weapon_Info_Display", WeaponInfoDisplay.class);
                    builder.append(PlaceholderAPI.applyPlaceholders(isKnownMain && other != null ? (other.dualWieldOffBossBar != null ? other.dualWieldOffBossBar : other.bossBar)
                                    : (this.dualWieldOffBossBar != null ? this.dualWieldOffBossBar : this.bossBar), player, offStack, offWeapon))
                            .append(dualWieldSplit)
                            .append(PlaceholderAPI.applyPlaceholders(isKnownMain || other == null ? (this.dualWieldMainBossBar != null ? this.dualWieldMainBossBar : this.bossBar)
                                    : (other.dualWieldMainBossBar != null ? other.dualWieldMainBossBar : other.bossBar), player, mainStack, mainWeapon));
                }
            } else {
                if (isKnownMain) {
                    builder.append(PlaceholderAPI.applyPlaceholders(bossBar, player, mainStack, mainWeapon));
                } else {
                    builder.append(PlaceholderAPI.applyPlaceholders(bossBar, player, offStack, offWeapon));
                }
            }

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
                magazineProgress = isKnownMain ? getMagazineProgress(mainStack, mainWeapon) : getMagazineProgress(offStack, offWeapon);
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

        if (showAmmoInExpLevel || showAmmoInExpProgress) {
            if (magazineProgress == -1) {
                magazineProgress = isKnownMain ? getMagazineProgress(mainStack, mainWeapon) : getMagazineProgress(offStack, offWeapon);
            }

            int lastExpTask = messageHelper.getExpTask();
            if (lastExpTask != 0) Bukkit.getServer().getScheduler().cancelTask(lastExpTask);

            if (CompatibilityAPI.getVersion() < 1.15) {
                CompatibilityAPI.getCompatibility().sendPackets(player,
                        ReflectionUtil.newInstance(packetPlayOutExperienceConstructor,
                                showAmmoInExpProgress ? (float) (magazineProgress != -1 ? magazineProgress : isKnownMain ? getMagazineProgress(mainStack, mainWeapon) : getMagazineProgress(offStack, offWeapon)) : player.getExp(),
                                player.getTotalExperience(),
                                showAmmoInExpLevel ? isKnownMain ? getAmmoLeft(mainStack, mainWeapon) : getAmmoLeft(offStack, offWeapon) : player.getLevel()));
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
                player.sendExperienceChange(showAmmoInExpProgress ? (float) (magazineProgress != -1 ? magazineProgress : isKnownMain ? getMagazineProgress(mainStack, mainWeapon) : getMagazineProgress(offStack, offWeapon)) : player.getExp(),
                        showAmmoInExpLevel ? isKnownMain ? getAmmoLeft(mainStack, mainWeapon) : getAmmoLeft(offStack, offWeapon) : player.getLevel());
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
        return (double) getWeaponHandler().getReloadHandler().getAmmoLeft(weaponStack, weaponTitle) / (double) getConfigurations().getInt(weaponTitle + ".Reload.Magazine_Size");
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

        String dualWieldMainActionBar = data.of("Dual_Wield.Main_Hand.Action_Bar_Message").assertType(String.class).get(null);
        String dualWieldMainBossBar = data.of("Dual_Wield.Main_Hand.Boss_Bar_Title").assertType(String.class).get(null);
        String dualWieldOffActionBar = data.of("Dual_Wield.Off_Hand.Action_Bar_Message").assertType(String.class).get(null);
        String dualWieldOffBossBar = data.of("Dual_Wield.Off_Hand.Boss_Bar_Title").assertType(String.class).get(null);

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