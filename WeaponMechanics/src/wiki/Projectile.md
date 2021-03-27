This section defines how your projectile moves and interacts with the environment. Remember that your projectiles
are not actually entities, this plugin just uses math to determine if a projectile hits something instead of letting
minecraft handle it.

```yaml
  Projectile: <path to another Projectile key>
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
```

#### Projectile: \<path to another Projectile key\>

If you don't want to duplicate same kind of `projectile` settings for each weapon,
you can simply make one and use it in each weapon. It is optional to use this, but recommended.

Example where weapons `my_weapon` and `my_other_weapon` would both use `my_projectile`
as their projectile configurations.
```yaml
my_weapon:
  Projectile: "my_projectile.Projectile"
  Shoot: <shoot configurations>
  Reload: <reload configurations>
  <etc.>

my_other_weapon:
  Projectile: "my_projectile.Projectile"
  Shoot: <shoot configurations>
  Reload: <reload configurations>
  <etc.>

my_projectile:
  Projectile:
    Settings: ...
    Projectile_Motion: ...
    Sticky: ...
    <etc.>
```

## Settings
Defines the main settings for the projectile (The hitbox and the display)

* `Type`: \<Type\>
    * What the projectile appears as, which is either an entity or invisible.
    * For entity types, check [references](References.md#entities).
    * If you want the projectile to be invisible, use `"invisible"`.
        * Using `invisible` is better since pitch and yaw calculations aren't required and there isn't
          need to send movement packets.
* `Width`: \<Double\>
    * The horizontal width of the hitbox of the projectile.
    * Defines "how big" the projectile is.
    * If you leave this at 0, it will default to the default hitbox size of `Type`.
* `Height`: \<Double\>
    * The vertical height of the hitbox of the projectile.
    * Defines "how big" the projectile is.
    * If you leave this at 0, it will default to the default hitbox size of `Type`.
* `Projectile_Item_Or_Block`:
    * This is the data used if the projectile type is `"falling_block"` or `"dropped_item"`.
    * This just uses the [item serializer](General.md#item-serializer).

## Projectile_Motion
Defines how the projectile moves.

#### `Gravity`: \<Double\>
How much vertical deceleration to apply. Negative numbers
make the projectile float upwards. `0.05` is a good number for this.

#### `Minimum`:
* `Speed`: \<Double\>
    * The minimum speed the projectile can travel at.
* `Remove_Projectile`: \<Boolean\>
    * Whether to remove the projectile when the minimum speed is reached.
    * true = The projectile will be removed when the minimum speed is reached.

#### `Maximum`:
* `Speed`: \<Double\>
    * The maximum speed the projectile can travel at.
* `Remove_Projectile`: \<Boolean\>
    * Whether to remove the projectile when the maximum speed is reached.

#### `Decrease_Motion`:
Think about speed. You cannot multiply speed by a negative number. A number `(0.0, 1.0)` will slow the
projectile down. A number `1.0` will keep the projectile at a constant speed. A number `(1.0, ∞)` will
increase the speed.

* `Base`: \<Double\>
    * The amount to multiply speed by every tick.
* `In_Water`: \<Double\>
    * The amount to multiply speed by when the projectile is in liquid.
* `When_Raining_Or_Snowing`: \<Double\>
    * The amount to multiply speed by when it is raining or snowing.

## Sticky
Defines to which blocks and entities this projectile can stick to.

#### Blocks:
* `Allow_Any_Block`: \<Boolean\>
    * If this is true then all blocks are stickable.
    * This overrides `Whitelist` and `List`.
* `Whitelist`: \<Boolean\>
    * Whether the use `List` as whitelist or blacklist.
    * `True` = only blocks listed in `List` can be sticked to.
* `List`: \<Material list\>
    * [Material](References.md#materials) list of stickable/unstickable blocks depending on `Whitelist`.

#### Entities:
* `Allow_Stick_To_Entities_After_Stick_Block`: \<Boolean\>
    * Whether projectile can be resticked to entity after being sticked to block.
    * In order to restick to entity, entity has to walk into projectile's hitbox.
* `Allow_Any_Entity`: \<Boolean\>
    * If this is true then all entities are stickable.
    * This overrides `Whitelist` and `List`.
* `Whitelist`: \<Boolean\>
    * Whether the use `List` as whitelist or blacklist.
    * `True` = only entities listed in `List` can be sticked to.
* `List`: \<EntityType list\>
    * [Entity](References.md#entities) list of stickable/unstickable entities depending on `Whitelist`.

## Bouncy
Defines from which blocks and entities this projectile bounces off. Using smaller projectile
width and height is better for bouncing since the calculations are then more accurate, values like `0.25` are recommended.

#### Blocks:
* `Maximum_Bounce_Amount`: \<Integer\>
    * The maximum amount of blocks this projectile can bounce off.
* `Allow_Any_Block`: \<Boolean\>
    * If this is true then all blocks are valid.
    * This overrides `Whitelist` and `List`.
* `Default_Speed_Modifier`: \<Double\>
    * The number to multiply the projectile speed by.
    * This is the default speed modifier, meaning that if the following list
      does not contain specific information for speed about the hit block, then it defaults
      to this value.
* `Whitelist`: \<Boolean\>
    * Whether the use `List` as whitelist or blacklist.
    * `True` = only blocks listed in `List` can be bounced off.
* `List`: \<Material list\>
    * [Material](References.md#materials) list of bounceable blocks depending on `Whitelist`.
    * Second arg is `speed multiplier` which multiplies projectile's current speed with its value.

#### Entities:
* `Maximum_Bounce_Amount`: \<Integer\>
    * The maximum amount of entities this projectile can bounce off.
* `Allow_Any_Entity`: \<Boolean\>
    * If this is true then all entities are valid.
    * This overrides `Whitelist` and `List`.
* `Default_Speed_Modifier`: \<Double\>
    * The number to multiply the projectile speed by.
    * This is the default speed modifier, meaning that if the following list
      does not contain specific information for speed about the hit entity, then it defaults
      to this value.
* `Whitelist`: \<Boolean\>
    * Whether the use `List` as whitelist or blacklist.
    * `True` = only entities listed in `List` can be bounced off.
* `List`: \<EntityType list\>
    * [Entity](References.md#entities) list of bounceable entities depending on `Whitelist`.
    * Second arg is `speed multiplier` which multiplies projectile's current speed with its value.

## Through
Defines which blocks and entities the projectile can pass through, and what happens to the projectile
afterwards. This is how you define bullet penetration.

#### Blocks:
* `Maximum_Pass_Throughs`: \<Integer\>
    * The maximum number of blocks to let the projectile pass through.
* `Allow_Any_Block`: \<Boolean\>
    * If this is true then all blocks are valid.
    * This overrides `Whitelist` and `List`.
* `Default_Speed_Modifier`: \<Double\>
    * The number to multiply the projectile speed by.
    * This is the default speed modifier, meaning that if the following list
      does not contain specific information for speed about the hit block, then it defaults
      to this value.
* `Whitelist`: \<Boolean\>
    * Whether the use `List` as whitelist or blacklist.
    * `True` = only blocks listed in `List` can be pass through.
* `List`: \<Material list\>
    * [Material](References.md#materials) list of valid blocks depending on `Whitelist`.
    * Second arg is `speed multiplier` which multiplies projectile's current speed with its value.

#### Entities:
* `Maximum_Pass_Throughs`: \<Integer\>
    * The maximum number of entities to let the projectile pass through.
* `Allow_Any_Entity`: \<Boolean\>
    * If this is true then all entities are valid.
    * This overrides `Whitelist` and `List`.
* `Default_Speed_Modifier`: \<Double\>
    * The number to multiply the projectile speed by.
    * This is the default speed modifier, meaning that if the following list.
      does not contain specific information for speed about the hit entity, then it defaults
      to this value.
* `Whitelist`: \<Boolean\>
    * Whether the use `List` as whitelist or blacklist.
    * `True` = only entities listed in `List` can be pass through.
* `List`: \<EntityType list\>
    * [Entity](References.md#entities) list of valid entities depending on `Whitelist`.
    * Second arg is `speed multiplier` which multiplies projectile's current speed with its value.