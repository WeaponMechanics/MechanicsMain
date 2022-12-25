package me.deecaad.core.mechanics.defaultmechanics;

import me.deecaad.core.MechanicsCore;
import me.deecaad.core.compatibility.CompatibilityAPI;
import me.deecaad.core.compatibility.entity.FakeEntity;
import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.file.inline.Argument;
import me.deecaad.core.file.inline.ArgumentMap;
import me.deecaad.core.file.inline.InlineSerializer;
import me.deecaad.core.file.inline.types.*;
import me.deecaad.core.file.serializers.ItemSerializer;
import me.deecaad.core.file.serializers.LocationAdjuster;
import me.deecaad.core.mechanics.CastData;
import me.deecaad.core.mechanics.Mechanic;
import me.deecaad.core.mechanics.Mechanics;
import me.deecaad.core.mechanics.targeters.Targeter;
import me.deecaad.core.placeholder.PlaceholderAPI;
import me.deecaad.core.utils.DistanceUtil;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FireworkMechanic extends Mechanic {

    public static class FireworkData extends InlineSerializer<FireworkData> {

        public static final Argument EFFECT = new Argument("type", new EnumType<>(FireworkEffect.Type.class), FireworkEffect.Type.BURST);
        public static final Argument COLOR = new Argument("color", new ListType<>(new ColorType()));
        public static final Argument TRAIL = new Argument("trail", new BooleanType(), false);
        public static final Argument FLICKER = new Argument("flicker", new BooleanType(), false);
        public static final Argument FADE_COLOR = new Argument("fadeColor", new ListType<>(new ColorType()), null);

        private FireworkEffect effect;

        /**
         * Default constructor for serializer
         */
        public FireworkData() {
        }

        @SuppressWarnings("unchecked")
        public FireworkData(Map<Argument, Object> args) {
            FireworkEffect.Builder builder = FireworkEffect.builder();
            builder.with(((List<FireworkEffect.Type>) args.get(EFFECT)).get(0));
            builder.withColor((List<Color>) args.get(COLOR));
            builder.trail((boolean) args.get(TRAIL));
            builder.flicker((boolean) args.get(FLICKER));
            builder.withFade((List<Color>) args.get(FADE_COLOR));
            effect = builder.build();
        }

        public FireworkEffect getEffect() {
            return effect;
        }

        @Override
        public ArgumentMap args() {
            return new ArgumentMap(EFFECT, COLOR, TRAIL, FLICKER, FADE_COLOR);
        }

        @Override
        public String getKeyword() {
            return "Firework";
        }
    }

    public static final Argument EFFECTS = new Argument("effects", new ListType<>(new NestedType<>(FireworkData.class)));
    public static final Argument FLIGHT_TIME = new Argument("flightTime", new IntegerType(0), 1);
    public static final Argument VIEWERS = new Argument("viewers", new NestedType<>(Targeter.class), null);

    private final ItemStack fireworkItem;
    private final int flightTime; // THIS IS A COPY OF THE VALUE IN 'fireworkItem'
    private final Targeter viewers;

    @SuppressWarnings("unchecked")
    public FireworkMechanic(Map<Argument, Object> args) {
        super(args);

        this.fireworkItem = new ItemStack(ReflectionUtil.getMCVersion() >= 13 ? Material.FIREWORK_ROCKET : Material.valueOf("FIREWORK"));
        FireworkMeta meta = (FireworkMeta) fireworkItem.getItemMeta();
        meta.setPower(flightTime = (int) args.get(FLIGHT_TIME));
        meta.addEffects(((List<FireworkData>) args.get(EFFECTS)).stream().map(FireworkData::getEffect).collect(Collectors.toList()));
        fireworkItem.setItemMeta(meta);
        viewers = (Targeter) args.get(VIEWERS);
    }

    @Override
    public ArgumentMap args() {
        return super.args().addAll(EFFECTS, FLIGHT_TIME, VIEWERS);
    }

    @Override
    public void use0(CastData cast) {

        // Determine which players will see the firework
        List<Player> players;
        if (viewers == null)
            players = DistanceUtil.getPlayersInRange(cast.getTargetLocation());
        else {
            players = new LinkedList<>();
            for (CastData target : viewers.getTargets(cast)) {
                if (target.getTarget() instanceof Player player)
                    players.add(player);
            }
        }

        // No need to generate a fake entity if nobody is going to see it.
        if (players.isEmpty()) {
            return;
        }

        FakeEntity fakeEntity = CompatibilityAPI.getCompatibility().getEntityCompatibility().generateFakeEntity(cast.getTargetLocation(), EntityType.FIREWORK, fireworkItem);
        if (flightTime > 1) fakeEntity.setMotion(0.001, 0.3, -0.001);
        fakeEntity.show();

        // If we need to explode the firework instantly, make sure to return
        if (flightTime <= 0) {
            fakeEntity.playEffect(EntityEffect.FIREWORK_EXPLODE);
            fakeEntity.remove();
            return;
        }

        // Schedule a task to explode the firework later.
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
}
