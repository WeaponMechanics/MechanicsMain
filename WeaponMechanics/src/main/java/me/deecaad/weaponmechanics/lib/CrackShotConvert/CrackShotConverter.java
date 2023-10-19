package me.deecaad.weaponmechanics.lib.CrackShotConvert;

import com.shampaggon.crackshot.MaterialManager;
import com.shampaggon.crackshot.SoundManager;
import me.deecaad.core.utils.EnumUtil;
import me.deecaad.core.utils.NumberUtil;
import me.deecaad.core.utils.StringUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.function.Function;

public class CrackShotConverter {

    public void convertOneKey(YamlConfiguration configuration, String key, YamlConfiguration outputConfiguration) {

        if (configuration.get(key + ".Reload.Reload_Amount") != null) {
            String weaponName = configuration.getString(key + ".Item_Information.Item_Name", key);
            weaponName = StringUtil.colorAdventure(weaponName);
            if (configuration.get(key + ".Firearm_Action.Type") != null) {
                outputConfiguration.set(key + ".Info.Weapon_Info_Display.Action_Bar.Message", weaponName + "%firearm-state% «%ammo-left%»%reload%");
            } else {
                outputConfiguration.set(key + ".Info.Weapon_Info_Display.Action_Bar.Message", weaponName + " «%ammo-left%»%reload%");
            }
        }
        for (Paths path : Paths.values()) {
            path.convert(key + "." + path.from, key + "." + path.to, configuration, outputConfiguration);
        }
    }

    private enum Paths {

        // ITEM INFORMATION
        WEAPON_NAME("Item_Information.Item_Name", "Info.Weapon_Item.Name"),
        WEAPON_TYPE("Item_Information.Item_Type", "Info.Weapon_Item.Type", new MaterialConvert()),
        WEAPON_LORE("Item_Information.Item_Lore", "Info.Weapon_Item.Lore", new LoreConvert()),
        SKIP_NAME_CHECK("Item_Information.Skip_Name_Check", "Info.Weapon_Converter_Check.Type"),
        SOUNDS_ACQUIRED("Item_Information.Sounds_Acquired", "Info.Weapon_Get_Mechanics", new SoundConvert()),
        MELEE_MODE("Item_Information.Melee_Mode", "Melee.Enable_Melee"),
        MELEE_ATTACHMENT("Item_Information.Melee_Attachment", "Melee.Melee_Attachment"),
        MELEE_HIT_DELAY("", "Melee.Melee_Hit_Delay", new MeleeHitDelayConvert()),

        // SHOOTING
        RIGHT_CLICK_TO_SHOOT("", "Shoot.Trigger.Main_Hand", new ShootButtonsConvert()),
        DUAL_WIELD("Shooting.Dual_Wield", "", new DualWieldConvert()), // After shoot buttons so this can override triggers
        DELAY_BETWEEN_SHOTS("Shooting.Delay_Between_Shots", "Shoot.Delay_Between_Shots", new ValueNonZeroConvert()),
        PROJECTILE_AMOUNT("Shooting.Projectile_Amount", "Shoot.Projectiles_Per_Shot", new ValueNonZeroConvert()),
        PROJECTILE_TYPE("Shooting.", "", new ProjectileTypeConvert()),
        REMOVE_BULLET_DROP("Shooting.Remove_Bullet_Drop", "Projectile.Projectile_Settings.Gravity", new ValueBooleanConvert(0.0, null)),
        PROJECTILE_SPEED("Shooting.Projectile_Speed", "Shoot.Projectile_Speed", new ValueDoubleConvert(x -> x * 2)),
        PROJECTILE_DAMAGE("Shooting.Projectile_Damage", "Damage.Base_Damage", new ValueNonZeroConvert()),
        PROJECTILE_DAMAGE_ARMOR("Shooting.Projectile_Damage", "Damage.Armor_Damage", new ValueDoubleConvert(x -> 3.0)),
        PROJECTILE_INCENDIARY("Shooting.Projectile_Incendiary.Duration", "Damage.Fire_Ticks", new ValueNonZeroConvert()),
        BULLET_SPREAD("Shooting.Bullet_Spread", "Shoot.Spread.Base_Spread", new ValueDoubleConvert(x -> x * 10)),
        SOUNDS_PROJECTILE("Shooting.Sounds_Projectile", "Projectile.Mechanics", new SoundConvert()),
        SOUNDS_SHOOT("Shooting.Sounds_Shoot", "Shoot.Mechanics", new SoundConvert()),
        REMOVAL_OR_DRAG_DELAY("Shooting.Removal_Or_Drag_Delay", "Projectile.Projectile_Settings.Maximum_Alive_Ticks", new RemovalOrDragDelayConvert()),

        // SNEAK
        // Divide with 2, since this decreases spread in WM, it doesn't set new value for it
        SNEAK_BULLET_SPREAD("Sneak.Bullet_Spread", "Shoot.Spread.Modify_Spread_When.Sneaking", new ValueDoubleConvert(x -> x == 0 ? -25 : -(x / 2 * 10))),
        SNEAK_BEFORE_SHOOTING("Shooting.Sneak_Before_Shooting", "Shoot.Trigger.Circumstance.Sneaking", new ValueBooleanConvert("REQUIRED", null)),

        // FULLY_AUTOMATIC
        FIRE_RATE("Fully_Automatic.Fire_Rate", "Shoot.Fully_Automatic_Shots_Per_Second", new ValueDoubleConvert(x -> (x * 60 + 240) / 60)),

