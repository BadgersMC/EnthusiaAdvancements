package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import io.github.badgersmc.advancements.infrastructure.plugins.LumaGuildsHook
import com.artillexstudios.axkoth.api.events.AxKothCapturedEvent
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Listens to AxKoth capture events and grants guild-scoped KOTH advancements.
 * Tracks consecutive captures per guild for the "Unstoppable Force" advancement.
 */
@Component
class KothListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val guildHook: LumaGuildsHook
) : Listener {

    // Track consecutive KOTH wins per guild (resets when different guild captures)
    private val consecutiveCaptures = ConcurrentHashMap<String, UUID>()
    private val consecutiveCounts = ConcurrentHashMap<UUID, Int>()

    @PostConstruct
    fun initialize() {
        if (!Bukkit.getPluginManager().isPluginEnabled("AxKoth")) {
            plugin.logger.info("AxKoth not found, KOTH advancement listener disabled")
            return
        }
        if (!guildHook.isAvailable()) {
            plugin.logger.info("LumaGuilds not available, KOTH advancement listener disabled")
            return
        }

        try {
            Bukkit.getPluginManager().registerEvents(this, plugin)
            plugin.logger.info("KOTH advancement listener enabled")
        } catch (e: NoClassDefFoundError) {
            plugin.logger.warning("AxKoth classes not found, KOTH listener disabled")
        }
    }

    @EventHandler
    fun onKothCaptured(event: AxKothCapturedEvent) {
        val capturer = event.capturer
        val guildId = guildHook.getPlayerGuildId(capturer.uniqueId) ?: return
        val kothName = event.koth.name

        // Grant KOTH_CAPTURE to all online guild members
        val members = guildHook.getOnlineGuildMembers(guildId)
        for (player in members) {
            GrantProgress.execute(registry, RequirementType.KOTH_CAPTURE, null, player)
        }

        // Track consecutive captures per KOTH arena
        val previousWinner = consecutiveCaptures[kothName]
        if (previousWinner == guildId) {
            val count = consecutiveCounts.merge(guildId, 1) { old, _ -> old + 1 } ?: 1
            if (count >= 1) {
                for (player in members) {
                    GrantProgress.execute(registry, RequirementType.KOTH_CONSECUTIVE_CAPTURE, null, player)
                }
            }
        } else {
            // Different guild won — reset streak
            if (previousWinner != null) {
                consecutiveCounts.remove(previousWinner)
            }
            consecutiveCounts[guildId] = 1
            // First capture still counts as 1 toward consecutive
            for (player in members) {
                GrantProgress.execute(registry, RequirementType.KOTH_CONSECUTIVE_CAPTURE, null, player)
            }
        }
        consecutiveCaptures[kothName] = guildId
    }
}
