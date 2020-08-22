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
todo

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
It's important to make sure your class 


Hey DeeCaaD, I need your help for the rest of this :p

Config is serialized onEnable in a task, walk through the logic of registering a
serializer then

## Overriding the WeaponHandler
This will be another one for you deecaad, you know this one inside and out