package me.deecaad.weaponmechanics.weapon;

import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.file.verify.ConfigSchema;

/**
 * The complete declared shape of a weapon config: every legal key for every section, nested to the
 * leaf. {@link WeaponSerializer#schema()} returns {@link #weapon()}, and {@code RootFileReader}
 * validates each weapon against it on load, logging a warning for any unknown/typo'd key (e.g. the
 * old {@code Victim_Mechanics}). It is purely a shape checker layered on top of the existing
 * handlers/serializers, which still do all real serialization.
 *
 * <p>Sections owned by other plugins ({@code Cosmetics} from WeaponMechanicsCosmetics) and
 * user-named maps ({@code Skin}) are declared as {@code allowUnknown} nodes so their keys never
 * warn. {@code Mechanics} lists are leaf {@code rawListKey}s — their mechanic-line contents are
 * compiled separately by {@code MechanicSerializer}, not validated as config keys here. Sections
 * backed by a full serializer with many keys ({@code Weapon_Item}) defer to that serializer's own
 * {@code schema()} via {@code nested(name, Class)}.
 */
public final class WeaponSchema {

    private WeaponSchema() {
    }

    /** The whole-weapon schema, composed from every section below. */
    public static ConfigSchema weapon() {
        return ConfigSchema.builder()
            .nested("Info", info())
            .nested("Skin", ConfigSchema.builder().allowUnknown().build()) // user-named skins + attachments
            .nested("Projectile", projectileObject()) // string reference => SchemaValidator early-returns
            .nested("Shoot", shoot())
            .nested("Reload", reload())
            .nested("Damage", damage())
            .nested("Scope", scope())
            .nested("Firearm_Action", firearmAction())
            .nested("Fire_Mode", fireMode())
            .nested("Explosion", explosion())
            .nested("Melee", melee())
            .nested("Trail", trail())
            .nested("Show_Time", showTime())
            .nested("Cosmetics", ConfigSchema.builder().allowUnknown().build()) // WeaponMechanicsCosmetics
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Shared building blocks
    // ------------------------------------------------------------------------------------------------

    /** A {@code Trigger} object (used by Shoot, Reload, Scope, Fire_Mode, Ammo_Switch). */
    private static ConfigSchema trigger() {
        return ConfigSchema.builder()
            .stringKey("Main_Hand")
            .stringKey("Off_Hand")
            .nested("Dual_Wield", ConfigSchema.builder()
                .stringKey("Main_Hand")
                .stringKey("Off_Hand")
                .build())
            .nested("Circumstance", circumstance())
            .build();
    }

    /** A {@code Circumstance} object: each circumstance maps to DENY/REQUIRED, plus Deny_Mechanics. */
    private static ConfigSchema circumstance() {
        return ConfigSchema.builder()
            .stringKey("Reloading")
            .stringKey("Zooming")
            .stringKey("Sneaking")
            .stringKey("Standing")
            .stringKey("Walking")
            .stringKey("Riding")
            .stringKey("Sprinting")
            .stringKey("Dual_Wielding")
            .stringKey("Swimming")
            .stringKey("In_Midair")
            .stringKey("Gliding")
            .stringKey("Ammo_Empty")
            .rawListKey("Deny_Mechanics")
            .build();
    }

    /** A {@code ListHolder} (block/entity whitelist with per-entry speed multipliers). */
    private static ConfigSchema listHolder() {
        return ConfigSchema.builder()
            .boolKey("Allow_Any")
            .rawListKey("List")
            .doubleKey("Default_Speed_Multiplier")
            .boolKey("Whitelist")
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Info
    // ------------------------------------------------------------------------------------------------

    private static ConfigSchema info() {
        return ConfigSchema.builder()
            .nested("Weapon_Item", ItemSerializer.class) // defers to ItemSerializer's own key set
            .nested("Weapon_Info_Display", ConfigSchema.builder().allowUnknown().build())
            .rawListKey("Weapon_Get_Mechanics")
            .rawListKey("Weapon_Equip_Mechanics")
            .rawListKey("Weapon_Break_Mechanics")
            .rawListKey("Weapon_Holster_Mechanics")
            .intKey("Weapon_Equip_Delay")
            .nested("Dual_Wield", ConfigSchema.builder()
                .boolKey("Whitelist")
                .rawListKey("Weapons")
                .rawListKey("Mechanics_On_Deny")
                .build())
            .nested("Weapon_Converter_Check", converterCheck())
            .nested("Cancel", ConfigSchema.builder()
                .boolKey("Drop_Item")
                .boolKey("Arm_Swing_Animation")
                .boolKey("Break_Blocks")
                .boolKey("Block_Interactions")
                .boolKey("Item_Interactions")
                .boolKey("Swap_Hands")
                .build())
            .build();
    }

    private static ConfigSchema converterCheck() {
        return ConfigSchema.builder()
            .boolKey("Type")
            .boolKey("Name")
            .boolKey("Lore")
            .boolKey("Enchantments")
            .boolKey("Custom_Model_Data")
            .build();
    }

    /** A standalone ammo definition (the shape of shared ammos/*.yml entries). */
    public static ConfigSchema ammoObject() {
        return ConfigSchema.builder()
            .stringKey("Symbol")
            .intKey("Experience_As_Ammo_Cost")
            .intKey("Money_As_Ammo_Cost")
            .nested("Item_Ammo", ConfigSchema.builder()
                .nested("Bullet_Item", ItemSerializer.class)
                .nested("Magazine_Item", ItemSerializer.class)
                .nested("Ammo_Converter_Check", converterCheck())
                .build())
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Projectile
    // ------------------------------------------------------------------------------------------------

    /** The inline {@code Projectile} object (also the shape of shared projectiles/*.yml entries). */
    public static ConfigSchema projectileObject() {
        return ConfigSchema.builder()
            .nested("Projectile_Settings", projectileSettings())
            .nested("Sticky", blockEntityHolder(false))
            .nested("Through", blockEntityHolder(true))
            .nested("Bouncy", bouncy())
            .rawListKey("Mechanics")
            .build();
    }

    private static ConfigSchema projectileSettings() {
        return ConfigSchema.builder()
            .stringKey("Type")
            .nested("Projectile_Item_Or_Block", ItemSerializer.class)
            .doubleKey("Gravity")
            .nested("Minimum", ConfigSchema.builder()
                .doubleKey("Speed")
                .boolKey("Remove_Projectile_On_Speed_Reached")
                .build())
            .nested("Maximum", ConfigSchema.builder()
                .doubleKey("Speed")
                .boolKey("Remove_Projectile_On_Speed_Reached")
                .build())
            .nested("Drag", ConfigSchema.builder()
                .doubleKey("Base")
                .doubleKey("In_Water")
                .doubleKey("When_Raining_Or_Snowing")
                .build())
            .boolKey("Disable_Entity_Collisions")
            .intKey("Maximum_Alive_Ticks")
            .doubleKey("Maximum_Travel_Distance")
            .doubleKey("Size")
            .boolKey("Incendiary_Projectile")
            .boolKey("Extinguish_In_Water")
            .build();
    }

    /** Sticky/Through share Blocks + Entities; Through adds Maximum_Through_Amount. */
    private static ConfigSchema blockEntityHolder(boolean through) {
        ConfigSchema.Builder builder = ConfigSchema.builder()
            .nested("Blocks", listHolder())
            .nested("Entities", listHolder());
        if (through)
            builder.doubleKey("Maximum_Through_Amount");
        return builder.build();
    }

    private static ConfigSchema bouncy() {
        return ConfigSchema.builder()
            .nested("Blocks", listHolder())
            .nested("Entities", listHolder())
            .intKey("Maximum_Bounce_Amount")
            .nested("Rolling", ConfigSchema.builder()
                .nested("Blocks", listHolder())
                .doubleKey("Required_Motion_To_Start_Rolling")
                .build())
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Shoot
    // ------------------------------------------------------------------------------------------------

    private static ConfigSchema shoot() {
        return ConfigSchema.builder()
            .nested("Trigger", trigger())
            .doubleKey("Projectile_Speed")
            .intKey("Delay_Between_Shots")
            .intKey("Projectiles_Per_Shot")
            .intKey("Durability_Per_Shot")
            .intKey("Fully_Automatic_Shots_Per_Second")
            .nested("Burst", ConfigSchema.builder()
                .intKey("Shots_Per_Burst")
                .intKey("Ticks_Between_Each_Shot")
                .build())
            .nested("Selective_Fire", ConfigSchema.builder()
                .nested("Trigger", trigger())
                .stringKey("Default")
                .rawListKey("Mechanics")
                .build())
            .nested("Spread", spread())
            .nested("Recoil", recoil())
            .nested("Offsets", offsets())
            .nested("Haptic", ConfigSchema.builder()
                .stringKey("Part")
                .doubleKey("Duration")
                .doubleKey("Frequency")
                .doubleKey("Amplitude")
                .doubleKey("Delay")
                .build())
            .rawListKey("Mechanics")
            .boolKey("Reset_Fall_Distance")
            .boolKey("Consume_Item_On_Shoot")
            .intKey("Ammo_Per_Shot")
            .boolKey("Destroy_When_Empty")
            .build();
    }

    private static ConfigSchema recoil() {
        return ConfigSchema.builder()
            .doubleKey("Mean_X")
            .doubleKey("Mean_Y")
            .doubleKey("Variance_X")
            .doubleKey("Variance_Y")
            .doubleKey("Speed")
            .doubleKey("Damping")
            .doubleKey("Damping_Recovery")
            .doubleKey("Smoothing")
            .doubleKey("Max_Accumulation")
            .doubleKey("Recovery_Percentage")
            .build();
    }

    private static ConfigSchema spread() {
        return ConfigSchema.builder()
            .nested("Spread_Image", ConfigSchema.builder()
                .stringKey("Name")
                .doubleKey("Field_Of_View_Width")
                .doubleKey("Field_Of_View_Height")
                .build())
            .doubleKey("Base_Spread")
            .nested("Modify_Spread_When", modifySpreadWhen())
            .nested("Changing_Spread", ConfigSchema.builder()
                .nested("Increase_Change_When", modifySpreadWhen())
                .doubleKey("Starting_Amount")
                .intKey("Reset_Time")
                .nested("Bounds", ConfigSchema.builder()
                    .doubleKey("Minimum")
                    .doubleKey("Maximum")
                    .boolKey("Reset_After_Reaching_Bound")
                    .build())
                .build())
            .build();
    }

    /** The per-circumstance spread multipliers, reused for Modify_Spread_When and Increase_Change_When. */
    private static ConfigSchema modifySpreadWhen() {
        return ConfigSchema.builder()
            .stringKey("Always")
            .stringKey("Zooming")
            .stringKey("Sneaking")
            .stringKey("Crawling")
            .stringKey("Standing")
            .stringKey("Walking")
            .stringKey("Riding")
            .stringKey("Sprinting")
            .stringKey("Dual_Wielding")
            .stringKey("Swimming")
            .stringKey("In_Midair")
            .stringKey("Gliding")
            .build();
    }

    private static ConfigSchema offsets() {
        ConfigSchema hands = ConfigSchema.builder()
            .stringKey("Left_Hand")
            .stringKey("Right_Hand")
            .build();
        return ConfigSchema.builder()
            .stringKey("Left_Hand")
            .stringKey("Right_Hand")
            .nested("Vive", hands)
            .nested("Scope", hands)
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Reload
    // ------------------------------------------------------------------------------------------------

    private static ConfigSchema reload() {
        return ConfigSchema.builder()
            .nested("Trigger", trigger())
            .intKey("Magazine_Size")
            .intKey("Reload_Duration")
            .intKey("Ammo_Per_Reload")
            .boolKey("Auto_Reload_When_Empty")
            .boolKey("Unload_Ammo_On_Reload")
            .intKey("Shoot_Delay_After_Reload")
            .rawListKey("Start_Mechanics")
            .rawListKey("Finish_Mechanics")
            .nested("Ammo", ConfigSchema.builder()
                .rawListKey("Ammos")
                .rawListKey("Out_Of_Ammo_Mechanics")
                .nested("Ammo_Switch_Trigger", trigger())
                .rawListKey("Ammo_Switch_Mechanics")
                .build())
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Damage
    // ------------------------------------------------------------------------------------------------

    private static ConfigSchema damage() {
        return ConfigSchema.builder()
            .doubleKey("Base_Damage")
            .doubleKey("Base_Explosion_Damage")
            .intKey("Armor_Damage")
            .intKey("Fire_Ticks")
            .boolKey("Enable_Owner_Immunity")
            .boolKey("Ignore_Teams")
            .rawListKey("Dropoff")
            .nested("Damage_Modifiers", damageModifiers())
            .nested("Explosion", ConfigSchema.builder()
                .nested("Damage_Modifiers", damageModifiers())
                .build())
            .rawListKey("Mechanics")
            .nested("Kill", ConfigSchema.builder().rawListKey("Mechanics").build())
            .nested("Backstab", damageBonus())
            .nested("Critical_Hit", ConfigSchema.builder()
                .doubleKey("Chance")
                .doubleKey("Bonus_Damage")
                .rawListKey("Mechanics")
                .build())
            .nested("Head", damageBonus())
            .nested("Body", damageBonus())
            .nested("Arms", damageBonus())
            .nested("Legs", damageBonus())
            .nested("Feet", damageBonus())
            .build();
    }

    /** Bonus damage section (Head/Body/Arms/Legs/Feet/Backstab): a flat bonus + its mechanics. */
    private static ConfigSchema damageBonus() {
        return ConfigSchema.builder()
            .doubleKey("Bonus_Damage")
            .rawListKey("Mechanics")
            .build();
    }

    private static ConfigSchema damageModifiers() {
        return ConfigSchema.builder()
            .doubleKey("Min")
            .doubleKey("Max")
            .doubleKey("Per_Armor_Point")
            .rawListKey("Armor")
            .rawListKey("Enchantments")
            .doubleKey("Head")
            .doubleKey("Body")
            .doubleKey("Arms")
            .doubleKey("Legs")
            .doubleKey("Feet")
            .doubleKey("Back")
            .doubleKey("Sneaking")
            .doubleKey("Walking")
            .doubleKey("Swimming")
            .doubleKey("Sprinting")
            .doubleKey("In_Midair")
            .doubleKey("Shielding")
            .rawListKey("Entities")
            .rawListKey("Potions")
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Scope
    // ------------------------------------------------------------------------------------------------

    private static ConfigSchema scope() {
        return ConfigSchema.builder()
            .nested("Trigger", trigger())
            .doubleKey("Zoom_Amount")
            .intKey("Shoot_Delay_After_Scope")
            .rawListKey("Mechanics")
            .boolKey("Unscope_After_Shot")
            .nested("Zoom_Stacking", ConfigSchema.builder()
                .rawListKey("Stacks")
                .rawListKey("Mechanics")
                .build())
            .nested("Zoom_Off", ConfigSchema.builder()
                .nested("Trigger", trigger())
                .rawListKey("Mechanics")
                .build())
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Firearm_Action / Fire_Mode
    // ------------------------------------------------------------------------------------------------

    private static ConfigSchema firearmAction() {
        ConfigSchema openClose = ConfigSchema.builder()
            .intKey("Time")
            .rawListKey("Mechanics")
            .build();
        return ConfigSchema.builder()
            .stringKey("Type")
            .intKey("Firearm_Action_Frequency")
            .nested("Open", openClose)
            .nested("Close", openClose)
            .build();
    }

    private static ConfigSchema fireMode() {
        return ConfigSchema.builder()
            .nested("Trigger", trigger())
            .rawListKey("Mechanics")
            .rawListKey("Order")
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Explosion
    // ------------------------------------------------------------------------------------------------

    private static ConfigSchema explosion() {
        return ConfigSchema.builder()
            .stringKey("Explosion_Exposure")
            .stringKey("Explosion_Shape")
            .nested("Explosion_Type_Data", ConfigSchema.builder()
                .doubleKey("Radius")  // SPHERE
                .doubleKey("Width")   // CUBE
                .doubleKey("Height")  // CUBE
                .doubleKey("Depth")   // PARABOLA
                .doubleKey("Angle")   // PARABOLA
                .doubleKey("Yield")   // DEFAULT
                .intKey("Rays")       // DEFAULT
                .build())
            .nested("Detonation", detonation())
            .nested("Block_Damage", ConfigSchema.builder()
                .doubleKey("Spawn_Falling_Block_Chance")
                .intKey("Damage_Per_Hit")
                .intKey("Default_Block_Durability")
                .stringKey("Default_Mode")
                .stringKey("Default_Mask")
                .rawListKey("Blocks")
                .build())
            .nested("Regeneration", ConfigSchema.builder()
                .intKey("Ticks_Before_Start")
                .intKey("Max_Blocks_Per_Update")
                .intKey("Ticks_Between_Updates")
                .build())
            .doubleKey("Knockback_Multiplier")
            .nested("Cluster_Bomb", ConfigSchema.builder()
                .intKey("Number_Of_Bombs")
                .nested("Split_Projectile", projectileObject())
                .doubleKey("Projectile_Speed")
                .intKey("Number_Of_Splits")
                .nested("Detonation", detonation())
                .rawListKey("Mechanics")
                .build())
            .nested("Airstrike", ConfigSchema.builder()
                .nested("Dropped_Projectile", projectileObject())
                .intKey("Minimum_Bombs")
                .intKey("Maximum_Bombs")
                .doubleKey("Height")
                .doubleKey("Vertical_Randomness")
                .doubleKey("Distance_Between_Bombs")
                .doubleKey("Maximum_Distance_From_Center")
                .intKey("Layers")
                .intKey("Delay_Between_Layers")
                .nested("Detonation", detonation())
                .rawListKey("Mechanics")
                .build())
            .nested("Flashbang", ConfigSchema.builder()
                .doubleKey("Effect_Distance")
                .rawListKey("Mechanics")
                .build())
            .rawListKey("Mechanics")
            .boolKey("Disable_Vanilla_Knockback")
            .boolKey("Disable_Entity_Collisions")
            .build();
    }

    private static ConfigSchema detonation() {
        return ConfigSchema.builder()
            .nested("Impact_When", ConfigSchema.builder()
                .boolKey("Spawn")
                .boolKey("Block")
                .boolKey("Entity")
                .boolKey("Time")
                .build())
            .intKey("Delay_After_Impact")
            .boolKey("Remove_Projectile_On_Detonation")
            .rawListKey("Impact_Mechanics")
            .build();
    }

    // ------------------------------------------------------------------------------------------------
    // Melee / Trail / Show_Time
    // ------------------------------------------------------------------------------------------------

    private static ConfigSchema melee() {
        return ConfigSchema.builder()
            .boolKey("Enable_Melee")
            .stringKey("Melee_Attachment")
            .doubleKey("Melee_Range")
            .intKey("Melee_Hit_Delay")
            .nested("Melee_Miss", ConfigSchema.builder()
                .rawListKey("Mechanics")
                .intKey("Melee_Miss_Delay")
                .boolKey("Consume_On_Miss")
                .build())
            .build();
    }

    private static ConfigSchema trail() {
        return ConfigSchema.builder()
            .doubleKey("Distance_Between_Particles")
            .stringKey("Particle_Chooser")
            .rawListKey("Particles")
            .stringKey("Shape")
            .nested("Shape_Data", ConfigSchema.builder().allowUnknown().build()) // shape-dependent params
            .build();
    }

    private static ConfigSchema showTime() {
        ConfigSchema cooldownOnly = ConfigSchema.builder().boolKey("Item_Cooldown").build();
        ConfigSchema cooldownExp = ConfigSchema.builder().boolKey("Item_Cooldown").boolKey("Exp").build();
        return ConfigSchema.builder()
            .nested("Reload", ConfigSchema.builder()
                .boolKey("Item_Cooldown")
                .boolKey("Exp")
                .stringKey("Action_Bar")
                .stringKey("Action_Bar_Cancelled")
                .nested("Bar", ConfigSchema.builder()
                    .stringKey("Left_Color")
                    .stringKey("Right_Color")
                    .stringKey("Left_Symbol")
                    .stringKey("Right_Symbol")
                    .intKey("Symbol_Amount")
                    .build())
                .nested("Boss_Bar", ConfigSchema.builder()
                    .stringKey("Message")
                    .stringKey("Color")
                    .stringKey("Style")
                    .build())
                .build())
            .nested("Weapon_Equip_Delay", cooldownOnly)
            .nested("Shoot_Delay_After_Scope", cooldownOnly)
            .nested("Shoot_Delay_After_Reload", cooldownOnly)
            .nested("Firearm_Actions", cooldownOnly)
            .nested("Melee_Hit_Delay", cooldownExp)
            .nested("Melee_Miss_Delay", cooldownExp)
            .build();
    }
}
