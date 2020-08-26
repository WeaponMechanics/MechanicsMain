## Developer API
This page is for developers. If you are not a Java developer, [click here]() to go back home.

  * [WeaponMechanicsAPI](#WeaponMechanicsAPI)
  * [Weapon Events](#Events)
  * [Config Serialization](#Config Serialization)
  * [Overriding the WeaponHandler](#Overriding the WeaponHandler)

Javadocs: <insert link here!>

## WeaponMechanicsAPI
todo

## Events
Most events are split into 2 parts, the *pre-event* and the *actual event*. The *pre-event*
is fired before most calculations happen (Excluding [protection checks]). This most useful
for cancelling the event, as it allows you to cancel things before WM does more heavy
calculations. The *actual event* can (generally) still be cancelled, but there is more benefit to 
trying to cancel a *pre-event*. *actual events* generally allow more control over what
is happening (e.g. Changing the amount of damage a weapon does)

##### WeaponPreShootEvent
This event is called right after an entity attempts to shoot. This
is a *pre-event* to [WeaponShootEvent]()

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to shoot
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event

##### WeaponShootEvent
This event is called right after the projectile launches. This event
is called for each projectile (So for shotguns, this can be called multiple
times for every shot).

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to shoot
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `ICustomProjectile getProjectile()` - Gets the nonnull fired projectile

##### WeaponDamageEntityEvent
This event is called before damage is applied to the effected entity.

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to damage
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
* `LivingEntity getVictim()` - Returns the nonnull living entity that is getting damaged
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `ICustomProjectile getProjectile()` - Gets the projectile that hit the victim
* `double getBaseDamage()` - Gets the base damage
* `void setBaseDamage(double)` - Sets the base damage
* `double getFinalDamage()` - Gets the final damage dealt to the entity
  * Using this method will force WM to calculate final damage (Unless
  another plugin already forced it)
* `void setFinalDamage(double)` - Sets the final damage
  * If final damage is set to a number greater than or equal to 0, then
  WM will skip normal damage calculations.
* `boolean isBackstab()` - Returns true if this is a backstab
* `void setBackstab(boolean)` - Sets if this is a backstab or not
* `DamagePoint getDamagePoint()` - Gets the point where the bullet hit the entity
* `void setDamagePoint(DamagePoint)` - Sets the point where the bullet hit the entity
  * If you set this to null, no damage based on points will be applied

##### WeaponKillEntityEvent
This event is called after a `WeaponDamageEntityEvent` is fired and the damage
kills the entity.

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to damage
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
* `LivingEntity getVictim()` - Returns the nonnull living entity that is getting damaged
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `WeaponDamageEntityEvent getDamageEvent()` - Gets the nonnull event from the damage that killed the entity

##### WeaponReloadEvent
This event is called right before a reload begins. Remember that reloads can occur
if somebody attempts to shoot with a gun but there is no ammo in it, OR if the player
triggers a reload directly. For weapons using `Reload_Bullets_Individually: true`, this
event will occur multiple times.

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to reload
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `int getReloadTime()` - Gets the time in ticks the reload will occur for
* `void setReloadTime()` - Sets the time in ticks the reload will occur for

##### WeaponReloadCancelEvent
This event is called whenever a player cancels a reload. A player can cancel
a reload by dropping, shooting, or switching their weapon. This event can also
occur if the entity reloading is killed.

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to shoot
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `int getReloadTime()` - Returns the time in ticks the reload should have lasted
* `int getElapsedTime()` - Returns the amount of time that actually passed
  * `event.getElapsedTime() < event.getReloadTime()` is always true
  
##### WeaponReloadCompleteEvent
This event is called right after a reload is completed. For weapons 
using `Reload_Bullets_Individually: true`, this event will occur 
multiple times.

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to shoot
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `int getReloadTime()` - Gets the time in ticks the reload will occur for

##### WeaponPreScopeEvent
This event is called right after an entity attempts to zoom in. This
is a *pre-event* to [WeaponScopeEvent]().

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to scope
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
  * It is not safe to assume this entity is a player
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event

##### WeaponScopeEvent
This event is called right before an entity actually zooms in.

* `ItemStack getItemStack()` - Returns the nonnull item weapon used to scope
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
  * It is not safe to assume this entity is a player
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `ScopeType getScopeType()` - Returns the scope type (`IN`, `STACK`, `OUT`)
* `int getZoomAmount()` - Returns the [zoom amount]
  * If this returns 0, it likely means this is zooming out
  * Make sure to check `ScopeType`
* `void setZoomAmount(int)` - Sets the [zoom amount] for this zoom
  * Make sure to set in range [1, 32]
* `int getStackAmount()` - Returns the [stacky zoom] amount
  * If this is 0, the feature is disabled or it is first stack
  
##### ProjectileMoveEvent
This event is called right before a projectile moves. Be careful with this
event, it is called once every tick per projectile. If your plugin relies on
this event, make sure to tell everybody who uses your plugin to make sure that
in `server > plugins > WeaponMechanics > config.yml`, `Disable_Projectile_Move_Event`
is set to false (It is false by default, so there should be no worries).

##### ProjectileHitEntityEvent
This event is called whenever a projectile hits an entity. Not to be confused with
`ProjectileDamageEntityEvent` (which is called later). This event event is useful if
you want to, for example, bounce the projectile. Any changes to the projectile should
be made here, changes to the entity should be done in the `ProjectileDamageEntityEvent`.

##### ProjectileHitBlockEvent
This event is called whenever a projectile hits a block. This event is useful if you
want to bounce the projectile off of the block or apply changes to the block (Maybe
your gun is a paintball gun).

## Config Serialization
The config files are all loaded into 1 `me.deecaad.core.file.Configuration`.
It's easy to pull values from config, but it's important to remember that not
every line is saved, and some types are different than you may expect. Consider
the following:
```yaml
My_Weapon:
  Explosion:
    Explosion_Exposure: Default
    Explosion_Shape: Default
```
The object at key `My_Weapon.Explosion` is an instance of 
`me.deecaad.weaponmechanics.weapon.explode.Explosion`. All keys
within serializers are not loaded into config, as they have already
been serialized into that `Explosion`. This means 
`Configuration#getString("My_Weapon.Explosion.Explosion_Shape")` will actually
return `null` instead of `Default`.

The same thing applies to all serializers.

If you want to replace a serializer, say you want to replace the explosion serializer,
you can do something like this:
```java
import me.deecaad.core.file.Serializer;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;

public class CustomExplosion extends Explosion implements Serializer<CustomExplosion> {
    // Missing implementation 
}
```
It's important to make sure your `Serializer` serializes an `Explosion` or a sub-class of
`Explosion`. This will ensure that WeaponMechanics can still use the `Explosion` as you
would expect. This applies to all serializers.


Hey DeeCaaD, I need your help for the rest of this :p

Config is serialized onEnable in a task, walk through the logic of registering a
serializer then

## Overriding the WeaponHandler
This will be another one for you deecaad, you know this one inside and out

[weapon title]: #
[zoom amount]: #
[stacky zoom]: #
[protection checks]: #