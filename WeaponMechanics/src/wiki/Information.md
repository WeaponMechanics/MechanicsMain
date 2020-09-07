```yaml
  Info:
    Weapon_Item: <Item Serializer>
    Dual_Wield:
      Whitelist: <true/false>
      Weapons:
      - <weapon title>
      - <etc.>
      Denied_Message: <Message Serializer>
    Weapon_Converter_Check:
      Type: <true/false>
      Name: <true/false>
      Lore: <true/false>
      Enchants: <true/false>
    Weapon_Get_Mechanics:
      - <Mechanic> <@Targeter>
    Cancel:
      Block_Interactions: <true/false>
      Item_Interactions: <true/false>
      Drop_Item: <true/false>
      Swap_Hands: <true/false>
      Arm_Swing_Animation: <true/false>
```
#### `Weapon_Item`:


#### `Dual_Wield`:
info
  
  * `Whitelist`: 
  * `Weapons`: 
  * `Denied_Message`:

#### `Weapon_Converter_Check`:
In order for a normal item to become a "weapon", the plugin needs to check if the item should become a weapon. This
is the list of checks the plugin goes through to determine if it should convert an item into a weapon. This is
especially useful if you want player's to be able to get weapons by vanilla means.
  
  * `Type`: 
    * Checks if the material of the item is the same
    * Material Examples: `stone`, `stick`, `emerald`
  * `Name`:
    * Checks if the name of the item is the same
    * Note: If your weapon name has a color (e.g. `"&eAK-47"`), then the item must have that color.
  * `Lore`:
    * Checks if the lore of the item is the same. 
    * Enable this if you want *COMPLETE CONTROL* over who gets weapons (normal players cannot give lore to items)
  * `Enchants`:
    * Checks if the enchantments of the item is the same.
    * Note: Checks both the enchantments *AND* the levels

#### `Weapon_Get_Mechanics`:

#### `Cancel`:
While holding the weapon with these features enabled, it cancels them. For example, if you set
`Drop_Item: true`, if somebody tries to drop the weapon, it won't drop. It is my personal
recommendation to use `Drop_Item: true`, `Swap_Hands: true`, and `Arm_Swing_Animation: true`.

  * `Block_Interactions`:
    * Cancels interactions with blocks
    * So this will cancel guns opening doors, chests, etc.
  * `Item_Interactions`:
    * Cancels item interactions
    * So this will cancel fishing rods, bows, foods, potions if that is your gun item
  * `Drop_Item`:
    * Cancels people from dropping their gun
  * `Swap_Hands`:
    * Cancels people swapping weapons from main hand to offhand and vice versa
  * `Arm_Swing_Animation`:
    * Cancels the arm swing animation introduced in 1.15