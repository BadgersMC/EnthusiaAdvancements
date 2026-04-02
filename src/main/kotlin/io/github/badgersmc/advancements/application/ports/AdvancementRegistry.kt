package io.github.badgersmc.advancements.application.ports

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import io.github.badgersmc.advancements.domain.RequirementType

interface AdvancementRegistry {
    fun getAdvancement(treeNamespace: String, key: String): Advancement?
    fun getTreeNamespaces(): Set<String>
    fun isTreeLoaded(namespace: String): Boolean
    fun findByRequirement(type: RequirementType, target: String?): List<Pair<String, String>>
}
