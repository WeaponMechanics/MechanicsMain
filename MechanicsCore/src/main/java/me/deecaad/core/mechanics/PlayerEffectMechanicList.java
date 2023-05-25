package me.deecaad.core.mechanics;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.core.mechanics.defaultmechanics.Mechanic;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class PlayerEffectMechanicList extends Mechanic {

    private List<PlayerEffectMechanic> mechanics;

    public PlayerEffectMechanicList() {
        mechanics = new LinkedList<>();  // LinkedList for smaller memory footprint
    }

    public void addMechanic(PlayerEffectMechanic mechanic) {

    }

    @NotNull
    @Override
    public Mechanic serialize(SerializeData data) throws SerializerException {
        throw new UnsupportedOperationException("Cannot directly serialize a PlayerEffectMechanicList");
    }

    @Override
    protected void use0(CastData cast) {

    }
}