        // BURSTFIRE
        SHOTS_PER_BURST("Burstfire.Shots_Per_Burst", "Shoot.Burst.Shots_Per_Burst", new ValueNonZeroConvert()),
        DELAY_BETWEEN_SHOTS_IN_BURST("Burstfire.Delay_Between_Shots_In_Burst", "Shoot.Burst.Ticks_Between_Each_Shot", new ValueNonZeroConvert()),

        // AMMO
        SOUNDS_OUT_OF_AMMO("Ammo.Sounds_Out_Of_Ammo", "Reload.Ammo.Out_Of_Ammo_Mechanics", new SoundConvert()),
        SOUNDS_SHOOT_WITH_NO_AMMO("Ammo.Sounds_Shoot_With_No_Ammo", "Reload.Ammo.Out_Of_Ammo_Mechanics", new SoundConvert()),

        // RELOAD
        RELOAD_TRIGGER("Reload.Enable", "Reload.Trigger.Main_Hand", new ValueBooleanConvert("drop_item", null)),
        DROP_ITEM_DENY("Reload.Enable", "Info.Cancel.Drop_Item", new ValueBooleanConvert(true, null)),
        RELOAD_AMOUNT("Reload.Reload_Amount", "Reload.Magazine_Size", new ValueNonZeroConvert()),
        RELOAD_BULLET_INDIVIDUALLY("Reload.Reload_Bullets_Individually", "Reload.Ammo_Per_Reload", new ValueBooleanConvert(1, null)),
        RELOAD_DURATION("Reload.Reload_Duration", "Reload.Reload_Duration", new ValueNonZeroConvert()),
        SOUNDS_RELOADING("Reload.Sounds_Reloading", "Reload.Start_Mechanics", new SoundConvert()),

        // FIREARM_ACTION
        FIREARM_ACTION_TYPE("Firearm_Action.Type", "Firearm_Action.Type", new FirearmActionConvert()),
        OPEN_DURATION("Firearm_Action.Open_Duration", "Firearm_Action.Open.Time", new ValueNonZeroConvert()),
        CLOSE_DURATION("Firearm_Action.Close_Duration", "Firearm_Action.Close.Time", new ValueNonZeroConvert()),
        SOUND_OPEN("Firearm_Action.Sound_Open", "Firearm_Action.Open.Mechanics", new SoundConvert()),
        SOUND_CLOSE("Firearm_Action.Sound_Close", "Firearm_Action.Close.Mechanics", new SoundConvert()),

        // HEADSHOT
        HEAD_BONUS_DAMAGE("Headshot.Bonus_Damage", "Damage.Head.Bonus_Damage"),
        HEAD_MESSAGE_SHOOTER("Headshot.Message_Shooter", "Damage.Head.Mechanics", new MessageConvert(false)),
        HEAD_MESSAGE_VICTIM("Headshot.Message_Victim", "Damage.Head.Mechanics", new MessageConvert(true)),
        HEAD_SOUNDS_SHOOTER("Headshot.Sounds_Shooter", "Damage.Head.Mechanics", new SoundConvert()),
        HEAD_SOUNDS_VICTIM("Headshot.Sounds_Victim", "Damage.Head.Mechanics", new SoundConvert(true)),

        // BACKSTAB
        BACK_BONUS_DAMAGE("Backstab.Bonus_Damage", "Damage.Backstab.Bonus_Damage"),
        BACK_MESSAGE_SHOOTER("Backstab.Message_Shooter", "Damage.Backstab.Mechanics", new MessageConvert(false)),
        BACK_MESSAGE_VICTIM("Backstab.Message_Victim", "Damage.Backstab.Mechanics", new MessageConvert(true)),
        BACK_SOUNDS_SHOOTER("Backstab.Sounds_Shooter", "Damage.Backstab.Mechanics", new SoundConvert()),
        BACK_SOUNDS_VICTIM("Backstab.Sounds_Victim", "Damage.Backstab.Mechanics", new SoundConvert(true)),

        // ABILITIES AND RECOIL
        ABILITIES_AND_RECOIL("", "", new AbilitiesAndRecoilConvert()),

        // POTION_EFFECTS
        POTION_EFFECTS("Potion_Effects.", "", new PotionEffectsConvert()),

        // FIREWORKS
        FIREWORK_PLAYER_SHOOT("Fireworks.Firework_Player_Shoot", "Shoot.Mechanics", new FireworkConvert()),
        FIREWORK_EXPLODE("Fireworks.Firework_Explode", "Explosion.Mechanics", new FireworkConvert()),
        FIREWORK_HIT("Fireworks.Firework_Hit", "Damage.Mechanics", new FireworkConvert(true)),
        FIREWORK_HEADSHOT("Fireworks.Firework_Headshot", "Damage.Head.Mechanics", new FireworkConvert(true)),
        FIREWORK_CRITICAL("Fireworks.Firework_Critical", "Damage.Critical_Hit.Mechanics", new FireworkConvert(true)),
        FIREWORK_BACKSTAB("Fireworks.Firework_Backstab", "Damage.Backstab.Mechanics", new FireworkConvert(true)),

