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
Whether to give entity night vision effect during scoping. Night vision
is given using packets so server doesn't never really know that player has night vision
enabled, this way it isn't possible for effect to stay even if server happens to crash.

#### `Zoom_Amount`: \<Integer\>
Defines the zoom amount. The value has to be between `1` and `32`.
Where `1` is lowest and `32` is the highest zoom.

#### `Mechanics`: \<Mechanics\>
These mechanics are run when entity zooms in.
See [the wiki for mechanics](General.md#mechanics)

#### `Zoom_Off`:

* `Trigger`: \<Trigger\>
  * Defines the zoom off trigger
  * It is optional to use this trigger
    * If this isn't defined then zoom in trigger is used instead
    * If this is defined and if this trigger is valid, entity zooms off
  * See [the wiki for trigger](General.md#trigger)
* `Mechanics`: \<Mechanics\>
  * These mechanics are run when entity zooms out
  * See [the wiki for mechanics](General.md#mechanics)

#### `Zoom_Stacking`:
Zoom stacking is triggered using zoom in trigger. Stacking will loop until reaching maximum
stacks then zooms off. When using zoom stacking scope can be zoomed off prematurely using zoom off trigger.

* `Maximum_Stacks`: \<Integer\>
  * Defines how many times zoom can stack
* `Increase_Zoom_Per_Stack`: \<Integer\>
  * Defines the amount of zoom levels increased per zoom stack
  * Keep in mind that zoom levels aren't allowed to go below `1` or above `32`.
    * Meaning make sure that `0` < `maximum stacks * increase zoom per stack + zoom amount` < `33` is valid
* `Mechanics`: \<Mechanics\>
  * These mechanics are run when entity stacks zoom
  * See [the wiki for mechanics](General.md#mechanics)