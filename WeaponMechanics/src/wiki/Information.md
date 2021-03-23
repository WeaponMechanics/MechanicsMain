```yaml
Info:
  Weapon_Item: <item serializer>
  Weapon_Info_Display: <weapon info display serializer>
  Dual_Wield: <dual wield serializer>
  Weapon_Converter_Check: <weapon converter serializer>
  Weapon_Get_Actions: <MechanicsSerializer>
  Cancel:
    Block_Interactions: <true/false>
    Item_Interactions: <true/false>
    Drop_Item: <true/false>
    Swap_Hands: <true/false>
    Arm_Swing_Animation: <true/false>
```

#### `Weapon_Item`:

Defines the item used as weapon.
This uses the [item serializer](General.md#item-serializer)

#### `Weapon_Info_Display`:
```yaml
Weapon_Info_Display:
  <MessageMechanicsSerializer>
  Update_Item_Name: <true/false>
  Show_Ammo_In:
    Boss_Bar_Progress: <true/false>
    Exp_Level: <true/false>
    Exp_Progress: <true/false>
```

#### `MessageMechanicsSerializer`:
You can freely use [MessageMechanics serializer](General.md#message) here.
It is recommended to take advantage of [placeholders](Placeholders.md).  

For example, you can use message like this:
```yaml
Weapon_Info_Display:
  Action_Bar:
    Message: "%weapon-title% %selective_fire_state%%firearm-state% <%ammo-left%>%reload%"
    Time: 50
```

* `Update_Item_Name`: \<Boolean\>
  * Whether weapon item name should be updated everytime weapon info is displayed
  * It is better option to show ammo for example in action bar using `MessageMechanics`.
* `Show_Ammo_In`:
  * Defines where ammo should also be shown when displaying weapon info
  * `Boss_Bar_Progress`: \<Boolean\>
    * Whether ammo should be shown as boss bar progress
    * Requires valid `Boss_Bar` configuration in `MessageMechanics` at this `Weapon_Info_Display`.
  * `Exp_Level`: \<Boolean\>
    * Whether ammo should be shown as exp levels
    * This only fakes exp level for players and its reset back to normal after `40` ticks
  * `Exp_Progress`: \<Boolean\>
    * Whether ammo should be shown as exp progress
    * This only fakes exp progress for players and its reset back to normal after `40` ticks

#### `Dual_Wield`:
Dual wielding with weapons is always disabled by default. If you want
to be able to shoot with both hands at same time, you'll have to add dual wielding
for both weapons while allowing them to dual wield with each other.

Only other hand may reload weapon at time and only other weapon may apply zooming.
Both weapons can still shoot simultaneously.

```yaml
Dual_Wield:
  Whitelist: <true/false>
  Weapons:
  - <weapon title>
  - <etc.>
  Mechanics_On_Deny: <MechanicsSerializer>
```

* `Whitelist`: \<Boolean\>
  * Whether the use `Weapons` as whitelist or blacklist
  * `True` = only weapons listed in `Weapons` can be used
* `Weapons`: \<String list\>
  * List of valid weapons depending on `Whitelist`
* `Mechanics_On_Deny`: \<Mechanics\>
  * These mechanics will be ran when dual wielding is denied
  * See [the wiki for mechanics](General.md#mechanics)

#### `Weapon_Converter_Check`:
In order for a normal item to become a "weapon", the plugin needs to check if the item should become a weapon. This
is the list of checks the plugin goes through to determine if it should convert an item into a weapon. This is
especially useful if you want players to be able to get weapons by vanilla means although using
recipes is more recommended approach.
```yaml
Weapon_Converter_Check:
  Type: <true/false>
  Name: <true/false>
  Lore: <true/false>
  Enchants: <true/false>
```

* `Type`: \<Boolean\>
  * Checks if the material of the item is the same
  * Material Examples: `stone`, `stick`, `emerald`
* `Name`: \<Boolean\>
  * Checks if the name of the item is the same
  * Note: If your weapon name has a color (e.g. `"&eAK-47"`), then the item must have that color.
* `Lore`: \<Boolean\>
  * Checks if the lore of the item is the same. 
  * Enable this if you want *COMPLETE CONTROL* over who gets weapons (normal players cannot give lore to items)
* `Enchants`: \<Boolean\>
  * Checks if the enchantments of the item is the same.
  * Note: Checks both the enchantments *AND* the levels

#### `Weapon_Get_Mechanics`:

These mechanics are ran when new weapon is given for player.  
See [the wiki for mechanics](General.md#mechanics)

#### `Cancel`:
While holding the weapon with these features enabled, it cancels them. For example, if you set
`Drop_Item: true`, if somebody tries to drop the weapon, it won't drop. It is my personal
recommendation to use `Drop_Item: true`, `Swap_Hands: true`, and `Arm_Swing_Animation: true`.

* `Block_Interactions`: \<Boolean\>
  * Cancels interactions with blocks
  * So this will cancel guns opening doors, chests, etc.
  * Cancelling this may cause weapon model to pop up and down more often
* `Item_Interactions`: \<Boolean\>
  * Cancels item interactions
  * So this will cancel fishing rods, bows, foods, potions if that is your gun item
  * Cancelling this may cause weapon model to pop up and down more often
* `Drop_Item`: \<Boolean\>
  * Cancels people from dropping their gun
* `Swap_Hands`: \<Boolean\>
  * Cancels people swapping weapons from main hand to offhand and vice versa
* `Arm_Swing_Animation`: \<Boolean\>
  * Cancels the arm swing animation