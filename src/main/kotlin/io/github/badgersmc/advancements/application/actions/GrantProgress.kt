package io.github.badgersmc.advancements.application.actions

import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import org.bukkit.entity.Player

object GrantProgress {

    fun execute(registry: AdvancementRegistry, type: RequirementType, target: String?, player: Player) {
        val matches = registry.findByRequirement(type, target)
        for ((namespace, key) in matches) {
            val advancement = registry.getAdvancement(namespace, key) ?: continue
            advancement.incrementProgression(player)
        }
    }
}
