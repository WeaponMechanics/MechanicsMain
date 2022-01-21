package me.deecaad.weaponmechanics.mechanics.defaultmechanics;

import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.file.serializers.LocationAdjuster;
import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.mechanics.CastData;
import me.deecaad.weaponmechanics.mechanics.IMechanic;
import me.deecaad.weaponmechanics.mechanics.Mechanics;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.List;

public class FireworkMechanic implements IMechanic<FireworkMechanic> {

    private LocationAdjuster locationAdjuster;
    private ItemStack fireworkItem;

    /**
     * Empty constructor to be used as serializer
     */
    public FireworkMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(WeaponMechanics.getPlugin(), this);
    }

    public FireworkMechanic(LocationAdjuster locationAdjuster, ItemStack fireworkItem) {
        this.locationAdjuster = locationAdjuster;
        this.fireworkItem = fireworkItem;
    }

    @Override
    public void use(CastData castData) {
        Location location = locationAdjuster != null ? locationAdjuster.getNewLocation(castData.getCastLocation()) : castData.getCastLocation();

        List<Player> players = DistanceUtil.getPlayersInRange(location);
        if (players.isEmpty()) {
            return;
        }

        int flightTime = ((FireworkMeta) fireworkItem.getItemMeta()).getPower();
        FakeEntity fakeEntity = CompatibilityAPI.getCompatibility().getEntityCompatibility().generateFakeEntity(location, EntityType.FIREWORK, fireworkItem);
        if (flightTime > 1) fakeEntity.setMotion(0.001, 0.3, -0.001);
        fakeEntity.show();
        if (flightTime <= 0) {
            fakeEntity.playEffect(EntityEffect.FIREWORK_EXPLODE);
            fakeEntity.remove();
            return;
        }
        new BukkitRunnable() {
            public void run() {
                fakeEntity.playEffect(EntityEffect.FIREWORK_EXPLODE);
                fakeEntity.remove();
            }
        }.runTaskLater(WeaponMechanics.getPlugin(), flightTime);
    }

    @Override
    public String getKeyword() {
        return "Firework";
    }

    @Override
    @Nonnull
    public FireworkMechanic serialize(SerializeData data) throws SerializerException {
        ItemStack fireworkItem = data.of("Item").assertExists().serializeNonStandardSerializer(new ItemSerializer());

        if (!(fireworkItem.getItemMeta() instanceof FireworkMeta)) {
            throw data.exception(null, "Item Type should be a firework when using a Firework Mechanic");
        }

        LocationAdjuster locationAdjuster = data.of("Location_Adjuster").serialize(LocationAdjuster.class);
        return new FireworkMechanic(locationAdjuster, fireworkItem);
    }
}
