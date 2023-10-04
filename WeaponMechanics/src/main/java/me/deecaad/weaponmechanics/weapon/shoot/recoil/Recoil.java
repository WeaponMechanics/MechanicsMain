package me.deecaad.weaponmechanics.weapon.shoot.recoil;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.weaponmechanics.compatibility.IWeaponCompatibility;
import me.deecaad.weaponmechanics.compatibility.WeaponCompatibilityAPI;
import me.deecaad.weaponmechanics.wrappers.HandData;
import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.VSE;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static me.deecaad.weaponmechanics.WeaponMechanics.getPlayerWrapper;

public class Recoil implements Serializer<Recoil> {

    public static long MILLIS_BETWEEN_ROTATIONS = 20;
    private static final Timer TIMER = new Timer();
    private static final IWeaponCompatibility weaponCompatibility = WeaponCompatibilityAPI.getWeaponCompatibility();

    private long pushTime;
    private long recoverTime;
    private List<Float> randomHorizontal;
    private List<Float> randomVertical;
    private RecoilPattern recoilPattern;
    private ModifyRecoilWhen modifyRecoilWhen;

    /**
     * Default constructor for serializer
     */
    public Recoil() {
    }

    public Recoil(long pushTime, long recoverTime, List<Float> randomHorizontal, List<Float> randomVertical, RecoilPattern recoilPattern, ModifyRecoilWhen modifyRecoilWhen) {
        this.pushTime = pushTime;
        this.recoverTime = recoverTime;
        this.randomHorizontal = randomHorizontal;
        this.randomVertical = randomVertical;
        this.recoilPattern = recoilPattern;
        this.modifyRecoilWhen = modifyRecoilWhen;
    }

    public void start(Player player, boolean mainHand) {

        if (Bukkit.getPluginManager().getPlugin("Vivecraft-Spigot-Extensions") != null
                && VSE.isVive(player)) {
            // Don't try to use this kind of recoil with Vivecraft players
            return;
        }

        PlayerWrapper playerWrapper = getPlayerWrapper(player);
        if (pushTime == 0 && recoverTime == 0) {
            // No need for task as rotation time and recover time are 0
            float rotateYaw = randomHorizontal.get(NumberUtil.random(randomHorizontal.size()));
            float rotatePitch = randomVertical.get(NumberUtil.random(randomVertical.size()));
            weaponCompatibility.modifyCameraRotation(playerWrapper.getPlayer(), rotateYaw, rotatePitch, false);
            return;
        }
        HandData handData = mainHand ? playerWrapper.getMainHandData() : playerWrapper.getOffHandData();
        RecoilTask recoilTask = handData.getRecoilTask();

        if (recoilTask == null) {
            // Normally shoot, recoil, recover
            recoilTask = new RecoilTask(playerWrapper, handData, this);
            handData.setRecoilTask(recoilTask);
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

    public ModifyRecoilWhen getModifyRecoilWhen() {
        return modifyRecoilWhen;
    }

    @Override
    public String getKeyword() {
        return "Recoil";
    }

    @Override
    @NotNull
    public Recoil serialize(@NotNull SerializeData data) throws SerializerException {
        RecoilPattern recoilPattern = data.of("Recoil_Pattern").serialize(RecoilPattern.class);
        List<Float> randomHorizontal = convertToFloatList(data.ofList("Horizontal"));
        List<Float> randomVertical = convertToFloatList(data.ofList("Vertical"));

        if (recoilPattern == null && randomHorizontal == null && randomVertical == null) {
            throw data.exception(null, "When using Recoil, you need to use at least one of: 'Recoil_Pattern', 'Horizontal', 'Vertical'");
        }

        ModifyRecoilWhen modifyRecoilWhen = (ModifyRecoilWhen) data.of("Modify_Recoil_When").serialize(new ModifyRecoilWhen());
        long pushTime = data.of("Push_Time").assertPositive().getNumber(0L).longValue();
        long recoverTime = data.of("Recover_Time").assertPositive().getNumber(0L).longValue();

        return new Recoil(pushTime, recoverTime, randomHorizontal, randomVertical, recoilPattern, modifyRecoilWhen);
    }

    private List<Float> convertToFloatList(SerializeData.ConfigListAccessor accessor) throws SerializerException {
        List<String[]> list = accessor.addArgument(double.class, true).assertList().get();
        if (list == null || list.isEmpty())
            return null;

        List<Float> floatList = new ArrayList<>();
        for (String[] split : list) {
            floatList.add(Float.parseFloat(split[0]));
        }

        return floatList;
    }
}