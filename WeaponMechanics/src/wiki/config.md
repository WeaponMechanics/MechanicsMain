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
  Parabolic:
    Noise_Chance: <Double>
    Noise_Distance: <Double>
  Spherical:
    Noise_Chance: <Double>
    Noise_Distance: <Double>

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

#### `Explosions`:
Determines how "random" the explosions look. This effect looks best for smaller explosions. 
  * `Noise_Chance`: \<Double\>
    * A number between 0.0 and 1.0.
    * Higher numbers means more air blocks.
  * `Noise_Distance`: \<Double\>
    * A number between 0.0 and 1.0.
    * Defines how far from the edge of the explosion to be considered noise.
    
#### `Placeholder_Symbols`:
These are the symbols used to various weapon states.

#### `Damage`:
Defines exactly how damage is applied to entities.

  * `Minimum_Rate`: \<Double\>
    * The minimum number to multiply damage by.
    * This makes sure that bullets don't accidentally heal people.
  * `Maximum_Rate`: \<Double\>
    * The maximum number to multiply damage by.
  * `Potions`:
    * Changes rate based on which potion effects the entity has.
    * Check the [Potion List](#Reference) for the different potion effects.
    * Check the default [config.yml](#Default Config) to see the format.
  * `Armor`:
    * Changes rate based on which armor pieces are equipped to different slots.
    * Check the default [config.yml](#Default Config) to see the format.
  * `Movement`:
    * Changes rate based on how the entity is moving/standing.
    * The idea here is to deal less damage to somebody who is walking then running.
  * `Critical_Points`:
    * Changes rate based on where the bullet hit the entity.
    * You probably want leg/arm shots to deal less damage than headshots.
    * Note, not all mobs have all different limbs, and calculates may not be 100% accurate.
    
#### `Entity_Hitboxes`: 
Defines each entity hitbox. Some ideas for you:
  * Change the size of an entity's head 
    * Heads in minecraft are HUGE. If you want headshots to instant kill, you may want to make the hitbox smaller.
  * Add new mobs
    * As new minecraft versions come out, you may want to add that mob to this list
    * You should NEVER have to do this. We will be updating this list as we update the plugin.

#### Default Config
<details>
    <summary>Default Config Options. This may be out-dated!</summary>

```yaml
# the debug level dictates what is printed to console
#   0: No printing
#   1: Errors/Info
#   2: Warnings (Suggested)
#   3: Debug
Debug_Level: 2

# This feature is currently incomplete, and will be added in a future update.
#
# Statistic tracking mostly exists for leaderboards. Currently the following
# statistics are tracked. In the future, more may be tracked.
#  - kills (per weapon)
Statistic_Tracker: "INCOMPLETE, DO NOT USE"

# Notifies you when there is an update. Major updates are exactly that: Major
# changes to the codebase, or a large number of changes. A minor is generally
# a small number of changes. A patch could be a very specific feature changed
# or added, or a bugfix to an existing feature.
Update_Checker:
  Enable: true
  Required_Versions_Behind:
    Major: 1
    Minor: 1
    Patch: 1

# If you don't use a specific trigger, it can be disabled here. The performance
# boost is minor, but it's here if you need it.
Disabled_Trigger_Checks:
  In_Midair: false
  Standing_And_Walking: false
  Jump: false
  Double_Jump: false
  Swim: false
  Glide: false
  Sneak: false
  Sprint: false
  Right_And_Left_Click: false
  Drop_Item: false
  Swap_Main_And_Hand_Items: false

# You can disable certain events to improve performance. Any addons that use
# these events will break.
Disabled_Events:
  Projectile_Move_Event: false

# This defines how smooth weapon recoil is. Lower values means smoother recoil,
# but more packets sent. In default minecraft this value would be 50. In WM, it
# can be lower. It is recommended to use values divisible by 5.
Recoil_Millis_Between_Rotations: 5

# These options define how "random" the explosions are. It doesn't effect block
# regeneration. These effects don't look very pretty on larger explosions, but
# they improve smaller ones.
Explosions:
  Cuboid:
    Noise_Chance: 0.20
    Noise_Distance: 1.0
  Parabolic:
    Noise_Chance: 0.25
    Noise_Distance: 1.25
  Spherical:
    Noise_Chance: 0.10
    Noise_Distance: 1.0

# These are the symbols used for various weapon states.
Placeholder_Symbols:
  REVOLVER:
    Open: "□"
    Close: "■"
    Ready: ""
  PUMP:
    Open: "□"
    Close: "■"
    Ready: ""
  LEVER:
    Open: "□"
    Close: "■"
    Ready: ""
  Reload: "ᴿ"
  Selective_Fire:
    SINGLE: "S"
    BURST: "B"
    AUTO: "A"

# This is the global damaging system. It defines how bullets damage entities
# with potion effects, armor, and other statuses.
Damage:
  Minimum_Rate: 0.5
  Maximum_Rate: 2.0
  Potions:
    DAMAGE_RESISTANCE: -0.05
    WEAKNESS: 0.05
  Armor:
    HELMET:
      LEATHER: -0.01
      GOLD: -0.02
      GOLDEN: -0.02
      CHAIN: -0.03
      IRON: -0.04
      DIAMOND: -0.05
      NETHERITE: -0.05
      TURTLE: -0.075
    CHESTPLATE:
      LEATHER: -0.01
      GOLD: -0.02
      GOLDEN: -0.02
      CHAIN: -0.03
      IRON: -0.04
      DIAMOND: -0.05
      NETHERITE: -0.05
    LEGGINS:
      LEATHER: -0.01
      GOLD: -0.02
      GOLDEN: -0.02
      CHAIN: -0.03
      IRON: -0.04
      DIAMOND: -0.05
      NETHERITE: -0.05
    BOOTS:
      LEATHER: -0.01
      GOLD: -0.02
      GOLDEN: -0.02
      CHAIN: -0.03
      IRON: -0.04
      DIAMOND: -0.05
      NETHERITE: -0.05
    Enchantments:
      PROTECTION_ENVIRONMENTAL: -0.005
      PROJECTILE_PROTECTION: -0.0175
  Movement:
    Sneaking: -0.01
    Walking: 0.025
    Swimming: -0.05
    Sprinting: 0.05
    In_Midair: 0.03
  Critical_Points:
    HEAD: 0.05
    BODY: 0.0
    ARMS: -0.005
    LEGS: -0.005
    FEET: -0.005

# These values are used internally for figuring out headshots, bodyshots, etc.
# Changing these values effectively allow you to decide how big a head hitbox
# (and other hitboxes) are.
#
# To see a visual representation of the hitboxes, use /wm test hitbox
Entity_Hitboxes:
  ELDER_GUARDIAN:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  WITHER_SKELETON:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
  STRAY:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
  HUSK:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
  ZOMBIE_VILLAGER:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.55
    ARMS: true
    LEGS: 0.125
    FEET: 0.075
  SKELETON_HORSE:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: false
    LEGS: 0.4
    FEET: 0.1
  ZOMBIE_HORSE:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: false
    LEGS: 0.4
    FEET: 0.1
  ARMOR_STAND:
    Horizontal_Entity: false
    HEAD: 0.0
    BODY: 0.5
    ARMS: true
    LEGS: 0.35
    FEET: 0.15
  DONKEY:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: false
    LEGS: 0.4
    FEET: 0.1
  MULE:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: false
    LEGS: 0.4
    FEET: 0.1
  EVOKER:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.55
    ARMS: true
    LEGS: 0.125
    FEET: 0.075
  VEX:
    Horizontal_Entity: false
    HEAD: 0.3
    BODY: 0.4
    ARMS: true
    LEGS: 0.2
    FEET: 0.1
  VINDICATOR:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.475
    ARMS: true
    LEGS: 0.2
    FEET: 0.075
  ILLUSIONER:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.55
    ARMS: true
    LEGS: 0.125
    FEET: 0.075
  CREEPER:
    Horizontal_Entity: false
    HEAD: 0.3
    BODY: 0.45
    ARMS: false
    LEGS: 0.2
    FEET: 0.05
  SKELETON:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
  SPIDER:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: true #Arms ARE the legs
    LEGS: 0.0
    FEET: 0.0
  GIANT:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
  ZOMBIE:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
  SLIME:
    Horizontal_Entity: false
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  GHAST: # Tentacles aren't part of hitbox
    Horizontal_Entity: false
    HEAD: 1.0
    BODY: 0.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  PIG_ZOMBIE:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
  ENDERMAN:
    Horizontal_Entity: false
    HEAD: 0.2
    BODY: 0.25
    ARMS: true
    LEGS: 0.5
    FEET: 0.05
  CAVE_SPIDER:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: true
    LEGS: 0.0 # Basically, the arms are now the legs.
    FEET: 0.0
  SILVERFISH:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  BLAZE:
    Horizontal_Entity: false
    HEAD: 0.335
    BODY: 0.335
    ARMS: false
    LEGS: 0.33 # Legs go under blaze as well -- weird hitbox
    FEET: 0.0
  MAGMA_CUBE:
    Horizontal_Entity: false
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  ENDER_DRAGON:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.8
    ARMS: true
    LEGS: 0.11
    FEET: 0.09
  WITHER:
    Horizontal_Entity: false
    HEAD: 0.365
    BODY: 0.335
    ARMS: false
    LEGS: 0.15
    FEET: 0.15
  BAT:
    Horizontal_Entity: false
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  WITCH:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.55
    ARMS: true
    LEGS: 0.125
    FEET: 0.075
  ENDERMITE:
    Horizontal_Entity: false
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  GUARDIAN:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  SHULKER:
    Horizontal_Entity: false
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  PIG:
    Horizontal_Entity: true
    HEAD: 0
    BODY: 0.625
    ARMS: false
    LEGS: 0.25
    FEET: 0.125
  SHEEP:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.55
    ARMS: false
    LEGS: 0.225
    FEET: 0.225
  COW:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: false
    LEGS: 0.45
    FEET: 0.05
  CHICKEN:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.6
    ARMS: true # wings
    LEGS: 0.35
    FEET: 0.05
  SQUID:
    Horizontal_Entity: false
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  WOLF:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: false
    LEGS: 0.4
    FEET: 0.1
  MUSHROOM_COW:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: false
    LEGS: 0.45
    FEET: 0.05
  SNOWMAN:
    Horizontal_Entity: false
    HEAD: 0.34
    BODY: 0.31
    ARMS: true # Very tiny arms
    LEGS: 0.35
    FEET: 0.0
  OCELOT:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.65
    ARMS: false
    LEGS: 0.3
    FEET: 0.05
  IRON_GOLEM:
    Horizontal_Entity: false
    HEAD: 0.225
    BODY: 0.425
    ARMS: true
    LEGS: 0.3
    FEET: 0.05
  HORSE:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: false
    LEGS: 0.4
    FEET: 0.1
  RABBIT:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.45
    ARMS: false
    LEGS: 0.45
    FEET: 0.1
  POLAR_BEAR:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.6
    ARMS: false
    LEGS: 0.3
    FEET: 0.1
  LLAMA:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.55
    ARMS: false
    LEGS: 0.35
    FEET: 0.1
  PARROT:
    Horizontal_Entity: false
    HEAD: 0.4
    BODY: 0.4
    ARMS: true # wings
    LEGS: 0.1
    FEET: 0.1
  VILLAGER:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.55
    ARMS: true
    LEGS: 0.125
    FEET: 0.075
  TURTLE:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.7
    ARMS: true # flippers
    LEGS: 0.3
    FEET: 0.0
  PHANTOM:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: true # wings
    LEGS: 0.0
    FEET: 0.0
  COD:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  SALMON:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  PUFFERFISH:
    Horizontal_Entity: false
    HEAD: 1.0
    BODY: 0.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  TROPICAL_FISH:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 1.0
    ARMS: false
    LEGS: 0.0
    FEET: 0.0
  DROWNED:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
  DOLPHIN:
    Horizontal_Entity: true
    HEAD: 0
    BODY: 1.0
    ARMS: false
    LEGS: 0
    FEET: 0
  CAT:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.65
    ARMS: false
    LEGS: 0.3
    FEET: 0.05
  PANDA:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.6
    ARMS: false
    LEGS: 0.3
    FEET: 0.1
  PILLAGER:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.55
    ARMS: true
    LEGS: 0.125
    FEET: 0.075
  RAVAGER:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.5
    ARMS: true # Long legs
    LEGS: 0.4
    FEET: 0.1
  TRADER_LLAMA:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.55
    ARMS: false
    LEGS: 0.35
    FEET: 0.1
  WANDERING_TRADER:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.55
    ARMS: true
    LEGS: 0.125
    FEET: 0.075
  FOX:
    Horizontal_Entity: true
    HEAD: 0.0
    BODY: 0.6
    ARMS: false
    LEGS: 0.3
    FEET: 0.1
  BEE:
    Horizontal_Entity: false
    HEAD: 0.0
    BODY: 0.925
    ARMS: true
    LEGS: 0.05
    FEET: 0.025
  PLAYER:
    Horizontal_Entity: false
    HEAD: 0.25
    BODY: 0.375
    ARMS: true
    LEGS: 0.25
    FEET: 0.125
```
</details>