        // SCOPE
        SCOPE_TRIGGER("", "Scope.Trigger.Main_Hand", new ScopeConvert()),
        NIGHT_VISION("Scope.Night_Vision", "Scope.Night_Vision"),
        ZOOM_AMOUNT("Scope.Zoom_Amount", "Scope.Zoom_Amount", new ValueDoubleConvert(x -> NumberUtil.lerp(1, 5, ((x > 6 ? 6 : x) / 6)))),
        // Divide with 2, since this decreases spread in WM, it doesn't set new value for it
        SCOPE_BULLET_SPREAD("Scope.Zoom_Bullet_Spread", "Shoot.Spread.Modify_Spread_When.Zooming", new ValueDoubleConvert(x -> x == 0 ? -25 : -(x / 2 * 10))),
        ZOOM_BEFORE_SHOOTING("Scope.Zoom_Before_Shooting", "Shoot.Trigger.Circumstance.Zooming", new ValueBooleanConvert("REQUIRED", null)),
        SOUNDS_TOGGLE_ZOOM("Scope.Sounds_Toggle_Zoom", "Scope.Mechanics", new SoundConvert()),

        // HIT_EVENTS
        HIT_MESSAGE_SHOOTER("Hit_Events.Message_Shooter", "Damage.Mechanics", new MessageConvert(false)),
        HIT_MESSAGE_VICTIM("Hit_Events.Message_Victim", "Damage.Mechanics", new MessageConvert(true)),
        HIT_SOUNDS_SHOOTER("Hit_Events.Sounds_Shooter", "Damage.Mechanics", new SoundConvert()),
        HIT_SOUNDS_VICTIM("Hit_Events.Sounds_Victim", "Damage.Mechanics", new SoundConvert(true)),

        // CRITICAL_HITS
        CRIT_BONUS_DAMAGE("Critical_Hits.Bonus_Damage", "Damage.Critical_Hit.Bonus_Damage"),
        CHANCE("Critical_Hits.Chance", "Damage.Critical_Hit.Chance", new ValueNonZeroConvert()),
        CRIT_MESSAGE_SHOOTER("Critical_Hits.Message_Shooter", "Damage.Critical_Hit.Mechanics", new MessageConvert(false)),
        CRIT_MESSAGE_VICTIM("Critical_Hits.Message_Victim", "Damage.Critical_Hit.Mechanics", new MessageConvert(true)),
        CRIT_SOUNDS_SHOOTER("Critical_Hits.Sounds_Shooter", "Damage.Critical_Hit.Mechanics", new SoundConvert()),
        CRIT_SOUNDS_VICTIM("Critical_Hits.Sounds_Victim", "Damage.Critical_Hit.Mechanics", new SoundConvert(true)),

        // AIRSTRIKES
        AIRSTRIKE("Airstrikes.", "Explosion.", new AirstrikeConvert()),
        AREA("Airstrikes.Area", "Explosion.Airstrike.Maximum_Distance_From_Center", new ValueDoubleConvert(x -> x * x * 2)),
        MINIMUM_BOMBS("Airstrikes.Area", "Explosion.Airstrike.Minimum_Bombs", new ValueDoubleConvert(x -> x * 2)),
        MAXIMUM_BOMBS("Airstrikes.Area", "Explosion.Airstrike.Maximum_Bombs", new ValueDoubleConvert(x -> x * 2)),
        DISTANCE_BETWEEN_BOMBS("Airstrikes.Distance_Between_Bombs", "Explosion.Airstrike.Distance_Between_Bombs", new ValueNonZeroConvert()),
        HEIGHT_DROPPED("Airstrikes.Height_Dropped", "Explosion.Airstrike.Height", new ValueNonZeroConvert()),
        VERTICAL_VARIATION("Airstrikes.Vertical_Variation", "Explosion.Airstrike.Vertical_Randomness", new ValueNonZeroConvert()),
        NUMBER_OF_STRIKES("Airstrikes.Multiple_Strikes.Number_Of_Strikes", "Explosion.Airstrike.Layers", new ValueNonZeroConvert()),
        DELAY_BETWEEN_STRIKES("Airstrikes.Multiple_Strikes.Delay_Between_Strikes", "Explosion.Airstrike.Delay_Between_Layers", new ValueNonZeroConvert()),

        // CLUSTER_BOMBS
        CLUSTER_BOMB("Cluster_Bombs.", "Explosion.", new ClusterBombConvert()),
        NUMBER_OF_SPLITS("Cluster_Bombs.Number_Of_Splits", "Explosion.Cluster_Bomb.Number_Of_Splits", new ValueNonZeroConvert()),
        NUMBER_OF_BOMBLETS("Cluster_Bombs.Number_Of_Bomblets", "Explosion.Cluster_Bomb.Number_Of_Bombs", new ValueNonZeroConvert()),
        SPEED_OF_BOMBLETS("Cluster_Bombs.Speed_Of_Bomblets", "Explosion.Cluster_Bomb.Projectile_Speed", new ValueDoubleConvert(x -> x * 2)),

