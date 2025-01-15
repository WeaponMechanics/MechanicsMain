package me.deecaad.weaponmechanics;

import com.google.common.base.Preconditions;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DefaultExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.DistanceExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.ExplosionExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.OptimizedExposure;
import me.deecaad.weaponmechanics.weapon.explode.exposures.VoidExposure;
import me.deecaad.weaponmechanics.weapon.explode.shapes.CubeExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.DefaultExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ExplosionShape;
import me.deecaad.weaponmechanics.weapon.explode.shapes.ParabolaExplosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.SphereExplosion;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A utility class of all {@link Registry registries} created by WeaponMechanics.
 */
public final class WeaponMechanicsRegistry {

    /**
     * All exposures that can be used in the
     * {@link me.deecaad.weaponmechanics.weapon.explode.Explosion}'s serializer.
     */
    public static final SimpleWritableRegistry<ExplosionExposure> EXPLOSION_EXPOSURES = new SimpleWritableRegistry<>(
        List.of(new DefaultExposure(), new DistanceExposure(), new OptimizedExposure(), new VoidExposure()));

    /**
     * All explosion shapes that can be used in the
     * {@link me.deecaad.weaponmechanics.weapon.explode.Explosion}'s serializer.
     */
    public static final SimpleWritableRegistry<ExplosionShape> EXPLOSION_SHAPES = new SimpleWritableRegistry<>(
        List.of(new CubeExplosion(), new DefaultExplosion(), new ParabolaExplosion(), new SphereExplosion()));

    /**
     * A simple registry implementation backed by a hashmap. This registry is writable.
     *
     * @param <T> The type of elements in the registry.
     */
    public static final class SimpleWritableRegistry<T extends Keyed> implements Registry<T> {

        private final @NotNull Map<NamespacedKey, T> map;

        public SimpleWritableRegistry(@NotNull Iterable<T> iterable) {
            this.map = new HashMap<>();
            iterable.forEach(t -> map.put(t.getKey(), t));
        }

        @Override
        public @Nullable T get(@NotNull NamespacedKey key) {
            return map.get(key);
        }

        public void put(@NotNull T t) {
            map.put(t.getKey(), t);
        }

        @Override
        public @NotNull T getOrThrow(@NotNull NamespacedKey key) {
            T t = get(key);
            Preconditions.checkArgument(t != null, "No registry entry found for key %s.", key);
            return t;
        }

        @Override
        public @NotNull Stream<T> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return map.values().iterator();
        }
    }
}
