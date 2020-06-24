package me.deecaad.core.effects.types;

import me.deecaad.core.effects.Effect;
import me.deecaad.core.utils.NumberUtils;

import javax.annotation.Nonnegative;

public abstract class SoundEffect extends Effect {

    private static final float MIN_PITCH = 0.0f;
    private static final float MAX_PITCH = 2.0f;

    protected final float pitch;
    protected final float pitchNoise;
    protected final float volume;

    protected SoundEffect(float pitch, @Nonnegative float pitchNoise, float volume) {
        this.pitch = pitch;
        this.pitchNoise = pitchNoise;
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public float getPitchNoise() {
        return pitchNoise;
    }

    public float getVolume() {
        return volume;
    }

    /**
     * Gets a pitch within [pitch - pitchNoise, pitch + pitchNoise] also
     * within [MIN_PITCH, MAX_PITCH]
     *
     * @return The generated pitch
     */
    float getRandomPitch() {
        float noise = (float) NumberUtils.random(-pitchNoise, pitchNoise);
        float pitch = this.pitch + noise;

        return NumberUtils.minMax(MIN_PITCH, pitch, MAX_PITCH);
    }
}
