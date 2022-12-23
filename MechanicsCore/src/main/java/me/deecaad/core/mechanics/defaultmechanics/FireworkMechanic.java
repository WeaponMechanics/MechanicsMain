package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.file.serializers.LocationAdjuster;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.utils.DistanceUtil;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.List;

public class FireworkMechanic implements Mechanic<FireworkMechanic> {

    private LocationAdjuster locationAdjuster;
    private ItemStack fireworkItem;

    /**
     * Empty constructor to be used as serializer
     */
    public FireworkMechanic() {
        if (Mechanics.hasMechanic(getKeyword())) return;
        Mechanics.registerMechanic(MechanicsCore.getPlugin(), this);
    }

    public FireworkMechanic(LocationAdjuster locationAdjuster, ItemStack fireworkItem) {
        this.locationAdjuster = locationAdjuster;
        this.fireworkItem = fireworkItem;
    }

    @Override
    public void use(CastData castData) {
        Location location = locationAdjuster != null
                ? locationAdjuster.getNewLocation(castData.getCastLocation())
                : castData.getCastLocation();

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
        }.runTaskLater(MechanicsCore.getPlugin(), flightTime);
    }

    @Override
    public String getKeyword() {
        return "Firework";
    }

    @Override
    public boolean shouldSerialize(SerializeData data) {
        // Let Mechanics handle auto serializer stuff
        return false;
    }

    @Override
    @Nonnull
    public FireworkMechanic serialize(SerializeData data) throws SerializerException {
        ItemStack fireworkItem = data.of("Item").assertExists().serialize(new ItemSerializer());

        if (!(fireworkItem.getItemMeta() instanceof FireworkMeta)) {
            throw data.exception(null, "Item Type should be a firework when using a Firework Mechanic");
        }

        LocationAdjuster locationAdjuster = data.of("Location_Adjuster").serialize(LocationAdjuster.class);
        return new FireworkMechanic(locationAdjuster, fireworkItem);
    }
}
