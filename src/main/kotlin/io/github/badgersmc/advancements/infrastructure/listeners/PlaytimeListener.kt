package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

@Component
class PlaytimeListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry
) : Listener {

    @PostConstruct
    fun initialize() {
        try {
            Class.forName("org.enthusia.playtime.event.PlayerPlaytimeTickEvent")
            Bukkit.getPluginManager().registerEvents(PlaytimeHandler(), plugin)
            plugin.logger.info("PlaytimePlugin individual playtime listener enabled")
        } catch (e: ClassNotFoundException) {
            plugin.logger.info("PlaytimePlugin not available, individual playtime listener disabled")
        }
    }

    private inner class PlaytimeHandler : Listener {
        @EventHandler
        fun onPlaytimeTick(event: org.enthusia.playtime.event.PlayerPlaytimeTickEvent) {
            if (event.isCancelled) return
            if (event.getActiveMinutes() <= 0) return
            GrantProgress.execute(registry, RequirementType.PLAYER_PLAYTIME, null, event.getPlayer())
        }
    }
}
