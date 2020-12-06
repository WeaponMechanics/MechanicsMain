package me.deecaad.weaponmechanics.packetlisteners;

import me.deecaad.compatibility.CompatibilityAPI;
import me.deecaad.core.file.serializers.ColorSerializer;
import me.deecaad.core.packetlistener.Packet;
import me.deecaad.core.packetlistener.PacketHandler;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.wrappers.IEntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.List;

import static me.deecaad.weaponmechanics.WeaponMechanics.debug;

// todo: move to WMP
public class OutEntityMetadataListener extends PacketHandler {
    
    public OutEntityMetadataListener() {
        super("PacketPlayOutEntityMetadata");
    }
    
    @Override
    public void onPacket(Packet packet) {
        try {
            // The entity's unique id and metadata
            int id = (int) packet.getFieldValue("a");
            List<?> metaData = (List<?>) packet.getFieldValue("b");
            
            // Get data if present
            if (metaData == null || metaData.isEmpty()) return;
            Object byteData = metaData.get(0);
            Field field = ReflectionUtil.getField(byteData.getClass(), "b");
            Object itemObject = ReflectionUtil.invokeField(field, byteData);
            if (!(itemObject instanceof Byte)) return;
    
            // Get the color the entity should be glowing (Or null if it should not be glowing)
            Entity entity = CompatibilityAPI.getCompatibility().getEntityById(packet.getPlayer().getWorld(), id);
            IEntityWrapper wrapper = WeaponMechanics.getEntityWrapper((LivingEntity) entity);
            ColorSerializer.ColorType color = ColorSerializer.ColorType.WHITE;//(wrapper != null) ? wrapper.getThermalColor(packet.getPlayer()) : null;
            
            // Sets the glowing flag
            byte previousValue = (byte) itemObject;
            byte newValue = -1 /*EntityCompatibility.EntityMeta.GLOWING.setFlag(previousValue, color != null)*/;
    
            // Sets the fields via reflection
            ReflectionUtil.setField(field, byteData, newValue);
            ReflectionUtil.setField(packet.getField("b", 0), packet, metaData);

        } catch (Exception ex) {
            debug.log(LogLevel.ERROR, "Error handling ThermalScope. If you see this, please report to dev", ex);
        }
    }
}
