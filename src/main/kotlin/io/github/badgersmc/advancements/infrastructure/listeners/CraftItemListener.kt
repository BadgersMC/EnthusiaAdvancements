package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.plugin.java.JavaPlugin

@Component
class CraftItemListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry
) : Listener {

    @PostConstruct
    fun register() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onCraft(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        val material = event.recipe.result.type.name
        GrantProgress.execute(registry, RequirementType.CRAFT_ITEM, material, player)
    }
}