        // EXPLOSIONS
        EXPLOSIONS("Explosions.", "Explosion.", new ExplosionConvert()),
        IGNITE_VICTIMS("Explosions.Ignite_Victims", "Damage.Fire_Ticks", new ValueNonZeroConvert()),
        ENABLE_OWNER_IMMUNITY("Explosions.Enable_Owner_Immunity", "Damage.Enable_Owner_Immunity"),
        EXPLOSION_NO_DAMAGE("Explosions.Explosion_No_Damage", "Damage.Base_Explosion_Damage", new ValueBooleanConvert(0, null)),
        EXPLOSION_POTION_EFFECT("Explosions.Explosion_Potion_Effect", "Damage.Mechanics", new ExplosionPotionEffectConvert()),
        EXPLOSION_RADIUS("Explosions.Explosion_Radius", "Explosion.Explosion_Type_Data.Yield", new ValueNonZeroConvert()),
        EXPLOSION_DELAY("Explosions.Explosion_Delay", "Explosion.Detonation.Delay_After_Impact", new ValueNonZeroConvert()),
        EXP_MESSAGE_SHOOTER("Explosions.Message_Shooter", "Damage.Mechanics", new MessageConvert(false)),
        EXP_MESSAGE_VICTIM("Explosions.Message_Victim", "Damage.Mechanics", new MessageConvert(true)),
        EXP_SOUNDS_SHOOTER("Explosions.Sounds_Shooter", "Damage.Mechanics", new SoundConvert()),
        EXP_SOUNDS_VICTIM("Explosions.Sounds_Victim", "Damage.Mechanics", new SoundConvert(true)),
        EXP_SOUNDS("Explosions.Sounds_Explode", "Explosion.Mechanics", new SoundConvert()),

        // EXTRAS
        ONE_TIME_USE("Extras.One_Time_Use", "Shoot.Consume_Item_On_Shoot"),
        DISABLE_UNDERWATER("Extras.Disable_Underwater", "Shoot.Trigger.Circumstance.Swimming", new ValueBooleanConvert("DENY", null)),
        MAKE_VICTIM_RUN_COMMAND("Extras.Make_Victim_Run_Commmand", "Damage.Mechanics", new CommandConvert(false, true)),
        RUN_CONSOLE_COMMAND("Extras.Run_Console_Command", "Damage.Mechanics", new CommandConvert(true, false)),

        DUMMY(null, null);

        private final String from;
        private final String to;
        private final Converter converter;

        Paths(String from, String to) {
            this.from = from;
            this.to = to;
            this.converter = new ValueConvert();
        }

        Paths(String from, String to, Converter converter) {
            this.from = from;
            this.to = to;
            this.converter = converter;
        }

