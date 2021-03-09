This page is for information on the WeaponMechanics `config.yml` file. This file is loaded separately then the weapon 
configuration.
```yaml
Debug_Level: <Integer>

Update_Checker:
  Enable: <Boolean>
  Required_Versions_Behind:
    Major: <Integer>
    Minor: <Integer>
    Patch: <Integer>

Disabled_Trigger_Checks:
  In_Midair: <Boolean>
  Standing_And_Walking: <Boolean>
  Jump: <Boolean>
  Double_Jump: <Boolean>
  Swim: <Boolean>
  Glide: <Boolean>
  Sneak: <Boolean>
  Sprint: <Boolean>
  Right_And_Left_Click: <Boolean>
  Drop_Item: <Boolean>
  Swap_Main_And_Hand_Items: <Boolean>

Disabled_Events:
  Projectile_Move_Event: <Boolean>

Recoil_Millis_Between_Rotations: <Integer>

Explosions:
  Cuboid:
    Noise_Chance: <Double>
    Noise_Distance: <Double>
    No_Block_Damage_In_Liquid: <Boolean>
  Parabolic:
    Noise_Chance: <Double>
    Noise_Distance: <Double>
    No_Block_Damage_In_Liquid: <Boolean>
  Spherical:
    Noise_Chance: <Double>
    Noise_Distance: <Double>
    No_Block_Damage_In_Liquid: <Boolean>

Placeholder_Symbols:
  REVOLVER:
    Open: <String>
    Close: <String>
    Ready: <String>
  PUMP:
    Open: <String>
    Close: <String>
    Ready: <String>
  LEVER:
    Open: <String>
    Close: <String>
    Ready: <String>
  Reload: <String>
  Selective_Fire:
    SINGLE: <String>
    BURST: <String>
    AUTO: <String>

Damage:
  Minimum_Rate: <Double>
  Maximum_Rate: <Double>
  Potions:
    Potion_Type: <Double>
  Armor:
    Equipment_Slot:
      Material: <Double>
    Enchantments:
      Enchantment_Type: <Double>
  Movement:
    Sneaking: <Double>
    Walking: <Double>
    Swimming: <Double>
    Sprinting: <Double>
    In_Midair: <Double>
  Critical_Points:
    HEAD: <Double>
    BODY: <Double>
    ARMS: <Double>
    LEGS: <Double>
    FEET: <Double>

Entity_Hitboxes:
  ENTITY_TYPE:
    Horizontal_Entity: <Boolean>
    HEAD: <Double>
    BODY: <Double>
    ARMS: <Boolean>
    LEGS: <Double>
    FEET: <Double>
```

#### `Debug_level`: \<Integer\>
Defines which messages to log.
  0. No messages are logged.
  1. Errors and important plugin information is printed.
  2. Warnings (Generally possible config mistakes) are printed.
  3. Debug messages are printed to console.

#### `Update_Checker`:
  * `Enable`: \<Boolean\> 
    * Whether to check for updates.
  * `Required_Versions_Behind`:
    * How outdated the plugin has to be before nagging you to update. 
    
#### `Disabled_Trigger_Checks`:
Defines which triggers shouldn't be calculated. Disabling triggers that you do not use may improve performance 
by the *smallest amount*, but I personally recommend you don't use this.

#### `Disabled_Events`:
Defines which events shouldn't be calculated. Disabling these events may improve performance, but it can break
compatibility with other plugins. Use with care.

#### `Recoil_Millis_Between_Rotations`: \<Integer\>
Defines how smooth weapon screen recoil is. Lower values means smoother recoil, but slightly worse performance.
It is recommended to use values divisible by 5.


