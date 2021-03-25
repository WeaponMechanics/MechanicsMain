```yaml
Scope:
  Trigger: <trigger serializer>
  Night_Vision: <true/false>
  Zoom_Amount: <1-32>
  Mechanics: <MechanicsSerializer>
  Zoom_Off:
    Trigger: <trigger serializer>
    Mechanics: <MechanicsSerializer>
  Zoom_Stacking:
    Maximum_Stacks: <maximum zoom stack amount>
    Increase_Zoom_Per_Stack: <amount>
    Mechanics: <MechanicsSerializer>
```

#### `Trigger`: \<Trigger\>
This is the trigger used to actually use scope. See [the wiki for trigger](General.md#trigger)

#### `Night_Vision`: \<Boolean\>
Whether to give entity night vision effect during scoping. The night vision
potion is given via packets.

#### `Zoom_Amount`: \<Integer\>
Defines the zoom amount. The value has to be between `1` and `32`.
Where `1` is lowest and `32` is the highest zoom.

#### `Mechanics`: \<Mechanics\>
These mechanics are run when entity zooms in.
See [the wiki for mechanics](General.md#mechanics)

#### `Zoom_Off`:

* `Trigger`: \<Trigger\>
  * If you want unscoping to use a different trigger, you can define that here.
  * For most use cases, you can just delete this line.
  * See [the wiki for trigger](General.md#trigger).
* `Mechanics`: \<Mechanics\>
  * These mechanics are run when entity zooms out.
  * See [the wiki for mechanics](General.md#mechanics).

#### `Zoom_Stacking`:
Zoom stacking allows you to create multiple "levels" to zoom to. After the stacking reaches
a maximum value, it will zoom out. Using the `Zoom_Off.Trigger`, you can exit zoom stacking
prematurely.

* `Maximum_Stacks`: \<Integer\>
  * Defines how many times zoom can stack.
* `Increase_Zoom_Per_Stack`: \<Integer\>
  * Defines the amount of zoom levels increased per zoom stack.
  * Keep in mind that zoom levels aren't allowed to go below `1` or above `32`.
    * Meaning make sure that `0 < maximum stacks * increase zoom per stack + zoom amount < 33` is valid.
* `Mechanics`: \<Mechanics\>
  * These mechanics are run when entity stacks zoom.
  * See [the wiki for mechanics](General.md#mechanics).