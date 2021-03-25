```yaml
Firearm_Action:
  Type: <FirearmType>
  Firearm_Action_Frequency: <use every x amount of shots>
  Open:
    Time: <ticks>
    Mechanics: <MechanicsSerializer>
  Close:
    Time: <ticks>
    Mechanics: <MechanicsSerializer>
```

#### `Type`: \<FirearmType\>
Defines how weapon's firearm actions will behave. There are 3 different 
firearm types. These firearm actions can add a layer of depth, especially
if you have custom sounds for the firearm actions.

* `REVOLVER`:
  * For revolvers, the cylinder has to be pulled out so ammo can be loaded.
  * *When Shooting*:
    * Weapon Fires
  * *When Reloading*:
    * Weapon Opens (The rotating section pops out).
    * The reload-timer starts (Bullets are loaded into each slot).
    * Weapon Closes (The rotating section pops back in).
* `PUMP`:
  * For pump action weapons, a pump has to be cocked back and forth to eject and load a shell.
  * Great for pump action shotguns.
  * *When Shooting*:
    * Weapon Fires
    * Weapon Opens (Ejecting a shell)
    * Weapon Closes (Loading a shell)
  * *When Reloading*:
    * Weapon Reloads
    * Weapon Opens (If not already open, ejecting a shell)
    * Weapon Closes (Loading a shell)
* `LEVER`
  * For lever/bolt action weapons, a lever must be pulled then pushed to eject and load a round.
  * Different from `PUMP` because the weapon **MUST** be opened before reloading.
  * Great for sniper rifles.
  * *When Shooting*:
    * Weapon Fires
    * Weapon Opens (Ejecting a round)
    * Weapon Closes (Loading a round)
  * *When Reloading*:
    * Weapon Opens (Ejecting a round, and unlocking the mag)
    * Weapon Reloads
    * Weapon Closes (Loading a round, and blocking the mag)
  
When using firearm actions with different fire types
* `SINGLE`: After shot firearm actions are triggered
* `BURST`: After burst is finished firearm actions are triggered
* `AUTO`: After cancelling full auto firearm actions are triggered

#### `Firearm_Action_Frequency`: \<Integer\>
Defines how often firearm actions are used.

If current ammo amount after shooting is divisible by `Firearm_Action_Frequency` then
firearm actions are triggered. For example when `Firearm_Action_Frequency: 2`, then if weapon is shot
and ammo left in its magazine is `18`, firearm actions will be triggered. If ammo left was `9` in 
this case, firearm actions wouldn't be triggered.

#### `Open`:

* `Time`: \<Integer\>
  * The time, in ticks, it takes to open the weapon.
* `Mechanics`: \<Mechanics\>
  * Delayed sound plays will be cancelled if firearm actions are cancelled
  * See [the wiki for mechanics](General.md#mechanics)

#### `Close`:

* `Time`: \<Integer\>
  * The time, in ticks, it takes to close the weapon.
* `Mechanics`: \<Mechanics\>
  * Delayed sound plays will be cancelled if firearm actions are cancelled
  * See [the wiki for mechanics](General.md#mechanics)