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
Defines how weapon's firearm actions will behave.

Available firearm action types and how they work
* `REVOLVER`
  * `Shoot`: Bang
  * `Reload`: Open, then reload timer, then close
* `PUMP`
  * `Shoot`: Bang, then open, then close
  * `Reload`: Reload timer, then open, then close
* `LEVER`
  * `Shoot`: Bang, then open, (bullet goes in), then close
  * `Reload`: Open, then reload timer, then close

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
  * The time in ticks it takes to open weapon
* `Mechanics`: \<Mechanics\>
  * See [the wiki for mechanics](General.md#mechanics)

#### `Close`:

* `Time`: \<Integer\>
  * The time in ticks it takes to close weapon
* `Mechanics`: \<Mechanics\>
  * See [the wiki for mechanics](General.md#mechanics)