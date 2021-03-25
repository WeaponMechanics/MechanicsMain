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
A projectile's base damage is the amount of damage it would apply in "normal"
conditions. Many rates are applied to this base damage. See `config.yml` to
edit specific rates.

Note; `1` damage is equal to `0.5` hearts. Therefore `20` damage is equal to `10` hearts.

#### `Base_Explosion_Damage`: \<Double\>
Explosion damage is calculated based on exposure. A high exposure probably means there were
no blocks between the entity and the explosion, and/or the entity was very close to the origin
of the explosion. To change how an explosion's exposure behaves, see [explosion exposure](Explosion.md#explosion_exposure).

#### `Fire_Ticks`: \<Integer\>
The amount of time, in ticks, that the victim will be lit on fire.

Note; 20 ticks = 1 second.

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
These mechanics are run for the shooter when doing any kind of damage.
See [the wiki for mechanics](General.md#mechanics).

#### `Victim_Mechanics`: \<Mechanics\>
These mechanics are run for the victim when doing any kind of damage.
See [the wiki for mechanics](General.md#mechanics).

#### `Head`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting head will cause.
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for the shooter when damaging head.
  * See [the wiki for mechanics](General.md#mechanics).
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for the victim when being damaged to head.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Body`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting body will cause.
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for the shooter when damaging the body.
  * See [the wiki for mechanics](General.md#mechanics).
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for the victim when being damaged to the body.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Arms`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting arms will cause.
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for the shooter when damaging the arms.
  * See [the wiki for mechanics](General.md#mechanics).
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for the victim when being damaged to the arms.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Legs`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting legs will cause.
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for the shooter when damaging legs.
  * See [the wiki for mechanics](General.md#mechanics).
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for the victim when being damaged to the legs.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Feet`:

* `Bonus_Damage`: \<Double\>
  * The bonus damage hitting feet will cause.
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for the shooter when damaging feet.
  * See [the wiki for mechanics](General.md#mechanics).
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for the victim when being damaged to the feet.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Backstab`:
Damage is backstab when projectile comes from behind victim or
when explosion occurs behind victim.

* `Bonus_Damage`: \<Double\>
  * The bonus damage doing backstab will cause.
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for the shooter when doing backstab.
  * See [the wiki for mechanics](General.md#mechanics).
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for the victim when being backstabbed.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Critical_Hit`:

* `Chance`: \<Integer\>
  * The critical hit chance
  * This value has to be between `1` and `100`. Where `100` means that
    all the shots would be critical hits.
* `Bonus_Damage`: \<Double\>
  * The bonus damage doing critical hit will cause.
* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for the shooter when doing critical hit.
  * See [the wiki for mechanics](General.md#mechanics).
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for the victim when being critical hit.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Kill`:

* `Shooter_Mechanics`: \<Mechanics\>
  * These mechanics are run for the shooter when killing the victim.
  * See [the wiki for mechanics](General.md#mechanics).
* `Victim_Mechanics`: \<Mechanics\>
  * These mechanics are run for the victim when being killed.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Dropoff`: \<String list\>
Defines how much projectile travel distance will increase or decrease damage.

* `travel distance`: \<Double\>
  * The amount of travel required to increase or decrease with given `damage amount`.
  * If projectile has travelled `9` blocks and this list only has `10~-3`, then
    damage won't be decreased by `3` since that `10` blocks wasn't reached.
    Only one element from this list is used and its chosen based on nearest
    `travel distance` and current projectile travel distance.
* `damage amount`: \<Double\>
  * The amount to increase (`+`) or decrease (`-`) damage.