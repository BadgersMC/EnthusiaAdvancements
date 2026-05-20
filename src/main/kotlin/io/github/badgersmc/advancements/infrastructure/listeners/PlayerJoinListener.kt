package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import io.github.badgersmc.advancements.infrastructure.advancement.UltimateAdvancementAdapter
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

@Component
class PlayerJoinListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val advancementAdapter: UltimateAdvancementAdapter
) : Listener {

    @PostConstruct
    fun register() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // Show all advancement tabs and grant root advancements
        for (namespace in advancementAdapter.getTreeNamespaces()) {
            advancementAdapter.showTabToPlayer(namespace, event.player)
            advancementAdapter.grantRootAdvancement(namespace, event.player)
        }
        // Process PLAYER_JOIN requirement triggers
        GrantProgress.execute(registry, RequirementType.PLAYER_JOIN, null, event.player)
    }
}
