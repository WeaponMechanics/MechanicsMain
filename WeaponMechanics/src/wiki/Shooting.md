```yaml
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
  Spread: <SpreadSerializer> # Scroll down for more information
  Projectile: <ProjectileSerializer> # Scroll down for more information
  Recoil: <RecoilSerializer> # Scroll down for more information
  Mechanics: <MechanicsSerializer>
```
#### `Trigger`: \<Trigger\>
This is the trigger used to actually shoot the gun. See [the wiki for trigger](General.md#trigger)

#### `Projectile_Speed`: \<Double\>
This is how fast your projectile moves when it is shot (It will slow down a bit after moving). Note that
`Projectile_Speed: 10` is very slow, because the projectile will move 1 block every update. You probably
don't want to use a projectile speed over 200 (Though you can).

**Developers**: This value is divided by 10.0 when serialized.

#### `Projectiles_Per_Shot`: \<Integer\>
How many projectiles are fired every right click. This is used mainly for shotguns.

#### `Selective_Fire`:
Select fire is simple implementation to allow choosing between `SINGLE`, `BURST` and `AUTO` fire modes.

First fire mode is always `SINGLE` when weapon is received.

The order of changing fire modes is `1. SINGLE`, `2. BURST`, `3. AUTO`. 
The order can also be just `1. SINGLE`, `2. AUTO` if `BURST` isn't enabled for this weapon.

Selective fire current state can be shown [weapon info display](Information.md#weapon_info_display)
and their symbols can be changed in file `WeaponMechanics/config.yml` at `Placeholder_Symbols` section.

* `Trigger`: \<Trigger\>
  * The trigger which changes fire mode to next one
* `Mechanics`: \<Mechanics\>
  * See [the wiki for mechanics](General.md#mechanics)

#### `Delay_Between_Shots`: \<Integer\>
If your gun is in standard or burst mode, this is the time in ticks (20 ticks = 1 second) between being able
to fire your gun.

Note, if you are holding right click, this may be inaccurate by 1 tick (Due to how minecraft 
handles holding right click). This also means it is faster to spam right click then to hold 
right click (Try placing blocks by spamming right click and holding right click, it's the same thing!).

**Developers**: This value is multiplied by 50 during serialization because it's stored as millis

#### `Fully_Automatic_Shots_Per_Second`: \<Integer\>
This makes your weapon a fully automatic weapon. It is recommended to use values [1, 20], but you can use larger
numbers if you want (This will make it look like your gun is firing multiple projectiles at once due to limitations
in minecraft). I suggest using one of the following: `1`, `2`, `4`, `5`, `10`, `15`, `20` (All other values are a bit weird)

#### `Burst`:
These options make your gun a burst gun. This is useful for firing a few bullets in rapid succession
while only shooting the gun once.

* `Shots_Per_Burst`: \<Integer\>
  * How many bullets are actually fired
  * Recommend using 2 or higher
* `Ticks_Between_Each_Shot`: \<Integer\>
  * The amount of time, in ticks, between each bullet
  * 20 ticks = 1 second
  
## Spread:
Spread is used to define how "random" a bullet's path is. You obviously don't want a bullet to fly perfectly
straight every time! You can replicate video games by making guns less accurate if the player is moving or jumping, 
or you could define exact spread shapes by using images.

It's important to remember: spread is **NOT** accuracy. In fact, they are opposites. Higher spread means
less accurate.

```yaml
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
```

#### `Spread_Image`: 
Spread images are `.png` files located in `server > plugins > WeaponMechanics > spread_patterns`. If you
use this, *you cannot use other spread features*.

* `Name`: \<String\>
  * The name of the image
  * You do not need to add `.png` to the end
* `Field_Of_View_Width`: \<Double\> 
  * How wide the image should be translated to spread
  * This is a bit confusing, but basically the edge of your image will use this value
  * Try playing around with numbers like `22.5`, `45`, and `90`
  * Defaults to `45`
* `Field_Of_View_Height`: \<Double\>
  * How wide the image should be translated to spread
  * This is a bit confusing, but basically the edge of your image will use this value
  * Try playing around with numbers like `22.5`, `45`, and `90`
  * Defaults to `45`

You can create your own spread pattern by using an application like Microsoft paint, paint.net, or photoshop. 
To make a spread image, use black to define where you want bullets to shoot and white to define where you 
don't want bullets to shoot. You can use a gradient of grays to change the chance of when a bullet shoots 
(The darker the color the high the chance). **ONLY USE A GRADIENT FROM WHITE TO BLACK. NO COLORS**

You can use any size image with no impact to performance!

Here is a `512 x 512` example of a gear:

![](gear.png)

#### `Base_Spread`: \<Double\>
This is the randomness applied vertically and horizontally.

#### `Modify_Spread_When`: 
This modifies the amount from `Base_Spread`. If you use set amounts (e.x. `Zooming: -0.3`),
the plugin will *ADD* that amount to the `Base_Spread` (Remember your algebra, `0.5 + -0.3 = 0.2`).
If you use percentages (e.x. `Zooming: 100%`) the plugin will *MULTIPLY* `Base_Spread` by that
amount and in this case it would then stay same when using `100%`. If the percentage is `130%`
it would increase spread by `30%` then again if it was `80%` it would decrease by `20%`.

* `Zooming`: \<Double\> 
  * When the shooter is currently scoping/zooming with their weapon
* `Sneaking`: \<Double\>
  * When the player is sneaking/crouching (`shift` key)
* `Standing`: \<Double\>
  * When not doing any of these others
* `Walking`: \<Double\> 
  * When the shooter is moving
* `Swimming`: \<Double\> 
  * When the shooter is in water (Not 1.13+ player swimming, just if they are in water)
* `In_Midair`: \<Double\> 
  * When the shooter is in mid air (Not on the ground)
* `Gliding`: \<Double\> 
  * When the player is gliding (Using an elytra)

Note:
* If your spread goes below 0 (e.x. `0.7 - 0.5 - 0.4 = -0.2`), it will automatically round up to 0

Example:
```yaml
Modify_Spread_When:
  Zooming: -0.5    # Make the gun more accurate when scoping
  Sneaking: -0.2
  Standing: 0.0
  Walking: 0.15
  Swimming: 0.15
  In_Midair: 0.4   # Player is probably jumping, so make them inaccurate
  Gliding: 0.6     # Player is probably moving super quick, so make them inaccurate
```

#### `Changing_Spread`:
This is changes spread after the first shot. This is generally used for guns that get less accurate the more
you fire it. Many online shooters use this (Sometimes very subtly). Very commonly used automatic guns that
are being sprayed.

* `Starting_Amount`: \<Double\>
  * The changing spread start amount.
  * Basically if this is `0` there won't be any spread change in the first shot
* `Increase_Change_When`:
  * This works just like [Modify_Spread_When](#modify_spread_when) (You can use the percentages)
  * Personally, I think you should only use the `Always` option. You can use the others if you want.
  * `Always`: \<Double\>
    * Every shot
  * `Zooming`: \<Double\> 
    * When the shooter is currently scoping/zooming with their weapon
  * `Sneaking`: \<Double\>
    * When the player is sneaking/crouching (`shift` key)
  * `Standing`: \<Double\>
    * When not doing any of these others
  * `Walking`: \<Double\> 
    * When the shooter is moving
  * `Swimming`: \<Double\> 
    * When the shooter is in water (Not 1.13+ player swimming, just if they are in water)
  * `In_Midair`: \<Double\> 
    * When the shooter is in mid air (Not on the ground)
  * `Gliding`: \<Double\> 
    * When the player is gliding (Using an elytra)
* `Bounds`:
  * These are the maximum and minimum values for **changing** spread. (CHANGING spread value will always stay within those bounds)
  * `Reset_After_Reach_Bounds`: \<Boolean\>
    * Use `true` to reset the spread back to `Base_Spread` + `Starting_Amount`. Otherwise use `false`
  * `Minimum_Spread`: \<Double\>
    * The lowest changing spread value allowed
  * `Maximum_Spread`: \<Double\>
    * The highest changing spread value allowed

Notes:
* Spread is super important for your guns, it sets how people use guns, and how people move while shooting.
Certain video games may hardly use spread, and will rely mostly on [Recoil](#recoil). Some videos games 
(valorant, for example), are mostly recoil based.
* My personal suggestion is that every single one of your gun's spread works the same (If one gun has
higher spread after the first few shots, all of your guns should have higher spread after the first
few shots)

## Projectile
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
#### `Settings`:
Defines the main settings for the projectile (The hitbox and the display)

* `Type`: \<Type\>
  * What the projectile appears as, which is either an entity or invisible
  * For entity types, check [references](References.md#entities)
  * If you want the projectile to be invisible, use `"invisible"`
    * Using `invisible` is better since pitch and yaw calculations aren't required and there isn't
    need to send movement packets.
* `Width`: \<Double\>
  * The horizontal width of the hitbox of the projectile.
  * Defines "how big" the projectile is
  * If you leave this at 0, it will default to the default hitbox size of `Type`
* `Height`: \<Double\>
  * The vertical height of the hitbox of the projectile.
  * Defines "how big" the projectile is
  * If you leave this at 0, it will default to the default hitbox size of `Type`
* `Projectile_Item_Or_Block`:
  * This is the data used if the projectile type is `"falling_block"` or `"dropped_item"`
  * This just uses the [item serializer](General.md#item-serializer)
    
## Projectile_Motion:
Defines how the projectile moves

#### `Gravity`: \<Double\>
How much vertical deceleration to apply. Negative numbers 
make the projectile float upwards. `0.05` is a good number for this.

#### `Minimum`:
* `Speed`: \<Double\>
  * The minimum speed the projectile can travel at
* `Remove_Projectile`: \<Boolean\>
  * Whether to remove the projectile when the minimum speed is reached
  * true = The projectile will be removed when the minimum speed is reached
  
#### `Maximum`:
* `Speed`: \<Double\>
  * The maximum speed the projectile can travel at
* `Remove_Projectile`: \<Boolean\>
  * Whether to remove the projectile when the maximum speed is reached
  
#### `Decrease_Motion`:
Think about speed. You cannot multiply speed by a negative number. A number `(0.0, 1.0)` will slow the
projectile down. A number `1.0` will keep the projectile at a constant speed. A number `(1.0, ∞)` will
increase the speed.

* `Base`: \<Double\>
  * The amount to multiply speed by every tick
* `In_Water`: \<Double\>
  * The amount to multiply speed by when the projectile is in liquid
* `When_Raining_Or_Snowing`: \<Double\>
  * The amount to multiply speed by when it is raining or snowing

## Sticky
Defines to which blocks and entities this projectile can stick to.

#### Blocks:
* `Allow_Any_Block`: \<Boolean\>
  * If this is true then all blocks are stickable
  * This overrides `Whitelist` and `List`
* `Whitelist`: \<Boolean\>
  * Whether the use `List` as whitelist or blacklist
  * `True` = only blocks listed in `List` can be sticked to
* `List`: \<Material list\>
  * [Material](References.md#materials) list of stickable/unstickable blocks depending on `Whitelist`

#### Entities:
* `Allow_Stick_To_Entities_After_Stick_Block`: \<Boolean\>
  * Whether projectile can be resticked to entity after being sticked to block
  * In order to restick to entity, entity has to walk into projectile's hitbox
* `Allow_Any_Entity`: \<Boolean\>
  * If this is true then all entities are stickable
  * This overrides `Whitelist` and `List`
* `Whitelist`: \<Boolean\>
  * Whether the use `List` as whitelist or blacklist
  * `True` = only entities listed in `List` can be sticked to
* `List`: \<EntityType list\>
  * [Entity](References.md#entities) list of stickable/unstickable entities depending on `Whitelist`

## Bouncy
Defines from which blocks and entities this projectile bounces off. Using smaller projectile
width and height is better for bouncing since the calculations are then more accurate, values like `0.25` are recommended.

#### Blocks:
* `Maximum_Bounce_Amount`: \<Integer\>
  * The maximum amount of blocks this projectile can bounce off
* `Allow_Any_Block`: \<Boolean\> 
  * If this is true then all blocks are valid
  * This overrides `Whitelist` and `List`
* `Default_Speed_Modifier`: \<Double\>
  * The number to multiply the projectile speed by
  * This is the default speed modifier, meaning that if the following list
    does not contain specific information for speed about the hit block, then it defaults
    to this value.
* `Whitelist`: \<Boolean\>
  * Whether the use `List` as whitelist or blacklist
  * `True` = only blocks listed in `List` can be bounced off
* `List`: \<Material list\>
  * [Material](References.md#materials) list of bounceable blocks depending on `Whitelist`
  * Second arg is `speed multiplier` which multiplies projectile's current speed with its value

#### Entities:
* `Maximum_Bounce_Amount`: \<Integer\>
  * The maximum amount of entities this projectile can bounce off
* `Allow_Any_Entity`: \<Boolean\>
  * If this is true then all entities are valid
  * This overrides `Whitelist` and `List`
* `Default_Speed_Modifier`: \<Double\>
  * The number to multiply the projectile speed by
  * This is the default speed modifier, meaning that if the following list
    does not contain specific information for speed about the hit entity, then it defaults
    to this value.
* `Whitelist`: \<Boolean\>
  * Whether the use `List` as whitelist or blacklist
  * `True` = only entities listed in `List` can be bounced off
* `List`: \<EntityType list\>
  * [Entity](References.md#entities) list of bounceable entities depending on `Whitelist`
  * Second arg is `speed multiplier` which multiplies projectile's current speed with its value

## Through
Defines which blocks and entities the projectile can pass through, and what happens to the projectile
afterwards. This is how you define bullet penetration.

#### Blocks:
* `Maximum_Pass_Throughs`: \<Integer\>
  * The maximum number of blocks to let the projectile pass through
* `Allow_Any_Block`: \<Boolean\>
  * If this is true then all blocks are valid
  * This overrides `Whitelist` and `List`
* `Default_Speed_Modifier`: \<Double\>
  * The number to multiply the projectile speed by
  * This is the default speed modifier, meaning that if the following list 
    does not contain specific information for speed about the hit block, then it defaults 
    to this value.
* `Whitelist`: \<Boolean\>
  * Whether the use `List` as whitelist or blacklist
  * `True` = only blocks listed in `List` can be pass through
* `List`: \<Material list\>
  * [Material](References.md#materials) list of valid blocks depending on `Whitelist`
  * Second arg is `speed multiplier` which multiplies projectile's current speed with its value

#### Entities:
* `Maximum_Pass_Throughs`: \<Integer\>
  * The maximum number of entities to let the projectile pass through
* `Allow_Any_Entity`: \<Boolean\>
  * If this is true then all entities are valid
  * This overrides `Whitelist` and `List`
* `Default_Speed_Modifier`: \<Double\>
  * The number to multiply the projectile speed by
  * This is the default speed modifier, meaning that if the following list 
    does not contain specific information for speed about the hit entity, then it defaults 
    to this value.
* `Whitelist`: \<Boolean\>
  * Whether the use `List` as whitelist or blacklist
  * `True` = only entities listed in `List` can be pass through
* `List`: \<EntityType list\>
  * [Entity](References.md#entities) list of valid entities depending on `Whitelist`
  * Second arg is `speed multiplier` which multiplies projectile's current speed with its value

## Recoil
Recoil basically moves player screen horizontally and vertically depending on configurations.
This is really smooth in WeaponMechanics since the screen update packets are sent every `5` milliseconds
by default, that is `10` times faster than normal server tick.
The packet send interval is configurable in `/WeaponMechanics/config.yml`.

And not only there is smooth recoil push, there is also smooth recoil recovery back to normal.

```yaml
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
```

#### `Push_Time`: \<Integer\>
The time in **milliseconds** it takes to reach the full recoil amount.
`50` milliseconds is equal to `1` server tick and equal to `0.05` seconds.
Use `0` to instantly push to full recoil amount.

#### `Recover_Time`: \<Integer\>
The time in **milliseconds** it takes to recover back to normal after full recoil amount is reached.
Note that after push there is also `60` milliseconds cooldown before starting recovery to make it more smooth.
`50` milliseconds is equal to `1` server tick and equal to `0.05` seconds.
Use `0` to instantly recover to normal.

#### `Horizontal`: \<Double list\>
The list of possible horizontal changes per shot. WeaponMechanics takes one of these randomly
from this list on each shot and uses it. If you don't want horizontal recoil, then simply don't use this.

Notes:
* Negative value means left, and positive right
* Horizontal means yaw, values like `5`, `-4`, `10` are recommended.

#### `Vertical`: \<Double list\>
The list of possible vertical changes per shot. WeaponMechanics takes one of these randomly
from this list on each shot and uses it. If you don't want vertical recoil, then simply don't use this.

Notes:
* Negative value means down, and positive up
* Vertical means pitch, values like `5`, `-4`, `10` are recommended.

#### `Recoil_Pattern`:

* `Repeat_Pattern`: \<Boolean\>
  * Whether the recoil pattern should start again after reaching its end
  * `True` = when `List` has reached its end, recoil pattern will start again
  * `False` = when `List` has reached its end, there won't be recoil until push and recovery
  have finished and new shot is made.
* `List`: \<String list\>
  * `<horizontal recoil>`: First arg means horizontal recoil and it works like said [above](#horizontal-double-list)
  * `<vertical recoil>`: Second arg means vertical recoil and it also works like said [above](#vertical-double-list)
  * `<chance to skip>`: Third arg means the chance to skip this from recoil pattern.
    Value has to be between `0` and `100`. This arg is optional.

## Mechanics

See [the wiki for mechanics](General.md#mechanics)