### Complete List of Modules
This is the *complete list of modules* for **WeaponMechanics v1.0.0**. This list
will likely be updated whenever a new version is released. To see the history of
changes to this page, [click here]().

Before you go copying and pasting, it is important to know that you should almost
never be using every single line here. This list is useful for copying and pasting
exactly what your weapon needs, and removing everything else.

```yaml
My_Weapon:
  Info:
    Weapon_Item: <item serializer>
    Weapon_Info_Display:
      Action_Bar:
        Message: <message>
        Time: <ticks>
      Title:
        Title: <title>
        Subtitle: <subtitle>
        Time: <fade in ticks>-<stay ticks>-<fade out ticks>
      Boss_Bar:
        Title: <title>
        Bar_Color: <BarColor>
        Bar_Style: <BarStyle>
        Time: <ticks>
      Update_Item_Name: <true/false>
      Show_Ammo_In:
        Boss_Bar_Progress: <true/false>
        Exp_Level: <true/false>
        Exp_Progress: <true/false>
    Dual_Wield:
      Whitelist: <true/false>
      Weapons:
        - <weapon title>
        - <etc.>
      Mechanics_On_Deny: <MechanicsSerializer>
    Weapon_Converter_Check:
      Type: <true/false>
      Name: <true/false>
      Lore: <true/false>
      Enchants: <true/false>
    Weapon_Get_Actions: <MechanicsSerializer>
    Cancel:
      Block_Interactions: <true/false>
      Item_Interactions: <true/false>
      Drop_Item: <true/false>
      Swap_Hands: <true/false>
      Arm_Swing_Animation: <true/false>
  Firearm_Action:
    Type: <FirearmType>
    Firearm_Action_Frequency: <use every x amount of shots>
    Open:
      Time: <ticks>
      Mechanics: <MechanicsSerializer>
    Close:
      Time: <ticks>
      Mechanics: <MechanicsSerializer>
  Damage:
    Base_Damage: <amount>
    Base_Explosion_Damage: <amount>
    Fire_Ticks: <ticks>
    Enable_Friendly_Fire: <Boolean>
    Enable_Owner_Immunity: <Boolean>
    Armor_Damage: <Integer>
    Shooter_Mechanics: <MechanicsSerializer>
    Victim_Mechanics: <MechanicsSerializer>
    Head:
      Bonus_Damage: <amount>
      Shooter_Mechanics: <MechanicsSerializer>
      Victim_Mechanics: <MechanicsSerializer>
    Body:
      Bonus_Damage: <amount>
      Shooter_Mechanics: <MechanicsSerializer>
      Victim_Mechanics: <MechanicsSerializer>
    Arms:
      Bonus_Damage: <amount>
      Shooter_Mechanics: <MechanicsSerializer>
      Victim_Mechanics: <MechanicsSerializer>
    Legs:
      Bonus_Damage: <amount>
      Shooter_Mechanics: <MechanicsSerializer>
      Victim_Mechanics: <MechanicsSerializer>
    Feet:
      Bonus_Damage: <amount>
      Shooter_Mechanics: <MechanicsSerializer>
      Victim_Mechanics: <MechanicsSerializer>
    Backstab:
      Bonus_Damage: <amount>
      Shooter_Mechanics: <MechanicsSerializer>
      Victim_Mechanics: <MechanicsSerializer>
    Critical_Hit:
      Chance: <1-100>
      Bonus_Damage: <amount>
      Shooter_Mechanics: <MechanicsSerializer>
      Victim_Mechanics: <MechanicsSerializer>
    Kill:
      Shooter_Mechanics: <MechanicsSerializer>
      Victim_Mechanics: <MechanicsSerializer>
    Dropoff:
      - <travel distance>~<damage amount>
  Scope:
    Trigger: <trigger serializer>
    Night_Vision: <true/false>
    Zoom_Amount: <1-32>
    Mechanics: <MechanicsSerializer>
    Zoom_Off:
      Trigger: <trigger serializer>
      Mechanics: <MechanicsSerializer>
    Zoom_Stacking:
      Maximum_Stacks: <maximum zoom stack amount>
      Increase_Zoom_Per_Stack: <amount>
      Mechanics: <MechanicsSerializer>
  Reload:
    Trigger: <trigger serializer>
    Magazine_Size: <amount>
    Ammo_Per_Reload: <amount>
    Unload_Ammo_On_Reload: <true/false>
    Reload_Duration: <ticks>
    Start_Mechanics: <MechanicsSerializer>
    Finish_Mechanics: <MechanicsSerializer>
    Ammo:
      Out_Of_Ammo: <MechanicsSerializer>
      Use_Item_As_Ammo:
        Ammo_Name: <name of ammo>
        Magazine:
          Item: <item serializer>
          Not_Able_To_Fill:
            Not_Same_Ammo_Name: <MechanicsSerializer>
            Magazine_Already_Full: <MechanicsSerializer>
          Magazine_Filled: <MechanicsSerializer>
        Ammo: <item serializer>
        Ammo_Converter_Check:
          Type: <true/false>
          Name: <true/false>
          Lore: <true/false>
          Enchants: <true/false>
      Use_Exp_As_Ammo:
        Exp_Cost: <exp per one ammo>
      Use_Money_As_Ammo:
        Money_Cost: <money per one ammo>
  Projectile: 
    Settings:
      Type: <ProjectileType>
      Width: <projectile width>
      Height: <projectile height>
      Projectile_Item_Or_Block:
        Type: <Material>:<Data>
        Durability: <durability>
        Unbreakable: <true/false>
        Custom_Model_Data: <custom model data number>
        Skull:
          Owning_Player: <UUID of player or name of player>
        Potion:
          Color: <ColorType>
    Projectile_Motion:
      Gravity: <gravity multiplier>
      Minimum:
        Speed: <minimum speed of projectile>
        Remove_Projectile_On_Speed_Reached: <true/false>
      Maximum:
        Speed: <maximum speed of projectile>
        Remove_Projectile_On_Speed_Reached: <true/false>
      Decrease_Motion:
        Base: <multiplier>
        In_Water: <multiplier>
        When_Raining_Or_Snowing: <multiplier>
    Sticky:
      Blocks:
        Allow_Any_Block: <true/false>
        Whitelist: <true/false>
        List:
          - <Material>:<data>
          - <etc.>
      Entities:
        Allow_Stick_To_Entities_After_Stick_Block: <true/false>
        Allow_Any_Entity: <true/false>
        Whitelist: <true/false>
        List:
          - <EntityType>
          - <etc.>
    Bouncy:
      Blocks:
        Maximum_Bounce_Amount: <amount>
        Allow_Any_Block: <true/false>
        Default_Speed_Modifier: <multiplier>
        Whitelist: <true/false>
        List:
          - <Material>:<data>-<speed multiplier>
          - <etc.>
      Entities:
        Maximum_Bounce_Amount: <amount>
        Allow_Any_Entity: <true/false>
        Default_Speed_Modifier: <multiplier>
        Whitelist: <true/false>
        List:
          - <EntityType>-<speed multiplier>
          - <etc.>
    Through:
      Blocks:
        Maximum_Pass_Throughs: <maximum block pass throughs>
        Allow_Any_Block: <true/false>
        Default_Speed_Modifier: <multiplier>
        Whitelist: <true/false>
        List:
          - <Material>:<data>-<speed multiplier>
          - <etc.>
      Entities:
        Maximum_Pass_Throughs: <maximum entity pass throughs>
        Allow_Any_Entity: <true/false>
        Default_Speed_Modifier: <multiplier>
        Whitelist: <true/false>
        List:
          - <EntityType>-<speed multiplier>
          - <etc.>
  Shoot:
    Trigger: <TriggerSerializer>
    Projectile_Speed: <speed>
    Projectiles_Per_Shot: <amount>
    Selective_Fire:
      Trigger: <TriggerSerializer>
      Mechanics: <MechanicsSerializer>
    Delay_Between_Shots: <ticks>
    Fully_Automatic_Shots_Per_Second: <amount>
    Burst:
      Shots_Per_Burst: <amount>
      Ticks_Between_Each_Shot: <ticks>
    Spread:
      Spread_Image:
        Name: <path>
        Field_Of_View_Width: <degrees>
        Field_Of_View_Height: <degrees>
      Base_Spread: <base spread>
      Modify_Spread_When:
        Zooming: <amount> or <amount>%
        Sneaking: <amount> or <amount>%
        Standing: <amount> or <amount>%
        Walking: <amount> or <amount>%
        Swimming: <amount> or <amount>%
        In_Midair: <amount> or <amount>%
        Gliding: <amount> or <amount>%
      Changing_Spread:
        Starting_Amount: <amount>
        Increase_Change_When:
          Always: <amount> or <amount>%
          Zooming: <amount> or <amount>%
          Sneaking: <amount> or <amount>%
          Standing: <amount> or <amount>%
          Walking: <amount> or <amount>%
          Swimming: <amount> or <amount>%
          In_Midair: <amount> or <amount>%
          Gliding: <amount> or <amount>%
        Bounds:
          Reset_After_Reaching_Bound: <true/false>
          Minimum_Spread: <minimum spread>
          Maximum_Spread: <maximum spread>
    Recoil:
      Push_Time: <push time in millis>
      Recover_Time: <recover time in millis>
      Horizontal:
        - <horizontal recoil>
        - <etc.>
      Vertical:
        - <vertical recoil>
        - <etc.>
      Recoil_Pattern:
        Repeat_Pattern: <true/false>
        List:
          - <horizontal recoil>-<vertical recoil>-<chance to skip>%
          - <etc.>
    Mechanics: <MechanicsSerializer>
  Explosion:
    Disable_Vanilla_Knockback: <Boolean>
    Explosion_Shape: <Sphere/Cube/Parabola/Default>
    Explosion_Exposure: <Default/Distance/None>
    Explosion_Type_Data:
      Yield: <Double>
      Radius: <Double>
      Angle: <Double>
      Depth: <Double>
      Width: <Double>
      Height: <Double>
      Rays: <Integer>
    Detonation:
      Detonate_After_Ticks: <Ticks>
      Impact_When:
        Shoot: <Boolean>
        Entity: <Boolean>
        Block: <Boolean>
        Liquid: <Boolean>
    Cluster_Bomb:
      Split_Projectile: <ProjectileSerializer>
      Projectile_Speed: <Double>
      Number_Of_Bombs: <Integer>
      Number_Of_Splits: <Integer>
    Airstrike:
      Dropped_Projectile: <ProjectileSerializer>
      Minimum_Bombs: <Integer>
      Maximum_Bombs: <Integer>
      Height: <Double>
      Vertical_Randomness: <Double>
      Distance_Between_Bombs: <Double>
      Maximum_Distance_From_Center: <Double>
      Layers: <Integer>
      Delay_Between_Layers: <Integer>
    Block_Damage:
      Break_Blocks: <Boolean>
      Spawn_Falling_Block_Chance: <Double>
      Damage_Per_Hit: <Integer>
      Blacklist: <Boolean>
      Block_List:
        - <Material>:<Data>~<Integer>
      Shots_To_Break_Blocks:
        - <Material>:<Data>~<Integer>
    Regeneration:
      Ticks_Before_Start: <Integer>
      Max_Blocks_Per_Update: <Integer>
      Ticks_Between_Updates: <Integer>
```