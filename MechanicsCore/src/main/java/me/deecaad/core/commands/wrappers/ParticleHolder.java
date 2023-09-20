package me.deecaad.core.commands.wrappers;

import org.bukkit.Particle;

public record ParticleHolder(Particle particle, Object nmsParticle, String asString) {
}
