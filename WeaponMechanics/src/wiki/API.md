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
All events are subclasses of the [WeaponEvent](#docs), so all events inherit the following methods:
* `String getWeaponTitle()` - Returns the nonnull [weapon title] of the weapon used
* `ItemStack getWeaponStack()` - Returns the nonnull item weapon used to shoot
  * Never try to get the weapon item yourself, use this method.
* `LivingEntity getShooter()` - Returns the nonnull living entity that caused the event
* `EntityType getShooterType()` - Returns the result of `getShooter().getType()`

Some events are split into 2 parts, the *pre-event* and the *event*. The *pre-event*
is fired before most calculations happen (Excluding [protection checks]). By cancelling
a *pre-event*, WeaponMechanics skips doing to calculations necessary to complete the task (saving cpu power).
*Events* generally allow more control over what is happening (e.g. Changing the amount of damage a weapon does).

Some events can be disabled in configuration to save resources, like the [ProjectileMoveEvent](#ProjectileMoveEvent).
By default, every event is enabled. If your plugin requires the usage of one of these events, I suggest either having
your plugin set that config value manually, or add something like this into your plugin:
```java
public class MyWeaponMechanicsAddon extends JavaPlugin {
    @Override
    public void onEnable() {
        
        // Change this to whichever event you use
        String event = "Projectile_Move_Event";

        Configuration config = WeaponMechanics.getBasicConfigurations();
        if (config.getBoolean("Disable_Events." + event)) {
            getLogger().log(Level.SEVERE, "Misconfiguration in WeaponMechanic's config.yml");
            getLogger().log(Level.SEVERE, "In order to fix, set \"Disable_Events.Projectile_Move_Event: false\"");
            getLogger().log(Level.SEVERE, "Disabling plugin to avoid error");
            // Missing implementation
        }
    }
}
```

##### ProjectileEndEvent
This event is called whenever a project ends. This may be because it hit a block, hit an
entity, or timed out (There could be other reasons for this event to occur).
 
 * `ICustomProjectile getProjectile()` - Gets the custom projectile involved with this event
 * `Location getLocation()` - Gets the current location of the projectile
 * `Location getLastLocation()` - Gets the previous location the projectile

##### ProjectileExplodeEvent
This event is called right before a projectile explodes.

* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
  * It's probably a bad idea to cancel this, but you can
* `ICustomProjectile getProjectile()` - Gets the custom projectile involved with this event
* `Location getLocation()` - Gets the current location of the projectile
* `Location getLastLocation()` - Gets the previous location the projectile
* `Explosion getExplosion()` - Gets the explosion that will be used

##### ProjectileHitBlockEvent
This event is called whenever a projectile hits a block. This event is useful if you
want to bounce the projectile off of the block or apply changes to the block (Maybe
your gun is a paintball gun). This event is a subclass of the `ProjectileMoveEvent`,
but is still called even when `Disabled_Events.Projectile_Move_Event: true`

* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `ICustomProjectile getProjectile()` - Gets the custom projectile involved with this event
* `Location getLocation()` - Gets the current location of the projectile
* `Location getLastLocation()` - Gets the previous location the projectile
* `Block getHitBlock()` - Gets the block that was hit
* `BlockFace getHitFace()` - Gets the hit block face

##### ProjectileHitEntityEvent
This event is called whenever a projectile hits an entity. Not to be confused with
`ProjectileDamageEntityEvent` (which is called later). Any changes to the projectile should
be made here, changes to the entity should be done in the `ProjectileDamageEntityEvent`.

* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `ICustomProjectile getProjectile()` - Gets the custom projectile involved with this event
* `Location getLocation()` - Gets the current location of the projectile
* `Location getLastLocation()` - Gets the previous location the projectile
* `Location getHitLocation()` - Gets the exact location the projectile hits the entity at
* `LivingEntity getEntity()` - Gets the entity that was hit
* `EntityType getEntityType()` - Gets the type of the entity that was hit
* `DamagePoint getPoint()` - Gets the nullable body part the projectile hit
* `void setPoint(DamagePoint)` - Sets the nullable point the projectile hit
* `boolean isBackStab()` - Returns true if the bullet hit the entity's back
* `void setBackStab(boolean)` - Sets the back stab state

##### ProjectileMoveEvent
This event is called after a projectile has moved. Be careful with this
event, it is called once every tick per projectile. If your plugin relies on
this event, make sure to tell everybody who uses your plugin to make sure that
in `server > plugins > WeaponMechanics > config.yml`, `Disabled_Events.Projectile_Move_Event`
is set to false (It is false by default, so there should be no worries).

* `ICustomProjectile getProjectile()` - Gets the custom projectile involved with this event
* `Location getLocation()` - Gets the current location of the projectile
* `Location getLastLocation()` - Gets the previous location the projectile

##### WeaponDamageEntityEvent
This event is called before damage is applied to the effected entity.

* `LivingEntity getVictim()` - Returns the nonnull living entity that is getting damaged
* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `ICustomProjectile getProjectile()` - Gets the projectile that hit the victim
* `double getBaseDamage()` - Gets the base damage
* `void setBaseDamage(double)` - Sets the base damage
* `double getFinalDamage()` - Gets the final damage dealt to the entity
  * Using this method will force WM to calculate final damage (Unless
  another plugin already forced it)
* `void setFinalDamage(double)` - Sets the final damage
* `boolean isBackstab()` - Returns true if this is a backstab
* `void setBackstab(boolean)` - Sets if this is a backstab or not
* `boolean isCritical()` - Returns true if this is a critical hit
* `void setCritical(boolean)` - Sets if this is a critical hit or not
* `DamagePoint getDamagePoint()` - Gets the point where the bullet hit the entity
* `void setDamagePoint(DamagePoint)` - Sets the point where the bullet hit the entity
  * If you set this to null, no damage based on points will be applied

##### WeaponHeldEvent
This event is called whenever an entity "holds" a weapon. A weapon can be held
if it is put in the mainhand/offhand through an open `PlayerInventory`, if a player
swaps hands, if a weapon is picked up into the main hand, or if they swap hotbar slots to a weapon.

* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `int getSlot()` - Gets the inventory slot the weapon is in

##### WeaponKillEntityEvent
This event is called after a `WeaponDamageEntityEvent` is fired and the damage
kills the entity.

* `LivingEntity getVictim()` - Returns the nonnull living entity that is getting damaged
* `WeaponDamageEntityEvent getDamageEvent()` - Gets the nonnull event from the damage that killed the entity

##### WeaponPickupEvent
This event is called when a weapon is added to any slot in a player's inventory either by
picking up the item from the ground, or by getting the item from command.

* `Item getItem()` - Gets the nullable item entity picked up from the ground
  * If the weapon was picked up off of the ground, this will not be null

##### WeaponPreReloadEvent
This event is called right after the trigger check for reloading is successful.

* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event

##### WeaponPreShootEvent
This event is called right after an entity attempts to shoot. This
is a *pre-event* to [WeaponShootEvent]()

* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event

##### WeaponReloadCancelEvent
This event is called whenever a player cancels a reload. A player can cancel
a reload by dropping, shooting, or switching their weapon. This event can also
occur if the entity reloading is killed.


* `int getReloadTime()` - Returns the time in ticks the reload should have lasted
* `int getElapsedTime()` - Returns the amount of time that actually passed
  * `event.getElapsedTime() < event.getReloadTime()` is always true
* `double getElapsedPercentage()` - Returns the percentage of "how close" the user was to completing the reload
  * This will always be a decimal [0, 1)
  
##### WeaponReloadCompleteEvent
This event is called right after a reload is completed. For weapons 
using `Reload_Bullets_Individually: true`, this event will occur 
multiple times.

* `int getReloadTime()` - Gets the amount of time, in ticks, it took for the reload to complete
* `int getReloadAmount()` - Gets the amount of ammunition loaded into the gun
* `int getMagazineSize()` - Gets the maximum size of the magazine used in the reload
* `WeaponReloadEvent getReloadEvent()` - Gets the initial event when the shooter started to reload
* `List<WeaponReloadCancelEvent> getCancelEvents()` - Gets all attempted cancel events involved
  * This list will be null if there were no attempts to cancel the event
  * All of these events were cancelled, which is why the WeaponReloadCompleteEvent was still called

##### WeaponReloadEvent
This event is called right before a reload begins. Remember that reloads can occur
if somebody attempts to shoot with a gun but there is no ammo in it, OR if the player
triggers a reload directly. For weapons using `Reload_Bullets_Individually: true`, this
event will occur multiple times.

* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `int getReloadTime()` - Gets the time in ticks the reload will occur for
* `void setReloadTime()` - Sets the time in ticks the reload will occur for

##### WeaponScopeEvent
This event is called right before an entity zooms in.

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

##### WeaponShootEvent
This event is called right after the projectile launches. This event
is called for each projectile (So for shotguns, this can be called multiple
times for every shot).

* `ICustomProjectile getProjectile()` - Gets the nonnull fired projectile
* `void setProjectile(ICustomProjectile projectile)` - Sets the nonnull fired projectile
  * This causes WeaponMechanics to forget about the previous projectile and
  instead fire this projectile
  * You can set your own implementations of `ICustomProjectile`

##### WeaponUnloadEvent
This event is called right before a weapon unloads it's ammo, generally right before a
reload.

* `boolean isCancelled()` - Returns true if this event has been cancelled
* `void setCancelled(boolean)` - Sets the cancellation state of this event
* `int getUnloadAmount()` - Gets the amount of ammo being unloaded
* `void setUnloadAmount(int)` - Sets the amount of ammo being unloaded
  * This ignores the capacity of the weapon
  * This only effects how much ammo is given back to the player
* `int getMagazineSize()` - Gets the amount of ammo that can be stored in the weapon
* `void setMagazineSize(int)` - Sets the amount of ammo that can be stored in the weapon

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