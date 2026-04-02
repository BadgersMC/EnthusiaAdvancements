package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin

@Component
class BlockBreakListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry
) : Listener {

    @PostConstruct
    fun register() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val material = event.block.type.name
        GrantProgress.execute(registry, RequirementType.BLOCK_BREAK, material, event.player)
    }
}
