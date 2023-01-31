package me.deecaad.weaponmechanics.lib.CrackShotConvert;

import com.shampaggon.crackshot.MaterialManager;
import me.DeeCaaD.CrackShotPlus.CSPapi;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

        SOUNDS_SHOOTER_HEAD("Damage.Custom_Sounds.Shooter_Location.Headshot", "Damage.Head.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_VICTIM_HEAD("Damage.Custom_Sounds.Victim_Location.Headshot", "Damage.Head.Mechanics", Type.STR, new CustomSoundConvert(true)),
        SOUNDS_SHOOTER_BACK("Damage.Custom_Sounds.Shooter_Location.Backstab", "Damage.Backstab.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_VICTIM_BACK("Damage.Custom_Sounds.Victim_Location.Backstab", "Damage.Backstab.Mechanics", Type.STR, new CustomSoundConvert(true)),
        SOUNDS_SHOOTER_CRIT("Damage.Custom_Sounds.Shooter_Location.Critical_Hit", "Damage.Critical_Hit.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_VICTIM_CRIT("Damage.Custom_Sounds.Victim_Location.Critical_Hit", "Damage.Critical_Hit.Mechanics", Type.STR, new CustomSoundConvert(true)),
        SOUNDS_SHOOTER_HIT("Damage.Custom_Sounds.Shooter_Location.Hit", "Damage.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_VICTIM_HIT("Damage.Custom_Sounds.Victim_Location.Hit", "Damage.Mechanics", Type.STR, new CustomSoundConvert(true)),

        COMMAND_SHOOTER_HEAD("Damage.Run_Command.Headshot.Command", "Damage.Head.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_HEAD("Damage.Run_Command.Headshot.Command", "Damage.Head.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),
        COMMAND_SHOOTER_BACK("Damage.Run_Command.Backstab.Command", "Damage.Backstab.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_BACK("Damage.Run_Command.Backstab.Command", "Damage.Backstab.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),
        COMMAND_SHOOTER_CRIT("Damage.Run_Command.Critical_Hit.Command", "Damage.Critical_Hit.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_CRIT("Damage.Run_Command.Critical_Hit.Command", "Damage.Critical_Hit.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),
        COMMAND_SHOOTER_HIT("Damage.Run_Command.Hit.Command", "Damage.Shooter_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_VICTIM_HIT("Damage.Run_Command.Hit.Command", "Damage.Victim_Mechanics.Commands", Type.STR, new CommandConvert(false)),

        // EXPLODE
        SOUNDS_AIRSTRIKE("Explode.Custom_Sounds.Explode_Location.Airstrike", "Explosion.Airstrike.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_SPLIT("Explode.Custom_Sounds.Explode_Location.Split", "Explosion.Cluster_Bomb.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_EXPLODE("Explode.Custom_Sounds.Explode_Location.Explode", "Explosion.Mechanics", Type.STR, new CustomSoundConvert()),

        // HELD
        SOUNDS_HELD("Held.Custom_Held_Sound", "Info.Weapon_Equip_Mechanics", Type.STR, new CustomSoundConvert()),
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
        SOUNDS_PRE_RELOAD_START("Reload.Custom_Pre_Reload_Sound", "Reload.Start_Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_RELOAD_START("Reload.Custom_Reload_Sound", "Reload.Start_Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_RELOAD_COMPLETE("Reload.Custom_Reload_Complete_Sound", "Reload.Finish_Mechanics", Type.STR, new CustomSoundConvert()),
        COMMAND_RELOAD_START("Reload.Reload_Run_Command", "Reload.Start_Mechanics.Commands", Type.STR, new CommandConvert(true)),
        COMMAND_RELOAD_COMPLETE("Reload.Reload_Complete_Run_Command", "Reload.Finish_Mechanics.Commands", Type.STR, new CommandConvert(true)),

        // SCOPE
        SOUNDS_SCOPE("Scope.Custom_Scope_Sound", "Scope.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_SCOPE_END("Scope.Custom_Scope_End_Sound", "Scope.Zoom_Off.Mechanics", Type.STR, new CustomSoundConvert()),
        SECOND_ZOOM_STACKS("Scope.Second_Zoom.Amount", "Scope.Zoom_Stacking.Stacks", Type.INT, new GeneralObjectModifier(x ->
                Collections.singletonList(NumberUtil.lerp(1, 5, (int) x > 6 ? 6 : ((double) ((int) x)) / 6)))),

        // SHOOT
        INVISIBLE_PROJECTILES("Shoot.Invisible_Projectiles", "Projectile.Projectile_Settings.Type", Type.BOOL, new GeneralObjectModifier(x -> "INVISIBLE")),
        SOUNDS_PREPARE_SHOOT("Shoot.Custom_Prepare_Shoot_Sound", "Shoot.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_PRE_SHOOT("Shoot.Custom_Pre_Shoot_Sound", "Shoot.Mechanics", Type.STR, new CustomSoundConvert()),
        SOUNDS_SHOOT("Shoot.Custom_Shoot_Sound", "Shoot.Mechanics", Type.STR, new CustomSoundConvert()),
        COMMAND_SHOOT("Shoot.Shoot_Run_Command", "Shoot.Mechanics.Commands", Type.STR, new CommandConvert(true)),
        BOUNCING_PROJECTILES("Shoot.Bouncing_Projectiles.", "Projectile.Bouncy.", Type.STR, new BouncingProjectilesConvert()),
        CAMERA_RECOIL("Shoot.Camera_Recoil.", "Shoot.Recoil.", Type.DOUBLE, new CameraRecoilConvert()),

        // SKIN
        SKIN("Skin.Default_Skin", "Skin.", Type.STR, new SkinConvert()),

        // Cosmetics
        TRAIL("Shoot.Trails.Default_Trail", "Trail.", Type.STR, new TrailConvert()),
        BLOCK_CRACK("Hit_Block.Break_Blocks", "Cosmetics.Block_Damage.", Type.STR, new BlockDamageConvert()),
        VISUAL_RELOAD("Reload.Visual_Reload.Default_Visual_Reload", "Show_Time.Reload.", Type.STR, new VisualReloadConvert()),

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
            // This is not always null... This only happens because the shaded
            // version of CSP is incomplete and returns null.
            return switch (this) {
                case BOOL -> CSPapi.getBoolean(key);
                case STR -> CSPapi.getString(key);
                case INT -> CSPapi.getInteger(key);
                case DOUBLE -> CSPapi.getDouble(key);
                case LIST -> CSPapi.getList(key);
            };
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

                value = StringUtil.colorAdventure((String) value);
            } else if (type == Type.LIST) {
                List<?> currentList = (List<?>) value;
                List<String> newList = new ArrayList<>(currentList.size());
                currentList.forEach(line -> newList.add(StringUtil.colorAdventure((String) line)));
                value = newList;
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

        private boolean isTarget;

        public CustomSoundConvert() {
            this.isTarget = false;
        }

        public CustomSoundConvert(boolean isTarget) {
            this.isTarget = isTarget;
        }

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String customSounds = CSPapi.getString(from);
            if (customSounds == null) return;

            List<String> mechanics = toConfig.getStringList(to);

            for (String sound : customSounds.replaceAll(" ", "").split(",")) {
                String[] splitted = sound.split("-");

                String soundName = splitted[0];
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

                if (this.isTarget) {
                    mechanics.add("CustomSound{sound=%s, volume=%s, pitch=%s} @Target{}".formatted(soundName, volume, pitch));
                } else {
                    mechanics.add("CustomSound{sound=%s, volume=%s, pitch=%s}".formatted(soundName, volume, pitch));
                }
            }

            toConfig.set(to, mechanics);
        }
    }

    private record CommandConvert(boolean onlyShooter) implements Converter {

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

    private record GeneralObjectModifier(Function<Object, Object> function) implements Converter {

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
            if (barStyle == null) barStyle = "NOTCHED_20";

            try {
                BossBar.Overlay.valueOf(barStyle);
            } catch (IllegalArgumentException e) {
                String temp = barStyle;
                barStyle = StringUtil.didYouMean(barStyle, EnumUtil.getOptions(BossBar.Overlay.class));
                WeaponMechanics.debug.error("Invalid boss bar style: " + temp + " swapped to: " + barStyle);
            }


            toConfig.set(to + ".Send_All_Server", true);
            toConfig.set(to + ".Boss_Bar.Title", title);
            toConfig.set(to + ".Boss_Bar.Color", barColor);
            toConfig.set(to + ".Boss_Bar.Style", barStyle);
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
            String defaultSkin = fromConfig.getString(from);
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

    private static class TrailConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String defaultTrail = CSPapi.getString(from);
            if (defaultTrail == null) return;


            Double d = CSPapi.getDouble(defaultTrail + ".Trail_Catch.Space_Between_Trails");
            toConfig.set(to + "Distance_Between_Particles", d == null ? 0.33 : d);
            toConfig.set(to + "Shape", "LINE");
            toConfig.set(to + "Particle_Chooser", "LOOP");

            toConfig.set(to + "Particles.Particle_1.Type", CSPapi.getString(defaultTrail + ".Trail"));
            if (CSPapi.getString(defaultTrail + ".Trail_Color") != null)
                toConfig.set(to + "Particles.Particle_1.Color", CSPapi.getString(defaultTrail + ".Trail_Color"));
            Integer i = CSPapi.getInteger(defaultTrail + ".Trail_Settings.Particle_Count");
            if (i != null) toConfig.set(to + ".Particles.Particle_1.Count", i);
        }
    }

    private static class BlockDamageConvert implements Converter {

        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {

            // If Break_Blocks doens't exist, try Block_Crack_Animation
            if (CSPapi.getString(from + ".Blacklist") == null) {
                String[] split = from.split("\\.");
                String[] copy = new String[split.length - 1];
                System.arraycopy(split, 0, copy, 0, copy.length);
                String path = String.join(".", copy);

                int min = fromConfig.getInt(path + ".Block_Crack_Animation.Minium_Crack", 0);
                int max = fromConfig.getInt(path + ".Block_Crack_Animation.Maxium_Crack", 0);
                if (min == 0 && max == 0)
                    return;

                // This is pretty "rough". WMC system doesn't match up
                // perfectly with CSP, so this is only 50% accurate.
                toConfig.set(to + "Damage_Per_Hit", Math.max(1, min));
                toConfig.set(to + "Default_Block_Durability", Math.max(1, max));
                toConfig.set(to + "Blacklist", true);
                return;
            }

            toConfig.set(to + "Break_Blocks", true);
            toConfig.set(to + "Ticks_Before_Regenerate", CSPapi.getString(from + ".Regen_Blocks_After_Milliseconds"));
            toConfig.set(to + "Blacklist", CSPapi.getBoolean(from + ".Blacklist"));
            toConfig.set(to + "Block_List", CSPapi.getList(from + ".Blocks_List"));
        }
    }

    private static class VisualReloadConvert implements Converter {
        @Override
        public void convert(String from, String to, Type type, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String visualReload = CSPapi.getString(from);
            if (visualReload == null) return;

            String weapon = from.split("\\.")[0];
            String reloadPath = visualReload + ".";
            toConfig.set(to + "Action_Bar", convert(CSPapi.getString(reloadPath + "Reload_Message.Action_Bar")));
            toConfig.set(to + "Title", convert(CSPapi.getString(reloadPath + "Reload_Message.Title")));
            toConfig.set(to + "Subtitle", convert(CSPapi.getString(reloadPath + "Reload_Message.Subtitle")));
            toConfig.set(to + "Boss_Bar.Message", convert(CSPapi.getString(reloadPath + "Reload_Message.Boss_Bar.Title")));
            toConfig.set(to + "Boss_Bar.Color", CSPapi.getString(reloadPath + "Reload_Message.Boss_Bar.Settings.Color"));
            toConfig.set(to + "Boss_Bar.Style", getBossBarStyle(CSPapi.getString(reloadPath + "Reload_Message.Boss_Bar.Settings.Style")));

            toConfig.set(to + "Action_Bar_Cancelled", "<red>Reload Cancelled");
            toConfig.set(to + "Exp", CSPapi.getString(reloadPath + "Reload_Message.Exp"));

            // Bar stuff
            toConfig.set(to + "Bar.Left_Color", convert(CSPapi.getString(reloadPath + "Left_Color")));
            toConfig.set(to + "Bar.Right_Color", convert(CSPapi.getString(reloadPath + "Right_Color")));
            toConfig.set(to + "Bar.Left_Symbol", convert(CSPapi.getString(reloadPath + "Symbol")));
            toConfig.set(to + "Bar.Symbol_Amount", CSPapi.getInteger(reloadPath + "Symbol_Amount"));
        }

        private static String convert(String msg) {
            if (msg == null)
                return null;

            msg = msg.replaceAll("#[Bb][Aa][Rr]#", "%bar%");
            msg = msg.replaceAll("#[Tt][Ii][Mm][Ee]#", "%time%");
            return StringUtil.colorAdventure(msg);
        }
    }

    private static String getMaterial(String type) {
        if (type == null) return null;

        try {
            Material material = MaterialManager.getMaterial(type);
            if (material != null) return material.name();
        } catch (NoClassDefFoundError | Exception e) {
            // If CrackShot is outdated... or other exception
            try {
                return Material.valueOf(type.toUpperCase()).name();
            } catch (IllegalArgumentException ignored) {
            }
        }

        String materialName = StringUtil.didYouMean(type, EnumUtil.getOptions(Material.class));
        WeaponMechanics.debug.error("Invalid material: " + type + " swapped to: " + materialName);
        return materialName;
    }

    private static String getBossBarStyle(String type) {
        if (type == null) return null;

        type = type.trim().toUpperCase(Locale.ROOT);
        if ("SOLID".equals(type))
            return "PROGRESS";
        else
            return "NOTCHED_" + type.substring("SEGMENTED_".length());
    }
}