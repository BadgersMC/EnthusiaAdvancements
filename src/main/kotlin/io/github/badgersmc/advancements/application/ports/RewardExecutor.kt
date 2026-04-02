package io.github.badgersmc.advancements.application.ports

import io.github.badgersmc.advancements.domain.Reward
import org.bukkit.entity.Player

interface RewardExecutor {
    fun execute(player: Player, rewards: List<Reward>)
}
