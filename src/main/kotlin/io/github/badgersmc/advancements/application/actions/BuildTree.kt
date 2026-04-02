package io.github.badgersmc.advancements.application.actions

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
import io.github.badgersmc.advancements.application.ports.RewardExecutor
import io.github.badgersmc.advancements.domain.AdvancementNodeDef
import io.github.badgersmc.advancements.domain.TreeDef
import io.github.badgersmc.advancements.infrastructure.advancement.RewardableAdvancement
import io.github.badgersmc.advancements.infrastructure.advancement.RewardableRootAdvancement
import org.bukkit.Material

data class BuildTreeResult(
    val tab: AdvancementTab,
    val advancements: Map<String, Advancement>
)

object BuildTree {

    fun execute(treeDef: TreeDef, api: UltimateAdvancementAPI, rewardExecutor: RewardExecutor): BuildTreeResult {
        val tab = api.createAdvancementTab(treeDef.namespace, treeDef.backgroundTexture)
        val sorted = treeDef.sortedNodes()
        val advancementMap = mutableMapOf<String, Advancement>()
        val children = mutableSetOf<BaseAdvancement>()

        for (node in sorted) {
            val display = buildDisplay(node)

            if (node.parentKey == null) {
                val root = RewardableRootAdvancement(
                    tab, node.key, node.maxProgression, display,
                    node.rewards, rewardExecutor
                )
                advancementMap[node.key] = root
            } else {
                val parent = advancementMap[node.parentKey]!!
                val child = RewardableAdvancement(
                    parent, node.key, node.maxProgression, display,
                    node.rewards, rewardExecutor
                )
                advancementMap[node.key] = child
                children.add(child)
            }
        }

        val root = advancementMap[treeDef.root.key] as RootAdvancement
        tab.registerAdvancements(root, children)
        tab.automaticallyShowToPlayers()
        tab.automaticallyGrantRootAdvancement()

        return BuildTreeResult(tab, advancementMap)
    }

    private fun buildDisplay(node: AdvancementNodeDef) =
        AdvancementDisplayBuilder(Material.valueOf(node.icon), node.title)
            .description(*node.description.toTypedArray())
            .frame(AdvancementFrameType.valueOf(node.frame.name))
            .showToast(node.showToast)
            .announceChat(node.announceChat)
            .coords(node.x, node.y)
            .build()
}
