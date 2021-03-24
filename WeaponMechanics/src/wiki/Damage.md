```yaml
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
```

#### `Base_Damage`: \<Double\>
Defines projectile's basic damage to entities. Keep in mind that damage point,
backstab, damage rates defined in `WeaponMechanics/config.yml` and things like that
will modify the actual final damage caused to entity.

Note:
* `1` damage is equal to `0.5` hearts. Therefore `20` damage is equal to `10` hearts.

#### `Base_Explosion_Damage`: \<Double\>
CJCrafter you'll have to fill this (exposures)

#### `Fire_Ticks`: \<Integer\>
Defines for how many ticks victim is set on fire.

#### `Enable_Friendly_Fire`: \<Boolean\>
Defines whether friendly fire is allowed. Basically `true` means that
shooter can damage players in same team.

#### `Enable_Owner_Immunity`: \<Boolean\>
Defines whether shooter is immune to this damage. `True` means that
shooter is immune to this damage.

#### `Armor_Damage`: \<Integer\>
Defines the amount of durability loss on a victim's armor.
If hit point (`HEAD`, `BODY`, `LEGS`, `FEET`) is found then only armor piece in that slot is damaged.
Otherwise, all armor pieces are damaged, and this is most likely only in explosion damage.

#### `Shooter_Mechanics`: \<Mechanics\>
These mechanics are run for shooter when doing any kind of damage
See [the wiki for mechanics](General.md#mechanics)

#### `Victim_Mechanics`: \<Mechanics\>
These mechanics are run for victim when doing any kind of damage
See [the wiki for mechanics](General.md#mechanics)

#### `Head`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting head will cause
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for shooter when damaging head
  * See [the wiki for mechanics](General.md#mechanics)
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for victim when being damaged to head
  * See [the wiki for mechanics](General.md#mechanics)

#### `Body`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting body will cause
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for shooter when damaging body
  * See [the wiki for mechanics](General.md#mechanics)
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for victim when being damaged to body
  * See [the wiki for mechanics](General.md#mechanics)

#### `Arms`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting arms will cause
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for shooter when damaging arms
  * See [the wiki for mechanics](General.md#mechanics)
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for victim when being damaged to arms
  * See [the wiki for mechanics](General.md#mechanics)

#### `Legs`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting legs will cause
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for shooter when damaging legs
  * See [the wiki for mechanics](General.md#mechanics)
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for victim when being damaged to legs
  * See [the wiki for mechanics](General.md#mechanics)

#### `Feet`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting feet will cause
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for shooter when damaging feet
  * See [the wiki for mechanics](General.md#mechanics)
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for victim when being damaged to feet
  * See [the wiki for mechanics](General.md#mechanics)

#### `Backstab`:
Damage is backstab when projectile comes from behind victim or
when explosion occurs behind victim.

* `Bonus_Damage`: \<Double\>
  * The bonus damage doing backstab will cause
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for shooter when doing backstab
  * See [the wiki for mechanics](General.md#mechanics)
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for victim when being backstabbed
  * See [the wiki for mechanics](General.md#mechanics)

#### `Critical_Hit`:

* `Chance`: \<Integer\>
  * The critical hit chance
  * This value has to be between `1` and `100`. Where `100` means that
    all the shots would be critical hits.
* `Bonus_Damage`: \<Double\>
  * The bonus damage doing critical hit will cause
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for shooter when doing critical hit
  * See [the wiki for mechanics](General.md#mechanics)
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for victim when being critical hit
  * See [the wiki for mechanics](General.md#mechanics)

#### `Kill`:

* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for shooter when killing victim
  * See [the wiki for mechanics](General.md#mechanics)
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for victim when being killed
  * See [the wiki for mechanics](General.md#mechanics)

#### `Dropoff`: \<String list\>
Defines how much projectile travel distance will increase or decrease damage.

* `travel distance`: \<Double\>
  * The amount of travel required to increase or decrease with given `damage amount`.
  * If projectile has travelled `9` blocks and this list only has `10~-3`, then
    damage won't be decreased by `3` since that `10` blocks wasn't reached.
    Only one element from this list is used and its chosen based on nearest
    `travel distance` and current projectile travel distance.
* `damage amount`: \<Double\>
  * The amount to increase (`+`) or decrease (`-`) damage