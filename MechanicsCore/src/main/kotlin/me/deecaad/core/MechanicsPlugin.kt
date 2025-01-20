package me.deecaad.core

import com.cjcrafter.foliascheduler.ServerImplementation
import com.jeff_media.updatechecker.UpdateChecker
import me.deecaad.core.file.Configuration
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

/**
 * The base class for plugins using MechanicsCore.
 */
class MechanicsPlugin(
    val updateChecker: UpdateChecker? = null,
    private val bStatsId: Int? = null,
): JavaPlugin() {

    /**
     * The scheduler compatibility layer.
     */
    lateinit var foliaScheduler: ServerImplementation

    /**
     * The parsed `config.yml` file. This is automatically loaded when
     * [onLoad] is called.
     */
    lateinit var config: Configuration

    /**
     * The bStats metrics object, or null if there is no bStats id.
     */
    var metrics: Metrics? = null

}