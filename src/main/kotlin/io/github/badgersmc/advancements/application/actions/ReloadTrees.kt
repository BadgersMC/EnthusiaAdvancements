package io.github.badgersmc.advancements.application.actions

import io.github.badgersmc.advancements.application.ports.AdvancementRegistry

interface ReloadableRegistry : AdvancementRegistry {
    fun reloadAll()
}

object ReloadTrees {

    fun execute(registry: ReloadableRegistry) {
        registry.reloadAll()
    }
}
