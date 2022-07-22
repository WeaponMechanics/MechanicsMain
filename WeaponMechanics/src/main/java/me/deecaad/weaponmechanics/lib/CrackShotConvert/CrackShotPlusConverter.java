package me.deecaad.weaponmechanics.lib.CrackShotConvert;

import com.shampaggon.crackshot.MaterialManager;
import me.DeeCaaD.CrackShotPlus.CSPapi;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CrackShotPlusConverter {

    public void convertOneKey(YamlConfiguration configuration, String key, YamlConfiguration outputConfiguration) {
        for (Paths path : Paths.values()) {
            path.convert(key + "." + path.from, key + "." + path.to, path.type, configuration, outputConfiguration);
        }
    }

    private enum Paths {

        // DAMAGE
        TITLE_SHOOTER_HEAD("Damage.Title_And_Subtitle.Shooter.Headshot.Title", "Damage.Head.Shooter_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_SHOOTER_HEAD("Damage.Title_And_Subtitle.Shooter.Headshot.Subtitle", "Damage.Head.Shooter_Mechanics.Message.Title.Subtitle", Type.STR),
        TITLE_VICTIM_HEAD("Damage.Title_And_Subtitle.Victim.Headshot.Title", "Damage.Head.Victim_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_VICTIM_HEAD("Damage.Title_And_Subtitle.Victim.Headshot.Subtitle", "Damage.Head.Victim_Mechanics.Message.Title.Subtitle", Type.STR),
        TITLE_SHOOTER_BACK("Damage.Title_And_Subtitle.Shooter.Backstab.Title", "Damage.Backstab.Shooter_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_SHOOTER_BACK("Damage.Title_And_Subtitle.Shooter.Backstab.Subtitle", "Damage.Backstab.Shooter_Mechanics.Message.Title.Subtitle", Type.STR),
        TITLE_VICTIM_BACK("Damage.Title_And_Subtitle.Victim.Backstab.Title", "Damage.Backstab.Victim_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_VICTIM_BACK("Damage.Title_And_Subtitle.Victim.Backstab.Subtitle", "Damage.Backstab.Victim_Mechanics.Message.Title.Subtitle", Type.STR),
        TITLE_SHOOTER_CRIT("Damage.Title_And_Subtitle.Shooter.Critical_Hit.Title", "Damage.Critical_Hit.Shooter_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_SHOOTER_CRIT("Damage.Title_And_Subtitle.Shooter.Critical_Hit.Subtitle", "Damage.Critical_Hit.Shooter_Mechanics.Message.Title.Subtitle", Type.STR),
        TITLE_VICTIM_CRIT("Damage.Title_And_Subtitle.Victim.Critical_Hit.Title", "Damage.Critical_Hit.Victim_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_VICTIM_CRIT("Damage.Title_And_Subtitle.Victim.Critical_Hit.Subtitle", "Damage.Critical_Hit.Victim_Mechanics.Message.Title.Subtitle", Type.STR),
        TITLE_SHOOTER_HIT("Damage.Title_And_Subtitle.Shooter.Hit.Title", "Damage.Shooter_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_SHOOTER_HIT("Damage.Title_And_Subtitle.Shooter.Hit.Subtitle", "Damage.Shooter_Mechanics.Message.Title.Subtitle", Type.STR),
        TITLE_VICTIM_HIT("Damage.Title_And_Subtitle.Victim.Hit.Title", "Damage.Victim_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_VICTIM_HIT("Damage.Title_And_Subtitle.Victim.Hit.Subtitle", "Damage.Victim_Mechanics.Message.Title.Subtitle", Type.STR),

        SPAWN_FIREWORK("Damage.Spawn_Firework.", "Damage.Victim_Mechanics.Firework.Item.", Type.STR, new FireworkConvert()),

        SOUNDS_SHOOTER_HEAD("Damage.Custom_Sounds.Shooter_Location.Headshot", "Damage.Head.Shooter_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_VICTIM_HEAD("Damage.Custom_Sounds.Victim_Location.Headshot", "Damage.Head.Victim_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_SHOOTER_BACK("Damage.Custom_Sounds.Shooter_Location.Backstab", "Damage.Backstab.Shooter_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_VICTIM_BACK("Damage.Custom_Sounds.Victim_Location.Backstab", "Damage.Backstab.Victim_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_SHOOTER_CRIT("Damage.Custom_Sounds.Shooter_Location.Critical_Hit", "Damage.Critical_Hit.Shooter_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_VICTIM_CRIT("Damage.Custom_Sounds.Victim_Location.Critical_Hit", "Damage.Critical_Hit.Victim_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_SHOOTER_HIT("Damage.Custom_Sounds.Shooter_Location.Hit", "Damage.Shooter_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_VICTIM_HIT("Damage.Custom_Sounds.Victim_Location.Hit", "Damage.Victim_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),

        COMMAND_SHOOTER_HEAD("Damage.Run_Command.Headshot.Command", "Damage.Head.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_HEAD("Damage.Run_Command.Headshot.Command", "Damage.Head.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),
        COMMAND_SHOOTER_BACK("Damage.Run_Command.Backstab.Command", "Damage.Backstab.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_BACK("Damage.Run_Command.Backstab.Command", "Damage.Backstab.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),
        COMMAND_SHOOTER_CRIT("Damage.Run_Command.Critical_Hit.Command", "Damage.Critical_Hit.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_CRIT("Damage.Run_Command.Critical_Hit.Command", "Damage.Critical_Hit.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),
        COMMAND_SHOOTER_HIT("Damage.Run_Command.Hit.Command", "Damage.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_HIT("Damage.Run_Command.Hit.Command", "Damage.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),

        // EXPLODE
        SOUNDS_AIRSTRIKE("Explode.Custom_Sounds.Explode_Location.Airstrike", "Explosion.Airstrike.Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_SPLIT("Explode.Custom_Sounds.Explode_Location.Split", "Explosion.Cluster_Bomb.Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_EXPLODE("Explode.Custom_Sounds.Explode_Location.Explode", "Explosion.Mechanics.Sounds", Type.STR, new CustomSoundConvert()),

        // HELD
        SOUNDS_HELD("Held.Custom_Held_Sound", "Info.Weapon_Equip_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        WEAPON_WEIGHT("Held.Weapon_Weight", "Info.Weapon_Item.Attributes", Type.DOUBLE, new GeneralObjectModifier(x -> Collections.singletonList("GENERIC_MOVEMENT_SPEED-" + x))),
        HIDE_ATTRIBUTES("Held.Hide_Attributes", "Info.Weapon_Item.Hide_Flags", Type.BOOL),
        DURABILITY("Held.Durability", "Info.Weapon_Item.Durability", Type.INT),
        CUSTOM_MODEL_DATA("Held.Custom_Model_Data", "Info.Weapon_Item.Custom_Model_Data", Type.INT),
        UPDATE_LORE("Held.Update_Lore.Lore", "Info.Weapon_Item.Lore", Type.LIST),

        // KILL
        COMMAND_SHOOTER_KILL("Kill.Command.Run_Command", "Damage.Kill.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_KILL("Kill.Command.Run_Command", "Damage.Kill.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),
        TITLE_SHOOTER_KILL("Kill.Title_And_Subtitle.Title", "Damage.Kill.Shooter_Mechanics.Message.Title.Title", Type.STR),
        SUBTITLE_SHOOTER_KILL("Kill.Title_And_Subtitle.Subtitle", "Damage.Kill.Shooter_Mechanics.Message.Title.Subtitle", Type.STR),
        KILLFEED("Kill.Killfeed.", "Damage.Kill.Shooter_Mechanics.Message.", Type.STR, new KillfeedConvert()),

        // RELOAD
        SOUNDS_PRE_RELOAD_START("Reload.Custom_Pre_Reload_Sound", "Reload.Start_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_RELOAD_START("Reload.Custom_Reload_Sound", "Reload.Start_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_RELOAD_COMPLETE("Reload.Custom_Reload_Complete_Sound", "Reload.Finish_Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        COMMAND_RELOAD_START("Reload.Reload_Run_Command", "Reload.Start_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_RELOAD_COMPLETE("Reload.Reload_Complete_Run_Command", "Reload.Finish_Mechanics.Commands", Type.STR, new CommandConvert(true)),

        // SCOPE
        SOUNDS_SCOPE("Scope.Custom_Scope_Sound", "Scope.Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_SCOPE_END("Scope.Custom_Scope_End_Sound", "Scope.Zoom_Off.Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SECOND_ZOOM_STACKS("Scope.Second_Zoom.Amount", "Scope.Zoom_Stacking.Stacks", Type.INT, new GeneralObjectModifier(x ->
                Collections.singletonList(NumberUtil.lerp(1, 5, (int) x > 6 ? 6 : ((double) ((int) x)) / 6)))),

        // SHOOT
        INVISIBLE_PROJECTILES("Shoot.Invisible_Projectiles", "Projectile.Projectile_Settings.Type", Type.BOOL, new GeneralObjectModifier(x -> "INVISIBLE")),
        SOUNDS_PREPARE_SHOOT("Shoot.Custom_Prepare_Shoot_Sound", "Shoot.Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_PRE_SHOOT("Shoot.Custom_Pre_Shoot_Sound", "Shoot.Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        SOUNDS_SHOOT("Shoot.Custom_Shoot_Sound", "Shoot.Mechanics.Sounds", Type.STR, new CustomSoundConvert()),
        COMMAND_SHOOT("Shoot.Shoot_Run_Command", "Shoot.Mechanics.Commands", Type.STR, new CommandConvert(true)),
        BOUNCING_PROJECTILES("Shoot.Bouncing_Projectiles.", "Projectile.Bouncy.", Type.STR, new BouncingProjectilesConvert()),
        CAMERA_RECOIL("Shoot.Camera_Recoil.", "Shoot.Recoil.", Type.DOUBLE, new CameraRecoilConvert()),

        // SKIN
        SKIN("Skin.Default_Skin", "Skin.", Type.STR, new SkinConvert()),

        DUMMY(null, null, Type.STR);

        private final String from;
        private final String to;
        private final Type type;
        private final Converter converter;

        Paths(String from, String to, Type type) {
            this.from = from;
            this.to = to;
            this.type = type;
            this.converter = new ValueConvert();
        }

        Paths(String from, String to, Type type, Converter converter) {
            this.from = from;
            this.to = to;
            this.type = type;
            this.converter = converter;
        }

        private void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            converter.convert(from, to, type, fromConfig, toConfig);
        }
    }

    private enum Type {
        BOOL,
        STR,
        INT,
        DOUBLE,
        LIST;

        Object get(String key) {
            switch (this) {
                case BOOL:
                    return CSPapi.getBoolean(key);
                case STR:
                    return CSPapi.getString(key);
                case INT:
                    return CSPapi.getInteger(key);
                case DOUBLE:
                    return CSPapi.getDouble(key);
                case LIST:
                    return CSPapi.getList(key);
            }
            return null;
        }
    }

    private interface Converter {

        void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig);
    }

    private static class ValueConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            Object value = type.get(from);
            if (value == null) return;

            if (type == Type.BOOL && !(Boolean) value) return;

            if (type == Type.STR) {
                value = ((String) value).replaceAll("#DAMAGED#", "%victim%")
                        .replaceAll("#PLAYER#", "%shooter%")
                        .replaceAll("#WEAPON#", "%weapon-title%")
                        .replaceAll("#KILLER#", "%shooter%")
                        .replaceAll("#KILLED#", "%victim%")
                        .replaceAll("#C#", "console:");
            }

            toConfig.set(to, value);
        }
    }

    private static class FireworkConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String shape = CSPapi.getString(from + "Shape");
            if (shape == null) return;

            Boolean flicker = CSPapi.getBoolean(from + "Flicker");
            if (flicker == null) flicker = false;

            toConfig.set(to + "Type", "firework_rocket");
            toConfig.set(to + "Firework.Power", 1);

            toConfig.set(to + "Firework.Effects", Collections.singletonList(shape + "-RED-" + flicker));
        }
    }

    private static class CustomSoundConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String customSounds = CSPapi.getString(from);
            if (customSounds == null) return;

            // Check if CS has already added bukkit sounds
            List<String> sounds = toConfig.getStringList(to);
            if (sounds == null) {
                sounds = new ArrayList<>();
            }

            for (String sound : customSounds.replaceAll(" ", "").split(",")) {
                String[] splitted = sound.split("-");

                String soundName = "custom:" + splitted[0];
                String volume = splitted[1];
                if (Double.parseDouble(volume) <= 0.05) {
                    volume = "1";
                }
                String pitch = splitted[2];
                if (Double.parseDouble(pitch) <= 0.5) {
                    pitch = "0.5";
                }
                if (Double.parseDouble(pitch) >= 2) {
                    pitch = "2.0";
                }

                sounds.add(soundName + "-" + volume + "-" + pitch);
            }

            toConfig.set(to, sounds);
        }
    }

    private static class CommandConvert implements Converter {

        private final boolean onlyShooter;

        public CommandConvert(boolean onlyShooter) {
            this.onlyShooter = onlyShooter;
        }

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String value = CSPapi.getString(from);
            if (value == null) return;

            value = value.replaceAll("#DAMAGED#", "%victim%")
                    .replaceAll("#PLAYER#", "%shooter%")
                    .replaceAll("#WEAPON#", "%weapon-title%")
                    .replaceAll("#KILLER#", "%shooter%")
                    .replaceAll("#KILLED#", "%victim%")
                    .replaceAll("#C#", "console:");

            List<String> commands = new ArrayList<>();
            for (String command : value.split(",")) {

                if (command.startsWith("#V#") && onlyShooter) continue;

                command = command.replaceFirst("#V#", "").replaceFirst("#P#", "");
                commands.add(command);
            }

            if (!commands.isEmpty()) {
                toConfig.set(to, commands);
            }
        }
    }

    private static class GeneralObjectModifier implements Converter {

        private final Function<Object, Object> function;

        public GeneralObjectModifier(Function<Object, Object> function) {
            this.function = function;
        }

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            Object value = type.get(from);
            if (value == null) return;

            if (type == Type.BOOL && !(Boolean) value) return;

            toConfig.set(to, function.apply(value));
        }
    }

    private static class KillfeedConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String title = CSPapi.getString(from + "Title");
            if (title == null) return;

            title = title.replaceAll("#KILLED#", "%victim%").replaceAll("#KILLER#", "%shooter%");

            Integer time = CSPapi.getInteger(from + "Time");
            if (time == null) time = 60;

            String barColor = CSPapi.getString(from + "Bar.Color");
            if (barColor == null) barColor = "WHITE";

            String barStyle = CSPapi.getString(from + "Bar.Style");
            if (barStyle == null) barStyle = "SEGMENTED_20 ";

            toConfig.set(to + ".Send_Globally", true);
            toConfig.set(to + ".Boss_Bar.Title", title);
            toConfig.set(to + ".Boss_Bar.Bar_Color", barColor);
            toConfig.set(to + ".Boss_Bar.Bar_Style", barStyle);
            toConfig.set(to + ".Boss_Bar.Time", time);
        }
    }

    private static class BouncingProjectilesConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            Integer bounceAmount = CSPapi.getInteger(from + "Bounce_Amount");
            if (bounceAmount == null) return;

            if (bounceAmount > 25) bounceAmount = -1;

            Double decreaseVelocityPerBounce = CSPapi.getDouble(from + "Decrese_Velocity_Per_Bounce");
            if (decreaseVelocityPerBounce == null) decreaseVelocityPerBounce = 0.6;

            toConfig.set(to + "Maximum_Bounce_Amount", bounceAmount);
            toConfig.set(to + ".Blocks.Allow_Any", true);
            toConfig.set(to + ".Blocks.Default_Speed_Multiplier", decreaseVelocityPerBounce);
            toConfig.set(to + ".Entities.Allow_Any", true);
            toConfig.set(to + ".Entities.Default_Speed_Multiplier", decreaseVelocityPerBounce);
        }
    }

    private static class CameraRecoilConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            Double upMin = CSPapi.getDouble(from + "Upwards.Minium");
            Double upMax = CSPapi.getDouble(from + "Upwards.Maxium");
            List<Double> up = new ArrayList<>();
            if (upMin != null) up.add(upMin);
            if (upMax != null) up.add(upMax);

            Double sidMin = CSPapi.getDouble(from + "Sideways.Minium");
            Double sidMax = CSPapi.getDouble(from + "Sideways.Maxium");

            List<Double> sid = new ArrayList<>();
            if (sidMin != null) sid.add(sidMin);
            if (sidMax != null) sid.add(sidMax);

            if (!up.isEmpty()) {
                toConfig.set(to + "Vertical", up);
            }

            if (!sid.isEmpty()) {
                toConfig.set(to + "Horizontal", sid);
            }
        }
    }

    private static class SkinConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String defaultSkin = CSPapi.getString(from);
            if (defaultSkin == null) return;

            String weapon = from.split("\\.")[0];
            String skinPath = weapon + "_" + defaultSkin;

            String normalType = CSPapi.getString(skinPath + ".Change_Item_Type");
            if (normalType != null) {
                toConfig.set(to + ".Default.Type", getMaterial(normalType));
                toConfig.set(weapon + ".Info.Weapon_Item.Type", getMaterial(normalType));
            }

            Integer normalDurability = CSPapi.getInteger(skinPath + ".Durability");
            if (normalDurability != null) {
                toConfig.set(weapon + ".Info.Weapon_Item.Unbreakable", true);
                toConfig.set(to + ".Default.Durability", normalDurability);
                toConfig.set(weapon + ".Info.Weapon_Item.Durability", normalDurability);
            }

            Integer normalCustomModelData = CSPapi.getInteger(skinPath + ".Custom_Model_Data");
            if (normalCustomModelData != null) {
                toConfig.set(to + ".Default.Custom_Model_Data", normalCustomModelData);
                toConfig.set(weapon + ".Info.Weapon_Item.Custom_Model_Data", normalCustomModelData);
            }

            String reloadPath = skinPath + ((CSPapi.getBoolean(weapon + ".Reload.Reload_Skin") != null && CSPapi.getBoolean(weapon + ".Reload.Reload_Skin")) ? "_Reload" : ".Reload");

            String reloadType = CSPapi.getString(reloadPath + ".Change_Item_Type");
            if (reloadType != null) toConfig.set(to + ".Reload.Type", reloadType);

            Integer reloadDurability = CSPapi.getInteger(reloadPath + ".Durability");
            if (reloadDurability != null) {
                toConfig.set(weapon + ".Info.Weapon_Item.Unbreakable", true);
                toConfig.set(to + ".Reload.Durability", reloadDurability);
            }

            Integer reloadCustomModelData = CSPapi.getInteger(reloadPath + ".Custom_Model_Data");
            if (reloadCustomModelData != null) toConfig.set(to + ".Reload.Custom_Model_Data", reloadCustomModelData);

            String scopePath = skinPath + ((CSPapi.getBoolean(weapon + ".Scope.Scope_Skin") != null && CSPapi.getBoolean(weapon + ".Scope.Scope_Skin")) ? "_Scope" : ".Scope");

            String scopeType = CSPapi.getString(scopePath + ".Change_Item_Type");
            if (scopeType != null) toConfig.set(to + ".Scope.Type", scopeType);

            Integer scopeDurability = CSPapi.getInteger(scopePath + ".Durability");
            if (scopeDurability != null) {
                toConfig.set(weapon + ".Info.Weapon_Item.Unbreakable", true);
                toConfig.set(to + ".Scope.Durability", scopeDurability);
            }

            Integer scopeCustomModelData = CSPapi.getInteger(scopePath + ".Custom_Model_Data");
            if (scopeCustomModelData != null) toConfig.set(to + ".Scope.Custom_Model_Data", scopeCustomModelData);
        }
    }

    private static String getMaterial(String type) {
        if (type == null) return null;

        try {
            Material material = MaterialManager.getMaterial(type);
            if (material != null) return material.name();
        } catch (Exception e) {
            String materialName = StringUtil.didYouMean(type, EnumUtil.getOptions(Material.class));
            WeaponMechanics.debug.error("Invalid material: " + type + " swapped to: " + materialName);
            return materialName;
        }

        String materialName = StringUtil.didYouMean(type, EnumUtil.getOptions(Material.class));
        WeaponMechanics.debug.error("Invalid material: " + type + " swapped to: " + materialName);
        return materialName;
    }
}