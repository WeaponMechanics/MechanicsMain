package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.compatibility.shoot.IShootCompatibility;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.NumberUtils;
import me.deecaad.weaponmechanics.wrappers.IPlayerWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import static me.deecaad.weaponmechanics.WeaponMechanics.getPlayerWrapper;

public class Recoil implements Serializer<Recoil> {

    public static long MILLIS_BETWEEN_ROTATIONS = 5;
    private static final Timer TIMER = new Timer();
    private static final IShootCompatibility shootCompatibility = CompatibilityAPI.getCompatibility().getShootCompatibility();

    private long pushTime;
    private long recoverTime;
    private List<Float> randomHorizontal;
    private List<Float> randomVertical;
    private RecoilPattern recoilPattern;

    /**
     * Empty constructor to be used as serializer
     */
    public Recoil() { }

    public Recoil(long pushTime, long recoverTime, List<Float> randomHorizontal, List<Float> randomVertical, RecoilPattern recoilPattern) {
        this.pushTime = pushTime;
        this.recoverTime = recoverTime;
        this.randomHorizontal = randomHorizontal;
        this.randomVertical = randomVertical;
        this.recoilPattern = recoilPattern;
    }

    public void start(Player player) {
        IPlayerWrapper playerWrapper = getPlayerWrapper(player);
        if (pushTime == 0 && recoverTime == 0) {
            // No need for task as rotation time and recover time are 0
            float rotateYaw = randomHorizontal.get(NumberUtils.random(randomHorizontal.size()));
            float rotatePitch = randomVertical.get(NumberUtils.random(randomVertical.size()));
            shootCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), rotateYaw, rotatePitch, false);
            return;
        }
        RecoilTask recoilTask = playerWrapper.getRecoilTask();
        if (recoilTask == null) {
            // Normally shoot, recoil, recover
            recoilTask = new RecoilTask(playerWrapper, this);
            playerWrapper.setRecoilTask(recoilTask);
            TIMER.scheduleAtFixedRate(recoilTask, 0, MILLIS_BETWEEN_ROTATIONS);
            return;
        }
        // Shoot during recoil
        //  -> continue from current recoil with new one (cancel last recoil)
        // Shoot during recover
        //  -> continue from current recover spot with new recoil (cancel last recover)
        recoilTask.setRecoil(this);
    }

    public long getPushTime() {
        return pushTime;
    }

    public long getRecoverTime() {
        return recoverTime;
    }

    public List<Float> getRandomHorizontal() {
        return randomHorizontal;
    }

    public List<Float> getRandomVertical() {
        return randomVertical;
    }

    public RecoilPattern getRecoilPattern() {
        return recoilPattern;
    }

    @Override
    public String getKeyword() {
        return "Recoil";
    }

    @Override
    public Recoil serialize(File file, ConfigurationSection configurationSection, String path) {
        RecoilPattern recoilPattern = new RecoilPattern().serialize(file, configurationSection, path + ".Recoil_Pattern");
        List<Float> randomHorizontal = convertToFloatList(file, configurationSection, path + ".Horizontal");
        List<Float> randomVertical = convertToFloatList(file, configurationSection, path + ".Vertical");

        if (recoilPattern != null && randomHorizontal == null && randomVertical == null) {
            return null;
        }

        long pushTime = configurationSection.getLong(path + ".Push_Time");
        long recoverTime = configurationSection.getLong(path + ".Recover_Time");

        return new Recoil(pushTime, recoverTime, randomHorizontal, randomVertical, recoilPattern);
    }

    private List<Float> convertToFloatList(File file, ConfigurationSection configurationSection, String path) {
        List<?> list = configurationSection.getList(path);
        if (list == null || list.isEmpty()) return null;

        List<Float> floatList = new ArrayList<>();
        for (Object value : list) {
            try {
                floatList.add(Float.parseFloat(value.toString()));
            } catch (NumberFormatException e) {
                DebugUtil.log(LogLevel.ERROR,
                        "Found an invalid value in configurations!",
                        "Located at file " + file + " in " + path + " (" + value.toString() + ") in configurations",
                        "Tried to get get float from " + value.toString() + ", but it wasn't float?");
            }
        }
        if (floatList.isEmpty()) {
            DebugUtil.log(LogLevel.ERROR,
                    "For some reason any value in list wasn't valid!",
                    "Located at file " + file + " in " + path + " in configurations");
            return null;
        }
        return floatList;
    }
}