        private void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            converter.convert(from, to, fromConfig, toConfig);
        }
    }

    private interface Converter {

        void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig);
    }

    private static class ValueConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            Object value = fromConfig.get(from);
            if (value == null) return;

            if (fromConfig.isString(from)) {
                value = StringUtil.colorAdventure((String) value);
            }

            toConfig.set(to, value);
        }
    }

    private static class ValueNonZeroConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            Object value = fromConfig.get(from);
            if (value == null) return;

            if (value instanceof Number) {
                double dvalue = ((Number) value).doubleValue();
                if (dvalue < 1) {
                    toConfig.set(to, 1);
                } else {
                    toConfig.set(to, value);
                }
                return;
            }

            toConfig.set(to, value);
        }
    }

    private static class ValueBooleanConvert implements Converter {

        private final Object ifValue;
        private final Object elseValue;
        private final String extraPathCheck;

        public ValueBooleanConvert(Object ifValue, Object elseValue) {
            this(ifValue, elseValue, null);
        }

        public ValueBooleanConvert(Object ifValue, Object elseValue, String extraPathCheck) {
            this.ifValue = ifValue;
            this.elseValue = elseValue;
            this.extraPathCheck = extraPathCheck;
        }

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {

            if (this.extraPathCheck != null && !fromConfig.getBoolean(from + this.extraPathCheck)) {
                return;
            }

            boolean value = fromConfig.getBoolean(from, false);

            if (elseValue == null) {
                if (!value) {
                    return;
                }
                toConfig.set(to, ifValue);
            } else {
                toConfig.set(to, value ? ifValue : elseValue);
            }
        }
    }

    private record ValueDoubleConvert(Function<Double, Double> function) implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            double value = fromConfig.getDouble(from, -55);
            if (value == -55) return;

            toConfig.set(to, function.apply(value));
        }
    }

    private static class LoreConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String value = fromConfig.getString(from);
            if (value == null) return;

            value = StringUtil.colorAdventure(value);

            toConfig.set(to, Arrays.asList(value.split("\\|")));
        }
    }

    private static class MaterialConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String value = fromConfig.getString(from);
            if (value == null) return;

            String material = getMaterial(value);
            if (material == null) return;

            toConfig.set(to, material);
        }
    }

    private static class SoundConvert implements Converter {

        private final boolean isTarget;

        public SoundConvert() {
            this.isTarget = false;
        }

        public SoundConvert(boolean isTarget) {
            this.isTarget = isTarget;
        }

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String value = fromConfig.getString(from);
            if (value == null) return;

            // SOUND-VOLUME-PITCH-DELAY

            List<String> mechanics = toConfig.getStringList(to);

            for (String sound : value.replaceAll(" ", "").split(",")) {
                String[] splitted = sound.split("-");

                if (sound.isEmpty()) {
                    continue;
                }

                String soundName = null;
                try {
                    soundName = SoundManager.get(splitted[0].toUpperCase(Locale.ROOT)).name();
                } catch (NoClassDefFoundError | Exception e) {
                    // If CrackShot is outdated... or other exception
                    try {
                        soundName = Sound.valueOf(splitted[0].toUpperCase(Locale.ROOT)).name();
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (soundName == null) {
                    soundName = StringUtil.didYouMean(splitted[0], EnumUtil.getOptions(Sound.class));
                    WeaponMechanics.debug.error("Invalid sound: " + splitted[0] + " swapped to: " + soundName);
                }

                String volume = "1";
                if (splitted.length > 1) {
                    volume = splitted[1];
                    try {
                        if (Double.parseDouble(volume) <= 0.05) {
                            volume = "1";
                        }
                    } catch (NumberFormatException e) {
                        volume = "1";
                    }
                }
                String pitch = "1";
                if (splitted.length > 2) {
                    pitch = splitted[2];
                    try {
                        double parsedPitch = Double.parseDouble(pitch);
                        if (parsedPitch <= 0.5) {
                            pitch = "0.5";
                        } else if (parsedPitch > 2) {
                            pitch = "2.0";
                        }
                    } catch (NumberFormatException e) {
                        pitch = "1";
                    }
                }
                String delay = null;
                if (splitted.length > 3) {
                    try {
                        if (Integer.parseInt(splitted[3]) > 0) {
                            delay = splitted[3];
                        }
                    } catch (NumberFormatException e) {
                        delay = null;
                    }
                }

                if (delay == null) {
                    if (this.isTarget) {
                        mechanics.add("Sound{sound=%s, volume=%s, pitch=%s} @Target{}".formatted(soundName, volume, pitch));
                    } else {
                        mechanics.add("Sound{sound=%s, volume=%s, pitch=%s}".formatted(soundName, volume, pitch));
                    }
                } else {
                    if (this.isTarget) {
                        mechanics.add("Sound{sound=%s, volume=%s, pitch=%s, delayBeforePlay=%s} @Target{}".formatted(soundName, volume, pitch, delay));
                    } else {
                        mechanics.add("Sound{sound=%s, volume=%s, pitch=%s, delayBeforePlay=%s}".formatted(soundName, volume, pitch, delay));
                    }
                }
            }

            toConfig.set(to, mechanics);
        }
    }

    private static class ExplosionPotionEffectConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String value = fromConfig.getString(from);
            if (value == null) return;

            List<String> mechanics = toConfig.getStringList(to);

            Arrays.stream(value.replaceAll(" ", "").split(",")).forEach(potion -> {
                String[] potArray = potion.split("-");
                // POTIONTYPE-DURATION-LEVEL
                mechanics.add("Potion{potion=%s, time=%s, level=%s} @Target{}".formatted(potArray[0], potArray[1], potArray[2]));
            });

            toConfig.set(to, mechanics);
        }
    }

    private static class DualWieldConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            if (!fromConfig.getBoolean(from)) return;

            String weapon = from.split("\\.")[0];

            String weaponName = fromConfig.getString(weapon + ".Item_Information.Item_Name", weapon);
            weaponName = StringUtil.colorAdventure(weaponName);

            toConfig.set(to + "Info.Dual_Wield.Whitelist", true);
            toConfig.set(to + "Info.Dual_Wield.Weapons", Collections.singletonList(weapon));

            toConfig.set(to + "Info.Weapon_Info_Display.Action_Bar.Dual_Wield.Main_Hand", "%ammo-left%»%reload% %firearm-state%" + weaponName);
            toConfig.set(to + "Info.Weapon_Info_Display.Action_Bar.Dual_Wield.Off_Hand", weaponName + "%firearm-state% %reload%«%ammo-left%");

            toConfig.set(to + "Shoot.Trigger.Main_Hand", "right_click");
            toConfig.set(to + "Shoot.Trigger.Off_Hand", "right_click");


            toConfig.set(to + "Reload.Trigger.Off_Hand", "drop_item");

            toConfig.set(to + "Shoot.Trigger.Dual_Wield.Main_Hand", "right_click");
            toConfig.set(to + "Shoot.Trigger.Dual_Wield.Off_Hand", "left_click");
        }
    }

    private record MessageConvert(boolean isTarget) implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String value = fromConfig.getString(from);
            if (value == null) return;
            value = StringUtil.colorAdventure(value).replaceAll(",", "\\\\,");

            List<String> mechanics = toConfig.getStringList(to);

            if (isTarget) {
                mechanics.add("Message{message=%s} @Target{}".formatted(value));
            } else {
                mechanics.add("Message{message=%s}".formatted(value));
            }

            toConfig.set(to, mechanics);
        }
    }

    private static class ProjectileTypeConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String type = fromConfig.getString(from + "Projectile_Type");
            if (type == null) return;

            if (type.equalsIgnoreCase("splash")) {
                WeaponMechanics.debug.error("Can't convert splash: " + from);
                return;
            }

            if (type.equalsIgnoreCase("energy")) {

                toConfig.set(to + "Projectile.Projectile_Settings.Type", "INVISIBLE");
                toConfig.set(to + "Projectile.Projectile_Settings.Gravity", 0.0);

                // Projectile_Subtype: RANGE-RADIUS-WALLS-VICTIMS
                String[] energySettings = fromConfig.getString(from + "Projectile_Subtype").split("-");

                try {
                    int range = Integer.parseInt(energySettings[0]);
                    toConfig.set(to + "Shoot.Projectile_Speed", range * 5);
                    toConfig.set(to + "Projectile.Projectile_Settings.Maximum_Travel_Distance", range);

                    double raySize = Double.parseDouble(energySettings[1]);
                    if (raySize > 0.1) {
                        toConfig.set(to + "Projectile.Projectile_Settings.Size", raySize);
                    }

                    int victims = Integer.parseInt(energySettings[3]);
                    if (victims == 0) {
                        toConfig.set(to + "Projectile.Through.Entities.Allow_Any", true);
                        toConfig.set(to + "Projectile.Through.Maximum_Through_Amount", -1);
                    } else {
                        toConfig.set(to + "Projectile.Through.Entities.Allow_Any", true);
                        toConfig.set(to + "Projectile.Through.Maximum_Through_Amount", victims);
                    }

                    String walls = energySettings[2];
                    if (walls.equalsIgnoreCase("ALL")) {
                        toConfig.set(to + "Projectile.Through.Blocks.Allow_Any", true);
                        toConfig.set(to + "Projectile.Through.Maximum_Through_Amount", -1);
                    } else if (!walls.equalsIgnoreCase("NONE")) {
                        toConfig.set(to + "Projectile.Through.Blocks.Allow_Any", true);
                        toConfig.set(to + "Projectile.Through.Maximum_Through_Amount", Integer.parseInt(energySettings[2]));
                    }

                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    WeaponMechanics.debug.error("Energy projectile subtype invalid: " + from + "Projectile_Subtype " + Arrays.toString(energySettings));
                }

                return;
            }

            if (type.equalsIgnoreCase("grenade") || type.equalsIgnoreCase("flare")) {

                String material = getMaterial(fromConfig.getString(from + "Projectile_Subtype"));
                if (material == null) {
                    WeaponMechanics.debug.error("Type " + type + " didn't have Projectile_Subtype... " + from);
                    return;
                }

                toConfig.set(to + "Projectile.Projectile_Settings.Type", "DROPPED_ITEM");

                toConfig.set(to + "Projectile.Projectile_Settings.Projectile_Item_Or_Block.Type", material);
                toConfig.set(to + "Projectile.Projectile_Settings.Disable_Entity_Collisions", true);

                toConfig.set(to + "Projectile.Bouncy.Blocks.Allow_Any", true);
                toConfig.set(to + "Projectile.Bouncy.Blocks.Default_Speed_Multiplier", 0.6);

                toConfig.set(to + "Projectile.Bouncy.Rolling.Required_Motion_To_Start_Rolling", 6);
                toConfig.set(to + "Projectile.Bouncy.Rolling.Blocks.Allow_Any", true);
                toConfig.set(to + "Projectile.Bouncy.Rolling.Blocks.Default_Speed_Multiplier", 0.9);
                toConfig.set(to + "Projectile.Bouncy.Rolling.Blocks.List", Arrays.asList("$_ICE-0.99", "ICE-0.99"));

                return;
            }

            if (type.equalsIgnoreCase("witherskull")) {
                type = "WITHER_SKULL";
            }

            toConfig.set(to + "Projectile.Projectile_Settings.Type", type);
        }
    }

    private static class FirearmActionConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String value = fromConfig.getString(from);
            if (value == null) return;

            if (value.equalsIgnoreCase("break")) {
                value = "revolver";
                WeaponMechanics.debug.error("Firearm action break was changed to revolver " + from);

            } else if (value.equalsIgnoreCase("bolt")) {
                value = "lever";
                WeaponMechanics.debug.error("Firearm action bolt was changed to lever " + from);
            }

            toConfig.set(to, value);
        }
    }

    private static class PotionEffectsConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String activations = fromConfig.getString(from + "Activation");
            if (activations == null) return;

            String shooter = fromConfig.getString(from + "Potion_Effect_Shooter");
            List<String> shooterEffects = new ArrayList<>();
            if (shooter != null) {
                Arrays.stream(shooter.replaceAll(" ", "").split(",")).forEach(potion -> {
                    String[] potArray = potion.split("-");
                    // POTIONTYPE-DURATION-LEVEL
                    shooterEffects.add("Potion{potion=%s, time=%s, level=%s}".formatted(potArray[0], potArray[1], potArray[2]));
                });
            }

            String victim = fromConfig.getString(from + "Potion_Effect_Victim");
            List<String> victimEffects = new ArrayList<>();
            if (victim != null) {
                Arrays.stream(victim.replaceAll(" ", "").split(",")).forEach(potion -> {
                    String[] potArray = potion.split("-");
                    // POTIONTYPE-DURATION-LEVEL
                    victimEffects.add("Potion{potion=%s, time=%s, level=%s} @Target{}".formatted(potArray[0], potArray[1], potArray[2]));
                });
            }

            for (String activation : activations.replaceAll(" ", "").split(",")) {
                String mechanicsTo = null;

                if (activation.equalsIgnoreCase("head")) {
                    mechanicsTo = to + "Damage.Head.Mechanics";
                } else if (activation.equalsIgnoreCase("back")) {
                    mechanicsTo = to + "Damage.Backstab.Mechanics";
                } else if (activation.equalsIgnoreCase("crit")) {
                    mechanicsTo = to + "Damage.Critical_Hit.Mechanics";
                } else if (activation.equalsIgnoreCase("hit")) {
                    mechanicsTo = to + "Damage.Mechanics";
                } else if (activation.equalsIgnoreCase("shoot")) {
                    mechanicsTo = to + "Shoot.Mechanics";
                } else if (activation.equalsIgnoreCase("reload")) {
                    mechanicsTo = to + "Reload.Start_Mechanics";
                }

                if (mechanicsTo != null) {
                    List<String> mechanics = toConfig.getStringList(mechanicsTo);

                    if (shooter != null) mechanics.addAll(shooterEffects);
                    if (victim != null) mechanics.addAll(victimEffects);

                    toConfig.set(mechanicsTo, mechanics);
                }
            }
        }
    }

    private static class AbilitiesAndRecoilConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            double abilitiesKnockback = fromConfig.getDouble(from + "Abilities.Knockback", -500);
            if (abilitiesKnockback != -500) {

                List<String> mechanics = toConfig.getStringList(to + "Damage.Mechanics");

                // From CS to WM speed conversion
                abilitiesKnockback *= 2;

                mechanics.add("Leap{speed=%s} @Target{}".formatted((abilitiesKnockback * 2)));

                toConfig.set(to + "Damage.Mechanics", mechanics);
            }

            double recoilAmount = fromConfig.getDouble(from + "Shooting.Recoil_Amount", -500);
            if (recoilAmount != -500) {

                List<String> mechanics = toConfig.getStringList(to + "Shoot.Mechanics");

                // From CS to WM conversion, negative because we want to leap backwards
                recoilAmount *= -2;

                boolean noVerticalRecoil = fromConfig.getBoolean(from + "Abilities.No_Vertical_Recoil");
                boolean sneakNoRecoil = fromConfig.getBoolean(from + "Sneak.No_Recoil");

                StringBuilder builder = new StringBuilder("Leap{speed=%s".formatted(recoilAmount));

                if (noVerticalRecoil) {
                    builder.append(", verticalMultiplier=0.0");
                }

                builder.append("}");

                if (sneakNoRecoil) {
                    builder.append(" ?Sneaking{inverted=true}");
                }

                mechanics.add(builder.toString());

                toConfig.set(to + "Shoot.Mechanics", mechanics);
            }
        }
    }

    private static class FireworkConvert implements Converter {

        private final boolean isTarget;

        public FireworkConvert() {
            this.isTarget = false;
        }

        public FireworkConvert(boolean isTarget) {
            this.isTarget = isTarget;
        }

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String fireworks = fromConfig.getString(from);
            if (fireworks == null) return;

            List<String> mechanics = toConfig.getStringList(to);

            for (String firework : fireworks.replaceAll(" ", "").split(",")) {
                // CS: TYPE-TRAIL-FLICKER-R-G-B
                // WM: <Firework.Type>-<ColorSerializer>-<Trail>-<Flicker>
                String[] splitFirework = firework.split("-");

                if (this.isTarget) {
                    mechanics.add("Firework{effects=[{shape=%s, color=RED, trail=%s, flicker=%s}]} @Target{}".formatted(splitFirework[0], splitFirework[1], splitFirework[2]));
                } else {
                    mechanics.add("Firework{effects=[{shape=%s, color=RED, trail=%s, flicker=%s}]}".formatted(splitFirework[0], splitFirework[1], splitFirework[2]));
                }

            }

            toConfig.set(to, mechanics);
        }
    }

    private static class AirstrikeConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            if (!fromConfig.getBoolean(from + "Enable")) {
                return;
            }

            int flareActivationDelay = fromConfig.getInt(from + "Flare_Activation_Delay", -1);
            if (flareActivationDelay != -1) {
                toConfig.set(to + "Detonation.Delay_After_Impact", flareActivationDelay);
                toConfig.set(to + "Detonation.Remove_Projectile_On_Detonation", true);
                toConfig.set(to + "Detonation.Impact_When.Spawn", true);

                toConfig.set(to + "Airstrike.Detonation.Impact_When.Block", true);
            }

            String blockType = fromConfig.getString(from + "Block_Type");
            if (blockType != null) {
                String material = getMaterial(blockType);
                if (material == null) {
                    WeaponMechanics.debug.error("Block_Type was invalid... " + from);
                    return;
                }

                toConfig.set(to + "Airstrike.Dropped_Projectile.Projectile_Settings.Type", "FALLING_BLOCK");
                toConfig.set(to + "Airstrike.Dropped_Projectile.Projectile_Settings.Projectile_Item_Or_Block.Type", material);
                toConfig.set(to + "Airstrike.Dropped_Projectile.Disable_Entity_Collisions", true);
            }
        }
    }

    private static class ClusterBombConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            if (!fromConfig.getBoolean(from + "Enable")) {
                return;
            }

            int delayBeforeSplit = fromConfig.getInt(from + "Delay_Before_Split", -1);
            if (delayBeforeSplit != -1) {
                toConfig.set(to + "Detonation.Delay_After_Impact", delayBeforeSplit);
                toConfig.set(to + "Detonation.Remove_Projectile_On_Detonation", true);
                toConfig.set(to + "Detonation.Impact_When.Spawn", true);
            }

            // Delay_Before_Detonation
            int delayBeforeDetonation = fromConfig.getInt(from + "Delay_Before_Detonation", -1);
            if (delayBeforeDetonation != -1) {
                toConfig.set(to + "Cluster_Bomb.Detonation.Delay_After_Impact", delayBeforeDetonation);
                toConfig.set(to + "Cluster_Bomb.Detonation.Remove_Projectile_On_Detonation", true);
                toConfig.set(to + "Cluster_Bomb.Detonation.Impact_When.Spawn", true);
                toConfig.set(to + "Cluster_Bomb.Detonation.Impact_When.Spawn", true);
            }

            String bombletType = fromConfig.getString(from + "Bomblet_Type");
            if (bombletType != null) {
                String material = getMaterial(bombletType);
                if (material == null) {
                    WeaponMechanics.debug.error("Bomblet_Type was invalid... " + from);
                    return;
                }

                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Projectile_Settings.Type", "DROPPED_ITEM");
                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Projectile_Settings.Projectile_Item_Or_Block.Type", material);
                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Projectile_Settings.Disable_Entity_Collisions", true);

                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Bouncy.Blocks.Allow_Any", true);
                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Bouncy.Blocks.Default_Speed_Multiplier", 0.6);

                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Bouncy.Rolling.Required_Motion_To_Start_Rolling", 6);
                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Bouncy.Rolling.Blocks.Allow_Any", true);
                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Bouncy.Rolling.Blocks.Default_Speed_Multiplier", 0.9);
                toConfig.set(to + "Cluster_Bomb.Split_Projectile.Bouncy.Rolling.Blocks.List", Arrays.asList("$_ICE-0.99", "ICE-0.99"));
            }
        }
    }

    private static class ExplosionConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            if (!fromConfig.getBoolean(from + "Enable")) {
                return;
            }

            String weapon = from.split("\\.")[0];
            String type = fromConfig.getString(weapon + ".Shooting.Projectile_Type");

            if (type != null && (type.equalsIgnoreCase("grenade")
                    || type.equalsIgnoreCase("flare"))) {
                toConfig.set(to + "Detonation.Impact_When.Spawn", true);
            } else if (fromConfig.getBoolean(from + "On_Impact_With_Anything", false)) {
                toConfig.set(to + "Detonation.Impact_When.Entity", true);
                toConfig.set(to + "Detonation.Impact_When.Block", true);
            } else {
                toConfig.set(to + "Detonation.Impact_When.Entity", true);
            }

            if (!fromConfig.getBoolean(from + "Explosion_No_Grief", false)) {
                toConfig.set(to + "Block_Damage.Default_Mode", "BREAK");
                toConfig.set(to + "Block_Damage.Spawn_Falling_Block_Chance", 0.5);
                toConfig.set(to + "Block_Damage.Blocks", Arrays.asList("BEDROCK CANCEL", "$LAVA CANCEL", "$WATER CANCEL"));
            }

            toConfig.set(to + "Explosion_Exposure", "DEFAULT");
            toConfig.set(to + "Explosion_Shape", "DEFAULT");
            toConfig.set(to + "Explosion_Type_Data.Rays", 16);
        }
    }

    private record CommandConvert(boolean onlyConsole, boolean isTarget) implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {

            String value = fromConfig.getString(from);
            if (value == null) return;

            List<String> mechanics = toConfig.getStringList(to);

            if (onlyConsole) {
                Arrays.stream(value.split("\\|")).forEach(command -> mechanics.add("Command{command=%s, console=true}".formatted(command)));
            } else if (isTarget) {
                Arrays.stream(value.split("\\|")).forEach(command -> mechanics.add("Command{command=%s} @Target{}".formatted(command)));
            } else {
                Arrays.stream(value.split("\\|")).forEach(command -> mechanics.add("Command{command=%s}".formatted(command)));
            }

            toConfig.set(to, mechanics);
        }
    }

    private static class ShootButtonsConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            boolean rightClick = fromConfig.getBoolean(from + "Shooting.Right_Click_To_Shoot");

            int fullAuto = fromConfig.getInt(from + "Fully_Automatic.Fire_Rate");

            if (fullAuto != 0 && !rightClick) {
                WeaponMechanics.debug.error("When using full auto, shoot trigger has to be right_click, swapping... " + from);
            }

            if (fullAuto != 0 || rightClick) {
                toConfig.set(to, "right_click");
            } else {
                toConfig.set(to, "left_click");
            }
        }
    }

    private static class ScopeConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            if (!fromConfig.getBoolean(from + "Scope.Enable")) {
                return;
            }
            if (fromConfig.getInt(from + "Fully_Automatic.Fire_Rate") != 0) {
                toConfig.set(to, "left_click");
                return;
            }

            if (fromConfig.getBoolean(from + "Shooting.Right_Click_To_Shoot")) {
                toConfig.set(to, "left_click");
            } else {
                toConfig.set(to, "right_click");
            }
        }
    }

    private static class MeleeHitDelayConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            if (!fromConfig.getBoolean(from + "Item_Information.Melee_Mode")) {
                return;
            }

            int delayBetweenShots = fromConfig.getInt(from + "Shooting.Delay_Between_Shots");
            if (delayBetweenShots < 1) return;

            toConfig.set(to, delayBetweenShots);
        }
    }

    private static class RemovalOrDragDelayConvert implements Converter {

        @Override
        public void convert(String from, String to, YamlConfiguration fromConfig, YamlConfiguration toConfig) {
            String removalOrDragDelay = fromConfig.getString(from);
            if (removalOrDragDelay == null) return;

            String[] split = removalOrDragDelay.split("-");

            // Removal_Or_Drag_Delay: 4-true will remove the projectile after 4 ticks.
            // Removal_Or_Drag_Delay: 3-false will drastically slow the projectile after 3 ticks.

            if (split[1].equalsIgnoreCase("false")) {
                WeaponMechanics.debug.error("Can't convert Removal_Or_Drag_Delay false option " + from);
                return;
            }

            toConfig.set(to, Integer.parseInt(split[0]));
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
                return Material.valueOf(type.toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException ignored) {
            }
        }

        String materialName = StringUtil.didYouMean(type, EnumUtil.getOptions(Material.class));
        WeaponMechanics.debug.error("Invalid material: " + type + " swapped to: " + materialName);
        return materialName;
    }
}