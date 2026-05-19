package io.github.badgersmc.advancements.infrastructure.advancement

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay
import io.github.badgersmc.advancements.application.ports.RewardExecutor
import io.github.badgersmc.advancements.domain.Reward
import org.bukkit.entity.Player

class RewardableAdvancement(
    parent: Advancement,
    key: String,
    maxProgression: Int,
    display: AdvancementDisplay,
    private val rewards: List<Reward>,
    private val rewardExecutor: RewardExecutor
) : BaseAdvancement(key, display, parent, maxProgression) {

    override fun giveReward(player: Player) {
        rewardExecutor.execute(player, rewards)
    }
}
