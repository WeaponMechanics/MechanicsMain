package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SkinList implements Serializer<Map<String, Skin>> {

    @Override
    public String getKeyword() {
        return "Skin";
    }

    @NotNull
    @Override
    public Map<String, Skin> serialize(SerializeData data) throws SerializerException {
        Map<String, Skin> skins = new HashMap<>();

        for (String skinName : data.config.getConfigurationSection(data.key).getKeys(false)) {
            Skin skin = new Skin().serialize(data.move(skinName));
            skins.put(skinName, skin);
        }

        return skins;
    }
}
