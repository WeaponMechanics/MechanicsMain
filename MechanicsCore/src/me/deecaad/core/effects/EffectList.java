package me.deecaad.core.effects;

import java.util.List;

public class EffectList {

    private List<Effect> effects;

    public EffectList(List<Effect> effects) {
        this.effects = effects;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void setEffects(List<Effect> effects) {
        this.effects = effects;
    }
}
