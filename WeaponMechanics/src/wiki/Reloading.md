```yaml
Reload:
  Trigger: <trigger serializer>
  Magazine_Size: <amount>
  Ammo_Per_Reload: <amount>
  Unload_Ammo_On_Reload: <true/false>
  Reload_Duration: <ticks>
  Start_Mechanics: <MechanicsSerializer>
  Finish_Mechanics: <MechanicsSerializer>
  Ammo: <ammo serializer>
```

#### `Trigger`: \<Trigger\>
This is the trigger used to actually start reloading the gun. See [the wiki for trigger](General.md#trigger)
Reloading is also triggered automatically if weapon runs out of ammo.

#### `Magazine_Size`: \<Integer\>
Defines how many bullets can fit into one magazine.

#### `Ammo_Per_Reload`: \<Integer\>
Defines how many bullets are reloaded per one reload. This is useful for
weapons like shotguns where you don't want to allow one reload to fill whole magazine.
For example if `Ammo_Per_Reload: 2`, reloading will loop over and over until reaching `Magazine_Size`
or when reloading is cancelled by shooting for example.

#### `Unload_Ammo_On_Reload`: \<Boolean\>
Defines if ammo is unloaded on reload. This basically means that all remaining ammo in weapon
when reload is triggered is taken off from weapon and if `Ammo` is defined they're given back to player.

#### `Reload_Duration`: \<Integer\>
Defines how long the actual reload will last in ticks before finishing. When using firearm actions
the actual reload finish time also includes open and close times.

#### `Start_Mechanics`: \<Mechanics\>
These mechanics are run when reloading starts.
Delayed sound plays will be cancelled if reloading is cancelled.
See [the wiki for mechanics](General.md#mechanics).

#### `Finish_Mechanics`: \<Mechanics\>
These mechanics are run when reloading is finished.
See [the wiki for mechanics](General.md#mechanics).

## Ammo
`Ammo` is used to make weapons require something to be able to reload.
If `Ammo` isn't used weapons basically have infinite ammo, even though
they can still need reloading.  

Only one ammo type may be used at time.

```yaml
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
```

#### `Out_Of_Ammo`: \<Mechanics\>
These mechanics are run when there is attempt to reload without ammo. 
See [the wiki for mechanics](General.md#mechanics).

#### `Use_Item_As_Ammo`:

* `Ammo_Name`: \<String\>
  * The name of ammo items and magazine items
    * This is only hidden name of items, not visible to players
  * Many weapons can have same `Ammo_Name` to be able to use same ammo items
* `Magazine`
  * Magazines can be filled through inventory. Basically when using magazines
    you'll need both magazine and ammo item. Filling magazines is as easy as opening
    inventory, then taking ammo item to cursor and then clicking magazine item with that ammo item.
  * `Item`: \<Item\>
    * Defines the magazine item
    * This uses the [item serializer](General.md#item-serializer)
  * `Not_Able_To_Fill`:
    * `Not_Same_Ammo_Name`: \<Mechanics\>
      * These mechanics are run when trying to fill magazine with wrong ammo
      * See [the wiki for mechanics](General.md#mechanics)
    * `Magazine_Already_Full`: \<Mechanics\>
      * These mechanics are run when trying to fill already full magazine
      * See [the wiki for mechanics](General.md#mechanics)
  * `Magazine_Filled`: \<Mechanics\>
    * These mechanics are run when magazine is filled successfully
    * See [the wiki for mechanics](General.md#mechanics)
* `Ammo`: \<Item\>
  * Defines the single ammo item
  * This uses the [item serializer](General.md#item-serializer)
* `Ammo_Converter_Check`:
  * In order for a normal item to become a "ammo/magazine", the plugin needs to check if the item should become a ammo or magazine. This
    is the list of checks the plugin goes through to determine if it should convert an item into a ammo or magazine. This is
    especially useful if you want players to be able to get ammo/magazine by vanilla means although using
    recipes is more recommended approach.
  * `Type`: \<Boolean\>
    * Checks if the material of the item is the same
    * Material Examples: `stone`, `stick`, `emerald`
  * `Name`: \<Boolean\>
    * Checks if the name of the item is the same
    * Note: If your ammo name has a color (e.g. `"&eMyAmmo"`), then the item must have that color.
  * `Lore`: \<Boolean\>
    * Checks if the lore of the item is the same.
    * Enable this if you want *COMPLETE CONTROL* over who gets ammo (normal players cannot give lore to items)
  * `Enchants`: \<Boolean\>
    * Checks if the enchantments of the item is the same.
    * Note: Checks both the enchantments *AND* the levels

#### `Use_Exp_As_Ammo`:
Using this you can use exp as weapon ammo. Basically this takes exp from player's
total exp in order to fill weapon's magazine.

* `Exp_Cost`: \<Integer\>
  * The amount of exp one ammo costs

#### `Use_Money_As_Ammo`:
Using this you can use economy as weapon ammo. Basically this takes money from player's
bank in order to fill weapon's magazine.  

This currently only supports `Vault`. Other economy plugin supports can be added by request.

* `Money_Cost`: \<Double\>
  * The amount of money one ammo costs