package io.github.badgersmc.advancements.infrastructure.advancement

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay
import io.github.badgersmc.advancements.application.ports.RewardExecutor
import io.github.badgersmc.advancements.domain.Reward
import org.bukkit.entity.Player

class RewardableRootAdvancement(
    advancementTab: AdvancementTab,
    key: String,
    maxProgression: Int,
    display: AdvancementDisplay,
    private val rewards: List<Reward>,
    private val rewardExecutor: RewardExecutor
) : RootAdvancement(advancementTab, key, display, "minecraft:textures/gui/advancements/backgrounds/stone.png", maxProgression) {

    override fun giveReward(player: Player) {
        rewardExecutor.execute(player, rewards)
    }
}
