```yaml
  Shoot:
    Trigger: <Trigger>
    Projectile_Speed: <Double>
    Projectiles_Per_Shot: <Integer>
    Selective_Fire:
      Trigger: <Trigger>
      Mechanics: <Mechanics>
    Delay_Between_Shots: <Integer>
    Fully_Automatic_Shots_Per_Second: <Integer>
    Burst:
      Shots_Per_Burst: <Integer>
      Ticks_Between_Each_Shot: <Integer>
    Spread: <Spread> # Scroll down for more information
    Projectile: <Projectile> # Scroll down for more information
    Recoil: <Recoil> # Scroll down for more information
    Mechanics:
```
#### `Trigger`: \<Trigger\>
This is the trigger used to actually shoot the gun. See [the wiki for trigger](#todo)

#### `Projectile_Speed`: \<Double\>
This is how fast your projectile moves when it is shot (It will slow down a bit after moving). Note that
`Projectile_Speed: 10` is very slow, because the projectile will move 1 block every update. You probably
don't want to use a projectile speed over 200 (Though you can).

**Developers**: This value is divided by 10.0 when serialized.

#### `Projectiles_Per_Shot`: \<Integer\>
How many projectiles are fired every right click. This is used mainly for shotguns.

#### `Selective_Fire`: 
This is a very simple version of [firemodes](#todo). 

#### `Delay_Between_Shots`: \<Integer\>
If your gun is in standard or burst mode, this is the time in ticks (20 ticks = 1 second) between being able
to fire your gun.

Note, if you are holding right click, this may be inaccurate by 1 tick (Due to how minecraft 
handles holding right click). This also means it is faster to spam right click then to hold 
right click (Try placing blocks by spamming right click and holding right click, it's the same thing!).

**Developers**: This value is multiplied by 50 during serialization

#### `Fully_Automatic_Shots_Per_Second`: \<Integer\>
This makes your weapon a fully automatic weapon. It is recommended to use values [1, 20], but you can use larger
numbers if you want (This will make it look like your gun is firing multiple projectiles at once due to limitations
in minecraft). I suggest using one of the following: `1`, `2`, `4`, `5`, `10`, `15`, `20` (All other values are a bit weird)

#### `Burst`:
These options make your gun a burst gun. This is useful for firing a few bullets in rapid succession
while only shooting the gun once.

  * `Shots_Per_Burst`: 
    * How many bullets are actually fired
    * Recommend using 2 or higher
  * `Ticks_Between_Each_Shot`:
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
If you use percentages (e.x. `Zooming: 0.20%`) the plugin will *MULTIPLY* `Base_Spread` by that
amount.

  * `Zooming`: \<true/false\> 
    * When the shooter is currently scoping/zooming with their weapon
  * `Sneaking`: \<true/false\>
    * When the player is sneaking/crouching (`shift` key)
  * `Standing`: \<true/false\>
    * 
  * `Walking`: \<true/false\> 
    * When the shooter is moving
  * `Swimming`: \<true/false\> 
    * When the shooter is in water (Not 1.13+ player swimming, just if they are in water)
  * `In_Midair`: \<true/false\> 
    * When the shooter is in mid air (Not on the ground)
  * `Gliding`: \<true/false\> 
    * When the player is gliding (Using an elytra)

Notes:
  * `2%` will actually double the amount of spread, making your gun less accurate
    * Meaning never use `200%`, for example, because that is 200 times more spread
  * `-2%` will cause a negative spread, meaning you should never use negative percentages
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

* `Starting_Amount`: todo
* `Increase_Change_When`:
  * This works just like [Modify_Spread_When]() (You can use the percentages)
  * Personally, I think you should only use the `Always` option. You can use the others if you want.
  * `Always`: \<true/false\>
    * 
  * `Zooming`: \<true/false\>
    * 
  * `Sneaking`: \<true/false\>
    *
  * `Standing`: \<true/false\>
    * 
  * `Walking`: \<true/false\>
    * 
  * `Swimming`: \<true/false\>
    * 
  * `In_Midair`: \<true/false\>
    *
  * `Gliding`: \<true/false\>
* `Bounds`:
  * These are the maximum and minimum values for spread. (Spread will always stay within those bounds)
  * `Reset_After_Reach_Bounds`: \<true/false\>
    * Use `true` to reset the spread back to `Base_Spread`. Otherwise use `false`
  * `Minimum_Spread`: \<Double\>
    * The lowest spread value allowed
  * `Maximum_Spread`: \<Double\>
    * The highest spread value allowed

Notes:
  * Spread is so important for your guns, it sets how people use guns, and how people move while shooting.
  Certain video games may hardly use spread, and will rely mostly on [Recoil](todo). Some videos games 
  (valorant, for example), are mostly recoil based.
  * My personal suggestion is that every single one of your gun's spread works the same (If one gun has
  higher spread after the first few shots, all of your guns should have higher spread after the first
  few shots)

Example: 
```yaml
    Spread:
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

## Projectile
This section defines how your projectile moves and interacts with the environment. Remember that your projectiles 
are not actually entities, this plugin just uses math to determine if a projectile hits something instead of letting
minecraft handle it (which makes the plugin much faster then other gun plugins!).

```yaml
    Projectile: <path to another Projectile key>
      Settings:
        Type: <ProjectileType>
        Width: <projectile width>
        Height: <projectile height>
        Projectile_Item_Or_Block: <Item>
      Projectile_Motion:
        Gravity: <gravity multiplier>
        Minimum:
          Speed: <minimum speed of projectile>
          Remove_Projectile: <true/false>
        Maximum:
          Speed: <maximum speed of projectile>
          Remove_Projectile: <true/false>
        Decrease_Motion:
          Base: <multiplier>
          In_Water: <multiplier>
          When_Raining_Or_Snowing: <multiplier>
      Through:
        Blocks:
          Default_Speed_Modifier: <multiplier>
          Default_Damage_Modifier: <amount>
          Maximum_Pass_Throughs: <maximum block pass throughs>
          Whitelist: <true/false>
          List:
            - <Material>:<data>-<speed multiplier>-<damage modifier>
            - <etc.>
        Entities:
          Default_Speed_Modifier: <multiplier>
          Default_Damage_Modifier: <amount>
          Maximum_Pass_Throughs: <maximum entity pass throughs>
          Whitelist: <true/false>
          List:
            - <EntityType>-<speed multiplier>-<damage modifier>
            - <etc.>
```