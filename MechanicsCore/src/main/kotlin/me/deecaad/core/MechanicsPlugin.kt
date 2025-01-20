package me.deecaad.core

import com.cjcrafter.foliascheduler.FoliaCompatibility
import com.cjcrafter.foliascheduler.ServerImplementation
import com.jeff_media.updatechecker.UpdateChecker
import me.deecaad.core.file.Configuration
import me.deecaad.core.utils.Debugger
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import org.bstats.bukkit.Metrics
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

/**
 * The base class for plugins using MechanicsCore.
 *
 * @param updateChecker The update checker to use. If null, no update checking will be performed.
 * @param primaryColor The primary color to use for messages.
 * @param secondaryColor The secondary color to use for messages.
 * @param bStatsId The bStats id to use. If null, no metrics will be collected.
 */
open class MechanicsPlugin(
    val updateChecker: UpdateChecker? = null,
    val primaryColor: Style = Style.style(NamedTextColor.GOLD),
    val secondaryColor: Style = Style.style(NamedTextColor.GRAY),
    bStatsId: Int? = null,
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
     * The logger for this plugin.
     */
    lateinit var debugger: MechanicsLogger

    /**
     * The bStats metrics object, or null if there is no bStats id.
     */
    var metrics: Metrics? = bStatsId?.let { Metrics(this, it) }

    /**
     * The Adventure API for sending messages to players.
     */
    val adventure = BukkitAudiences.create(this)

    override fun onLoad() {
        foliaScheduler = FoliaCompatibility(this).serverImplementation
    }

    override fun onEnable() {

    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
        foliaScheduler.cancelTasks()
    }

    open fun registerMetrics() {
    }



}